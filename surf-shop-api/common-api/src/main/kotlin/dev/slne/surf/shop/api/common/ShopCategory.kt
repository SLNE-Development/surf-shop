package dev.slne.surf.shop.api.common

import dev.slne.surf.surfapi.core.api.generated.ItemTypeKeys
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

enum class ShopCategory(
    val internalName: String,
    val displayName: Component,
    val iconMaterial: Key
) {
    BUILDING_BLOCKS(
        internalName = "building_blocks",
        displayName = text("Building Blocks"),
        iconMaterial = ItemTypeKeys.BRICKS
    ),
    COLORED_BLOCKS(
        internalName = "colored_blocks",
        displayName = text("Colored Blocks"),
        iconMaterial = ItemTypeKeys.CYAN_WOOL
    ),
    NATURAL_BLOCKS(
        internalName = "natural_blocks",
        displayName = text("Natural Blocks"),
        iconMaterial = ItemTypeKeys.GRASS_BLOCK
    ),
    FUNCTIONAL_BLOCKS(
        internalName = "functional_blocks",
        displayName = text("Functional Blocks"),
        iconMaterial = ItemTypeKeys.OAK_SIGN
    ),
    REDSTONE_BLOCKS(
        internalName = "redstone_blocks",
        displayName = text("Redstone Blocks"),
        iconMaterial = ItemTypeKeys.REDSTONE
    ),
    TOOLS_AND_UTILITIES(
        internalName = "tools_and_utilities",
        displayName = text("Tools & Utilities"),
        iconMaterial = ItemTypeKeys.DIAMOND_PICKAXE
    ),
    COMBAT(
        internalName = "combat",
        displayName = text("Combat"),
        iconMaterial = ItemTypeKeys.NETHERITE_SWORD
    ),
    FOOD_AND_DRINKS(
        internalName = "food_and_drinks",
        displayName = text("Food & Drinks"),
        iconMaterial = ItemTypeKeys.GOLDEN_APPLE
    ),
    INGREDIENTS(
        internalName = "ingredients",
        displayName = text("Ingredients"),
        iconMaterial = ItemTypeKeys.IRON_INGOT
    ),
    SPAWN_EGGS(
        internalName = "spawn_eggs",
        displayName = text("Spawn Eggs"),
        iconMaterial = ItemTypeKeys.CREEPER_SPAWN_EGG
    ),
    OPERATOR_UTILITIES(
        internalName = "operator_utilities",
        displayName = text("Operator Utilities"),
        iconMaterial = ItemTypeKeys.COMMAND_BLOCK
    ),
    UNKNOWN(
        internalName = "unknown",
        displayName = text("Unknown"),
        iconMaterial = ItemTypeKeys.BARRIER
    );

    companion object {
        fun byInternalName(name: String) =
            entries.firstOrNull { it.internalName == name } ?: UNKNOWN
    }
}