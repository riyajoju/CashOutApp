package com.xyzbroker.cashoutapp.data.remote.network.common

/** Generic network outcomes. Endpoint-specific interpretation belongs in domain. */
sealed class NetworkHandler<out T> {

    /** HTTP success; the endpoint's response body still needs business validation. */
    data class Success<T>(val data: T) : NetworkHandler<T>()

    data class HttpError(
        val statusCode: Int,
        val errorCode: String? = null,
        val message: String? = null,
        val cause: Throwable? = null,
    ) : NetworkHandler<Nothing>()

    /** Includes timeouts and lost connections; a submit may already have been accepted. */
    data class ConnectionError(
        val cause: Throwable,
        val message: String = "Unable to connect to the service",
    ) : NetworkHandler<Nothing>()

    data class UnexpectedError(
        val cause: Throwable,
        val message: String = "An unexpected error occurred",
    ) : NetworkHandler<Nothing>()
}
