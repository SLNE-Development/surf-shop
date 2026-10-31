package dev.slne.surf.shop.core.common.service

import dev.slne.surf.shop.api.shop.Shop
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ShopMutationsTest {

    private class FakeStore {
        val shops = ConcurrentHashMap<UUID, Shop>()
        var persistCalls = 0
        var failNext: Throwable? = null
        var gate: CompletableDeferred<Unit>? = null

        suspend fun persist(shop: Shop): Shop {
            persistCalls++
            gate?.await()
            failNext?.let {
                failNext = null
                throw it
            }
            shops[shop.shopUuid] = shop
            return shop
        }
    }

    private val uuid: UUID = UUID.randomUUID()
    private val store = FakeStore().apply {
        shops[uuid] = Shop.empty().copy(shopUuid = uuid, storedItemCount = 10, pricePerItem = 1.0)
    }
    private val mutations = ShopMutations(currentShop = { store.shops[it] }, persist = store::persist)

    @Test
    fun `changeStock applies the delta to the current shop`(): Unit = runBlocking {
        val result = mutations.changeStock(uuid, 5, false)

        assertIs<StockChangeResult.Changed>(result)
        assertEquals(15, store.shops.getValue(uuid).storedItemCount)
    }

    @Test
    fun `save with unknown outcome quarantines the shop`(): Unit = runBlocking {
        store.failNext = IOException("response lost")
        assertFailsWith<IOException> { mutations.changeStock(uuid, 5, false) }

        assertTrue(mutations.isQuarantined(uuid))
        assertFailsWith<ShopSaveRejectedException> { mutations.changeStock(uuid, -2, false) }
        assertFailsWith<ShopSaveRejectedException> { mutations.updatePrice(uuid, 2.0) }
        assertEquals(1, store.persistCalls)
    }

    @Test
    fun `definitely rejected save does not quarantine`(): Unit = runBlocking {
        store.failNext = ShopSaveRejectedException("rejected")
        assertFailsWith<ShopSaveRejectedException> { mutations.changeStock(uuid, 5, false) }

        assertFalse(mutations.isQuarantined(uuid))
        assertIs<StockChangeResult.Changed>(mutations.changeStock(uuid, -2, false))
        assertEquals(8, store.shops.getValue(uuid).storedItemCount)
    }

    @Test
    fun `clearQuarantine allows mutations again`(): Unit = runBlocking {
        store.failNext = IOException("response lost")
        assertFailsWith<IOException> { mutations.changeStock(uuid, 5, false) }

        mutations.clearQuarantine()

        assertIs<StockChangeResult.Changed>(mutations.changeStock(uuid, -2, false))
    }

    @Test
    fun `saved shop carries the block state at completion, not at start`(): Unit = runBlocking {
        mutations.block(uuid)
        store.gate = CompletableDeferred()

        val pending = async { mutations.changeStock(uuid, 1, false) }
        while (store.persistCalls == 0) yield()

        mutations.unblock(uuid)
        store.gate!!.complete(Unit)

        val result = pending.await()
        assertIs<StockChangeResult.Changed>(result)
        assertFalse(result.shop.isBlocked)
    }

    @Test
    fun `saved shop is blocked when blocked during the save`(): Unit = runBlocking {
        store.gate = CompletableDeferred()

        val pending = async { mutations.updatePrice(uuid, 3.0) }
        while (store.persistCalls == 0) yield()

        mutations.block(uuid)
        store.gate!!.complete(Unit)

        assertTrue(pending.await()!!.isBlocked)
    }
}
