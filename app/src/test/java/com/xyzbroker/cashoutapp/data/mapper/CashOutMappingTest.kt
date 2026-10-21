package com.xyzbroker.cashoutapp.data.mapper

import com.xyzbroker.cashoutapp.data.remote.dto.*
import com.xyzbroker.cashoutapp.data.remote.network.common.NetworkResult
import com.xyzbroker.cashoutapp.domain.model.CashOutRequest
import com.xyzbroker.cashoutapp.domain.result_handlimg.CashOutError
import com.xyzbroker.cashoutapp.domain.result_handlimg.Result
import com.xyzbroker.cashoutapp.domain.result_handlimg.isSubmitOutcomeUnknown
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.io.IOException

class CashOutMappingTest {
    @Test
    fun serviceCodes_mapToDomainReasons() {
        val cases = mapOf(
            "IDEMPOTENCY_KEY_REQUIRED" to CashOutError.IdempotencyKeyRequired,
            "UNAUTHENTICATED" to CashOutError.Unauthenticated,
            "ACCOUNT_RESTRICTED" to CashOutError.AccountRestricted,
            "ACCOUNT_NOT_FOUND" to CashOutError.AccountNotFound,
            "NOT_FOUND" to CashOutError.NotFound,
            "INTERNAL_ERROR" to CashOutError.InternalError,
            "IDEMPOTENCY_KEY_REUSED" to CashOutError.IdempotencyKeyReused,
        )
        cases.forEach { (code, expected) ->
            assertEquals(
                expected,
                ProblemDto(code = code).toCashOutError()
            )
        }

        assertEquals(
            CashOutError.Unknown("FUTURE_CODE"),
            ProblemDto(code = "FUTURE_CODE").toCashOutError()
        )
        assertEquals(CashOutError.Unknown(null), ProblemDto().toCashOutError())
    }

    @Test
    fun problemExtras_arePreservedAndHeaderRetryTakesPrecedence() {
        val fields = mapOf("iban" to "INVALID_IBAN", "amount" to "INVALID_AMOUNT")
        assertEquals(
            CashOutError.ValidationError(fields),
            ProblemDto(code = "VALIDATION_ERROR", fieldErrors = fields).toCashOutError()
        )
        assertEquals(
            CashOutError.ValidationError(emptyMap()),
            ProblemDto(code = "VALIDATION_ERROR").toCashOutError()
        )
        assertEquals(
            CashOutError.WithdrawalNeedsSupport("SUP-1"),
            ProblemDto(
                code = "WITHDRAWAL_NEEDS_SUPPORT",
                supportReference = "SUP-1"
            ).toCashOutError()
        )
        assertEquals(
            CashOutError.BelowMinimum(BigDecimal("50.00")),
            ProblemDto(code = "BELOW_MINIMUM", minimum = BigDecimal("50.00")).toCashOutError()
        )
        assertEquals(
            CashOutError.InsufficientFunds(BigDecimal("1250.50")),
            ProblemDto(
                code = "INSUFFICIENT_FUNDS",
                maxWithdrawable = BigDecimal("1250.50")
            ).toCashOutError()
        )
        val limited = ProblemDto(code = "RATE_LIMITED", retryAfterSeconds = 15)
        assertEquals(CashOutError.RateLimited(30), limited.toCashOutError(30))
        assertEquals(CashOutError.RateLimited(15), limited.toCashOutError())
    }

    @Test
    fun transportFailures_doNotBecomeBusinessSuccess() {
        val outcomes: List<NetworkResult<String>> = listOf(
            NetworkResult.HttpError(502, null, null),
            NetworkResult.NoUsableResponse(IOException("reset"))
        )
        outcomes.forEach {
            assertEquals(
                Result.Error(CashOutError.NoResponse),
                it.toResult { Result.Success(it) })
        }
        assertEquals(
            Result.Error(CashOutError.NotSent),
            NetworkResult.NotSent(IOException()).toResult { Result.Success(it) })
    }

    @Test
    fun legacyRefusalAndIncompleteSuccess_areNotReceipts() {
        assertEquals(
            Result.Error(CashOutError.WithdrawalFailed),
            CashOutResponseDto(isSuccess = false, errorCode = "WITHDRAWAL_FAILED").toCashOutResult()
        )
        assertEquals(
            Result.Error(CashOutError.NoResponse),
            CashOutResponseDto(isSuccess = true).toCashOutResult()
        )
        assertEquals(Result.Error(CashOutError.NoResponse), CashOutResponseDto().toCashOutResult())
        assertEquals(
            Result.Error(CashOutError.Unknown("NEW_REFUSAL")),
            CashOutResponseDto(isSuccess = false, errorCode = "NEW_REFUSAL").toCashOutResult()
        )
    }

    @Test
    fun acceptedReceipt_preservesReferenceExactAmountAndArrival() {
        val result = CashOutResponseDto(
            isSuccess = true,
            reference = "CO-1",
            status = "processing",
            amount = "250.00",
            currency = "USD",
            estimatedArrival = "2026-10-23"
        ).toCashOutResult() as Result.Success
        assertEquals("CO-1", result.data.reference)
        assertEquals(BigDecimal("250.00"), result.data.amount)
        assertEquals(LocalDate.of(2026, 10, 23), result.data.estimatedArrival)
    }

    @Test
    fun incompleteSummary_neverProducesAnAvailableBalance() {
        val full = AccountResponseDto(
            currency = "USD",
            balance = BigDecimal.TEN,
            equity = BigDecimal.ONE,
            freeMargin = BigDecimal.TEN
        )
        listOf(
            full.copy(currency = null),
            full.copy(balance = null),
            full.copy(equity = null),
            full.copy(freeMargin = null)
        ).forEach {
            assertEquals(Result.Error(CashOutError.NoResponse), it.toAccountBalanceResult())
        }
        assertTrue(full.toAccountBalanceResult() is Result.Success)
    }

    @Test
    fun requestMapping_rejectsRounding() {
        val request = CashOutRequest(BigDecimal("250.001"), "A Customer", "GB82")
        assertThrows(ArithmeticException::class.java) { request.toDto() }
        assertEquals(BigDecimal("250.00"), request.copy(amount = BigDecimal("250")).toDto().amount)
    }

    @Test
    fun uncertainSubmitReasons_areNeverReportedAsKnownRefusals() {
        listOf(
            CashOutError.NoResponse,
            CashOutError.InternalError,
            CashOutError.Unknown("NEW"),
            CashOutError.WithdrawalNeedsSupport("SUP")
        ).forEach { assertTrue(it.isSubmitOutcomeUnknown) }
        listOf(
            CashOutError.NotSent,
            CashOutError.WithdrawalFailed,
            CashOutError.AccountRestricted,
            CashOutError.RateLimited(30),
            CashOutError.InsufficientFunds()
        ).forEach { assertFalse(it.isSubmitOutcomeUnknown) }
    }
}
