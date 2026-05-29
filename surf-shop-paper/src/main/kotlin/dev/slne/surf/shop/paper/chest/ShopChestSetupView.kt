package dev.slne.surf.shop.paper.chest

import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.api.shopchest.StaticShopChest
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.paper.menu.CreateShopView
import dev.slne.surf.shop.paper.menu.edit.EditShopView
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.formatPriceNice
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object ShopChestSetupView : View() {
    private val chestState = initialState<StaticShopChest>("shop-chest")

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            spacer("")
        }
    }

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                shopColored("Shop Chest".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(3)
            .layout("OOOOOOOOO", "O C S E O", "OOOOOOOOO")
            .cancelInteractions()
            .build()
    }

    override fun onFirstRender(render: RenderContext) {
        val chest = chestState.get(render)

        render.layoutSlot('O', outlineItem)

        render.layoutSlot('C', createShopItem()).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                CreateShopView::class.java,
                ImmutableMap.of(
                    "create-item", ItemStack.empty(),
                    "create-price", 0.0
                )
            )
        }

        render.layoutSlot('S', selectShopItem()).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                ShopChestSelectShopView::class.java,
                ImmutableMap.of("shop-chest", chest)
            )
        }

        val shopUuid = chest.shopUuid
        val currentShop = if (shopUuid != null) {
            ShopService.loadedShops.firstOrNull { it.shopUuid == shopUuid }
        } else null

        if (currentShop != null) {
            render.layoutSlot('E', currentShopItem(currentShop)).onClick { context ->
                context.playGeneralClickSound()
                context.openForPlayer(
                    EditShopView::class.java,
                    ImmutableMap.of("edit-shop", currentShop)
                )
            }
        } else {
            render.layoutSlot('E', noShopSetItem())
        }
    }

    private fun createShopItem() = MenuHeads.CREATE_BUTTON.clone().apply {
        displayName {
            shopColored("Neuen Shop erstellen")
        }

        buildLore {
            emptyLine()
            line {
                spacer("Erstelle einen neuen Shop und".toSmallCaps())
            }
            line {
                spacer("weise ihn dieser Kiste zu.".toSmallCaps())
            }
        }
    }

    private fun selectShopItem() = buildItem(Material.COMPASS) {
        displayName {
            shopColored("Bestehenden Shop auswählen")
        }

        buildLore {
            emptyLine()
            line {
                spacer("Wähle einen deiner bestehenden".toSmallCaps())
            }
            line {
                spacer("Shops für diese Kiste aus.".toSmallCaps())
            }
        }
    }

    private fun currentShopItem(shop: Shop) = shop.item.clone().apply {
        amount = 1
        displayName {
            shopColored("Aktueller Shop")
        }

        val oldLore = lore()?.toMutableList() ?: mutableListOf()
        val newEntries = mutableListOf<Component>()

        newEntries.add(Component.empty())
        newEntries.add(buildText {
            spacer("-")
            appendSpace()
            shopColored("Preis: ")
            variableValue("${formatPriceNice(shop.pricePerItem)}/Item")
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
        newEntries.add(Component.empty())
        newEntries.add(buildText {
            spacer("Klicke, um den Shop zu bearbeiten.".toSmallCaps())
        })

        lore(oldLore + newEntries)
    }

    private fun noShopSetItem() = MenuHeads.QUESTION.clone().apply {
        displayName {
            shopColored("Kein Shop zugewiesen")
        }

        buildLore {
            emptyLine()
            line {
                spacer("Erstelle oder wähle einen Shop".toSmallCaps())
            }
            line {
                spacer("um ihn dieser Kiste zuzuweisen.".toSmallCaps())
            }
        }
    }
}
