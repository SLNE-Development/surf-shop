package dev.slne.surf.shop.paper.chest

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.shop.api.shopchest.StaticShopChest
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.common.service.StaticShopChestService
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.paper.hook.FancyHologramsHook
import dev.slne.surf.shop.paper.menu.*
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.location
import kotlinx.coroutines.withContext
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import java.util.concurrent.CompletableFuture

object ShopChestSelectShopView : View() {
    private val chestState = initialState<StaticShopChest>("shop-chest")

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            spacer("")
        }
    }

    private val previousItem = MenuHeads.ARROW_LEFT.clone().apply {
        displayName {
            shopColored("Vorherige Seite")
        }
    }

    private val nextItem = MenuHeads.ARROW_RIGHT.clone().apply {
        displayName {
            shopColored("Nächste Seite")
        }
    }

    private val backItem = MenuHeads.CROSS.clone().apply {
        displayName {
            error("Zurück")
        }
    }

    private val paginationState = buildLazyAsyncPaginationState { context ->
        CompletableFuture.supplyAsync {
            ShopService.loadedShops
                .filter { it.seller == context.player.uniqueId }
                .sortedBy { it.item.type.name }
                .toMutableList()
                .map { it to createShopItem(it, context.player.uniqueId, true) }
        }
    }.elementFactory { _, builder, _, shop ->
        builder.withItem(shop.second).onClick { context ->
            context.playGeneralClickSound()

            val shop = shop.first
            val chest = chestState.get(context)

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
                        ShopChestSetupView::class.java,
                        ImmutableMap.of("shop-chest", updated)
                    )
                }
            }
        }
    }.layoutTarget('R').build()

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                shopColored("Shop auswählen".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(6)
            .layout(
                "OOOOOOOOO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "OOO<B>OOO"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val pagination = paginationState.get(render)

        render.availableSlot(loadingItem)
            .displayIf(pagination::isLoading)
            .updateOnStateChange(paginationState)

        render.layoutSlot('O', outlineItem)

        render.layoutSlot('B', backItem).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                ShopChestSetupView::class.java,
                ImmutableMap.of("shop-chest", chestState.get(context))
            )
        }

        render
            .layoutSlot('<')
            .renderWith {
                if (pagination.canBack()) {
                    previousItem
                } else {
                    outlineItem
                }
            }
            .watch(paginationState)
            .onClick { context ->
                if (!pagination.canBack()) {
                    return@onClick
                }

                context.playNewPageSound()
                pagination.back()
            }

        render
            .layoutSlot('>')
            .renderWith {
                if (pagination.canAdvance()) {
                    nextItem
                } else {
                    outlineItem
                }
            }
            .watch(paginationState)
            .onClick { context ->
                if (!pagination.canAdvance()) {
                    return@onClick
                }

                context.playNewPageSound()
                pagination.advance()
            }
    }
}
