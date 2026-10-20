package com.xyzbroker.cashoutapp.domain.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * A received cash-out request. The account is debited, but it is processing: nothing is paid yet.
 * amount, currency, and estimatedArrival are null if the service sent them in an unreadable form.
 */
data class CashOutReceipt(
    val reference: String,
    val amount: BigDecimal?,
    val currency: String?,
    val estimatedArrival: LocalDate?,
)
