package dev.slne.surf.shop.paper.menu

import com.github.shynixn.mccoroutine.folia.scope
import com.google.common.collect.ImmutableMap
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.util.mutableObject2ObjectMapOf
import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.api.paper.inventory.framework.view.*
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIcon
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconColor
import dev.slne.surf.api.paper.inventory.framework.view.icon.ViewIconType
import dev.slne.surf.api.paper.inventory.framework.view.pagination.pagination
import dev.slne.surf.api.paper.inventory.framework.view.settings.PaginationViewRows
import dev.slne.surf.api.paper.inventory.framework.view.state.get
import dev.slne.surf.api.paper.inventory.framework.view.state.mutableState
import dev.slne.surf.api.paper.inventory.framework.view.state.set
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.api.shop.ShopSortingType
import dev.slne.surf.shop.api.shopchest.StaticShopChest
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.core.paper.util.sellerName
import dev.slne.surf.shop.paper.dialog.searchShopItemDialog
import dev.slne.surf.shop.paper.menu.buy.buyShopItemView
import dev.slne.surf.shop.paper.menu.deal.doneDealsView
import dev.slne.surf.shop.paper.menu.delete.deleteShopView
import dev.slne.surf.shop.paper.menu.edit.editShopView
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.appendBlob
import dev.slne.surf.shop.paper.util.searchInputCache
import kotlinx.coroutines.future.future
import me.devnatan.inventoryframework.context.Context
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

val ownShopsListView: AbstractSurfView = paginatedSurfView("Deine Shops") {
    val selectedSort = mutableState(ShopSortingType.TIME_ASC)

    settings {
        navigateBackOnOutsideClick(false)
        paginationViewRows(PaginationViewRows.FOUR)
    }

    pagination {
        lazyAsyncSource { context ->
            plugin.scope.future {
                getOwnLoadedShopsWithItems(
                    context.player.uniqueId,
                    plugin.getSorting(context.player.uniqueId),
                    searchInputCache[context.player.uniqueId],
                    context
                )
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

    layoutTarget('R')

    onInit {
        layout(
            "ORRRRRRRO",
            "ORRRRRRRO",
            "ORRRRRRRO",
            "ORRRRRRRO",
            "WU     AS"
        )
    }

    onFirstRender {
        selectedSort[this] = plugin.getSorting(this.player.uniqueId)

        slot(5, 9)
            .updateOnClick()
            .renderWith { sortItem(selectedSort[this]) }
            .onClick { context ->
                context.playGeneralClickSound()

                if (context.isRightClick) {
                    selectedSort[this] = selectedSort[this].previous()
                } else {
                    selectedSort[this] = selectedSort[this].next()
                }

                plugin.setSorting(context.player.uniqueId, selectedSort[this])
                openForPlayer(ownShopsListView::class.java)
            }
        slot(5, 1, backItem).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(shopListView::class.java)
            OwnShopState.setInOwn(context.player.uniqueId, false)
        }
        slot(5, 2, doneDealsItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(doneDealsView::class.java)
        }
        slot(5, 8, searchItem(this.player.uniqueId)).onClick { context ->
            context.playGeneralClickSound()

            if (context.isShiftClick) {
                searchInputCache.remove(context.player.uniqueId)
                context.openForPlayer(ownShopsListView::class.java)
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

private val backItem = ViewIcon(ViewIconType.RELOAD, ViewIconColor.RED).build {
    displayName {
        error("Zurück")
    }
}

private val doneDealsItem = buildItem(Material.CHEST) {
    displayName {
        shopColored("Abgeschlossene Deals")
    }
}

private suspend fun getOwnLoadedShopsWithItems(
    seller: UUID,
    sortType: ShopSortingType,
    search: String?,
    context: Context
): List<Pair<Shop, ItemStack>> {
    val base = ShopService.loadedShops.filter { it.seller == seller }

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

    return sorted
        .map { (shop, stats) ->
            shop to createShopItem(shop, playerId, viewOnly = viewOnly, stats = stats)
        }
        .toMutableList()
}

object OwnShopState {
    private val inOwn = mutableObjectSetOf<UUID>()
    fun isInOwn(playerUuid: UUID) = inOwn.contains(playerUuid)

    fun setInOwn(playerUuid: UUID, inOwn: Boolean) {
        if (inOwn) {
            this.inOwn.add(playerUuid)
        } else {
            this.inOwn.remove(playerUuid)
        }
    }
}

object StaticShopState {
    private val inStaticShop = mutableObjectSetOf<UUID>()
    fun isInStaticShop(playerUuid: UUID) = inStaticShop.contains(playerUuid)

    fun setInStaticShop(playerUuid: UUID, inStaticShop: Boolean) {
        if (inStaticShop) {
            this.inStaticShop.add(playerUuid)
        } else {
            this.inStaticShop.remove(playerUuid)
        }
    }
}

object NpcShopState {
    private val inNpcShop = mutableObjectSetOf<UUID>()
    fun isInNpcShop(playerUuid: UUID) = inNpcShop.contains(playerUuid)

    fun setInNpcShop(playerUuid: UUID, inNpcShop: Boolean) {
        if (inNpcShop) {
            this.inNpcShop.add(playerUuid)
        } else {
            this.inNpcShop.remove(playerUuid)
        }
    }
}

object ChestShopEditState {
    private val chestMap = mutableObject2ObjectMapOf<UUID, StaticShopChest>()

    fun getChest(playerUuid: UUID): StaticShopChest? = chestMap[playerUuid]

    fun setChest(playerUuid: UUID, chest: StaticShopChest?) {
        if (chest != null) {
            chestMap[playerUuid] = chest
        } else {
            chestMap.remove(playerUuid)
        }
    }
}
