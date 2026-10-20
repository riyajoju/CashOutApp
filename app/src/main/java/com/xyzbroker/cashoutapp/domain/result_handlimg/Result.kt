package com.xyzbroker.cashoutapp.domain.result_handlimg

/**
 * Generic outcome of an operation that either succeeds or fails. Import it explicitly: it shadows kotlin.Result.
 * For a submit, check CashOutError.isSubmitOutcomeUnknown before telling the customer it failed.
 */
sealed interface Result<out T> {

    data class Success<out T>(val data: T) : Result<T>

    data class Error(val reason: CashOutError) : Result<Nothing>
}
