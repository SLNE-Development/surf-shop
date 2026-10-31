package dev.slne.surf.shop.paper.util

import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.api.core.util.mutableObject2ObjectMapOf
import dev.slne.surf.shop.api.shopchest.StaticShopChest
import dev.slne.surf.shop.core.common.util.ShopPriceRules
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

val searchInputCache = mutableObject2ObjectMapOf<UUID, String>()

fun SurfComponentBuilder.displayKey(key: String) =
    append(
        MiniMessage.miniMessage().deserialize("<key:$key>")
    ).color(Colors.WHITE) // https://minecraft.fandom.com/wiki/Key_codes

fun SurfComponentBuilder.translatable(key: String) = append(Component.translatable(key))

fun formatPriceNice(price: Double): String = castCoinFormat.format(price)

fun formatPriceNice(price: BigDecimal): String = castCoinFormat.format(price)

fun Player.sendInvalidPriceMessage() = sendText {
    appendErrorPrefix()
    error("Der angegebene Preis kann nicht angewendet werden. Er muss zwischen")
    appendSpace()
    variableValue(formatPriceNice(ShopPriceRules.MIN_PRICE))
    appendSpace()
    error("und")
    appendSpace()
    variableValue(formatPriceNice(ShopPriceRules.MAX_PRICE))
    appendSpace()
    error("liegen und darf höchstens 2 Nachkommastellen haben.")
}

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