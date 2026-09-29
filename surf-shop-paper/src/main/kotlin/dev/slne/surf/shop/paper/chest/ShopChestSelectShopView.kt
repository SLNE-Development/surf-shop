package dev.slne.surf.shop.paper.chest

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.layoutTarget
import dev.slne.surf.api.paper.inventory.framework.view.onFirstRender
import dev.slne.surf.api.paper.inventory.framework.view.paginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.AbstractPaginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.shop.api.shopchest.StaticShopChest
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.common.service.StaticShopChestService
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.paper.hook.FancyHologramsHook
import dev.slne.surf.shop.paper.menu.createShopItem
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.location
import kotlinx.coroutines.withContext
import org.bukkit.Sound
import java.util.concurrent.CompletableFuture

val shopChestSelectShopView: AbstractPaginatedSurfView = paginatedSurfView("Shop auswählen") {
    val chestState = initialState<StaticShopChest>("shop-chest")

    settings {
        paginationViewRows(PaginationViewRows.FOUR)
        navigateBackOnOutsideClick(false)
    }

    layoutTarget('I')

    pagination {
        lazyAsyncSource { context ->
            CompletableFuture.supplyAsync {
                ShopService.loadedShops
                    .filter { it.seller == context.player.uniqueId }
                    .sortedBy { it.item.type.name }
                    .map {
                        it to createShopItem(
                            it,
                            context.player.uniqueId,
                            true,
                            stats = DealService.getDealStats(it.internalId)
                        )
                    }
                    .toMutableList()
            }
        }

        elementFactory { _, builder, _, shopAndItem ->
            builder.withItem(shopAndItem.second).onClick { context ->
                context.playGeneralClickSound()

                val shop = shopAndItem.first
                val chest = chestState[context]

                plugin.launch {
                    val updated = StaticShopChestService.updateChestShop(chest.chestUuid, shop.shopUuid)

                    if (updated == null) {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Es gab einen Fehler beim Zuweisen des Shops zu dieser Kiste!")
                        }
                        context.player.playSound(true) {
                            type(Sound.ENTITY_VILLAGER_NO)
                        }

                        withContext(plugin.entityDispatcher(context.player)) {
                            context.closeForPlayer()
                        }
                        return@launch
                    }

                    if (plugin.hasFancyHolograms) {
                        chest.location?.let {
                            FancyHologramsHook.createAndOrDelete(chest.chestUuid, it, shop)
                        }
                    }

                    context.player.sendText {
                        appendSuccessPrefix()
                        success("Shop wurde dieser Kiste zugewiesen!")
                    }
                    context.player.playSound(true) {
                        type(Sound.ENTITY_PLAYER_LEVELUP)
                    }
                    withContext(plugin.entityDispatcher(context.player)) {
                        context.openForPlayer(
                            shopChestSetupView::class.java,
                            ImmutableMap.of("shop-chest", updated)
                        )
                    }
                }
            }
        }
    }

    onFirstRender {
        slot(5, 1, ViewIcon(ViewIconType.RELOAD, ViewIconColor.RED).build {
            displayName { error("Zurück") }
        }).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                shopChestSetupView::class.java,
                ImmutableMap.of("shop-chest", chestState[context])
            )
        }
    }
}
