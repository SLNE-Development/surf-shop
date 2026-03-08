package dev.slne.surf.shop.paper.util

import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.*

val searchInputCache = mutableObject2ObjectMapOf<UUID, String>()

fun SurfComponentBuilder.displayKey(key: String) =
    append(
        MiniMessage.miniMessage().deserialize("<key:$key>")
    ).color(Colors.WHITE) // https://minecraft.fandom.com/wiki/Key_codes

fun SurfComponentBuilder.translatable(key: String) = append(Component.translatable(key))

fun formatPriceNice(price: Int): String {
    val priceString = price.toString()
    val stringBuilder = StringBuilder()

    for (i in priceString.indices) {
        if (i > 0 && (priceString.length - i) % 3 == 0) {
            stringBuilder.append('.')
        }
        stringBuilder.append(priceString[i])
    }

    return "${stringBuilder}CC"
}