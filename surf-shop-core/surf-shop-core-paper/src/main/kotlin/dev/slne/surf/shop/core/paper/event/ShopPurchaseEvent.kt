package dev.slne.surf.shop.core.paper.event

import dev.slne.surf.shop.api.shop.Shop
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.math.BigDecimal
import java.util.*

/**
 * Called asynchronously once a purchase has been paid (the payment is committed), whether the items are
 * handed out immediately or queued until the buyer joins again.
 */
class ShopPurchaseEvent(
    val buyer: UUID,
    val shop: Shop,
    val amount: Int,
    val total: BigDecimal,
    val transactionId: UUID
) : Event(true) {

    override fun getHandlers(): HandlerList = HANDLERS

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList = HANDLERS
    }
}
