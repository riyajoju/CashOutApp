package com.xyzbroker.cashoutapp.data.remote.network.common

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.xyzbroker.cashoutapp.data.remote.dto.ProblemDto
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException


suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): NetworkResult<T> {
    val response = try {
        call()
    } catch (e: CancellationException) { // When the app goes background or when the coroutine is canceled
        throw e
    } catch (e: IOException) { // Network issues
        return if (e.isCertainlyNotSent()) NetworkResult.NotSent(e) else NetworkResult.NoUsableResponse(
            e
        )
    } catch (e: Exception) {
        // Includes 2xx bodies Gson could not parse: the server may have acted on the request.
        return NetworkResult.NoUsableResponse(e)
    }

    if (response.isSuccessful) {
        val body = response.body()
            ?: return NetworkResult.NoUsableResponse(IllegalStateException("Empty ${response.code()} body"))
        // When the duplicate request sent
        val replayed = response.headers()["Idempotent-Replayed"].equals("true", ignoreCase = true)
        return NetworkResult.Success(body, replayed)
    }

    return NetworkResult.HttpError(
        status = response.code(),
        problem = response.parseProblem(),
        // Only the delta-seconds form is read; an HTTP-date Retry-After is ignored.
        retryAfterSeconds = response.headers()["Retry-After"]?.trim()?.toLongOrNull(),
    )
}

private fun IOException.isCertainlyNotSent(): Boolean =
    (this is ConnectException || this is UnknownHostException || this is NoRouteToHostException) && suppressed.isEmpty()

// Reads the error body once. Non-JSON bodies (e.g. gateway HTML) to be parsed by the caller(null)
private fun Response<*>.parseProblem(): ProblemDto? {
    val raw = try {
        errorBody()?.string()
    } catch (e: IOException) {
        null
    }
    if (raw.isNullOrBlank()) return null
    return try {
        Gson().fromJson(raw, ProblemDto::class.java)
    } catch (e: JsonParseException) {
        null
    }
}
