package dev.slne.surf.shop.paper.hook

import de.oliver.fancyholograms.api.FancyHologramsPlugin
import de.oliver.fancyholograms.api.data.TextHologramData
import dev.slne.surf.api.core.minimessage.miniMessage
import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.paper.util.item
import org.bukkit.Location
import org.bukkit.entity.Display
import java.util.*
import kotlin.jvm.optionals.getOrNull

object FancyHologramsHook {
    fun createAndOrDelete(
        chestUuid: UUID,
        shopLocation: Location,
        linkedShop: Shop
    ) {
        val manager = FancyHologramsPlugin.get().hologramManager

        manager.getHologram("shop-$chestUuid").getOrNull()?.let {
            manager.removeHologram(it)
        }

        val holoData =
            TextHologramData("shop-$chestUuid", shopLocation.clone().add(0.0, 1.5, 0.0))
        holoData.billboard = Display.Billboard.VERTICAL
        holoData.removeLine(0)
        holoData.addLine("<#6B9BD1>1x <reset>${miniMessage.serialize(linkedShop.item.displayName())}")

        manager.addHologram(manager.create(holoData))
    }

    fun deleteHologramIfExists(chestUuid: UUID) {
        val manager = FancyHologramsPlugin.get().hologramManager

        manager.getHologram("shop-$chestUuid").getOrNull()?.let {
            manager.removeHologram(it)
        }
    }
}