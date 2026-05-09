package dev.slne.surf.shop.paper.permission

import dev.slne.surf.api.paper.permission.PermissionRegistry

object PermissionRegistry : PermissionRegistry() {
    private const val PREFIX = "surf.shop"
    private const val PREFIX_COMMAND = "$PREFIX.command"

    val SHOP_COMMAND = create("$PREFIX_COMMAND.shop")
    val SHOP_COMMAND_VIEW_ONLY_BYPASS = create("$PREFIX_COMMAND.shop.view-only.bypass")
    val SHOP_COMMAND_ADMIN = create("$PREFIX_COMMAND.shop.admin")
    val CHEST_SHOP_BREAK_BYPASS = create("$PREFIX.chests.bypass")
}
