package com.xyzbroker.cashoutapp.data.remote.network.common

import com.xyzbroker.cashoutapp.data.remote.dto.ProblemDto
import java.io.IOException


sealed interface NetworkResult<out T> {

    // 2XX with parsed body.The body may still be a denial (e.g. WITHDRAWAL_FAILED).
    data class Success<T>(
        val data: T,
        val replayed: Boolean
    ) : NetworkResult<T>

    // Non 2xx. problem is null when the body is not a problem JSON (e.g. gateway HTML).
    data class HttpError(
        val status: Int,
        val problem: ProblemDto?,
        val retryAfterSeconds: Long?
    ) : NetworkResult<Nothing>

    data class NoUsableResponse(val cause: Throwable) : NetworkResult<Nothing>

    data class NotSent(val cause: IOException) : NetworkResult<Nothing>
}

