package dev.slne.surf.shop.paper.menu

import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.api.auction.AuctionSortType
import dev.slne.surf.shop.core.service.auctionService
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.Context
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material

@Suppress("UnstableApiUsage")
object AuctionListView : View() {
    private val sortTypeState = initialState<AuctionSortType>("sort")

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

    private val sortItem = buildItem(Material.COMPARATOR) {
        displayName {
            auctionColored("Sortieren")
        }
    }

    private val updateItem = buildItem(Material.REPEATER) {
        displayName {
            auctionColored("Aktualisieren")
        }
    }

    private val paginationState = buildComputedPaginationState<Auction> {
        auctionService.loadedAuctions.filter { !it.isEmpty() }.toMutableList()
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
                "UOOPCNOOS"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        val pagination = paginationState.get(render)
        render.layoutSlot('S', sortItem).onClick { context ->
            // TODO: Sort Menu
        }
        render.layoutSlot('U', updateItem).onClick { context ->
            // TODO: Update Menu
        }
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('C', createItem).onClick { context ->
            viewFrame.open(CreateAuctionView::class.java, context.player)
        }
        render
            .layoutSlot('P', previousItem)
            .displayIf { _ -> paginationState.get(render).canBack() }
            .updateOnStateChange(paginationState)
            .onClick { _ ->
                paginationState.get(render).back()
            }

        render
            .layoutSlot('N', nextItem)
            .displayIf { _ -> paginationState.get(render).canAdvance() }
            .updateOnStateChange(paginationState)
            .onClick { _ ->
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
    buildLore {
        emptyLine()

    }
}

fun SurfComponentBuilder.auctionColored(text: Any, vararg decoration: TextDecoration) =
    coloredComponent(text.toString(), TextColor.color(252, 233, 121), *decoration)