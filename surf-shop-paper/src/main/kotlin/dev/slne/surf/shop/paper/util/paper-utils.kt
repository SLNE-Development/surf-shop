package dev.slne.surf.shop.paper.util

import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

val searchInputCache = mutableObject2ObjectMapOf<UUID, String>()

fun SurfComponentBuilder.displayKey(key: String) =
    append(
        MiniMessage.miniMessage().deserialize("<key:$key>")
    ).color(Colors.WHITE) // https://minecraft.fandom.com/wiki/Key_codes

fun SurfComponentBuilder.translatable(key: String) = append(Component.translatable(key))

fun formatPriceNice(price: Int): String = castCoinFormat.format(price)

val castCoinFormat = DecimalFormat("#,##0.## ¤", DecimalFormatSymbols(Locale.GERMANY).apply {
    decimalSeparator = ','
    groupingSeparator = '.'
    currencySymbol = "CC"
})

fun SurfComponentBuilder.appendBlob() = append {
    darkSpacer("▪")
    appendSpace()
}