package dev.slne.surf.shop.paper.menu.deal

import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.dealService
import dev.slne.surf.shop.core.paper.util.boughtByName
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.core.service.shopService
import dev.slne.surf.shop.paper.menu.*
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.formatPriceNice
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object DoneDealsView : View() {
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

    private val paginationState = buildLazyPaginationState { context ->
        dealService.loadedDeals.asSequence()
            .map { deal -> deal to shopService.loadedShops.find { it.internalId == deal.shopInternalId } }
            .filter { it.second != null }.filter { it.second?.seller == context.player.uniqueId }
            .sortedByDescending { it.first.boughtAt }
            .toMutableList()
    }.elementFactory { context, builder, _, dealToShop ->
        builder.withItem(createDealAndShopItem(dealToShop)).onClick { context ->
            context.playGeneralClickSound()
        }
    }.layoutTarget('R').build()

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                shopColored("Du hast verkauft...".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(6)
            .layout(
                "OOOOOOOOO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "OOOPBNOOO"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val pagination = paginationState.get(render)

        render.layoutSlot('O', outlineItem)

        render
            .layoutSlot('P')
            .renderWith {
                if (pagination.canBack()) {
                    previousItem
                } else {
                    ShopListView.outlineItem
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
            .layoutSlot('N')
            .renderWith {
                if (pagination.canAdvance()) {
                    nextItem
                } else {
                    ShopListView.outlineItem
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

        render.layoutSlot('B', backItem).onClick { click ->
            click.playGeneralClickSound()

            if (OwnShopState.isInOwn(click.player.uniqueId)) {
                click.openForPlayer(OwnShopsListView::class.java)
            } else {
                click.openForPlayer(ShopListView::class.java)
            }
        }
    }

    fun createDealAndShopItem(dealAndShop: Pair<Deal, Shop?>): ItemStack {
        val deal = dealAndShop.first
        val shop = dealAndShop.second ?: return buildItem(Material.BARRIER) {
            displayName {
                shopColored("Shop nicht gefunden")
            }
        }

        return shop.item.clone().apply {
            displayName {
                variableValue("${deal.amount}x ")
                spacer("an ")
                variableValue(deal.boughtByName ?: "#Unbekannt")
                spacer(" für ")
                variableValue(formatPriceNice(shop.pricePerItem * deal.amount))
            }
            amount = 1
        }
    }
}