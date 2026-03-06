package dev.slne.surf.shop.paper.hook

import dev.heliosares.auxprotect.api.AuxProtectAPI
import dev.heliosares.auxprotect.database.DbEntry
import dev.heliosares.auxprotect.database.EntryAction
import dev.heliosares.auxprotect.spigot.AuxProtectSpigot
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.paper.plugin
import org.bukkit.entity.Player

object AuxProtectHook {
    lateinit var shopCreatedAction: EntryAction
    lateinit var shopDeletedAction: EntryAction
    lateinit var shopBoughtAction: EntryAction
    lateinit var shopDepositedAction: EntryAction
    lateinit var shopWithdrawnAction: EntryAction

    fun create() {
        // @formatter:off
        shopCreatedAction = AuxProtectAPI.createAction(plugin.name, "surfshop_created", "created shop", null)
        shopDeletedAction = AuxProtectAPI.createAction(plugin.name, "surfshop_deleted", "deleted shop", null)
        shopBoughtAction = AuxProtectAPI.createAction(plugin.name, "surfshop_bought", "bought from shop", null)
        shopDepositedAction = AuxProtectAPI.createAction(plugin.name, "surfshop_deposited", "deposited items into shop", null)
        shopWithdrawnAction = AuxProtectAPI.createAction(plugin.name, "surfshop_withdrawn", "withdrew items from shop", null)
        // @formatter:on

        plugin.logger.info("Hooked into AuxProtect. Creations, deletions, buys, deposits and withdrawals will now be logged.")
    }

    fun logBuy(player: Player, shop: Shop, amount: Int) {
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

    fun logCreate(player: Player, shop: Shop) {
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

    fun logDelete(player: Player, shop: Shop) {
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

    fun logDeposit(player: Player, shop: Shop, amount: Int) {
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

    fun logWithdraw(player: Player, shop: Shop, amount: Int) {
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