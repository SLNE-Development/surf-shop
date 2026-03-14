package dev.slne.surf.shop.paper.command.argument

import com.sk89q.worldguard.WorldGuard
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.service.shopService
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText

class ShopArgument(nodeName: String) :
    CustomArgument<Shop, String>(StringArgument(nodeName), { info ->
        WorldGuard.getInstance().platform.regionContainer.loaded.flatMap { it.regions.values }
            .filter { it.getFlag(ProtectionFlagsRegistry.SURF_PROTECTION) != null }
            .firstOrNull { it.id == info.input }
            ?: throw CustomArgumentException.fromAdventureComponent(
                buildText {
                    appendErrorPrefix()
                    error("Das Grundstück wurde nicht gefunden.")
                })
    }) {
    init {
        this.replaceSuggestions(
            ArgumentSuggestions.stringCollection {
                shopService.loadedShops.map { it.shopUuid }
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