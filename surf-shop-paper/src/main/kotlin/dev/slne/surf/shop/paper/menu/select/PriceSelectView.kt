package dev.slne.surf.shop.paper.menu.select

import dev.slne.surf.shop.paper.menu.auctionColored
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.state.MutableState
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.math.max

object PriceSelectView : View() {
    private val itemState: State<ItemStack> = initialState("create-item")
    private val amountState: State<Int> = initialState("create-amount")
    private val priceState: MutableState<Int> = initialState("create-price")

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                auctionColored("Preis festlegen".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout(
                "OOOOOOOOO",
                "O       O",
                "O21 P 34O",
                "O       O",
                "OOOOBOOOO"
            )
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)

        render.layoutSlot('1', minusOne).onClick { context ->
            priceState.set(max(0, priceState.get(render) - 1), render)
            context.update()
        }

        render.layoutSlot('2', minusThirtyTwo).onClick { context ->
            priceState.set(max(0, priceState.get(render) - 32), render)
            context.update()
        }

        render.layoutSlot('3', plusOne).onClick { context ->
            priceState.set(priceState.get(render) + 1, render)
            context.update()
        }

        render.layoutSlot('4', plusThirtyTwo).onClick { context ->
            priceState.set(priceState.get(render) + 32, render)
            context.update()
        }

        render.layoutSlot('B', backItem).onClick { context ->
            context.back()
        }

        render.layoutSlot('P', valueItem(render))
    }

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName { spacer("") }
    }

    private fun valueItem(context: RenderContext) = buildItem(Material.GOLD_INGOT) {
        displayName {
            auctionColored("Preis: ", TextDecoration.BOLD)
            appendSpace()
            auctionColored(priceState.get(context))
        }
    }

    private val plusOne = MenuHeads.PLUS.clone().apply {
        displayName { auctionColored("+1") }
    }

    private val plusThirtyTwo = MenuHeads.PLUS.clone().apply {
        displayName { auctionColored("+32") }
    }

    private val minusOne = MenuHeads.MINUS.clone().apply {
        displayName { auctionColored("-1") }
    }

    private val minusThirtyTwo = MenuHeads.MINUS.clone().apply {
        displayName { auctionColored("-32") }
    }

    private val backItem = MenuHeads.CHECK.clone().apply {
        displayName { auctionColored("Übernehmen") }
    }
}
