package com.xyzbroker.cashoutapp.domain.usecase

import java.math.BigDecimal
import javax.inject.Inject

sealed interface AmountValidation {
    data class Valid(val amount: BigDecimal) : AmountValidation

    data object Empty : AmountValidation

    data object Invalid : AmountValidation
    data class BelowMinimum(val minimum: BigDecimal) : AmountValidation
    data class AboveAvailable(val available: BigDecimal) : AmountValidation
}

class ValidateCashOutAmountUseCase @Inject constructor() {

    operator fun invoke(input: String, available: BigDecimal): AmountValidation {
        val text = input.trim()
        if (text.isEmpty()) return AmountValidation.Empty
        if (!AMOUNT_PATTERN.matches(text)) return AmountValidation.Invalid
        // The pattern allows at most two decimals, so setScale(2) never rounds.
        val amount = BigDecimal(text).setScale(2)
        return when {
            amount < MINIMUM -> AmountValidation.BelowMinimum(MINIMUM)
            amount > available -> AmountValidation.AboveAvailable(available)
            else -> AmountValidation.Valid(amount)
        }
    }

    companion object {
        val MINIMUM: BigDecimal = BigDecimal("50.00")

        // ASCII digits only, optional dot with one or two decimals; no signs, commas, or exponents.
        private val AMOUNT_PATTERN = Regex("""\d+(\.\d{1,2})?""")
    }
}
