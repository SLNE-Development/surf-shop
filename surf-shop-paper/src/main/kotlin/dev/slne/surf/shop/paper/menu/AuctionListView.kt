package dev.slne.surf.shop.paper.menu

import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.api.auction.AuctionSortType
import dev.slne.surf.shop.core.service.auctionService
import dev.slne.surf.shop.core.util.dealCount
import dev.slne.surf.shop.paper.dialog.searchAuctionItemDialog
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.searchInputCache
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.Context
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.context.SlotClickContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack

@Suppress("UnstableApiUsage")
object AuctionListView : View() {
    private val selectedSort = mutableState(AuctionSortType.TIME_ASC)

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            spacer("")
        }
    }

    private val createItem = MenuHeads.CREATE_BUTTON.clone().apply {
        displayName {
            auctionColored("Auktion erstellen")
        }
    }

    private val previousItem = MenuHeads.ARROW_LEFT.clone().apply { // TODO: Menu Heads
        displayName {
            auctionColored("Vorherige Seite")
        }
    }

    private val nextItem = MenuHeads.ARROW_RIGHT.clone().apply { // TODO: Menu Heads
        displayName {
            auctionColored("Nächste Seite")
        }
    }

    private val searchItem = buildItem(Material.BRUSH) {
        displayName {
            auctionColored("Suchen")
        }
    }

    private fun sortItem(state: AuctionSortType) = buildItem(Material.COMPARATOR) {
        displayName {
            auctionColored("Sortieren")
        }

        buildLore {
            emptyLine()
            line { auctionColored("Sortierung".toSmallCaps(), TextDecoration.BOLD) }
            line {
                if (state == AuctionSortType.PRICE_ASC) {
                    appendSpace()
                    spacer("-")
                    appendSpace()
                    auctionColored("Preis aufsteigend")
                } else {
                    spacer("-")
                    appendSpace()
                    white("Preis aufsteigend")
                }
            }

            line {
                if (state == AuctionSortType.PRICE_DESC) {
                    appendSpace()
                    spacer("-")
                    appendSpace()
                    auctionColored("Preis absteigend")
                } else {
                    spacer("-")
                    appendSpace()
                    white("Preis absteigend")
                }
            }

            line {
                if (state == AuctionSortType.TIME_ASC) {
                    appendSpace()
                    spacer("-")
                    appendSpace()
                    auctionColored("Zeit aufsteigend")
                } else {
                    spacer("-")
                    appendSpace()
                    white("Zeit aufsteigend")
                }
            }

            line {
                if (state == AuctionSortType.TIME_DESC) {
                    appendSpace()
                    spacer("-")
                    appendSpace()
                    auctionColored("Zeit absteigend")
                } else {
                    spacer("-")
                    appendSpace()
                    white("Zeit absteigend")
                }
            }

            line {
                if (state == AuctionSortType.MOST_STORED) {
                    appendSpace()
                    spacer("-")
                    appendSpace()
                    auctionColored("Meiste gelagerte Items")
                } else {
                    spacer("-")
                    appendSpace()
                    white("Meiste gelagerte Items")
                }
            }

            line {
                if (state == AuctionSortType.MOST_DEALS) {
                    appendSpace()
                    spacer("-")
                    appendSpace()
                    auctionColored("Meiste Verkäufe")
                } else {
                    spacer("-")
                    appendSpace()
                    white("Meiste Verkäufe")
                }
            }
        }
    }

    private val updateItem = buildItem(Material.REPEATER) {
        displayName {
            auctionColored("Aktualisieren")
        }
    }

    private val paginationState = buildComputedPaginationState<Auction> { context ->
        getLoadedAuctionsSortedFiltered(
            plugin.getSorting(context.player.uniqueId),
            searchInputCache[context.player.uniqueId]
        ).toMutableList()
    }.itemFactory { builder, auction ->
        builder.withItem(createAuctionItem(auction))
    }.layoutTarget('R').build()

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                auctionColored("Auktionen".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(6)
            .layout(
                "OOOOOOOOO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "UAOPCNOOS"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        selectedSort.set(plugin.getSorting(render.player.uniqueId), render)

        render
            .layoutSlot('S')
            .updateOnClick()
            .renderWith { sortItem(selectedSort.get(render)) }
            .onClick { context ->
                context.playGeneralClickSound()

                selectedSort.set(selectedSort.get(render).next(), render)
                plugin.setSorting(context.player.uniqueId, selectedSort.get(render))
            }
        render.layoutSlot('U', updateItem).onClick { context ->
            // TODO: Update Menu

            context.playGeneralClickSound()
        }
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('A', searchItem).onClick { context ->
            context.playGeneralClickSound()
            context.player.closeInventory()
            context.player.showDialog(searchAuctionItemDialog())
        }
        render.layoutSlot('C', createItem).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                CreateAuctionView::class.java,
                ImmutableMap.of(
                    "create-item",
                    ItemStack.empty(),
                    "create-price",
                    0
                )
            )
        }
        render
            .layoutSlot('P')
            .renderWith {
                previousItem
            }
            .watch(paginationState)
            .displayIf { _ -> paginationState.get(render).canBack() }
            .onClick { context ->
                context.playNewPageSound()
                paginationState.get(render).back()
            }

        render
            .layoutSlot('N')
            .renderWith {
                nextItem
            }
            .watch(paginationState)
            .displayIf { _ -> paginationState.get(render).canAdvance() }
            .onClick { context ->
                context.playNewPageSound()
                paginationState.get(render).advance()
            }
    }

    override fun onUpdate(update: Context) {
        updatePagination(update)
    }

    override fun onResume(origin: Context, target: Context) {
        target.update()
    }

    private fun updatePagination(context: Context) {
        val pagination = paginationState.get(context)
        pagination.switchTo(pagination.currentPageIndex())
    }
}

