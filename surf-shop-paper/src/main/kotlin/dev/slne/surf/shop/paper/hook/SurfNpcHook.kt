package dev.slne.surf.shop.paper.hook

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.util.toObjectSet
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.npc.api.dsl.npc
import dev.slne.surf.npc.api.event.NpcInteractEvent
import dev.slne.surf.npc.api.npc.skin.NpcSkin
import dev.slne.surf.npc.api.npc.skin.NpcSkinPart
import dev.slne.surf.shop.paper.menu.ChestShopEditState
import dev.slne.surf.shop.paper.menu.OwnShopState
import dev.slne.surf.shop.paper.menu.ShopListView
import dev.slne.surf.shop.paper.util.searchInputCache
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object SurfNpcHook {
    val world: World get() = Bukkit.getWorlds().first()

    fun create() {
        ShopNpcLocations.entries.forEach {
            npc {
                displayName {
                    variableValue("Shops".toSmallCaps(), TextDecoration.BOLD)
                }
                uniqueName = "surf_shop_npc-${it.name.lowercase()}"
                location = it.location
                type = EntityType.MANNEQUIN
                skin = NpcSkin(
                    "Shops",
                    "ewogICJ0aW1lc3RhbXAiIDogMTc2Mzc0NDAwNjkxOSwKICAicHJvZmlsZUlkIiA6ICIxN2Q0ODA1ZDRmMTA0YTA5OWRiYzJmNzYzMDNjYmRkZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJnaWZ0bWV0b25uZXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGY4MTFjNGVmMmI2OTQwOTUxYTU2Y2Y4ZWRmNjFiYmM3MjdiOTM0MWY1ZTdhNzVkZTA3ZWIzNGExNmJjOTg3YSIKICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjM0MGMwZTAzZGQyNGExMWIxNWE4YjMzYzJhN2U5ZTMyYWJiMjA1MWIyNDgxZDBiYTdkZWZkNjM1Y2E3YTkzMyIKICAgIH0KICB9Cn0=",
                    "FMUMem5ik5ULtfvVCiF3hBb/zbDCrc8FIdAO8809idiKUVj/aBGUjOMeke8FZQ22euauc17BaQiLrVGz+r5FTZ1S/+57Ik6h/yC3OTcvWFT315aJNqpuuOjBzo0x8EmWsJFpj+L1rPLsMv+MM/T+UYFgx1C9dgY/2zZfxrS2KrMh6qGXSp7sXrHIlQBYm+Fcm0JKsqrvkdNWuAtdVSKKVKBHXy0veyDFFFVzlY8K8QqgGyi0SNj1SxguAeyVvaV/rN8TbnNxm2xkTt/9q3A+1myoPM6y7+u5Fy9QSD6m3JTHe98GsVWxlvB5I7TAcBFDitpLujiQPtHxeM6n50ij35pZW1ip3rdC2Wrnel3Ceyhr0kzGT2vuM08oa2xfP7cBhFeS8EpY5uP7qFyo03nk3RwEYRR7gXU38wxgkkJKn00Y68Nzl2BRkZOwY4kFqztiFAiJWx0QA7uKql6ySvz1ckXrUXO8Jye9RaLUeLKaG3cZhBk/rFlPX4t4lNyGHFsUiJPvub0kqACPoHSI5czCz1i47CWWGLFqj4Ok7jlKKnsgWkxDDddNnFVPUnkcXWbrKrCmVwZAV/K1JKKpBfcOSkFEYEquFBHCdmGDrWEUTUQsjVglToNtX8u2sK3X6K1GIlCuRlUBsq8Pe5+boAH7S2OgPTSGbXRGi+BkxtHi6t4=",
                    NpcSkinPart.entries.toObjectSet()
                )
            }
        }
    }

    enum class ShopNpcLocations(
        x: Double,
        y: Double,
        z: Double,
        yaw: Float,
        pitch: Float
    ) {
        CENTRAL(-13.5, 73.0, -13.5, -45f, 0f),
        NORTH(-1.5, 75.0, -25010.5, 0f, 0f),
        NORTH_EAST(24994.5, 81.0, -24975.5, 180f, 0f),
        NORTH_WEST(-24978.5, 71.0, -24991.5, 90f, 0f),
        SOUTH(18.0, 116.0, 24993.5, 45f, 0f),
        SOUTH_EAST(24985.5, 114.99268, 24984.5, -45f, 0f),
        SOUTH_WEST(-25020.5, 101.0, 25000.5, -90f, 0f),
        EAST(25004.5, 98.0, 8.5, 150f, 0f),
        WEST(-24999.5, 82.0, -38.5, 0f, 0f);

        val location = Location(world, x, y, z, yaw, pitch)
    }

    object NpcListener : Listener {
        @EventHandler
        fun onNpcInteract(event: NpcInteractEvent) {
            if (event.npc.uniqueName.startsWith("surf_shop_npc-")) {
                searchInputCache.remove(event.player.uniqueId)
                viewFrame.open(ShopListView::class.java, event.player)
                OwnShopState.setInOwn(event.player.uniqueId, false)
                ChestShopEditState.setChest(event.player.uniqueId, null)
            }
        }
    }
}