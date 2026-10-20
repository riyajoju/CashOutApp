package com.xyzbroker.cashoutapp.data.remote.dto

/**
 * 200 body of POST …/cashouts. The same shape carries success (isSuccess = true) and the legacy
 * refusal (isSuccess = false, errorCode). Every field is optional; amount is a decimal string.
 */
data class CashOutResponseDto(
    val isSuccess: Boolean? = null,
    val errorCode: String? = null,
    val cashoutId: String? = null,
    val reference: String? = null,
    val status: String? = null,
    val amount: String? = null,
    val currency: String? = null,
    val estimatedArrival: String? = null,
)
