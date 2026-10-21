package com.xyzbroker.cashoutapp.presentation.cashout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xyzbroker.cashoutapp.R
import com.xyzbroker.cashoutapp.domain.model.AccountBalance
import com.xyzbroker.cashoutapp.domain.model.CashOutReceipt
import com.xyzbroker.cashoutapp.domain.result_handlimg.CashOutError
import com.xyzbroker.cashoutapp.domain.usecase.AmountValidation
import com.xyzbroker.cashoutapp.domain.usecase.ValidateCashOutAmountUseCase
import com.xyzbroker.cashoutapp.presentation.theme.CashOutAppTheme
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale

@Composable
fun CashOutRoute(
    modifier: Modifier = Modifier,
    viewModel: CashOutViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CashOutScreen(
        state = state,
        onRetryBalance = viewModel::loadBalance,
        onAmountChange = viewModel::onAmountChange,
        onBeneficiaryNameChange = viewModel::onBeneficiaryNameChange,
        onIbanChange = viewModel::onIbanChange,
        onSubmit = viewModel::onSubmit,
        onStartNew = viewModel::onStartNew,
        modifier = modifier,
    )
}

@Composable
fun CashOutScreen(
    state: CashOutUiState,
    onRetryBalance: () -> Unit,
    onAmountChange: (String) -> Unit,
    onBeneficiaryNameChange: (String) -> Unit,
    onIbanChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onStartNew: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            stringResource(R.string.cash_out_title),
            style = MaterialTheme.typography.headlineMedium
        )
        when (state) {
            CashOutUiState.Loading -> CircularProgressIndicator()
            CashOutUiState.BalanceError -> {
                Text(stringResource(R.string.cash_out_balance_error))
                Button(onClick = onRetryBalance) { Text(stringResource(R.string.cash_out_retry)) }
            }

            is CashOutUiState.Form -> CashOutForm(
                form = state,
                onAmountChange = onAmountChange,
                onBeneficiaryNameChange = onBeneficiaryNameChange,
                onIbanChange = onIbanChange,
                onSubmit = onSubmit,
                onStartNew = onStartNew,
            )
        }
    }
}

@Composable
private fun CashOutForm(
    form: CashOutUiState.Form,
    onAmountChange: (String) -> Unit,
    onBeneficiaryNameChange: (String) -> Unit,
    onIbanChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onStartNew: () -> Unit,
) {
    val currency = form.balance.currency
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(R.string.cash_out_available))
        Text(
            formatMoney(form.balance.available, currency),
            style = MaterialTheme.typography.titleMedium
        )
    }

    val amountError = amountErrorText(form.amountError, currency)
        ?: if (FormField.AMOUNT in form.fieldErrors) stringResource(R.string.cash_out_amount_invalid) else null
    OutlinedTextField(
        value = form.amount,
        onValueChange = onAmountChange,
        label = { Text(stringResource(R.string.cash_out_amount_label)) },
        supportingText = {
            Text(
                amountError ?: stringResource(
                    R.string.cash_out_amount_minimum,
                    formatMoney(ValidateCashOutAmountUseCase.MINIMUM, currency)
                )
            )
        },
        isError = amountError != null,
        enabled = form.inputsEnabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        ),
        modifier = Modifier.fillMaxWidth(),
    )
    val nameError = when (FormField.BENEFICIARY_NAME) {
        in form.missingFields -> stringResource(R.string.cash_out_name_required)
        in form.fieldErrors -> stringResource(R.string.cash_out_name_invalid)
        else -> null
    }
    OutlinedTextField(
        value = form.beneficiaryName,
        onValueChange = onBeneficiaryNameChange,
        label = { Text(stringResource(R.string.cash_out_beneficiary_label)) },
        supportingText = nameError?.let { { Text(it) } },
        isError = nameError != null,
        enabled = form.inputsEnabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        ),
        modifier = Modifier.fillMaxWidth(),
    )
    val ibanError = when (FormField.IBAN) {
        in form.missingFields -> stringResource(R.string.cash_out_iban_required)
        in form.fieldErrors -> stringResource(R.string.cash_out_iban_invalid)
        else -> null
    }
    OutlinedTextField(
        value = form.iban,
        onValueChange = onIbanChange,
        label = { Text(stringResource(R.string.cash_out_iban_label)) },
        supportingText = ibanError?.let { { Text(it) } },
        isError = ibanError != null,
        enabled = form.inputsEnabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.Done,
        ),
        modifier = Modifier.fillMaxWidth(),
    )

    when (val status = form.submitStatus) {
        is SubmitStatus.Received -> ReceivedCard(status.receipt)
        is SubmitStatus.Refused -> Text(
            refusedText(status.reason, currency),
            color = MaterialTheme.colorScheme.error
        )

        is SubmitStatus.Unknown -> {
            Text(
                stringResource(
                    if (status.reason is CashOutError.WithdrawalNeedsSupport) R.string.cash_out_needs_support
                    else R.string.cash_out_unknown,
                ),
                color = MaterialTheme.colorScheme.error,
            )
            (status.reason as? CashOutError.WithdrawalNeedsSupport)?.supportReference?.let {
                Text(stringResource(R.string.cash_out_support_reference, it))
            }
        }

        null -> Unit
    }

    if (form.submitStatus is SubmitStatus.Received) {
        Button(onClick = onStartNew, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.cash_out_start_new))
        }
    } else if (form.canSubmit || form.isSubmitting) {
        Button(onClick = onSubmit, enabled = form.canSubmit, modifier = Modifier.fillMaxWidth()) {
            if (form.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    stringResource(
                        if (form.submitStatus is SubmitStatus.Unknown) R.string.cash_out_check_again
                        else R.string.cash_out_submit,
                    ),
                )
            }
        }
    }
}

