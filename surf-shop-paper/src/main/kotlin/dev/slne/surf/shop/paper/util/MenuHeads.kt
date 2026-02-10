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
    val STATE_EDITING =
        createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTdiNWQ3NTI0ODAzMmY0ZWQ4MDQ0NzMwMjFkZTI5ZWQwZGNjN2M4NGFkZjkzNDM0YzE5MmQ3YzIyMzlmMjMxNiJ9fX0=")
    val STATE_BLOCKED =
        createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2U1YWQxMzU2N2EzY2E5NjgyYzUxMzliNzJlZjhiNTY0NTUxZjRmNGNjYzE4NGE5NDYwZDVkNTVkNzIwZmU2NiJ9fX0=")
    val WORLD =
        createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM4Y2YzZjhlNTRhZmMzYjNmOTFkMjBhNDlmMzI0ZGNhMTQ4NjAwN2ZlNTQ1Mzk5MDU1NTI0YzE3OTQxZjRkYyJ9fX0=")
    val CHECK =
        createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWE3NWM4ZTUxYzNkMTA1YmFiNGM3ZGUzM2E3NzA5MzczNjRiNWEwMWMxNWI3ZGI4MmNjM2UxZmU2ZWI5MzM5NiJ9fX0=")
    val DELETE =
        createSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ==")

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