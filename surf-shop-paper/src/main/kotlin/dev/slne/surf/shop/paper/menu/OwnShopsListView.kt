package dev.slne.surf.shop.paper.menu

import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.api.shop.ShopSortingType
import dev.slne.surf.shop.core.common.util.dealCount
import dev.slne.surf.shop.core.service.shopService
import dev.slne.surf.shop.paper.dialog.searchShopItemDialog
import dev.slne.surf.shop.paper.menu.buy.BuyShopItemView
import dev.slne.surf.shop.paper.menu.delete.DeleteShopView
import dev.slne.surf.shop.paper.menu.edit.EditShopView
import dev.slne.surf.shop.paper.plugin
import dev.slne.surf.shop.paper.util.MenuHeads
import dev.slne.surf.shop.paper.util.appendBlob
import dev.slne.surf.shop.paper.util.searchInputCache
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import dev.slne.surf.surfapi.bukkit.api.builder.buildLore
import dev.slne.surf.surfapi.bukkit.api.builder.displayName
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.titleBuilder
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import me.devnatan.inventoryframework.View
import me.devnatan.inventoryframework.ViewConfigBuilder
import me.devnatan.inventoryframework.context.RenderContext
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import java.util.*

@Suppress("UnstableApiUsage")
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

    private val paginationState = buildLazyPaginationState { context ->
        getLoadedShopsSortedFiltered(
            context.player.uniqueId,
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
                shopColored("Meine Shops".toSmallCaps(), TextDecoration.BOLD)
            }
            .size(6)
            .layout(
                "OOOOOOOOO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "ORRRRRRRO",
                "UAOPCNOOS"
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

                render.openForPlayer(OwnShopsListView::class.java) // Re-open to apply new sorting - this is currently necessary, inventory framework dev is working on a fix.
            }
        render.layoutSlot('U', updateItem).onClick { context ->
            context.openForPlayer(OwnShopsListView::class.java)
            context.playGeneralClickSound()
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

private fun getLoadedShopsSortedFiltered(
    seller: UUID,
    sortType: ShopSortingType,
    search: String?
): List<Shop> {
    val base = shopService.loadedShops.filter { it.seller == seller }

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
                    var matched = false

                    for (token in tokens) {
                        if (token.contains(term)) {
                            matched = true
                            break
                        }
                    }

                    if (!matched) return@filter false
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
        ShopSortingType.MOST_DEALS -> filtered.sortedByDescending { it.dealCount }
        ShopSortingType.ITEM_NAME -> filtered.sortedBy { it.item.type.name }
    }
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