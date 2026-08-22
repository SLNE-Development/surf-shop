package dev.slne.surf.shop.paper.dialog

import dev.slne.surf.api.paper.dialog.search.searchDialog
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.shop.paper.menu.OwnShopState
import dev.slne.surf.shop.paper.menu.OwnShopsListView
import dev.slne.surf.shop.paper.menu.shopColored
import dev.slne.surf.shop.paper.menu.shopListView
import dev.slne.surf.shop.paper.util.searchInputCache

@Suppress("UnstableApiUsage")
fun searchShopItemDialog(initial: String) = searchDialog(
    title = {
        shopColored("Suche ein Item...")
    },
    searchInput = {
        initialValue = initial
    },
    body = {
        plainMessage {
            shopColored("Gib den Namen eines Items ein, um nach Shops zu suchen. Suche nach Verzauberungsnamen (z.b. \"Mending\" oder \"Soulbound\"), Potion-Effekten (z.b. \"Fire Resistence\") oder nutze @Spielername um nach einem Verkäufer zu suchen.")
        }
    },
    onSearch = { player, query ->
        searchInputCache[player.uniqueId] = query

        if (OwnShopState.isInOwn(player.uniqueId)) {
            viewFrame.open(OwnShopsListView::class.java, player)
        } else {
            viewFrame.open(shopListView::class.java, player)
        }
    },
    onClose = { player, query ->
        searchInputCache[player.uniqueId] = query
        if (OwnShopState.isInOwn(player.uniqueId)) {
            viewFrame.open(OwnShopsListView::class.java, player)
        } else {
            viewFrame.open(shopListView::class.java, player)
        }
    }
)