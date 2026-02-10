package dev.slne.surf.shop.paper.util

import com.destroystokyo.paper.profile.ProfileProperty
import dev.slne.surf.surfapi.bukkit.api.builder.buildItem
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ResolvableProfile
import io.papermc.paper.datacomponent.item.TooltipDisplay
import org.bukkit.Material

@Suppress("UnstableApiUsage")
object MenuHeads {
    val CREATE_BUTTON =
        createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjliODYxYWFiYjMxNmM0ZWQ3M2I0ZTU0MjgzMDU3ODJlNzM1NTY1YmEyYTA1MzkxMmUxZWZkODM0ZmE1YTZmIn19fQ==")
    val CHECK =
        createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWE3NWM4ZTUxYzNkMTA1YmFiNGM3ZGUzM2E3NzA5MzczNjRiNWEwMWMxNWI3ZGI4MmNjM2UxZmU2ZWI5MzM5NiJ9fX0=")
    val ARROW_LEFT =
        createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTJmMDQyNWQ2NGZkYzg5OTI5MjhkNjA4MTA5ODEwYzEyNTFmZTI0M2Q2MGQxNzViZWQ0MjdjNjUxY2JlIn19fQ==")
    val ARROW_RIGHT =
        createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQ4NjVhYWUyNzQ2YTliOGU5YTRmZTYyOWZiMDhkMThkMGE5MjUxZTVjY2JlNWZhNzA1MWY1M2VhYjliOTQifX19")

    private fun createSkull(textures: String) = buildItem(Material.PLAYER_HEAD) {
        setData(
            DataComponentTypes.PROFILE, ResolvableProfile.resolvableProfile().addProperty(
                ProfileProperty("textures", textures)
            ).build()
        )
        setData(
            DataComponentTypes.TOOLTIP_DISPLAY,
            TooltipDisplay.tooltipDisplay().addHiddenComponents(DataComponentTypes.PROFILE).build()
        )
    }
}