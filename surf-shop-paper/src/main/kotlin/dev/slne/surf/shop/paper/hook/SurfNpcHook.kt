package dev.slne.surf.shop.paper.hook

import dev.slne.surf.npc.api.dsl.npc
import dev.slne.surf.npc.api.event.NpcInteractEvent
import dev.slne.surf.npc.api.npc.skin.NpcSkin
import dev.slne.surf.npc.api.npc.skin.NpcSkinPart
import dev.slne.surf.shop.paper.menu.ShopListView
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.util.toObjectSet
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
            val npc = npc {
                displayName {
                    variableValue("Shops".toSmallCaps(), TextDecoration.BOLD)
                }
                uniqueName = "surf_shop_npc-${it.name.lowercase()}"
                location = it.location
                type = EntityType.MANNEQUIN
                skin = NpcSkin(
                    "Shops",
                    "",
                    "",
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
        CENTRAL(0.0, 115.0, 0.0, 0f, 0f),
        NORTH(0.0, 115.0, -25000.0, 0f, 0f),
        NORTH_EAST(25000.0, 115.0, -25000.0, -45f, 0f),
        NORTH_WEST(-25000.0, 115.0, -25000.0, 45f, 0f),
        SOUTH(0.0, 115.0, 25000.0, 180f, 0f),
        SOUTH_EAST(25000.0, 115.0, 25000.0, -135f, 0f),
        SOUTH_WEST(-25000.0, 115.0, 25000.0, 135f, 0f),
        EAST(25000.0, 115.0, 0.0, -90f, 0f),
        WEST(-25000.0, 115.0, 0.0, 90f, 0f);

        val location = Location(world, x, y, z, yaw, pitch)
    }

    object NpcListener : Listener {
        @EventHandler
        fun onNpcInteract(event: NpcInteractEvent) {
            if (event.npc.uniqueName.startsWith("surf_shop_npc-")) {
                viewFrame.open(ShopListView::class.java, event.player)
            }
        }
    }
}