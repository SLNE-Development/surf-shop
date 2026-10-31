package dev.slne.surf.shop.core.common.util

import kotlin.test.Test
import kotlin.test.assertEquals

class StockMathTest {

    @Test
    fun `adding stock increases the count`() {
        assertEquals(StockCalc.Ok(15, 5), StockMath.apply(10, 5, false))
    }

    @Test
    fun `removing exactly the stored amount empties the shop`() {
        assertEquals(StockCalc.Ok(0, -10), StockMath.apply(10, -10, false))
    }

    @Test
    fun `strict removal of more than stored is insufficient`() {
        assertEquals(StockCalc.Insufficient(3), StockMath.apply(3, -5, false))
    }

    @Test
    fun `partial removal takes only what is stored`() {
        assertEquals(StockCalc.Ok(0, -3), StockMath.apply(3, -5, true))
    }

    @Test
    fun `adding beyond Int MAX_VALUE overflows`() {
        assertEquals(StockCalc.Overflow, StockMath.apply(Int.MAX_VALUE, 1, false))
        assertEquals(StockCalc.Ok(Int.MAX_VALUE, 1), StockMath.apply(Int.MAX_VALUE - 1, 1, false))
    }

    @Test
    fun `zero delta keeps the count`() {
        assertEquals(StockCalc.Ok(10, 0), StockMath.apply(10, 0, false))
    }
}
