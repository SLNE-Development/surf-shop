package dev.slne.surf.shop.paper.hook

import dev.heliosares.auxprotect.api.AuxProtectAPI
import dev.heliosares.auxprotect.database.DbEntry
import dev.heliosares.auxprotect.database.EntryAction
import dev.heliosares.auxprotect.spigot.AuxProtectSpigot
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.paper.plugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object AuxProtectHook {
    lateinit var shopCreatedAction: EntryAction
    lateinit var shopDeletedAction: EntryAction
    lateinit var shopBoughtAction: EntryAction
    lateinit var shopDepositedAction: EntryAction
    lateinit var shopWithdrawnAction: EntryAction

    fun create() {
        Bukkit.getAsyncScheduler().runNow(plugin) {
            runCatching {
                // @formatter:off
                shopCreatedAction = createAction(plugin.name, "surfshop_created", "created shop")
                shopDeletedAction = createAction(plugin.name, "surfshop_deleted", "deleted shop")
                shopBoughtAction = createAction(plugin.name, "surfshop_bought", "bought from shop")
                shopDepositedAction = createAction(plugin.name, "surfshop_deposited", "deposited into shop")
                shopWithdrawnAction = createAction(plugin.name, "surfshop_withdrawn", "withdrew from shop")
                // @formatter:on
            }

            plugin.logger.info("Hooked into AuxProtect. Creations, deletions, buys, deposits and withdrawals will now be logged.")
        }
    }

    private fun createAction(name: String, key: String, nText: String): EntryAction = runCatching {
        AuxProtectAPI.createAction(name, key, nText, null)
    }.getOrNull() ?: EntryAction.getAction(key)

    fun logBuy(player: Player, shop: Shop, amount: Int) {
        println("Logging buy action for player ${player.name}, shop owned by ${shop.seller}, amount $amount, price per item ${shop.pricePerItem}, item ${shop.item.serialize()}")
        Bukkit.getAsyncScheduler().runNow(plugin) {
            AuxProtectAPI.add(
                DbEntry(
                    AuxProtectSpigot.getLabel(player),
                    shopBoughtAction,
                    true,
                    null,
                    "#shop",
                    "shopOwner=${shop.seller}; amount=$amount; pricePerItem=${shop.pricePerItem}; item=${shop.item.serialize()} "
                )
            )
        }

        println("Logged buy action for player ${player.name}, shop owned by ${shop.seller}, amount $amount, price per item ${shop.pricePerItem}, item ${shop.item.serialize()}")
    }

    fun logCreate(player: Player, shop: Shop) {
        Bukkit.getAsyncScheduler().runNow(plugin) {
            AuxProtectAPI.add(
                DbEntry(
                    AuxProtectSpigot.getLabel(player),
                    shopCreatedAction,
                    true,
                    null,
                    "#shop",
                    "shopOwner=${shop.seller}; item=${shop.item.serialize()}"
                )
            )
        }
    }

    fun logDelete(player: Player, shop: Shop) {
        Bukkit.getAsyncScheduler().runNow(plugin) {
            AuxProtectAPI.add(
                DbEntry(
                    AuxProtectSpigot.getLabel(player),
                    shopDeletedAction,
                    true,
                    null,
                    "#shop",
                    "shopOwner=${shop.seller}; item=${shop.item.serialize()}"
                )
            )
        }
    }

    fun logDeposit(player: Player, shop: Shop, amount: Int) {
        Bukkit.getAsyncScheduler().runNow(plugin) {
            AuxProtectAPI.add(
                DbEntry(
                    AuxProtectSpigot.getLabel(player),
                    shopDepositedAction,
                    true,
                    null,
                    "#shop",
                    "shopOwner=${shop.seller}; amount=$amount; item=${shop.item.serialize()}"
                )
            )
        }
    }

    fun logWithdraw(player: Player, shop: Shop, amount: Int) {
        Bukkit.getAsyncScheduler().runNow(plugin) {
            AuxProtectAPI.add(
                DbEntry(
                    AuxProtectSpigot.getLabel(player),
                    shopWithdrawnAction,
                    true,
                    null,
                    "#shop",
                    "shopOwner=${shop.seller}; amount=$amount; item=${shop.item.serialize()}"
                )
            )
        }
    }
}