package com.xyzbroker.cashoutapp.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class AccountBalance(
    val currency: String,
    val balance: BigDecimal,
    val equity: BigDecimal,
    val freeMargin: BigDecimal,
) {
    val available: BigDecimal
        get() = minOf(balance, equity, freeMargin).setScale(2, RoundingMode.FLOOR)
}
