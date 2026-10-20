package com.xyzbroker.cashoutapp.data.remote.api

import com.xyzbroker.cashoutapp.data.remote.dto.AccountResponseDto
import com.xyzbroker.cashoutapp.data.remote.dto.CashOutRequestDto
import com.xyzbroker.cashoutapp.data.remote.dto.CashOutResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface CashOutApi {

    @GET("v1/accounts/{accountNumber}/summary")
    suspend fun getSummary(
        @Path("accountNumber") accountNumber: String,
    ): Response<AccountResponseDto>

    @POST("v1/accounts/{accountNumber}/cashouts")
    suspend fun submitCashOut(
        @Path("accountNumber") accountNumber: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: CashOutRequestDto,
    ): Response<CashOutResponseDto>
}
