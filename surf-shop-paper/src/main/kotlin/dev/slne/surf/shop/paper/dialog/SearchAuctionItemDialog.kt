package dev.slne.surf.shop.paper.dialog

import com.google.common.collect.ImmutableMap
import dev.slne.surf.shop.paper.menu.AuctionListView
import dev.slne.surf.shop.paper.menu.auctionColored
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
            auctionColored("Gib den Namen eines Items ein, um nach Auktionen zu suchen. Suche Nach Enchantment Namen, z.b. \"Mending\" oder \"Soulbound\"")
        }
    },
    onSearch = { _, _ ->

    },
    onClose = { player, query ->
        viewFrame.open(AuctionListView::class.java, player, ImmutableMap.of("search", query))
    }
)