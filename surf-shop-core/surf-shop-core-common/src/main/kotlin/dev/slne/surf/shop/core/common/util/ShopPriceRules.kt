package dev.slne.surf.shop.core.common.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Shared rules for shop prices and purchase totals, used by the Paper plugin and the backend.
 */
object ShopPriceRules {
    const val MIN_PRICE: Double = 0.01
    const val MAX_PRICE: Double = 100_000_000.0

    /** Upper bound of a single purchase total; follows surf-transaction's `decimal(20, 10)` amount column. */
    val MAX_TOTAL: BigDecimal = BigDecimal("9999999999.99")

    val TAX_RATE: BigDecimal = BigDecimal("0.03")

    private val PRICE_INPUT = Regex("""^\d{1,9}([.,]\d{1,2})?$""")

    /** Whether [price] is a finite number within [MIN_PRICE]..[MAX_PRICE]. */
    fun isValidPrice(price: Double): Boolean = price.isFinite() && price in MIN_PRICE..MAX_PRICE

    /**
     * Parses player input such as `5`, `1,5` or `1.50` into a price.
     *
     * Accepts `.` or `,` as decimal separator and at most two decimal places; rejects exponents,
     * `NaN`, `Infinity`, signs, thousands separators and values outside the allowed range.
     *
     * @return the parsed price, or `null` when the input is invalid
     */
    fun parsePriceInput(text: String?): Double? {
        val trimmed = text?.trim() ?: return null
        if (!PRICE_INPUT.matches(trimmed)) return null

        val price = trimmed.replace(',', '.').toDouble()
        return price.takeIf(::isValidPrice)
    }

    /** Exact total for [amount] items, rounded to cents (`HALF_UP`). */
    fun totalPrice(pricePerItem: Double, amount: Int): BigDecimal =
        pricePerItem.toBigDecimal()
            .multiply(BigDecimal(amount))
            .setScale(2, RoundingMode.HALF_UP)

    /** Whether [total] is positive and does not exceed [MAX_TOTAL]. */
    fun isAllowedTotal(total: BigDecimal): Boolean =
        total.signum() > 0 && total <= MAX_TOTAL

    /** Shop tax for [total], rounded to cents (`HALF_DOWN`). */
    fun taxFor(total: BigDecimal): BigDecimal =
        total.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_DOWN)
}
