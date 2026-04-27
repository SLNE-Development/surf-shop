package dev.slne.surf.shop.paper.menu

import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.core.util.dateTimeFormatter
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.api.shop.ShopSortingType
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.paper.util.dealCount
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.core.paper.util.sellerName
import dev.slne.surf.shop.core.paper.util.totalSoldItems
import dev.slne.surf.shop.paper.dialog.searchShopItemDialog
import dev.slne.surf.shop.paper.menu.buy.BuyShopItemView
import dev.slne.surf.shop.paper.menu.delete.DeleteShopView
import dev.slne.surf.shop.paper.menu.edit.EditShopView
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.*
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import me.devnatan.inventoryframework.context.SlotClickContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

object ShopListView : View() {
    private val selectedSort = mutableState(ShopSortingType.TIME_ASC)

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            spacer("")
        }
    }

    private val createItem = MenuHeads.CREATE_BUTTON.clone().apply {
        displayName {
            shopColored("Shop erstellen")
        }
    }

    private val previousItem = MenuHeads.ARROW_LEFT.clone().apply {
        displayName {
            shopColored("Vorherige Seite")
        }
    }

    private val nextItem = MenuHeads.ARROW_RIGHT.clone().apply {
        displayName {
            shopColored("Nächste Seite")
        }
    }

    private fun searchItem(playerUuid: UUID) = buildItem(Material.BRUSH) {
        displayName {
            shopColored("Suchen")
        }

        buildLore {
            emptyLine()

            if (searchInputCache.containsKey(playerUuid)) {
                line {
                    appendBlob()
                    spacer("Aktueller Suchbegriff: ".toSmallCaps())
                    variableValue(searchInputCache[playerUuid] ?: "#null")
                }

                emptyLine()
            }

            line {
                appendBlob()
                spacer("Nutze ".toSmallCaps())
                white("@Name".toSmallCaps())
                spacer(" für Verkäufersuche".toSmallCaps())
            }

            line {
                appendBlob()
                white("SHIFT".toSmallCaps())
                spacer(" zum resetten".toSmallCaps())
            }
        }
    }

    private fun ownShopsItem(context: RenderContext) = buildItem(Material.PLAYER_HEAD) {
        displayName {
            shopColored("Eigene Shops")
        }

        editMeta(SkullMeta::class.java) {
            it.owningPlayer = context.player
        }

        buildLore {
            emptyLine()
            line {
                appendBlob()
                spacer("Hier kannst du deine eigenen Shops ansehen und verwalten.".toSmallCaps())
            }
            line {
                appendBlob()
                spacer("Klicke, um alle deine Shops anzuzeigen.".toSmallCaps())
            }
        }
    }

    private fun sortItem(state: ShopSortingType) = buildItem(Material.COMPARATOR) {
        displayName {
            shopColored("Sortieren")
        }

        buildLore {
            emptyLine()
            line { shopColored("Sortierung".toSmallCaps(), TextDecoration.BOLD) }

            line {
                if (state == ShopSortingType.ITEM_NAME) {
                    appendSpace()
                    spacer("-")
                    appendSpace()
                    shopColored("Itemname")
                } else {
                    spacer("-")
                    appendSpace()
                    white("Itemname")
                }
            }

            line {
                if (state == ShopSortingType.PRICE_ASC) {
                    appendSpace()
                    spacer("-")
                    appendSpace()
                    shopColored("Preis aufsteigend")
                } else {
                    spacer("-")
                    appendSpace()
                    white("Preis aufsteigend")
                }
            }

            line {
                if (state == ShopSortingType.PRICE_DESC) {
                    appendSpace()
                    spacer("-")
                    appendSpace()
                    shopColored("Preis absteigend")
                } else {
                    spacer("-")
                    appendSpace()
                    white("Preis absteigend")
                }
            }

            line {
                if (state == ShopSortingType.TIME_ASC) {
                    appendSpace()
                    spacer("-")
                    appendSpace()
                    shopColored("Zeit aufsteigend")
                } else {
                    spacer("-")
                    appendSpace()
                    white("Zeit aufsteigend")
                }
            }

            line {
                if (state == ShopSortingType.TIME_DESC) {
                    appendSpace()
                    spacer("-")
                    appendSpace()
                    shopColored("Zeit absteigend")
                } else {
                    spacer("-")
                    appendSpace()
                    white("Zeit absteigend")
                }
            }

            line {
                if (state == ShopSortingType.MOST_STORED) {
                    appendSpace()
                    spacer("-")
                    appendSpace()
                    shopColored("Meiste gelagerte Items")
                } else {
                    spacer("-")
                    appendSpace()
                    white("Meiste gelagerte Items")
                }
            }

            line {
                if (state == ShopSortingType.MOST_DEALS) {
                    appendSpace()
                    spacer("-")
                    appendSpace()
                    shopColored("Meiste Verkäufe")
                } else {
                    spacer("-")
                    appendSpace()
                    white("Meiste Verkäufe")
                }
            }

            line {
                if (state == ShopSortingType.SELLER_NAME) {
                    appendSpace()
                    spacer("-")
                    appendSpace()
                    shopColored("Verkäufer")
                } else {
                    spacer("-")
                    appendSpace()
                    white("Verkäufer")
                }
            }
        }
    }

    private val updateItem = buildItem(Material.REPEATER) {
        displayName {
            shopColored("Aktualisieren")
        }
    }

    private val paginationState = buildLazyPaginationState { context ->
        getLoadedShopsSortedFiltered(
            plugin.getSorting(context.player.uniqueId),
            searchInputCache[context.player.uniqueId]
        ).toMutableList()
    }.elementFactory { context, builder, _, shop ->
        builder.withItem(createShopItem(shop, context.player.uniqueId)).onClick { context ->
            context.playGeneralClickSound()

            if (shop.seller == context.player.uniqueId) {
                if (context.isShiftLeftClick) {
                    context.openForPlayer(
                        DeleteShopView::class.java,
                        ImmutableMap.of(
                            "delete-shop",
                            shop
                        )
                    )
                } else {
                    context.openForPlayer(
                        EditShopView::class.java,
                        ImmutableMap.of(
                            "edit-shop",
                            shop
                        )
                    )
                }
            } else {
                context.openForPlayer(
                    BuyShopItemView::class.java,
                    ImmutableMap.of(
                        "buy-shop",
                        shop
                    )
                )
            }
        }
    }.layoutTarget('R').build()

    override fun onInit(config: ViewConfigBuilder) {
        config
            .titleBuilder {
                shopColored("Shops".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(6)
            .layout(
                "OOOOOOOOO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "UAOPCNO}S"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        selectedSort.set(plugin.getSorting(render.player.uniqueId), render)
        val pagination = paginationState.get(render)

        render
            .layoutSlot('S')
            .updateOnClick()
            .renderWith { sortItem(selectedSort.get(render)) }
            .onClick { context ->
                context.playGeneralClickSound()

                if (context.isRightClick) {
                    selectedSort.set(selectedSort.get(render).previous(), render)
                } else {
                    selectedSort.set(selectedSort.get(render).next(), render)
                }

                plugin.setSorting(context.player.uniqueId, selectedSort.get(render))

                render.openForPlayer(ShopListView::class.java) // Re-open to apply new sorting - this is currently necessary, inventory framework dev is working on a fix.
            }
        render.layoutSlot('U', updateItem).onClick { context ->
            context.openForPlayer(ShopListView::class.java)
            context.playGeneralClickSound()
        }
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('A', searchItem(render.player.uniqueId)).onClick { context ->
            context.playGeneralClickSound()

            if (context.isShiftClick) {
                searchInputCache.remove(context.player.uniqueId)
                context.openForPlayer(ShopListView::class.java)
                return@onClick
            }

            context.player.closeInventory()
            context.player.showDialog(
                searchShopItemDialog(
                    searchInputCache.getOrDefault(
                        context.player.uniqueId,
                        ""
                    )
                )
            )
        }
        render.layoutSlot('C', createItem).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(
                CreateShopView::class.java,
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
                if (pagination.canBack()) {
                    previousItem
                } else {
                    outlineItem
                }
            }
            .watch(paginationState)
            .onClick { context ->
                if (!pagination.canBack()) {
                    return@onClick
                }

                context.playNewPageSound()
                pagination.back()
            }

        render
            .layoutSlot('N')
            .renderWith {
                if (pagination.canAdvance()) {
                    nextItem
                } else {
                    outlineItem
                }
            }
            .watch(paginationState)
            .onClick { context ->
                if (!pagination.canAdvance()) {
                    return@onClick
                }

                context.playNewPageSound()
                pagination.advance()
            }
        render.layoutSlot('}', ownShopsItem(render)).onClick { click ->
            click.openForPlayer(OwnShopsListView::class.java)
            click.playGeneralClickSound()

            OwnShopState.setInOwn(click.player.uniqueId, true)
        }
    }
}

fun createShopItem(shop: Shop, viewer: UUID, shopChest: Boolean = false) = shop.item.clone().apply {
    amount = 1

    val oldLore = lore()?.toMutableList() ?: mutableListOf()
    val newEntries = mutableListOf<Component>()

    newEntries.add(Component.empty())
    newEntries.add(buildText {
        shopColored("Verkaufsinformation".toSmallCaps(), TextDecoration.BOLD)
    })

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

    newEntries.add(buildText {
        spacer("-")
        appendSpace()
        shopColored("Verkäufer: ")
        variableValue(shop.sellerName)
    })

    val dealCount = shop.dealCount
    newEntries.add(buildText {
        spacer("-")
        appendSpace()
        shopColored("Abgeschlossene Käufe: ")
        if (dealCount > 0) {
            variableValue(dealCount)
        } else {
            error("Noch keine abgeschlossen")
        }
    })

    val totalSold = shop.totalSoldItems
    newEntries.add(buildText {
        spacer("-")
        appendSpace()
        shopColored("Gesamt verkauft: ")
        if (totalSold > 0) {
            variableValue("$totalSold Items")
        } else {
            error("Noch keine verkauft")
        }
    })

    newEntries.add(buildText {
        spacer("-")
        appendSpace()
        shopColored("Erstellt am: ")
        variableValue(shop.createdAt.format(dateTimeFormatter))
    })

    newEntries.add(Component.empty())

    if (!shopChest) {
        if (shop.seller == viewer) {
            newEntries.add(buildText {
                appendBlob()
                spacer("Klicke, um den Shop zu bearbeiten.".toSmallCaps())
            })

            newEntries.add(buildText {
                appendBlob()
                spacer("Drücke ".toSmallCaps())
                white("SHIFT".toSmallCaps())
                spacer(" + ")
                displayKey("key.mouse.left")
                spacer(" um den Shop zu löschen.".toSmallCaps())
            })
        } else {
            newEntries.add(buildText {
                appendBlob()
                spacer("Klicke, um Items zu kaufen.".toSmallCaps())
            })
        }
        newEntries.add(Component.empty())
    } else {
        newEntries.add(buildText {
            appendBlob()
            spacer("Klicke um die Chest Shop auszuwählen.".toSmallCaps())
        })
    }

    newEntries.add(buildText {
        darkSpacer(shop.shopUuid.toString())
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

private fun getLoadedShopsSortedFiltered(
    sortType: ShopSortingType,
    search: String?
): List<Shop> {
    val base = ShopService.loadedShops.filter { it.storedItemCount > 0 }

    val filtered = if (search.isNullOrBlank()) {
        base
    } else {
        val terms = search
            .trim()
            .lowercase()
            .split(' ')
            .filter { it.isNotEmpty() }

        if (terms.isEmpty()) {
            base
        } else {
            base.filter { shop ->
                val tokens = shop.searchableTokens

                for (term in terms) {
                    if (term.startsWith("@")) {
                        val sellerSearch = term.removePrefix("@")
                        if (sellerSearch.isEmpty()) continue
                        val sellerName = shop.sellerName.lowercase()
                        if (!sellerName.contains(sellerSearch)) return@filter false
                    } else {
                        var matched = false

                        for (token in tokens) {
                            if (token.contains(term)) {
                                matched = true
                                break
                            }
                        }

                        if (!matched) return@filter false
                    }
                }

                true
            }
        }
    }

    return when (sortType) {
        ShopSortingType.PRICE_ASC -> filtered.sortedBy { it.pricePerItem }
        ShopSortingType.PRICE_DESC -> filtered.sortedByDescending { it.pricePerItem }
        ShopSortingType.TIME_ASC -> filtered.sortedBy { it.createdAt }
        ShopSortingType.TIME_DESC -> filtered.sortedByDescending { it.createdAt }
        ShopSortingType.MOST_STORED -> filtered.sortedByDescending { it.storedItemCount }
        ShopSortingType.MOST_DEALS -> {
            val dealCountMap = DealService.loadedDeals.groupingBy { it.shopInternalId }.eachCount()
            filtered.sortedByDescending { dealCountMap[it.internalId] ?: 0 }
        }

        ShopSortingType.ITEM_NAME -> filtered.sortedBy { it.item.type.name }
        ShopSortingType.SELLER_NAME -> filtered.sortedBy { it.sellerName.lowercase() }
    }
}


fun SurfComponentBuilder.shopColored(text: Any, vararg decoration: TextDecoration) =
    coloredComponent(text.toString(), TextColor.color(252, 233, 121), *decoration)