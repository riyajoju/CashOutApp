package com.xyzbroker.cashoutapp.domain.result_handlimg

import java.math.BigDecimal
import java.time.LocalDate

/** How a cash-out submit ended, from the customer's point of view. */
sealed interface SubmitOutcome {

    /** Request received and processing. The account is debited but nothing is paid yet. */
    data class Received(
        val reference: String,
        val amount: BigDecimal?,
        val currency: String?,
        val estimatedArrival: LocalDate?,
    ) : SubmitOutcome

    /** The service decided not to act. Nothing was debited. */
    data class Refused(
        val reason: CashOutError,
        val correlationId: String? = null,
    ) : SubmitOutcome

    /** The request never reached the service. Nothing was debited. */
    data object NotSent : SubmitOutcome

    /** Money may or may not have moved. Retry with the same idempotency key to learn the outcome. */
    data class Unknown(
        val correlationId: String? = null,
    ) : SubmitOutcome

    /** Money may or may not have moved and a person must resolve it. Do not retry. */
    data class NeedsSupport(
        val supportReference: String?,
        val correlationId: String? = null,
    ) : SubmitOutcome
}
