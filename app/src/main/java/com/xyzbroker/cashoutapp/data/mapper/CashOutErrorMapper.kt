package com.xyzbroker.cashoutapp.data.mapper

import com.xyzbroker.cashoutapp.data.remote.dto.ProblemDto
import com.xyzbroker.cashoutapp.data.remote.network.common.NetworkResult
import com.xyzbroker.cashoutapp.domain.result_handlimg.CashOutError
import com.xyzbroker.cashoutapp.domain.result_handlimg.Result

fun <T, R> NetworkResult<T>.toResult(onSuccess: (T) -> Result<R>): Result<R> = when (this) {
    is NetworkResult.Success -> onSuccess(data)
    // No problem body means the service did not write this error, e.g. a gateway HTML page.
    is NetworkResult.HttpError -> Result.Error(
        problem?.toCashOutError(retryAfterSeconds) ?: CashOutError.NoResponse
    )

    is NetworkResult.NoUsableResponse -> Result.Error(CashOutError.NoResponse)
    is NetworkResult.NotSent -> Result.Error(CashOutError.NotSent)
}

fun ProblemDto.toCashOutError(retryAfterHeader: Long? = null): CashOutError = when (code) {
    "IDEMPOTENCY_KEY_REQUIRED" -> CashOutError.IdempotencyKeyRequired
    "VALIDATION_ERROR" -> CashOutError.ValidationError(fieldErrors.orEmpty())
    "UNAUTHENTICATED" -> CashOutError.Unauthenticated
    "ACCOUNT_RESTRICTED" -> CashOutError.AccountRestricted
    "ACCOUNT_NOT_FOUND" -> CashOutError.AccountNotFound
    "NOT_FOUND" -> CashOutError.NotFound
    "INTERNAL_ERROR" -> CashOutError.InternalError
    "WITHDRAWAL_NEEDS_SUPPORT" -> CashOutError.WithdrawalNeedsSupport(supportReference)
    "BELOW_MINIMUM" -> CashOutError.BelowMinimum(minimum)
    "INSUFFICIENT_FUNDS" -> CashOutError.InsufficientFunds(maxWithdrawable)
    "IDEMPOTENCY_KEY_REUSED" -> CashOutError.IdempotencyKeyReused
    "RATE_LIMITED" -> CashOutError.RateLimited(retryAfterHeader ?: retryAfterSeconds)
    else -> CashOutError.Unknown(code)
}
