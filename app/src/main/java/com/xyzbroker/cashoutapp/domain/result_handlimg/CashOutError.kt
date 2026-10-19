package com.xyzbroker.cashoutapp.domain.result_handlimg

import java.math.BigDecimal

/** Business reason carried by an HTTP failure, independent of its status code. */
sealed class CashOutError(val code: String?) {
    /** Legacy HTTP 200 refusal; no money was debited. */
    data object WithdrawalFailed : CashOutError("WITHDRAWAL_FAILED")

    data object IdempotencyKeyRequired : CashOutError("IDEMPOTENCY_KEY_REQUIRED")

    data class ValidationError(
        val fieldErrors: Map<String, String> = emptyMap(),
    ) : CashOutError("VALIDATION_ERROR")

    data object Unauthenticated : CashOutError("UNAUTHENTICATED")
    data object AccountRestricted : CashOutError("ACCOUNT_RESTRICTED")
    data object AccountNotFound : CashOutError("ACCOUNT_NOT_FOUND")
    data object NotFound : CashOutError("NOT_FOUND")

    /** On submission, the outcome is unknown; do not assume that nothing was debited. */
    data object InternalError : CashOutError("INTERNAL_ERROR")

    /** Money may or may not have moved. Requires human support. */
    data class WithdrawalNeedsSupport(
        val supportReference: String? = null,
    ) : CashOutError("WITHDRAWAL_NEEDS_SUPPORT")

    // Preserve exact API amounts; their units must follow the sandbox contract.
    data class BelowMinimum(
        val minimum: BigDecimal? = null,
    ) : CashOutError("BELOW_MINIMUM")

    data class InsufficientFunds(
        val maxWithdrawable: BigDecimal? = null,
    ) : CashOutError("INSUFFICIENT_FUNDS")

    data object IdempotencyKeyReused : CashOutError("IDEMPOTENCY_KEY_REUSED")

    data class RateLimited(
        val retryAfterSeconds: Long? = null,
    ) : CashOutError("RATE_LIMITED")

    /** Missing/unrecognized code, including an empty or unparseable error body. */
    data class Unknown(val rawCode: String? = null) : CashOutError(rawCode)
}
