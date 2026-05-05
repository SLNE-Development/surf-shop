package dev.slne.surf.shop.paper.menu.select

import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.util.objectListOf
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.shop.paper.menu.CreateShopView
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.settings.SettingsHook
import dev.slne.surf.shop.paper.settings.hasSettingsApi
import dev.slne.surf.shop.paper.util.MenuHeads
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.state.State
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack

private val blacklistedItems = objectListOf(
    Material.PLAYER_HEAD
)

object PlayerInventorySelectItemView : View() {
    private val priceState: State<Int> = initialState("create-price")
    private val itemState = initialState<ItemStack>("create-item")

    private val paginationState = buildComputedPaginationState<ItemStack> { context ->
        context.player.inventory.storageContents.filterNotNull().toMutableList()
    }.itemFactory { builder, item ->
        builder.withItem(item).onClick { context ->

            if (blacklistedItems.contains(item.type)) {
                if (!hasSettingsApi() || SettingsHook.hasShopSoundsEnabled(context.player.uniqueId)) {
                    context.player.playSound(true) {
                        type(Sound.BLOCK_NOTE_BLOCK_BASS)
                        pitch(2f)
                    }
                }
                context.player.sendText {
                    appendErrorPrefix()
                    error("Dieses Item kann nicht verkauft werden.")
                }
                return@onClick
            }

            if (!hasSettingsApi() || SettingsHook.hasShopSoundsEnabled(context.player.uniqueId)) {
                context.player.playSound(true) {
                    type(Sound.BLOCK_NOTE_BLOCK_PLING)
                    pitch(2f)
                }
            }

            context.openForPlayer(
                CreateShopView::class.java,
                ImmutableMap.of(
                    "create-item",
                    item.clone().apply {
                        amount = 1
                    },
                    "create-price",
                    priceState.get(context)
                )
            )
        }
    }.layoutTarget('I').build()

    override fun onInit(config: ViewConfigBuilder) {
        config.titleBuilder {
            shopColored("Item wählen".toSmallCaps(), TextDecoration.BOLD)
        }
            .size(6)
            .layout(
                "XXXXQXXXX",
                "IIIIIIIII",
                "IIIIIIIII",
                "IIIIIIIII",
                "IIIIIIIII",
                "XXXXBXXXX"
            )
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        render.layoutSlot('X', outlineItem)
        render.layoutSlot('Q', explainItem)
        render.layoutSlot('B', backItem).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                CreateShopView::class.java, ImmutableMap.of(
                    "create-price", priceState.get(context),
                    "create-item", itemState.get(context)
                )
            )
        }
    }

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            spacer("")
        }
    }

    private val backItem = MenuHeads.CROSS.clone().apply {
        displayName {
            error("Abbrechen")
        }
    }

    private val explainItem = MenuHeads.QUESTION.clone().apply {
        displayName {
            shopColored("Erklärung".toSmallCaps(), TextDecoration.BOLD)
        }

        buildLore {
            emptyLine()
            line {
                spacer("-")
                appendSpace()
                shopColored("Klicke auf ein Item, um es auszuwählen.")
            }
            line {
                spacer("-")
                appendSpace()
                shopColored("Nach der Auswahl kommst du in das Vorschau-Menü,")
            }
            line {
                appendSpace()
                appendSpace()
                appendSpace()
                shopColored("in dem du deinen Shop erstellen kannst.")
            }
        }
    }
}