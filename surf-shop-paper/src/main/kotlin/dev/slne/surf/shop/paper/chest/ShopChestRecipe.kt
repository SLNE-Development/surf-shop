package dev.slne.surf.shop.paper.chest

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.paper.builder.buildItem
import dev.slne.surf.api.paper.builder.buildLore
import dev.slne.surf.api.paper.builder.displayName
import dev.slne.surf.shop.paper.plugin
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.persistence.PersistentDataType

object ShopChestRecipe {
    val SHOP_CHEST_KEY = NamespacedKey(plugin, "shop_chest")
    private val SHOP_COLOR = TextColor.color(252, 233, 121)

    val shopChestItem: ItemStack = buildItem(Material.CHEST) {
        displayName {
            coloredComponent("Shop Chest".toSmallCaps(), SHOP_COLOR, TextDecoration.BOLD)
        }

        buildLore {
            emptyLine()
            line {
                spacer("Platziere diese Kiste, um einen".toSmallCaps())
            }
            line {
                spacer("Shop-Zugangspunkt zu erstellen.".toSmallCaps())
            }
        }

        editMeta {
            it.persistentDataContainer.set(SHOP_CHEST_KEY, PersistentDataType.BOOLEAN, true)
        }
    }

    fun createRecipe(): ShapedRecipe {
        val recipe = ShapedRecipe(SHOP_CHEST_KEY, shopChestItem)
        recipe.shape(" B ", "BCB", " B ")
        recipe.setIngredient('B', RecipeChoice.MaterialChoice(Material.GOLD_INGOT))
        recipe.setIngredient('C', RecipeChoice.MaterialChoice(Material.CHEST))
        return recipe
    }

    fun isShopChest(item: ItemStack): Boolean {
        if (item.type != Material.CHEST) return false
        val meta = item.itemMeta ?: return false
        return meta.persistentDataContainer.getOrDefault(
            SHOP_CHEST_KEY,
            PersistentDataType.BOOLEAN,
            false
        )
    }
}
