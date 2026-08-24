package dev.slne.surf.shop.paper.menu

import com.github.shynixn.mccoroutine.folia.scope
import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.playSound
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.core.util.dateTimeFormatter
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.layoutTarget
import dev.slne.surf.api.paper.inventory.framework.view.onFirstRender
import dev.slne.surf.api.paper.inventory.framework.view.paginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.AbstractPaginatedSurfView
import dev.slne.surf.api.paper.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.paper.inventory.framework.view.settings
import dev.slne.surf.api.paper.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.mutableState
import dev.slne.surf.api.paper.inventory.framework.view.state.set
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.api.shop.ShopSortingType
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.common.service.ShopDealStats
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.core.paper.util.sellerName
import dev.slne.surf.shop.paper.dialog.searchShopItemDialog
import dev.slne.surf.shop.paper.menu.buy.buyShopItemView
import dev.slne.surf.shop.paper.menu.delete.deleteShopView
import dev.slne.surf.shop.paper.menu.edit.editShopView
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.appendBlob
import dev.slne.surf.shop.paper.util.displayKey
import dev.slne.surf.shop.paper.util.formatPriceNice
import dev.slne.surf.shop.paper.util.searchInputCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.withContext
import me.devnatan.inventoryframework.context.Context
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

val shopListView: AbstractPaginatedSurfView = paginatedSurfView("Shops") {
    val selectedSort = mutableState(ShopSortingType.TIME_ASC)

    settings {
        paginationViewRows(PaginationViewRows.FOUR)
    }

    layoutTarget('I')

    pagination {
        lazyAsyncSource { context ->
            plugin.scope.future {
                getLoadedShopsSortedFiltered(
                    plugin.getSorting(context.player.uniqueId),
                    searchInputCache[context.player.uniqueId],
                    context
                ).toMutableList()
            }
        }

        itemFactory { shop ->
            withItem(
                shop.second
            ).onClick { context ->
                context.playGeneralClickSound()
                val shop = shop.first

                if (!context.player.canUseFullShopView()) {
                    if (shop.seller == context.player.uniqueId) {
                        context.openForPlayer(
                            editShopView::class.java,
                            ImmutableMap.of(
                                "edit-shop",
                                shop
                            )
                        )
                    } else {
                        context.player.sendText {
                            appendErrorPrefix()
                            error("Du kannst unterwegs nichts kaufen! Bitte begib dich zum Spawn.")
                        }
                    }
                    return@onClick
                }

                if (shop.seller == context.player.uniqueId) {
                    if (context.isShiftLeftClick) {
                        context.openForPlayer(
                            deleteShopView::class.java,
                            ImmutableMap.of(
                                "delete-shop",
                                shop
                            )
                        )
                    } else {
                        context.openForPlayer(
                            editShopView::class.java,
                            ImmutableMap.of(
                                "edit-shop",
                                shop
                            )
                        )
                    }
                } else {
                    context.openForPlayer(
                        buyShopItemView::class.java,
                        ImmutableMap.of(
                            "buy-shop",
                            shop
                        )
                    )
                }
            }
        }
    }

    onFirstRender {
        selectedSort[this] = plugin.getSorting(this.player.uniqueId)

        slot(5, 1)
            .updateOnClick()
            .renderWith { sortItem(selectedSort[this@onFirstRender]) }
            .onClick { context ->
                context.playGeneralClickSound()

                if (context.isRightClick) {
                    selectedSort[this@onFirstRender] = selectedSort[this@onFirstRender].previous()
                } else {
                    selectedSort[this] = selectedSort[this].next()
                }

                plugin.setSorting(context.player.uniqueId, selectedSort[this])
                this@onFirstRender.openForPlayer(shopListView::class.java)
            }
        slot(5, 2, searchItem(this.player.uniqueId)).onClick { context ->
            context.playGeneralClickSound()

            if (context.isShiftClick) {
                searchInputCache.remove(context.player.uniqueId)
                context.openForPlayer(shopListView::class.java)
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
        if (this.player.canUseFullShopView()) {
            slot(5, 9, createItem).onClick { context ->
                context.playGeneralClickSound()
                context.openForPlayer(
                    createShopView::class.java,
                    ImmutableMap.of(
                        "create-item",
                        ItemStack.empty(),
                        "create-price",
                        0.0
                    )
                )
            }
        } else {
            slot(5, 9, viewOnlyItem).onClick { context ->
                context.playGeneralClickSound()
                context.player.sendText {
                    appendInfoPrefix()
                    info("Zum Kaufen, Erstellen und Lager bearbeiten musst du zum Spawn.")
                }
            }
        }
        slot(5, 8, ownShopsItem(this)).onClick { click ->
            click.openForPlayer(OwnShopsListView::class.java)
            click.playGeneralClickSound()

            OwnShopState.setInOwn(click.player.uniqueId, true)
        }
    }
}

private val createItem = ViewIcon(ViewIconType.PLUS, ViewIconColor.GREEN).build {
    displayName {
        text("Shop erstellen", TextColor.fromHexString("#91CE22"), TextDecoration.BOLD)
    }
}

private val viewOnlyItem = buildItem(Material.SPYGLASS) {
    displayName {
        shopColored("Ansichtsmodus")
    }

    buildLore {
        emptyLine()
        line {
            appendBlob()
            spacer("Du kannst Shops hier ansehen und durchsuchen.".toSmallCaps())
        }
        line {
            appendBlob()
            spacer("Zum Kaufen, Erstellen und Lager bearbeiten musst du zum Spawn.".toSmallCaps())
        }
        line {
            appendBlob()
            spacer("Eigene Shops kannst du hier im Preis anpassen.".toSmallCaps())
        }
    }
}

private fun searchItem(playerUuid: UUID) =
    ViewIcon(ViewIconType.SEARCH, ViewIconColor.YELLOW).build {
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
            if (context.player.canUseFullShopView()) {
                spacer("Hier kannst du deine eigenen Shops ansehen und verwalten.".toSmallCaps())
            } else {
                spacer("Hier kannst du eigene Shops ansehen und Preise anpassen.".toSmallCaps())
            }
        }
        line {
            appendBlob()
            spacer("Klicke, um alle deine Shops anzuzeigen.".toSmallCaps())
        }
    }
}

