package dev.slne.surf.shop.paper.hook

import dev.heliosares.auxprotect.AuxProtectPaper
import dev.heliosares.auxprotect.api.AuxProtectAPI
import dev.heliosares.auxprotect.database.DbEntry
import dev.heliosares.auxprotect.database.EntryAction
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
        Bukkit.getAsyncScheduler().runNow(plugin) {
            AuxProtectAPI.add(
                DbEntry(
                    AuxProtectPaper.getLabel(player),
                    shopBoughtAction,
                    true,
                    AuxProtectPaper.getLabel(shop.seller),
                    "pricePerItem=${shop.pricePerItem}, paidPrice=${shop.pricePerItem * amount}; item=${shop.item}"
                )
            )
        }
    }

    fun logCreate(player: Player, shop: Shop) {
        Bukkit.getAsyncScheduler().runNow(plugin) {
            AuxProtectAPI.add(
                DbEntry(
                    AuxProtectPaper.getLabel(player),
                    shopCreatedAction,
                    true,
                    shop.item.type.toString(),
                    "pricePerItem=${shop.pricePerItem}; item=${shop.item}"
                )
            )
        }
    }

    fun logDelete(player: Player, shop: Shop) {
        Bukkit.getAsyncScheduler().runNow(plugin) {
            AuxProtectAPI.add(
                DbEntry(
                    AuxProtectPaper.getLabel(player),
                    shopDeletedAction,
                    true,
                    shop.item.type.toString(),
                    "item=${shop.item}"
                )
            )
        }
    }

    fun logDeposit(player: Player, shop: Shop, amount: Int) {
        Bukkit.getAsyncScheduler().runNow(plugin) {
            AuxProtectAPI.add(
                DbEntry(
                    AuxProtectPaper.getLabel(player),
                    shopDepositedAction,
                    true,
                    shop.item.type.toString(),
                    "amount=$amount; item=${shop.item}"
                )
            )
        }
    }

    fun logWithdraw(player: Player, shop: Shop, amount: Int) {
        Bukkit.getAsyncScheduler().runNow(plugin) {
            AuxProtectAPI.add(
                DbEntry(
                    AuxProtectPaper.getLabel(player),
                    shopWithdrawnAction,
                    true,
                    shop.item.type.toString(),
                    "amount=$amount; item=${shop.item}"
                )
            )
        }
    }
}