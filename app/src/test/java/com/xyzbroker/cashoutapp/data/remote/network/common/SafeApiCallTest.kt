package com.xyzbroker.cashoutapp.data.remote.network.common

import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.Headers
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SafeApiCallTest {
    @Test
    fun success_preservesBodyAndReplayHeader() = runBlocking {
        val result = safeApiCall {
            Response.success(
                "receipt", Headers.headersOf("Idempotent-Replayed", "TRUE")
            )
        }
        assertEquals(NetworkResult.Success("receipt", true), result)
        assertEquals(
            NetworkResult.Success("receipt", false), safeApiCall { Response.success("receipt") })
    }

    @Test
    fun emptySuccess_isUncertain() = runBlocking {
        assertTrue(safeApiCall<String> { Response.success(null) } is NetworkResult.NoUsableResponse)
    }

    @Test
    fun cancellation_isRethrownUnchanged() = runBlocking {
        val cancellation = CancellationException("cancelled")
        try {
            safeApiCall<String> { throw cancellation }
            fail("Cancellation must propagate")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
        }
    }

    @Test
    fun connectionFailures_withoutEarlierFailures_areNotSent() = runBlocking {
        listOf(
            ConnectException(), UnknownHostException(), NoRouteToHostException()
        ).forEach { cause ->
            assertEquals(NetworkResult.NotSent(cause), safeApiCall<String> { throw cause })
        }
    }

    @Test
    fun connectionFailure_afterAnEarlierFailure_isUncertain() = runBlocking {
        val cause = ConnectException().apply { addSuppressed(SocketTimeoutException()) }
        assertEquals(NetworkResult.NoUsableResponse(cause), safeApiCall<String> { throw cause })
    }

    @Test
    fun timeoutResetAndMalformedSuccess_areUncertain() = runBlocking {
        listOf(
            SocketTimeoutException(), IOException("reset"), JsonSyntaxException("bad JSON")
        ).forEach { cause ->
            assertEquals(NetworkResult.NoUsableResponse(cause), safeApiCall<String> { throw cause })
        }
    }

    @Test
    fun problemResponse_preservesStatusExtrasAndRetryHeader() = runBlocking {
        val raw = okhttp3.Response.Builder()
            .request(okhttp3.Request.Builder().url("http://localhost/").build())
            .protocol(okhttp3.Protocol.HTTP_1_1).code(429).message("Limited")
            .header("Retry-After", "30").build()
        val result = safeApiCall<String> {
            Response.error(
                """{"code":"RATE_LIMITED","retryAfterSeconds":15,"correlationId":"trace-1"}""".toResponseBody(
                    "application/json".toMediaType()
                ), raw
            )
        } as NetworkResult.HttpError
        assertEquals(429, result.status)
        assertEquals("RATE_LIMITED", result.problem?.code)
        assertEquals("trace-1", result.problem?.correlationId)
        assertEquals(15L, result.problem?.retryAfterSeconds)
        assertEquals(30L, result.retryAfterSeconds)
    }

    @Test
    fun gatewayHtmlAndUnreadableErrors_keepHttpStatus() = runBlocking {
        for (status in listOf(502, 503, 504)) {
            for (body in listOf("<html>Gateway error</html>", "", "{broken")) {
                val result = safeApiCall<String> {
                    Response.error(status, body.toResponseBody("text/html".toMediaType()))
                } as NetworkResult.HttpError
                assertEquals(status, result.status)
                assertNull(result.problem)
            }
        }
    }
}
