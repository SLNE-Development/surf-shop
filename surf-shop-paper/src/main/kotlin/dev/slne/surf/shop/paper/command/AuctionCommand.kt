package dev.slne.surf.shop.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.shop.paper.menu.AuctionListView
import dev.slne.surf.shop.paper.permission.PermissionRegistry
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame

fun auctionCommand() = commandTree("auction") {
    withPermission(PermissionRegistry.AUCTION_COMMAND)

    playerExecutor { player, _ ->
        viewFrame.open(AuctionListView::class.java, player)
    }
}