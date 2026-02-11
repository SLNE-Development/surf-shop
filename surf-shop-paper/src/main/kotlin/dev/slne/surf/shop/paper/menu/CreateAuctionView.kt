package dev.slne.surf.shop.paper.menu

import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.paper.menu.select.PlayerInventorySelectItemView
import dev.slne.surf.shop.paper.menu.select.PriceSelectView
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.Colors
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object CreateAuctionView : View() {
    private val itemState: State<ItemStack> = initialState("create-item")
    private val priceState: State<Int> = initialState("create-price")

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                auctionColored("Auktion erstellen".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(5)
            .layout("OOOOOOOOO", "O       O", "O P I C O", "O       O", "OOOOBOOOO")
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('P', pricePerItemItem.clone().apply {
            if (priceState.get(render) > 0) {
                buildLore {
                    emptyLine()
                    line {
                        spacer("-")
                        appendSpace()
                        auctionColored("Aktueller Preis Pro Item: ${priceState.get(render)}")
                    }
                }
            }
        }).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                PriceSelectView::class.java,
                ImmutableMap.of(
                    "create-item", itemState.get(render),
                    "create-price", priceState.get(render)
                )
            )
        }

        if (itemState.get(render)?.isEmpty == true) {
            render.layoutSlot('I', itemNotSet).onClick { context ->
                context.playGeneralClickSound()
                context.openForPlayer(
                    PlayerInventorySelectItemView::class.java,
                    ImmutableMap.of(
                        "create-price",
                        priceState.get(context)
                    )
                )
            }
        } else {
            render.layoutSlot('I', itemState.get(render).apply {
                amount = 1
            }).onClick { context ->
                context.playGeneralClickSound()
                context.openForPlayer(
                    PlayerInventorySelectItemView::class.java,
                    ImmutableMap.of(
                        "create-price",
                        priceState.get(context)
                    )
                )
            }
        }

        render.layoutSlot('C', createItem(render)).onClick { context ->
            context.playGeneralClickSound()
        }
        render.layoutSlot('B', backItem).onClick { context ->
            context.playGeneralClickSound()
            context.back()
        }
    }

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            spacer("")
        }
    }

    private fun createItem(context: RenderContext) = MenuHeads.CHECK.clone().apply {
        displayName {
            auctionColored("Auktion erstellen")
        }

        buildLore {
            emptyLine()
            line {
                spacer("-")
                appendSpace()
                auctionColored("Item: ")
                if (itemState.get(context)?.isEmpty == true) {
                    variableValue("Kein Item ausgewählt")
                } else {
                    append(
                        Component.translatable(itemState.get(context).type.translationKey())
                            .color(Colors.VARIABLE_VALUE)
                    )
                }
            }

            line {
                spacer("-")
                appendSpace()
                auctionColored("Preis pro Item: ")
                if (priceState.get(context) <= 0) {
                    variableValue("Kein Preis festgelegt")
                } else {
                    variableValue(priceState.get(context))
                }
            }
        }
    }

    private val itemNotSet = MenuHeads.QUESTION.clone().apply {
        displayName {
            auctionColored("Kein Item ausgewählt")
        }
    }

    private val backItem = MenuHeads.CROSS.clone().apply {
        displayName {
            error("Abbrechen")
        }
    }

    private val pricePerItemItem = MenuHeads.DOLLAR.clone().apply {
        displayName {
            auctionColored("Preis pro Item festlegen")
        }
    }
}