private fun sortItem(state: ShopSortingType) =
    ViewIcon(ViewIconType.COG, ViewIconColor.YELLOW).build {
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

fun createShopItem(
    shop: Shop,
    viewer: UUID,
    shopChest: Boolean = false,
    viewOnly: Boolean = false,
    stats: ShopDealStats = ShopDealStats()
) = shop.item.clone().apply {
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

    newEntries.add(buildText {
        spacer("-")
        appendSpace()
        shopColored("Abgeschlossene Käufe: ")
        if (stats.dealCount > 0) {
            variableValue(stats.dealCount)
        } else {
            error("Noch keine abgeschlossen")
        }
    })

    newEntries.add(buildText {
        spacer("-")
        appendSpace()
        shopColored("Gesamt verkauft: ")
        if (stats.totalSoldItems > 0) {
            variableValue("${stats.totalSoldItems} Items")
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
        if (viewOnly) {
            if (shop.seller == viewer) {
                newEntries.add(buildText {
                    appendBlob()
                    spacer("Klicke, um den Preis zu bearbeiten.".toSmallCaps())
                })

                newEntries.add(buildText {
                    appendBlob()
                    spacer("Zum Bearbeiten des Lagers oder Löschen des Shops musst du zum Spawn.".toSmallCaps())
                })
            } else {
                newEntries.add(buildText {
                    appendBlob()
                    spacer("Dieser Shop ist hier nur zur Ansicht.".toSmallCaps())
                })

                newEntries.add(buildText {
                    appendBlob()
                    spacer("Zum Kaufen musst du zum Spawn.".toSmallCaps())
                })
            }
        } else {
            if (shop.seller == viewer) {
                newEntries.add(buildText {
                    appendBlob()
                    spacer("Klicke, um den Shop zu bearbeiten.".toSmallCaps())
                })

                newEntries.add(buildText {
                    appendBlob()
                    error("Drücke ".toSmallCaps())
                    white("Shift")
                    spacer(" + ")
                    displayKey("key.mouse.left")
                    error(" um den Shop zu löschen.".toSmallCaps())
                })
            } else {
                newEntries.add(buildText {
                    appendBlob()
                    spacer("Klicke, um Items zu kaufen.".toSmallCaps())
                })
            }
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

private suspend fun getLoadedShopsSortedFiltered(
    sortType: ShopSortingType,
    search: String?,
    context: Context
): MutableList<Pair<Shop, ItemStack>> = withContext(Dispatchers.Default) {
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
                        if (!shop.sellerName.lowercase().contains(sellerSearch)) return@filter false
                    } else {
                        if (tokens.none { it.contains(term) }) return@filter false
                    }
                }

                true
            }
        }
    }

    val withStats = filtered.map { shop ->
        shop to DealService.getDealStats(shop.internalId)
    }

    val sorted = when (sortType) {
        ShopSortingType.PRICE_ASC -> withStats.sortedBy { it.first.pricePerItem }
        ShopSortingType.PRICE_DESC -> withStats.sortedByDescending { it.first.pricePerItem }
        ShopSortingType.TIME_ASC -> withStats.sortedBy { it.first.createdAt }
        ShopSortingType.TIME_DESC -> withStats.sortedByDescending { it.first.createdAt }
        ShopSortingType.MOST_STORED -> withStats.sortedByDescending { it.first.storedItemCount }
        ShopSortingType.MOST_DEALS -> withStats.sortedByDescending { it.second.dealCount }
        ShopSortingType.ITEM_NAME -> withStats.sortedBy { it.first.item.type.name }
        ShopSortingType.SELLER_NAME -> withStats.sortedBy { it.first.sellerName.lowercase() }
    }

    val playerId = context.player.uniqueId
    val viewOnly = !context.player.canUseFullShopView()

    return@withContext sorted
        .map { (shop, stats) ->
            shop to createShopItem(shop, playerId, viewOnly = viewOnly, stats = stats)
        }
        .toMutableList()
}


fun SurfComponentBuilder.shopColored(text: Any, vararg decoration: TextDecoration) =
    coloredComponent(text.toString(), TextColor.color(252, 233, 121), *decoration)
