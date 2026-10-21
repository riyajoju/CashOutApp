package com.xyzbroker.cashoutapp.data.remote.api

import com.xyzbroker.cashoutapp.BuildConfig
import com.xyzbroker.cashoutapp.data.remote.dto.CashOutRequestDto
import com.xyzbroker.cashoutapp.data.remote.dto.DestinationDto
import com.xyzbroker.cashoutapp.di.NetworkModule
import kotlinx.coroutines.runBlocking
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal

// Checks the exact requests the app puts on the wire, using the production OkHttp client.
class CashOutApiTest {

    private val server = MockWebServer()
    private lateinit var api: CashOutApi

    @Before
    fun setUp() {
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(NetworkModule.provideOkhttpClient(HttpLoggingInterceptor()))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CashOutApi::class.java)
    }

    @After
    fun tearDown() = server.shutdown()

    private fun request(amount: String) = CashOutRequestDto(
        amount = BigDecimal(amount),
        destination = DestinationDto("A. Customer", "GB82 WEST 1234 5698 7654 32"),
    )

    @Test
    fun submit_sendsKeyAuthAndExactJson() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"isSuccess":true,"reference":"CO-1"}"""))

        api.submitCashOut("100245", "key-1", request("250.00"))

        val sent = server.takeRequest()
        assertEquals("POST", sent.method)
        assertEquals("/v1/accounts/100245/cashouts", sent.path)
        assertEquals("key-1", sent.getHeader("Idempotency-Key"))
        assertEquals("Bearer ${BuildConfig.API_TOKEN}", sent.getHeader("Authorization"))
        assertEquals("application/json; charset=UTF-8", sent.getHeader("Content-Type"))
        // amount is a number with two decimals, method defaults to bank, and the null reason is omitted.
        assertEquals(
            """{"amount":250.00,"method":"bank","destination":{"beneficiaryName":"A. Customer","iban":"GB82 WEST 1234 5698 7654 32"}}""",
            sent.body.readUtf8(),
        )
    }

    @Test
    fun submit_largeAmountIsNotSentInExponentForm() = runBlocking {
        server.enqueue(MockResponse().setBody("{}"))

        api.submitCashOut("100245", "key-2", request("10000000.00"))

        assertEquals(
            true,
            server.takeRequest().body.readUtf8().startsWith("""{"amount":10000000.00,""")
        )
    }

    @Test
    fun summary_keepsAllFourDecimals() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"accountNumber":"100245","currency":"USD","status":"active","balance":34412.6500,""" +
                        """"equity":36120.5512,"freeMargin":34347.2000,"version":1000}""",
            ),
        )

        val body = api.getSummary("100245").body()!!

        assertEquals("/v1/accounts/100245/summary", server.takeRequest().path)
        assertEquals(BigDecimal("34412.6500"), body.balance)
        assertEquals(BigDecimal("36120.5512"), body.equity)
        assertEquals(BigDecimal("34347.2000"), body.freeMargin)
        assertEquals(1000L, body.version)
    }
}
