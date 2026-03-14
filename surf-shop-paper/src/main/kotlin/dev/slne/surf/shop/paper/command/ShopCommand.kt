package dev.slne.surf.shop.paper.command

import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.service.shopService
import dev.slne.surf.shop.paper.command.argument.shopArgument
import dev.slne.surf.shop.paper.menu.ShopListView
import dev.slne.surf.shop.paper.permission.PermissionRegistry
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.searchInputCache
import dev.slne.surf.surfapi.bukkit.api.command.executors.anyExecutorSuspend
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText

fun shopCommand() = commandTree("shop") {
    withPermission(PermissionRegistry.SHOP_COMMAND)

    playerExecutor { player, _ ->
        searchInputCache.remove(player.uniqueId)
        viewFrame.open(
            ShopListView::class.java,
            player
        )
    }

    literalArgument("admin") {
        withPermission(PermissionRegistry.SHOP_COMMAND_ADMIN)
        literalArgument("clearCacheAndFetch") {
            anyExecutorSuspend { sender, _ ->
                shopService.fetchShops()

                sender.sendText {
                    appendSuccessPrefix()
                    success("Der Shop-Cache wurde geleert und alle Shops wurden neu geladen.")
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
                    val success = shopService.deleteShop(shop)

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
            viewFrame.open(
                ShopListView::class.java,
                player
            )
        }
    }
}