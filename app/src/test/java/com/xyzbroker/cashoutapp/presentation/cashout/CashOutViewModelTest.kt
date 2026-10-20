package com.xyzbroker.cashoutapp.presentation.cashout

import androidx.lifecycle.SavedStateHandle
import com.xyzbroker.cashoutapp.domain.model.*
import com.xyzbroker.cashoutapp.domain.repository.CashOutRepository
import com.xyzbroker.cashoutapp.domain.result_handlimg.CashOutError
import com.xyzbroker.cashoutapp.domain.result_handlimg.Result
import com.xyzbroker.cashoutapp.domain.usecase.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class CashOutViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeRepository()
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) = CashOutViewModel(
        handle, GetCashOutDetailsUseCase(repository), ValidateCashOutAmountUseCase(), SubmitCashOutUseCase(repository))
    private fun CashOutViewModel.fill() {
        onAmountChange("250.00")
        onBeneficiaryNameChange("A. Customer")
        onIbanChange("GB82 WEST 1234 5698 7654 32")
    }
    private fun CashOutViewModel.form() = uiState.value as CashOutUiState.Form

    @Test fun missingFields_preventAnySubmit() = runTest(dispatcher) {
        val vm = viewModel(); runCurrent()
        vm.onSubmit(); runCurrent()
        assertTrue(repository.calls.isEmpty())
        assertEquals(AmountValidation.Empty,vm.form().amountError)
        assertEquals(setOf(FormField.BENEFICIARY_NAME,FormField.IBAN),vm.form().missingFields)
    }
    @Test fun unknownOutcome_locksInputsAndRetryUsesIdenticalBodyAndKey() = runTest(dispatcher) {
        val vm = viewModel(); runCurrent(); vm.fill()
        repository.outcome = Result.Error(CashOutError.NoResponse)
        vm.onSubmit(); runCurrent()
        assertTrue(vm.form().submitStatus is SubmitStatus.Unknown)
        assertFalse(vm.form().inputsEnabled)
        vm.onAmountChange("500.00"); vm.onIbanChange("different"); vm.onBeneficiaryNameChange("different")
        vm.onSubmit(); runCurrent()
        assertEquals(2,repository.calls.size)
        assertEquals(repository.calls[0],repository.calls[1])
        assertEquals(BigDecimal("250.00"),repository.calls[1].first.amount)
    }
    @Test fun supportRequired_blocksRetry() = runTest(dispatcher) {
        val vm = viewModel(); runCurrent(); vm.fill()
        repository.outcome = Result.Error(CashOutError.WithdrawalNeedsSupport("SUP-1"))
        vm.onSubmit(); runCurrent(); vm.onSubmit(); runCurrent()
        assertFalse(vm.form().canSubmit)
        assertFalse(vm.form().inputsEnabled)
        assertEquals(1,repository.calls.size)
    }
    @Test fun receivedRequest_isNotPaidAndCannotBeSubmittedAgain() = runTest(dispatcher) {
        val vm = viewModel(); runCurrent(); vm.fill()
        repository.outcome = Result.Success(CashOutReceipt("CO-1",BigDecimal("250.00"),"USD",null))
        vm.onSubmit(); runCurrent(); vm.onSubmit(); runCurrent()
        assertTrue(vm.form().submitStatus is SubmitStatus.Received)
        assertFalse(vm.form().canSubmit)
        assertEquals(1,repository.calls.size)
        vm.onStartNew(); runCurrent()
        assertEquals("",vm.form().amount)
        assertTrue(vm.form().inputsEnabled)
        assertEquals(2,repository.balanceLoads)
    }
    @Test fun knownRefusal_allowsNewIntentionWithNewKey() = runTest(dispatcher) {
        val vm = viewModel(); runCurrent(); vm.fill()
        repository.outcome = Result.Error(CashOutError.AccountRestricted)
        vm.onSubmit(); runCurrent()
        assertTrue(vm.form().submitStatus is SubmitStatus.Refused)
        assertTrue(vm.form().inputsEnabled)
        vm.onSubmit(); runCurrent()
        assertNotEquals(repository.calls[0].second,repository.calls[1].second)
    }
    @Test fun rapidDoubleTap_sendsOnlyOneRequest() = runTest(dispatcher) {
        val vm = viewModel(); runCurrent(); vm.fill()
        repository.pending = CompletableDeferred()
        vm.onSubmit(); vm.onSubmit(); runCurrent()
        assertEquals(1,repository.calls.size)
        assertTrue(vm.form().isSubmitting)
        repository.pending!!.complete(Result.Error(CashOutError.NoResponse)); runCurrent()
        assertFalse(vm.form().isSubmitting)
    }
    @Test fun restoredPendingIntention_reusesKeyEvenWhenBalanceFalls() = runTest(dispatcher) {
        repository.balance = AccountBalance("USD",BigDecimal("100.00"),BigDecimal("100.00"),BigDecimal("100.00"))
        val handle = SavedStateHandle(mapOf("idempotencyKey" to "existing-key","amount" to "250.00",
            "beneficiaryName" to "A. Customer","iban" to "GB82"))
        val vm = viewModel(handle); runCurrent()
        assertTrue(vm.form().submitStatus is SubmitStatus.Unknown)
        vm.onSubmit(); runCurrent()
        assertEquals("existing-key",repository.calls.single().second)
        assertEquals(BigDecimal("250.00"),repository.calls.single().first.amount)
    }
    @Test fun serverFieldErrors_areShownAndEditingClearsTheField() = runTest(dispatcher) {
        val vm = viewModel(); runCurrent(); vm.fill()
        repository.outcome = Result.Error(CashOutError.ValidationError(mapOf("iban" to "INVALID_IBAN")))
        vm.onSubmit(); runCurrent()
        assertEquals(setOf(FormField.IBAN),vm.form().fieldErrors)
        vm.onIbanChange("corrected")
        assertTrue(vm.form().fieldErrors.isEmpty())
    }
    @Test fun balanceFailure_showsErrorAndCanBeRetried() = runTest(dispatcher) {
        repository.balanceError = true
        val vm = viewModel(); runCurrent()
        assertEquals(CashOutUiState.BalanceError,vm.uiState.value)
        repository.balanceError = false
        vm.loadBalance(); runCurrent()
        assertTrue(vm.uiState.value is CashOutUiState.Form)
    }
    private class FakeRepository : CashOutRepository {
        var balance = AccountBalance("USD",BigDecimal("1250.50"),BigDecimal("2000.00"),BigDecimal("1500.00"))
        var balanceError = false
        var balanceLoads = 0
        var outcome: Result<CashOutReceipt> = Result.Error(CashOutError.NoResponse)
        var pending: CompletableDeferred<Result<CashOutReceipt>>? = null
        val calls = mutableListOf<Pair<CashOutRequest,String>>()
        override suspend fun getAccountBalance(): Result<AccountBalance> {
            balanceLoads++
            return if (balanceError) Result.Error(CashOutError.NoResponse) else Result.Success(balance)
        }
        override suspend fun submitCashOut(request: CashOutRequest,idempotencyKey: String): Result<CashOutReceipt> {
            calls += request to idempotencyKey
            return pending?.await() ?: outcome
        }
    }
}
