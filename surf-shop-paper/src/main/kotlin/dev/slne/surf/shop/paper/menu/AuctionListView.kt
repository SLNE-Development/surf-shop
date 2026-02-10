package dev.slne.surf.shop.paper.menu

import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.core.service.auctionService
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.ViewType
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material

class AuctionListView : View() {
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

    private val previousItem = buildItem(Material.ARROW) { // TODO: Menu Heads
        displayName {
            auctionColored("Vorherige Seite")
        }
    }

    private val nextItem = buildItem(Material.ARROW) { // TODO: Menu Heads
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

    private val pagination = computedPaginationState<Auction>({
        auctionService.loadedAuctions.filter { !it.isEmpty() }.toMutableList()
    }, { context, builder, index, value ->
        builder.withItem(createAuctionItem(value))
        builder.onClick { context ->
            // TODO: Buy Confirmation, Amount etc
        }
    })

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                spacer("Auktionen")
            }
            .size(6)
            .type(ViewType.CHEST)
            .layout(
                "OOOOOOOOO",
                "OIIIIIIIIO",
                "OIIIIIIIIO",
                "OIIIIIIIIO",
                "OIIIIIIIIO",
                "UOOPCNOOS"
            )
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('S', sortItem).onClick { context ->
            // TODO: Sort Menu
        }
        render.layoutSlot('U', updateItem).onClick { context ->
            // TODO: Update Menu
        }
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('C', createItem).onClick { context ->
            // TODO: Create Auction Menu
        }
        render
            .layoutSlot('P', previousItem)
            .displayIf { _ -> pagination.get(render).canBack() }
            .updateOnStateChange(pagination)
            .onClick { _ ->
                pagination.get(render).back()
            }

        render
            .layoutSlot('N', nextItem)
            .displayIf { _ -> pagination.get(render).canAdvance() }
            .updateOnStateChange(pagination)
            .onClick { _ ->
                pagination.get(render).advance()
            }
    }
}

fun createAuctionItem(auction: Auction) = auction.item.clone().apply {
    buildLore {
        emptyLine()

    }
}

fun SurfComponentBuilder.auctionColored(text: Any, vararg decoration: TextDecoration) =
    coloredComponent(text.toString(), TextColor.color(53, 187, 232), *decoration)