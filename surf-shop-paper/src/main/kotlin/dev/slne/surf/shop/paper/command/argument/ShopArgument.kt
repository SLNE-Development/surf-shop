package dev.slne.surf.shop.paper.command.argument

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.common.service.ShopService
import java.util.*

class ShopArgument(nodeName: String) :
    CustomArgument<Shop, String>(StringArgument(nodeName), { info ->
        runCatching { UUID.fromString(info.input) }
            .getOrNull()
            ?.let(ShopService::getShop)
            ?: throw CustomArgumentException.fromAdventureComponent(
                buildText {
                    appendErrorPrefix()
                    error("Der Shop wurde nicht gefunden.")
                })
    }) {
    init {
        this.replaceSuggestions(
            ArgumentSuggestions.stringCollection {
                ShopService.loadedShops.map { it.shopUuid.toString() }
            }
        )
    }
}

inline fun CommandTree.shopArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(
    ShopArgument(nodeName).setOptional(optional).apply(block)
)

inline fun Argument<*>.shopArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(
    ShopArgument(nodeName).setOptional(optional).apply(block)
)

inline fun CommandAPICommand.shopArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand =
    withArguments(ShopArgument(nodeName).setOptional(optional).apply(block))
