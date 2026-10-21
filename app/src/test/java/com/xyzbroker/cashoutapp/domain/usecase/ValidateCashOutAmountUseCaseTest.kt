package com.xyzbroker.cashoutapp.domain.usecase

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class ValidateCashOutAmountUseCaseTest {
    private val validate = ValidateCashOutAmountUseCase()
    private val available = BigDecimal("1250.50")

    @Test
    fun emptyAndWhitespace_areRequired() {
        listOf("", "  ").forEach { assertEquals(AmountValidation.Empty, validate(it, available)) }
    }

    @Test
    fun invalidFormats_areRejected() {
        listOf("-50", "+50", "50.001", "1.0E7", "1,000", "50.", ".50", "NaN", "abc").forEach {
            assertEquals(it, AmountValidation.Invalid, validate(it, available))
        }
    }

    @Test
    fun minimumAndAvailable_boundariesAreInclusive() {
        assertEquals(
            AmountValidation.BelowMinimum(BigDecimal("50.00")),
            validate("49.99", available)
        )
        assertEquals(AmountValidation.Valid(BigDecimal("50.00")), validate("50", available))
        assertEquals(AmountValidation.Valid(available), validate("1250.50", available))
        assertEquals(AmountValidation.AboveAvailable(available), validate("1250.51", available))
    }

    @Test
    fun validInput_isTrimmedAndNormalizedWithoutRounding() {
        assertEquals(AmountValidation.Valid(BigDecimal("250.10")), validate(" 250.1 ", available))
    }
}
