package dev.slne.surf.shop.core.common.util

import dev.slne.surf.shop.core.common.util.ShopPriceRules.MAX_PRICE
import dev.slne.surf.shop.core.common.util.ShopPriceRules.isAllowedTotal
import dev.slne.surf.shop.core.common.util.ShopPriceRules.isValidPrice
import dev.slne.surf.shop.core.common.util.ShopPriceRules.parsePriceInput
import dev.slne.surf.shop.core.common.util.ShopPriceRules.taxFor
import dev.slne.surf.shop.core.common.util.ShopPriceRules.totalPrice
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShopPriceRulesTest {

    @Test
    fun `parsePriceInput accepts plain and decimal input`() {
        assertEquals(5.0, parsePriceInput(" 5 "))
        assertEquals(1.5, parsePriceInput("1,5"))
        assertEquals(1.5, parsePriceInput("1.5"))
        assertEquals(0.01, parsePriceInput("0.01"))
        assertEquals(100_000_000.0, parsePriceInput("100000000"))
    }

    @Test
    fun `parsePriceInput rejects invalid input`() {
        listOf(
            null, "", "   ", "abc", "NaN", "Infinity", "-Infinity", "1e309", "1e3",
            "-1", "0", "0,001", "0.009", "100000000.01", "1.000", "1.000,50", "1,2,3"
        ).forEach { assertNull(parsePriceInput(it), "should reject '$it'") }
    }

    @Test
    fun `isValidPrice requires finite value in range`() {
        assertTrue(isValidPrice(0.01))
        assertTrue(isValidPrice(100_000_000.0))
        assertTrue(isValidPrice(0.015))
        assertFalse(isValidPrice(Double.NaN))
        assertFalse(isValidPrice(Double.POSITIVE_INFINITY))
        assertFalse(isValidPrice(0.0099))
        assertFalse(isValidPrice(100_000_000.01))
        assertFalse(isValidPrice(1e-12))
    }

    @Test
    fun `totalPrice is exact and rounded to cents`() {
        assertEquals(BigDecimal("0.30"), totalPrice(0.1, 3))
        assertEquals(BigDecimal("0.02"), totalPrice(0.015, 1))
        assertEquals(BigDecimal("6400000000.00"), totalPrice(100_000_000.0, 64))
    }

    @Test
    fun `isAllowedTotal enforces MAX_TOTAL boundary`() {
        assertTrue(isAllowedTotal(totalPrice(MAX_PRICE, 99)))
        assertFalse(isAllowedTotal(totalPrice(MAX_PRICE, 100)))
        assertFalse(isAllowedTotal(BigDecimal.ZERO))
    }

    @Test
    fun `taxFor uses three percent rounded half down`() {
        assertEquals(BigDecimal("0.30"), taxFor(BigDecimal("10.00")))
        assertEquals(BigDecimal("0.01"), taxFor(BigDecimal("0.50")))
    }
}
