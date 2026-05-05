package dev.slne.surf.shop.paper.menu.settings

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.settings.SettingsHook
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import org.bukkit.Material

object SettingsShopView : View() {
    private val dealMessagesState = mutableState(false)
    private val shopSoundsState = mutableState(false)
    private val dealMessagesInitialState = mutableState(false)
    private val shopSoundsInitialState = mutableState(false)

    override fun onInit(config: ViewConfigBuilder) {
        config
            .title("Einstellungen")
            .size(3)
            .layout(
                "OOOOOOOOO",
                "OOOAOBOOO",
                "OOOOOOOOO"
            )
            .cancelOnClick()
    }

    override fun onFirstRender(render: RenderContext) {
        dealMessagesInitialState.set(SettingsHook.hasDealMadeMessagesEnabled(render.player.uniqueId), render)
        shopSoundsInitialState.set(SettingsHook.hasShopSoundsEnabled(render.player.uniqueId), render)

        dealMessagesState.set(dealMessagesInitialState.get(render), render)
        shopSoundsState.set(shopSoundsInitialState.get(render), render)

        render.layoutSlot('O', outlineItem)
        render.layoutSlot('A').renderWith { dealMessagesItem(dealMessagesState.get(render)) }.onClick { click ->
            dealMessagesState.set(!dealMessagesState.get(click), click)
            click.update()

            click.player.sendText {
                success("Du hast die Deal-Nachrichten ")
                variableValue(if (dealMessagesState.get(click)) "aktiviert" else "deaktiviert")
                success(".")
            }
        }
        render.layoutSlot('B').renderWith { shopSoundsItem(shopSoundsState.get(render)) }.onClick { click ->
            shopSoundsState.set(!shopSoundsState.get(click), click)
            click.update()

            click.player.sendText {
                success("Du hast die Shop-Sounds ")
                variableValue(if (shopSoundsState.get(click)) "aktiviert" else "deaktiviert")
                success(".")
            }
        }

    }

    override fun onClose(close: me.devnatan.inventoryframework.context.CloseContext) {
        val playerUuid = close.player.uniqueId

        val dealMessagesCurrent = dealMessagesState.get(close)
        val shopSoundsCurrent = shopSoundsState.get(close)

        val dealMessagesInitial = dealMessagesInitialState.get(close)
        val shopSoundsInitial = shopSoundsInitialState.get(close)

        plugin.launch {
            if (dealMessagesCurrent != dealMessagesInitial) {
                SettingsHook.setHasDealMadeMessagesEnabled(playerUuid, dealMessagesCurrent)
            }

            if (shopSoundsCurrent != shopSoundsInitial) {
                SettingsHook.setShopSoundsEnabled(playerUuid, shopSoundsCurrent)
            }
        }
    }
}

private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
    displayName {
        spacer("")
    }
}

private fun dealMessagesItem(enabled: Boolean) =
    buildItem(if (enabled) Material.LIME_CANDLE else Material.RED_CANDLE) {
        displayName {
            shopColored("Deal-Nachrichten")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()

                if (enabled) {
                    success("✔ Aktiviert")
                } else {
                    error("✘ Deaktiviert")
                }
            }
        }
    }

private fun shopSoundsItem(enabled: Boolean) =
    buildItem(if (enabled) Material.LIME_CANDLE else Material.RED_CANDLE) {
        displayName {
            shopColored("Shop-Sounds")
        }

        buildLore {
            emptyLine()
            line {
                spacer("»")
                appendSpace()

                if (enabled) {
                    success("✔ Aktiviert")
                } else {
                    error("✘ Deaktiviert")
                }
            }
        }
    }
