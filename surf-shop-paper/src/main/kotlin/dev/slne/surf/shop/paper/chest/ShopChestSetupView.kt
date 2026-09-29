package dev.slne.surf.shop.paper.chest

import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.*
import dev.slne.surf.api.paper.inventory.framework.view.container.dsl.blockRow
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.initialState
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.api.shopchest.StaticShopChest
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.paper.menu.createShopView
import dev.slne.surf.shop.paper.menu.edit.editShopView
import dev.slne.surf.shop.paper.menu.playGeneralClickSound
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.util.formatPriceNice
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

val shopChestSetupView: AbstractSurfView = surfView("Shop Chest") {
    val chestState = initialState<StaticShopChest>("shop-chest")

    settings {
        rows(3)
        navigateBackOnOutsideClick(false)
    }

    containerDefaults {
        blockRow(1)
        blockRow(2)
        blockRow(3)
    }

    onFirstRender {
        val chest = chestState[this]

        slot(2, 3, createNewShopItem()).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                createShopView::class.java,
                ImmutableMap.of(
                    "create-item", ItemStack.empty(),
                    "create-price", 0.0
                )
            )
        }

        slot(2, 5, selectShopItem()).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                shopChestSelectShopView::class.java,
                ImmutableMap.of("shop-chest", chest)
            )
        }

        val currentShop = chest.shopUuid?.let { ShopService.getShop(it) }

        if (currentShop != null) {
            slot(2, 7, currentShopItem(currentShop)).onClick { context ->
                context.playGeneralClickSound()
                context.openForPlayer(
                    editShopView::class.java,
                    ImmutableMap.of("edit-shop", currentShop)
                )
            }
        } else {
            slot(2, 7, noShopSetItem())
        }
    }
}

private fun createNewShopItem() = ViewIcon(ViewIconType.PLUS, ViewIconColor.GREEN).build {
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

private fun noShopSetItem() = ViewIcon(ViewIconType.QUESTION_MARK, ViewIconColor.YELLOW).build {
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
