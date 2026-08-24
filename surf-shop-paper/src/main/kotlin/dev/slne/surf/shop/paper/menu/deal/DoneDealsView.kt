package dev.slne.surf.shop.paper.menu.deal

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.util.dateTimeFormatter
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.layoutTarget
import dev.slne.surf.api.paper.inventory.framework.view.paginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.shop.api.deal.Deal
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.paper.util.boughtByName
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.util.formatPriceNice
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture

val doneDealsView = paginatedSurfView("Verkaufsverlauf") {
    settings {
        paginationViewRows(PaginationViewRows.FOUR)
    }

    layoutTarget('I')

    pagination {
        lazyAsyncSource { context ->
            CompletableFuture.supplyAsync {
                DealService.loadedDeals.asSequence()
                    .mapNotNull { deal ->
                        val shop = ShopService.getShopByInternalId(deal.shopInternalId)
                        if (shop?.seller == context.player.uniqueId) deal to shop else null
                    }
                    .sortedByDescending { it.first.boughtAt }
                    .toMutableList()
            }
        }

        elementFactory { _, builder, _, dealToShop ->
            builder.withItem(createDealAndShopItem(dealToShop)).onClick { context ->
                context.playGeneralClickSound()
            }
        }
    }
}

private fun createDealAndShopItem(dealAndShop: Pair<Deal, Shop?>): ItemStack {
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
            variableValue(deal.boughtByName)
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
