package dev.slne.surf.shop.paper.dialog

import dev.slne.surf.shop.paper.menu.ShopListView
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.util.searchInputCache
import dev.slne.surf.surfapi.bukkit.api.dialog.search.searchDialog
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame

@Suppress("UnstableApiUsage")
fun searchShopItemDialog() = searchDialog(
    title = {
        shopColored("Suche ein Item...")
    },
    searchInput = {

    },
    body = {
        plainMessage {
            shopColored("Gib den Namen eines Items ein, um nach Shops zu suchen. Suche nach Verzauberungsnamen, z.b. \"Mending\" oder \"Soulbound\"")
        }
    },
    onSearch = { player, query ->
        searchInputCache[player.uniqueId] = query
        viewFrame.open(ShopListView::class.java, player)
    },
    onClose = { player, query ->
        searchInputCache[player.uniqueId] = query
        viewFrame.open(ShopListView::class.java, player)
    }
)