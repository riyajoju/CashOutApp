package com.xyzbroker.cashoutapp.presentation.cashout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xyzbroker.cashoutapp.domain.model.AccountBalance
import com.xyzbroker.cashoutapp.domain.model.CashOutReceipt
import com.xyzbroker.cashoutapp.domain.model.CashOutRequest
import com.xyzbroker.cashoutapp.domain.result_handlimg.CashOutError
import com.xyzbroker.cashoutapp.domain.result_handlimg.Result
import com.xyzbroker.cashoutapp.domain.result_handlimg.isSubmitOutcomeUnknown
import com.xyzbroker.cashoutapp.domain.usecase.AmountValidation
import com.xyzbroker.cashoutapp.domain.usecase.GetCashOutDetailsUseCase
import com.xyzbroker.cashoutapp.domain.usecase.SubmitCashOutUseCase
import com.xyzbroker.cashoutapp.domain.usecase.ValidateCashOutAmountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class FormField(val apiName: String) {
    AMOUNT("amount"),
    BENEFICIARY_NAME("beneficiaryName"),
    IBAN("iban"),
}

// Submit Status
sealed interface SubmitStatus {
    data class Received(val receipt: CashOutReceipt) : SubmitStatus
    data class Refused(val reason: CashOutError) : SubmitStatus
    data class Unknown(val reason: CashOutError) : SubmitStatus
}

sealed interface CashOutUiState {
    data object Loading : CashOutUiState
    data object BalanceError : CashOutUiState

    data class Form(
        val balance: AccountBalance,
        val amount: String = "",
        val beneficiaryName: String = "",
        val iban: String = "",
        val amountError: AmountValidation? = null,
        val missingFields: Set<FormField> = emptySet(),
        val fieldErrors: Set<FormField> = emptySet(),
        val isSubmitting: Boolean = false,
        val submitStatus: SubmitStatus? = null,
    ) : CashOutUiState {
        val inputsEnabled: Boolean
            get() = !isSubmitting && submitStatus !is SubmitStatus.Unknown && submitStatus !is SubmitStatus.Received

        val canSubmit: Boolean
            get() = !isSubmitting && submitStatus !is SubmitStatus.Received &&
                    (submitStatus as? SubmitStatus.Unknown)?.reason !is CashOutError.WithdrawalNeedsSupport
    }
}

@HiltViewModel
class CashOutViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getCashOutDetails: GetCashOutDetailsUseCase,
    private val validateAmount: ValidateCashOutAmountUseCase,
    private val submitCashOut: SubmitCashOutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CashOutUiState>(CashOutUiState.Loading)
    val uiState: StateFlow<CashOutUiState> = _uiState.asStateFlow()

    // This will restore the form even after process death.
    private var idempotencyKey: String?
        get() = savedStateHandle[KEY_IDEMPOTENCY]
        set(value) {
            savedStateHandle[KEY_IDEMPOTENCY] = value
        }

    init {
        loadBalance()
    }

    fun loadBalance() {
        _uiState.value = CashOutUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val result = getCashOutDetails()) {
                is Result.Success -> CashOutUiState.Form(
                    balance = result.data,
                    amount = savedStateHandle[KEY_AMOUNT] ?: "",
                    beneficiaryName = savedStateHandle[KEY_NAME] ?: "",
                    iban = savedStateHandle[KEY_IBAN] ?: "",
                    submitStatus = idempotencyKey?.let { SubmitStatus.Unknown(CashOutError.NoResponse) },
                )

                is Result.Error -> CashOutUiState.BalanceError
            }
        }
    }

    fun onAmountChange(value: String) = editForm(KEY_AMOUNT, value, change = {
        copy(amount = value, amountError = null, fieldErrors = fieldErrors - FormField.AMOUNT)
    })

    fun onBeneficiaryNameChange(value: String) = editForm(KEY_NAME, value) {
        copy(
            beneficiaryName = value,
            missingFields = missingFields - FormField.BENEFICIARY_NAME,
            fieldErrors = fieldErrors - FormField.BENEFICIARY_NAME,
        )
    }

    fun onIbanChange(value: String) = editForm(KEY_IBAN, value) {
        copy(
            iban = value,
            missingFields = missingFields - FormField.IBAN,
            fieldErrors = fieldErrors - FormField.IBAN
        )
    }

    fun onSubmit() {
        val form = _uiState.value as? CashOutUiState.Form ?: return
        if (!form.canSubmit) return

        val retryKey = idempotencyKey
        val amount = if (retryKey != null) {
            // Retrying an unknown outcome: resend exactly the same request, even if available has changed since.
            form.amount.trim().toBigDecimal().setScale(2)
        } else {
            // Check every field at once so the customer sees all problems together; send nothing until all pass.
            val validation = validateAmount(form.amount, form.balance.available)
            val missing = buildSet {
                if (form.beneficiaryName.isBlank()) add(FormField.BENEFICIARY_NAME)
                if (form.iban.isBlank()) add(FormField.IBAN)
            }
            if (validation !is AmountValidation.Valid || missing.isNotEmpty()) {
                _uiState.value = form.copy(
                    amountError = validation.takeUnless { it is AmountValidation.Valid },
                    missingFields = missing,
                    submitStatus = null,
                )
                return
            }
            validation.amount
        }
        val key = retryKey ?: UUID.randomUUID().toString().also { idempotencyKey = it }
        val request = CashOutRequest(amount, form.beneficiaryName, form.iban)

        _uiState.value = form.copy(isSubmitting = true)
        viewModelScope.launch {
            val result = submitCashOut(request, key)
            val status = when (result) {
                is Result.Success -> SubmitStatus.Received(result.data)
                is Result.Error ->
                    if (result.reason.isSubmitOutcomeUnknown) SubmitStatus.Unknown(result.reason)
                    else SubmitStatus.Refused(result.reason)
            }
            // A known outcome ends this intention; the next submit is a new request with a new key.
            if (status !is SubmitStatus.Unknown) idempotencyKey = null

            val serverFieldErrors =
                ((result as? Result.Error)?.reason as? CashOutError.ValidationError)
                    ?.fieldErrors?.keys.orEmpty()
            val current = _uiState.value as? CashOutUiState.Form ?: return@launch
            _uiState.value = current.copy(
                isSubmitting = false,
                submitStatus = status,
                fieldErrors = FormField.entries.filter { it.apiName in serverFieldErrors }.toSet(),
            )
        }
    }

    //After a received request: clear the form and show the updated available amount.
    fun onStartNew() {
        listOf(KEY_AMOUNT, KEY_NAME, KEY_IBAN).forEach { savedStateHandle.remove<String>(it) }
        idempotencyKey = null
        loadBalance()
    }

    private fun editForm(
        key: String,
        value: String,
        change: CashOutUiState.Form.() -> CashOutUiState.Form
    ) {
        val form = _uiState.value as? CashOutUiState.Form ?: return
        if (!form.inputsEnabled) return
        savedStateHandle[key] = value
        _uiState.value = form.change().copy(submitStatus = null)
    }

    private companion object {
        const val KEY_IDEMPOTENCY = "idempotencyKey"
        const val KEY_AMOUNT = "amount"
        const val KEY_NAME = "beneficiaryName"
        const val KEY_IBAN = "iban"
    }
}