@Composable
private fun ReceivedCard(receipt: CashOutReceipt) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.cash_out_received_title),
                style = MaterialTheme.typography.titleMedium
            )
            val amount = receipt.amount
            val currency = receipt.currency
            Text(
                if (amount != null && currency != null) {
                    stringResource(R.string.cash_out_received_body, formatMoney(amount, currency))
                } else {
                    stringResource(R.string.cash_out_received_body_no_amount)
                },
            )
            Text(stringResource(R.string.cash_out_reference, receipt.reference))
            receipt.estimatedArrival?.let {
                Text(stringResource(R.string.cash_out_estimated_arrival, it.format(ARRIVAL_FORMAT)))
            }
        }
    }
}

@Composable
private fun amountErrorText(error: AmountValidation?, currency: String): String? = when (error) {
    AmountValidation.Empty -> stringResource(R.string.cash_out_amount_required)
    AmountValidation.Invalid -> stringResource(R.string.cash_out_amount_invalid)
    is AmountValidation.BelowMinimum ->
        stringResource(R.string.cash_out_amount_below_minimum, formatMoney(error.minimum, currency))

    is AmountValidation.AboveAvailable ->
        stringResource(
            R.string.cash_out_amount_above_available,
            formatMoney(error.available, currency)
        )

    is AmountValidation.Valid, null -> null
}

// Every refusal here means nothing was debited; the wording is the app's own, never the server's detail.
@Composable
private fun refusedText(reason: CashOutError, currency: String): String = when (reason) {
    CashOutError.AccountRestricted -> stringResource(R.string.cash_out_refused_restricted)
    is CashOutError.InsufficientFunds -> reason.maxWithdrawable
        ?.let { stringResource(R.string.cash_out_refused_insufficient, formatMoney(it, currency)) }
        ?: stringResource(R.string.cash_out_refused_generic)

    is CashOutError.BelowMinimum -> stringResource(
        R.string.cash_out_amount_below_minimum,
        formatMoney(reason.minimum ?: ValidateCashOutAmountUseCase.MINIMUM, currency),
    )

    is CashOutError.ValidationError -> stringResource(R.string.cash_out_refused_fields)
    is CashOutError.RateLimited -> reason.retryAfterSeconds
        ?.let { stringResource(R.string.cash_out_refused_rate_limited, it.toInt()) }
        ?: stringResource(R.string.cash_out_refused_rate_limited_no_time)

    CashOutError.NotSent -> stringResource(R.string.cash_out_not_sent)
    else -> stringResource(R.string.cash_out_refused_generic)
}

private val ARRIVAL_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.US)

/** Formats an amount that already has scale 2; DecimalFormat formats BigDecimal exactly. */
private fun formatMoney(amount: BigDecimal, currencyCode: String): String =
    NumberFormat.getCurrencyInstance(Locale.US).apply {
        currency = Currency.getInstance(currencyCode)
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(amount)

@Preview(showBackground = true)
@Composable
private fun CashOutScreenPreview() {
    CashOutAppTheme {
        CashOutScreen(
            state = CashOutUiState.Form(
                balance = AccountBalance(
                    "USD",
                    BigDecimal("34412.6500"),
                    BigDecimal("36120.5512"),
                    BigDecimal("34347.2000")
                ),
                amount = "250.00",
            ),
            onRetryBalance = {},
            onAmountChange = {},
            onBeneficiaryNameChange = {},
            onIbanChange = {},
            onSubmit = {},
            onStartNew = {},
        )
    }
}
