package dev.slne.surf.shop.core.common.util

/** Outcome of applying a stock delta to a shop's stored item count. */
sealed interface StockCalc {
    /** The new count and the delta that was actually applied (smaller than requested for partial removals). */
    data class Ok(val newCount: Int, val appliedDelta: Int) : StockCalc

    /** A strict removal asked for more items than are stored. */
    data class Insufficient(val available: Int) : StockCalc

    /** Adding the delta would exceed [Int.MAX_VALUE]. */
    data object Overflow : StockCalc
}

object StockMath {
    /**
     * Applies [delta] to [current] without overflow.
     *
     * @param allowPartialRemoval when removing more than stored, remove everything instead of failing
     */
    fun apply(current: Int, delta: Int, allowPartialRemoval: Boolean): StockCalc {
        val target = current.toLong() + delta

        return when {
            target > Int.MAX_VALUE -> StockCalc.Overflow
            target >= 0 -> StockCalc.Ok(target.toInt(), delta)
            allowPartialRemoval -> StockCalc.Ok(0, -current)
            else -> StockCalc.Insufficient(current)
        }
    }
}
