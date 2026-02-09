package dev.slne.surf.shop.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.slne.surf.shop.paper.permission.PermissionRegistry

fun auctionCommand() = commandTree("auction") {
    withPermission(PermissionRegistry.AUCTION_COMMAND)
}