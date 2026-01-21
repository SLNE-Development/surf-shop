package dev.slne.surf.shop.auction.paper.server.commands

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.shop.auction.api.auction.AuctionManager
import dev.slne.surf.shop.auction.paper.api.auction.createAuction
import dev.slne.surf.shop.auction.paper.server.gui.AuctionGui
import dev.slne.surf.shop.auction.paper.server.plugin
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import org.bukkit.Material
import java.time.OffsetDateTime

fun auctionCommand() = commandAPICommand("auction") {
    createCommand()

    playerExecutor { player, _ ->
        AuctionGui.mainGui(player).open()
    }
}

private fun CommandAPICommand.createCommand() = subcommand("create") {
    integerArgument("startingBid", min = 1)
    booleanArgument("instantBuyEnabled")
    integerArgument("instantBuyPrice", min = 1)
    integerArgument("amount", min = 1, max = 1000)

    playerExecutor { player, args ->
        val startingBid: Int by args
        val instantBuyEnabled: Boolean by args
        val instantBuyPrice: Int by args
        val amount: Int by args

        val holdingItem = player.inventory.itemInMainHand

        if (holdingItem.type == Material.AIR) {
            player.sendText {
                error("You must be holding an item to create an auction!")
            }

            return@playerExecutor
        }

        plugin.launch {
            repeat(amount) {
                val auction = AuctionManager.createAuction(
                    ownerUuid = player.uniqueId,
                    item = holdingItem,
                    startingBid = startingBid,
                    instantBuyEnabled = instantBuyEnabled,
                    instantBuyPrice = instantBuyPrice,
                    startsAt = OffsetDateTime.now(),
                    endsAt = OffsetDateTime.now().plusDays(7)
                )

                player.sendText {
                    success("Auction created successfully! ")
                    variableValue(auction.uuid.toString())
                }
            }
        }
    }
}