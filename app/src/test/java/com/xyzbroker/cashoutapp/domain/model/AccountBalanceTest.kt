package com.xyzbroker.cashoutapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class AccountBalanceTest {
    @Test
    fun available_usesEachPossibleMinimumAndNeverRoundsUp() {
        val low = BigDecimal("1250.5099")
        val high = BigDecimal("3000.0000")
        listOf(
            Triple(low, high, high),
            Triple(high, low, high),
            Triple(high, high, low)
        ).forEach { (b, e, f) ->
            assertEquals(BigDecimal("1250.50"), AccountBalance("USD", b, e, f).available)
        }
    }

    @Test
    fun zeroAvailable_isPreserved() {
        assertEquals(
            BigDecimal("0.00"),
            AccountBalance("USD", BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ONE).available
        )
    }
}
