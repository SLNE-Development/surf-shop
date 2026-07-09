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
import dev.slne.surf.api.paper.inventory.framework.titleBuilder
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.api.shop.ShopSortingType
import dev.slne.surf.shop.api.shopchest.StaticShopChest
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.paper.util.item
import dev.slne.surf.shop.core.paper.util.sellerName
import dev.slne.surf.shop.paper.dialog.searchShopItemDialog
import dev.slne.surf.shop.paper.menu.buy.BuyShopItemView
import dev.slne.surf.shop.paper.menu.deal.DoneDealsView
import dev.slne.surf.shop.paper.menu.delete.DeleteShopView
import dev.slne.surf.shop.paper.menu.edit.EditShopView
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.appendBlob
import dev.slne.surf.shop.paper.util.searchInputCache
import kotlinx.coroutines.future.future
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.Context
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.*

object OwnShopsListView : View() {
    private val selectedSort = mutableState(ShopSortingType.TIME_ASC)

    private val outlineItem = buildItem(Material.GRAY_STAINED_GLASS_PANE) {
        displayName {
            spacer("")
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

    private val backItem = MenuHeads.CROSS.clone().apply {
        displayName {
            error("Zurück")
        }
    }

    private val updateItem = buildItem(Material.REPEATER) {
        displayName {
            shopColored("Aktualisieren")
        }
    }

    private val paginationState = buildLazyAsyncPaginationState { context ->
        plugin.scope.future {
            getOwnLoadedShopsWithItems(
                context.player.uniqueId,
                plugin.getSorting(context.player.uniqueId),
                searchInputCache[context.player.uniqueId],
                context
            )
        }
    }.elementFactory { context, builder, _, shop ->
        builder.withItem(
            shop.second
        ).onClick { context ->
            context.playGeneralClickSound()

            val shop = shop.first

            if (!context.player.canUseFullShopView()) {
                if (shop.seller == context.player.uniqueId) {
                    context.openForPlayer(
                        EditShopView::class.java,
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
                shopColored("Meine Shops".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(6)
            .layout(
                "OOOOOOOOO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "WUOPCNOAS"
            )
            .cancelInteractions()
    }

    override fun onFirstRender(render: RenderContext) {
        selectedSort.set(plugin.getSorting(render.player.uniqueId), render)
        val pagination = paginationState.get(render)

        render.availableSlot(loadingItem)
            .displayIf(pagination::isLoading)
            .updateOnStateChange(paginationState)

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

                render.openForPlayer(OwnShopsListView::class.java) // Re-open to apply new sorting - this is currently necessary, inventory framework dev is working on a fix.
            }
        render.layoutSlot('U', updateItem).onClick { context ->
            context.openForPlayer(OwnShopsListView::class.java)
            context.playGeneralClickSound()
        }
        render.layoutSlot('W', doneDealsItem).onClick { click ->
            click.playGeneralClickSound()
            click.openForPlayer(DoneDealsView::class.java)
        }
        render.layoutSlot('O', outlineItem)
        render.layoutSlot('A', searchItem(render.player.uniqueId)).onClick { context ->
            context.playGeneralClickSound()

            if (context.isShiftClick) {
                searchInputCache.remove(context.player.uniqueId)
                context.openForPlayer(OwnShopsListView::class.java)
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
        render.layoutSlot('C', backItem).onClick { context ->
            context.playGeneralClickSound()
            context.openForPlayer(ShopListView::class.java)
            OwnShopState.setInOwn(context.player.uniqueId, false)
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
