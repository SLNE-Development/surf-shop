package dev.slne.surf.shop.paper.command

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.shop.core.paper.util.denyShopKey
import dev.slne.surf.shop.paper.permission.PermissionRegistry
import org.bukkit.persistence.PersistentDataType

fun denyShopCommand() = commandTree("denyshop") {
    withPermission(PermissionRegistry.SHOP_DENY_SHOP_COMMAND)
    playerExecutor { player, _ ->
        val item = player.inventory.itemInMainHand

        if (item.isEmpty) {
            player.sendText {
                appendErrorPrefix()
                error("Du musst das Item in der Hand halten, das du verbieten/erlauben möchtest.")
            }
            return@playerExecutor
        }

        val isDenied = item.persistentDataContainer.has(denyShopKey)

        if (isDenied) {
            item.editPersistentDataContainer {
                it.remove(denyShopKey)
            }
            player.sendText {
                appendSuccessPrefix()
                success("Das Item ist nun wieder erlaubt.")
            }
        } else {
            item.editPersistentDataContainer {
                it.set(denyShopKey, PersistentDataType.BOOLEAN, true)
            }
            player.sendText {
                appendSuccessPrefix()
                success("Das Item ist nun verboten.")
            }
        }
    }
}