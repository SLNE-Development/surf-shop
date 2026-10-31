package dev.slne.surf.shop.paper.hook

import dev.heliosares.auxprotect.AuxProtectPaper
import dev.heliosares.auxprotect.api.AuxProtectAPI
import dev.heliosares.auxprotect.database.DbEntry
import dev.heliosares.auxprotect.database.EntryAction
import dev.heliosares.auxprotect.exceptions.AlreadyExistsException
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.paper.event.ShopPurchaseEvent
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.paper.plugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level

object AuxProtectHook {

    private enum class ShopAction(val key: String, val text: String) {
        CREATED("surfshop_created", "created shop"),
        DELETED("surfshop_deleted", "deleted shop"),
        BOUGHT("surfshop_bought", "bought from shop"),
        DEPOSITED("surfshop_deposited", "deposited into shop"),
        WITHDRAWN("surfshop_withdrawn", "withdrew from shop")
    }

    /** Registered actions; an action that is missing is skipped instead of failing the log call. */
    private val actions = ConcurrentHashMap<ShopAction, EntryAction>()

    /** How long to wait for AuxProtect's database before giving up (one check per second). */
    private const val MAX_CONNECT_ATTEMPTS = 60

    /**
     * Registers the shop actions once AuxProtect's database is connected (registering earlier fails, e.g.
     * with `BusyException`) and starts logging committed purchases.
     */
    fun create() {
        PurchaseListener.register()

        val attempts = AtomicInteger()
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, { task ->
            if (AuxProtectAPI.getSQLManager()?.isConnected == true) {
                task.cancel()
                registerActions()
            } else if (attempts.incrementAndGet() >= MAX_CONNECT_ATTEMPTS) {
                task.cancel()
                plugin.logger.warning(
                    "AuxProtect's database is not connected after $MAX_CONNECT_ATTEMPTS seconds; " +
                            "shop actions will not be logged to AuxProtect."
                )
            }
        }, 0, 1, TimeUnit.SECONDS)
    }

    private fun registerActions() {
        ShopAction.entries.forEach { action ->
            val entryAction = try {
                AuxProtectAPI.createAction(plugin.name, action.key, action.text, null)
            } catch (_: AlreadyExistsException) {
                EntryAction.getAction(action.key)
            } catch (e: Exception) {
                plugin.logger.log(Level.WARNING, "Could not register AuxProtect action ${action.key}", e)
                null
            }

            if (entryAction != null) {
                actions[action] = entryAction
            } else {
                plugin.logger.warning("AuxProtect action ${action.key} is unavailable and will not be logged.")
            }
        }

        plugin.logger.info("Hooked into AuxProtect: ${actions.size}/${ShopAction.entries.size} shop actions are logged.")
    }

    /** @param user a player or player UUID, see [AuxProtectPaper.getLabel] */
    private fun log(action: ShopAction, user: Any, target: String, data: String) {
        val entryAction = actions[action] ?: return
        val label = AuxProtectPaper.getLabel(user)

        Bukkit.getAsyncScheduler().runNow(plugin) {
            AuxProtectAPI.add(DbEntry(label, entryAction, true, target, data))
        }
    }

    /** Logs purchases once they are paid – not when the buy button is clicked. */
    private object PurchaseListener : Listener {
        @EventHandler
        fun onPurchase(event: ShopPurchaseEvent) = log(
            ShopAction.BOUGHT, event.buyer, AuxProtectPaper.getLabel(event.shop.seller),
            "pricePerItem=${event.shop.pricePerItem}, amount=${event.amount}, paidPrice=${event.total}; " +
                    "item=${event.shop.item}; transaction=${event.transactionId}"
        )
    }

    fun logCreate(player: Player, shop: Shop) = log(
        ShopAction.CREATED, player, shop.item.type.toString(),
        "pricePerItem=${shop.pricePerItem}; item=${shop.item}"
    )

    fun logDelete(player: Player, shop: Shop) = log(
        ShopAction.DELETED, player, shop.item.type.toString(),
        "item=${shop.item}"
    )

    fun logDeposit(player: Player, shop: Shop, amount: Int) = log(
        ShopAction.DEPOSITED, player, shop.item.type.toString(),
        "amount=$amount; item=${shop.item}"
    )

    fun logWithdraw(player: Player, shop: Shop, amount: Int) = log(
        ShopAction.WITHDRAWN, player, shop.item.type.toString(),
        "amount=$amount; item=${shop.item}"
    )
}
