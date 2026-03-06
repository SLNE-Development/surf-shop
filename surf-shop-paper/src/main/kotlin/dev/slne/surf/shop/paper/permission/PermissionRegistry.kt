package dev.slne.surf.shop.paper.permission

import dev.slne.surf.surfapi.bukkit.api.permission.PermissionRegistry

object PermissionRegistry : PermissionRegistry() {
    private const val PREFIX = "surf.shop"
    private const val PREFIX_COMMAND = "$PREFIX.command"

    val SHOP_COMMAND = create("$PREFIX_COMMAND.shop")
    val SHOP_COMMAND_ADMIN = create("$PREFIX_COMMAND.shop.admin")
}