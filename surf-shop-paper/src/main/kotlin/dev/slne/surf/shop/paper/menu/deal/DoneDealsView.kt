package dev.slne.surf.shop.paper.menu.deal

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.util.dateTimeFormatter
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.paper.util.boughtByName
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.paper.menu.*
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.formatPriceNice
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.Component
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
        val shopsById = ShopService.loadedShops.associateBy { it.internalId }

        DealService.loadedDeals.asSequence()
            .mapNotNull { deal ->
                val shop = shopsById[deal.shopInternalId]
                if (shop?.seller == context.player.uniqueId) deal to shop else null
            }
            .sortedByDescending { it.first.boughtAt }
            .toMutableList()
    }.elementFactory { _, builder, _, dealToShop ->
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
                "OOOPBNOOD"
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

        render.layoutSlot('D', dealLogItem).onClick { click ->
            click.openForPlayer(DoneDealsView::class.java)
            click.playGeneralClickSound()
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

    private val dealLogItem = buildItem(Material.CHEST) {
        displayName {
            shopColored("Abgeschlossene Deals")
        }
    }

    fun createDealAndShopItem(dealAndShop: Pair<Deal, Shop?>): ItemStack {
        val deal = dealAndShop.first
        val shop = dealAndShop.second ?: return buildItem(Material.BARRIER) {
            displayName {
                error("Shop nicht gefunden")
            }
        }

        return shop.item.clone().apply {
            amount = 1

            val oldLore = lore()?.toMutableList() ?: mutableListOf()
            val newEntries = mutableListOf<Component>()

            newEntries.add(Component.empty())
            newEntries.add(buildText {
                shopColored("Verkaufsprotokoll".toSmallCaps(), TextDecoration.BOLD)
            })

            newEntries.add(buildText {
                spacer("-")
                appendSpace()
                shopColored("Menge: ")
                variableValue("${deal.amount}x")
            })

            newEntries.add(buildText {
                spacer("-")
                appendSpace()
                shopColored("Gesamtpreis: ")
                variableValue(formatPriceNice(shop.pricePerItem * deal.amount))
            })

            newEntries.add(buildText {
                spacer("-")
                appendSpace()
                shopColored("Käufer: ")
                variableValue(deal.boughtByName ?: "#Unbekannt")
            })

            newEntries.add(buildText {
                spacer("-")
                appendSpace()
                shopColored("Verkauft am: ")
                variableValue(deal.boughtAt.format(dateTimeFormatter))
            })

            newEntries.add(Component.empty())


            lore(oldLore + newEntries)
        }
    }
}