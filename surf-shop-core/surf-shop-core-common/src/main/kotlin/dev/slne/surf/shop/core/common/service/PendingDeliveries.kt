package dev.slne.surf.shop.core.common.service

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/** A paid purchase whose items could not be handed out because the buyer was offline. */
data class PendingDelivery<T>(
    val transactionId: UUID,
    val shopUuid: UUID,
    val amount: Int,
    val item: T
)

/**
 * In-memory queue of paid purchases that are handed out when the buyer joins again.
 * Not persistent: whatever is still pending on shutdown must be reported for manual reconciliation.
 */
class PendingDeliveries<T> {
    private val byPlayer = ConcurrentHashMap<UUID, List<PendingDelivery<T>>>()

    fun add(playerUuid: UUID, delivery: PendingDelivery<T>) {
        byPlayer.merge(playerUuid, listOf(delivery)) { current, added -> current + added }
    }

    /** Removes and returns everything queued for [playerUuid], oldest first. */
    fun take(playerUuid: UUID): List<PendingDelivery<T>> = byPlayer.remove(playerUuid).orEmpty()

    fun snapshot(): Map<UUID, List<PendingDelivery<T>>> = byPlayer.toMap()

    fun count(): Int = byPlayer.values.sumOf { it.size }
}