fun createAuctionItem(auction: Auction) = auction.item.clone().apply {
    val oldLore = lore()?.toMutableList() ?: mutableListOf()
    val newEntries = mutableListOf<Component>()

    newEntries.add(Component.empty())
    newEntries.add(buildText {
        auctionColored("Verkaufsinformation".toSmallCaps(), TextDecoration.BOLD)
    })

    newEntries.add(buildText {
        spacer("-")
        appendSpace()
        auctionColored("Preis: ")
        variableValue("${auction.pricePerItem}/Item")
    })

    newEntries.add(buildText {
        spacer("-")
        appendSpace()
        auctionColored("Auf Lager: ")
        if (auction.storedItemCount > 0) {
            variableValue("${auction.storedItemCount} Items")
        } else {
            error("Ausverkauft")
        }
    })

    newEntries.add(buildText {
        spacer("-")
        appendSpace()
        auctionColored("Verkäufer: ")
        variableValue(auction.sellerName)
    })

    newEntries.add(buildText {
        spacer("-")
        appendSpace()
        auctionColored("Erstellt am: ")
        variableValue(auction.createdAt)
    })

    lore(oldLore + newEntries)
}

fun SlotClickContext.playGeneralClickSound() {
    player.playSound(true) {
        type(Sound.UI_BUTTON_CLICK)
    }
}

fun SlotClickContext.playNewPageSound() {
    player.playSound(true) {
        type(Sound.ENTITY_CHICKEN_EGG)
    }
}

private fun getLoadedAuctionsSortedFiltered(
    sortType: AuctionSortType,
    search: String?
): List<Auction> {

    val base = auctionService.loadedAuctions

    val filtered = if (search.isNullOrBlank()) {
        base
    } else {
        val terms = search
            .lowercase()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }

        base.filter { auction ->
            val item = auction.item

            val material = item.type.name.lowercase()
            val enchantments = item.enchantments.keys
                .joinToString(" ") { it.key.key.lowercase() }

            val searchable = "$material $enchantments"

            terms.all { term -> searchable.contains(term) }
        }
    }

    return when (sortType) {
        AuctionSortType.PRICE_ASC -> filtered.sortedBy { it.pricePerItem }
        AuctionSortType.PRICE_DESC -> filtered.sortedByDescending { it.pricePerItem }
        AuctionSortType.TIME_ASC -> filtered.sortedBy { it.createdAt }
        AuctionSortType.TIME_DESC -> filtered.sortedByDescending { it.createdAt }
        AuctionSortType.MOST_STORED -> filtered.sortedByDescending { it.storedItemCount }
        AuctionSortType.MOST_DEALS -> filtered.sortedByDescending { it.dealCount }
    }
}


fun SurfComponentBuilder.auctionColored(text: Any, vararg decoration: TextDecoration) =
    coloredComponent(text.toString(), TextColor.color(252, 233, 121), *decoration)