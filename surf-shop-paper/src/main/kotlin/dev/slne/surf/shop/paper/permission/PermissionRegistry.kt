package dev.slne.surf.shop.paper.permission

import dev.slne.surf.surfapi.bukkit.api.permission.PermissionRegistry

object PermissionRegistry : PermissionRegistry() {
    private const val PREFIX = "surf.shop"
    private const val PREFIX_COMMAND = "$PREFIX.command"

    val AUCTION_COMMAND = create("$PREFIX_COMMAND.auction")
}