package dev.slne.surf.shop.core.common.service

import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDeliveriesTest {

    private val deliveries = PendingDeliveries<String>()
    private val player: UUID = UUID.randomUUID()
    private val shop: UUID = UUID.randomUUID()

    private fun delivery(item: String, amount: Int = 1) =
        PendingDelivery(transactionId = UUID.randomUUID(), shopUuid = shop, amount = amount, item = item)

    @Test
    fun `take returns queued deliveries in order and removes them`() {
        val first = delivery("grass", 1)
        val second = delivery("stone", 5)
        deliveries.add(player, first)
        deliveries.add(player, second)

        assertEquals(listOf(first, second), deliveries.take(player))
        assertTrue(deliveries.take(player).isEmpty())
    }

    @Test
    fun `take for a player without deliveries is empty`() {
        assertTrue(deliveries.take(UUID.randomUUID()).isEmpty())
    }

    @Test
    fun `deliveries are kept per player`() {
        val other = UUID.randomUUID()
        val mine = delivery("grass")
        val theirs = delivery("stone")
        deliveries.add(player, mine)
        deliveries.add(other, theirs)

        assertEquals(listOf(mine), deliveries.take(player))
        assertEquals(listOf(theirs), deliveries.take(other))
    }

    @Test
    fun `snapshot and count reflect what is still pending`() {
        val other = UUID.randomUUID()
        deliveries.add(player, delivery("grass"))
        deliveries.add(player, delivery("dirt"))
        deliveries.add(other, delivery("stone"))

        assertEquals(3, deliveries.count())
        assertEquals(setOf(player, other), deliveries.snapshot().keys)

        deliveries.take(player)
        assertEquals(1, deliveries.count())
    }
}
