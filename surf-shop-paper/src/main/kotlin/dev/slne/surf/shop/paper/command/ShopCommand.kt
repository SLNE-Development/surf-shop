package dev.slne.surf.shop.paper.command

import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.common.service.StaticShopChestService
import dev.slne.surf.shop.core.paper.util.sellerName
import dev.slne.surf.shop.paper.command.argument.shopArgument
import dev.slne.surf.shop.paper.menu.ChestShopEditState
import dev.slne.surf.shop.paper.menu.NpcShopState
import dev.slne.surf.shop.paper.menu.OwnShopState
import dev.slne.surf.shop.paper.menu.shopListView
import dev.slne.surf.shop.paper.permission.PermissionRegistry
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.searchInputCache

fun shopCommand() = commandTree("shop") {
    withPermission(PermissionRegistry.SHOP_COMMAND)

    playerExecutor { player, _ ->
        searchInputCache.remove(player.uniqueId)
        ChestShopEditState.setChest(player.uniqueId, null)
        NpcShopState.setInNpcShop(player.uniqueId, false)
        OwnShopState.setInOwn(player.uniqueId, false)
        viewFrame.open(
            shopListView::class.java,
            player
        )
    }

    literalArgument("admin") {
        withPermission(PermissionRegistry.SHOP_COMMAND_ADMIN)
        literalArgument("clearCacheAndFetch") {
            anyExecutorSuspend { sender, _ ->
                ShopService.fetchShops()
                DealService.fetchDeals()
                StaticShopChestService.fetchChests()

                sender.sendText {
                    appendSuccessPrefix()
                    success("Die Caches wurden geleert und alle Daten wurden neu geladen.")
                }
            }
        }

        literalArgument("clearPlayerDataCache") {
            anyExecutor { sender, _ ->
                searchInputCache.clear()
                plugin.sorts.clear()

                sender.sendText {
                    appendSuccessPrefix()
                    success("Der Player-Daten-Cache wurde geleert.")
                }
            }
        }

        literalArgument("forceDelete") {
            shopArgument("shop") {
                anyExecutorSuspend { sender, args ->
                    val shop: Shop by args
                    val success = ShopService.deleteShop(shop)

                    sender.sendText {
                        appendSuccessPrefix()
                        if (success) {
                            success("Der Shop von ${shop.sellerName} wurde erfolgreich gelöscht.")
                        } else {
                            error("Der Shop von ${shop.sellerName} konnte nicht gelöscht werden.")
                        }
                    }
                }
            }
        }
    }

    greedyStringArgument("search") {
        playerExecutor { player, args ->
            val search: String by args
            searchInputCache[player.uniqueId] = search
            ChestShopEditState.setChest(player.uniqueId, null)
            NpcShopState.setInNpcShop(player.uniqueId, false)
            viewFrame.open(
                shopListView::class.java,
                player
            )
            OwnShopState.setInOwn(player.uniqueId, false)
        }
    }
}
