package com.xyzbroker.cashoutapp.data.remote.dto

import java.math.BigDecimal

data class ProblemDto(
    val code: String? = null,
    val detail: String? = null,
    val correlationId: String? = null,
    val fieldErrors: Map<String, String>? = null,
    val minimum: BigDecimal? = null,
    val maxWithdrawable: BigDecimal? = null,
    val supportReference: String? = null,
    val retryAfterSeconds: Long? = null,
)
