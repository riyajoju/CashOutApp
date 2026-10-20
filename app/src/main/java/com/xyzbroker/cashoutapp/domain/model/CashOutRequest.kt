package com.xyzbroker.cashoutapp.domain.model

import java.math.BigDecimal

/** What the customer asked for. amount must have at most two decimals; it is never rounded. */
data class CashOutRequest(
    val amount: BigDecimal,
    val beneficiaryName: String,
    val iban: String,
)
