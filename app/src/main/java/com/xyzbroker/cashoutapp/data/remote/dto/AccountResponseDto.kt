package com.xyzbroker.cashoutapp.data.remote.dto

import java.math.BigDecimal

/** GET …/summary. Money is a JSON number with up to 4 decimals, read as BigDecimal to keep it exact. */
data class AccountResponseDto(
    val accountNumber: String? = null,
    val currency: String? = null,
    val status: String? = null,
    val balance: BigDecimal? = null,
    val equity: BigDecimal? = null,
    val freeMargin: BigDecimal? = null,
    val version: Long? = null,
)
