package dev.slne.surf.shop.paper.dialog

import dev.slne.surf.shop.paper.menu.AuctionListView
import dev.slne.surf.shop.paper.menu.auctionColored
import dev.slne.surf.shop.paper.util.searchInputCache
import dev.slne.surf.surfapi.bukkit.api.dialog.search.searchDialog
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame

@Suppress("UnstableApiUsage")
fun searchAuctionItemDialog() = searchDialog(
    title = {
        auctionColored("Suche ein Item...")
    },
    searchInput = {

    },
    body = {
        plainMessage {
            auctionColored("Gib den Namen eines Items ein, um nach Auktionen zu suchen. Suche nach Verzauberungsnamen, z.b. \"Mending\" oder \"Soulbound\"")
        }
    },
    onSearch = { player, query ->
        searchInputCache[player.uniqueId] = query
        viewFrame.open(AuctionListView::class.java, player)
    },
    onClose = { player, query ->
        searchInputCache[player.uniqueId] = query
        viewFrame.open(AuctionListView::class.java, player)
    }
)