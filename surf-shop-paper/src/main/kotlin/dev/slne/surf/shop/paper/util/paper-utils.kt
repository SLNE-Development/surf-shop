package dev.slne.surf.shop.paper.util

import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.core.util.mutableObject2ObjectMapOf
import dev.slne.surf.shop.api.shopchest.StaticShopChest
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
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

val StaticShopChest.location
    get() = Bukkit.getWorld(worldName)?.let {
        Location(it, x.toDouble() + 0.5, y.toDouble(), z.toDouble() + 0.5)
    }