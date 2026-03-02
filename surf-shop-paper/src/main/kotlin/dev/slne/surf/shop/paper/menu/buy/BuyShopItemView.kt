package dev.slne.surf.shop.paper.menu.buy

import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.paper.menu.outlineItem
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.playNoSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration


object BuyShopItemView : View() {
    private val shopState = initialState<Shop>("buy-shop")
    private val amountState = mutableState(1)

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                shopColored("Items kaufen".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(3)
            .layout("OOOOOOOOO", "O   I C O", "OOOOBOOOO")
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('I', shopState.get(render).item.clone().apply {
            val oldLore = lore()?.toMutableList() ?: mutableListOf()
            val newEntries = mutableListOf<Component>()

            val amount = amountState.get(render)
            val shop = shopState.get(render)

            newEntries.add(Component.empty())
            newEntries.add(buildText {
                shopColored("Shopinformationen".toSmallCaps(), TextDecoration.BOLD)
            })

            newEntries.add(buildText {
                spacer("-")
                appendSpace()
                shopColored("Einzelpreis: ")
                variableValue(shop.pricePerItem)
            })

            newEntries.add(buildText {
                spacer("-")
                appendSpace()
                shopColored("Auf Lager: ")
                if (shop.storedItemCount > 0) {
                    variableValue("${shop.storedItemCount} Items")
                } else {
                    error("Ausverkauft")
                }
            })

            newEntries.add(buildText {
                spacer("-")
                appendSpace()
                shopColored("Verkäufer: ")
                variableValue(shop.sellerName)
            })

            newEntries.add(Component.empty())

            newEntries.add(buildText {
                shopColored("Kaufsinformationen".toSmallCaps(), TextDecoration.BOLD)
            })

            newEntries.add(buildText {
                spacer("-")
                appendSpace()
                shopColored("Anzahl: ")
                variableValue(amount)
            })

            newEntries.add(buildText {
                spacer("-")
                appendSpace()
                shopColored("Gesamtpreis: ")
                variableValue(amount * shop.pricePerItem)
            })

            lore(oldLore + newEntries)
        })
            .updateOnStateChange(amountState)
            .onClick { context ->
                context.playGeneralClickSound()

                val shop = shopState.get(context)
                val amount = amountState.get(context)

                if (context.isLeftClick) {
                    if (shop.storedItemCount < amount + 1) {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Es sind nicht genügend Items auf Lager!")
                        }

                        context.player.playNoSound()
                        return@onClick
                    }
                    amountState.set(amountState.get(context) + 1, context)
                    return@onClick
                }

                if (context.isShiftLeftClick) {
                    if (shop.storedItemCount < amount + 64) {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Es sind nicht genügend Items auf Lager!")
                        }

                        context.player.playNoSound()
                        return@onClick
                    }
                    amountState.set(amountState.get(context) + 64, context)
                    return@onClick
                }

                if (context.isRightClick) {
                    if (amount - 1 <= 0) {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Die Menge muss mindestens 1 betragen!")
                        }

                        context.player.playNoSound()
                        return@onClick
                    }
                    amountState.set(amountState.get(context) - 1, context)
                    return@onClick
                }

                if (context.isShiftRightClick) {
                    if (amount - 64 <= 0) {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Die Menge muss mindestens 1 betragen!")
                        }

                        context.player.playNoSound()
                        return@onClick
                    }

                    amountState.set(amountState.get(context) - 64, context)

                }
            }
    }
}