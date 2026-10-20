package com.xyzbroker.cashoutapp.data.remote.dto

import java.math.BigDecimal

data class CashOutRequestDto(
    val amount: BigDecimal,
    val method: String = "bank",
    val destination: DestinationDto,
    val reason: String? = null,
)

data class DestinationDto(
    val beneficiaryName: String,
    val iban: String,
)
