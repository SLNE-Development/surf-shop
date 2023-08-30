package dev.slne.surf.shop.server.util;

import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.ServerShop;
import dev.slne.surf.shop.server.shop.gui.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ShopUtils {

    /**
     * The surrounding block faces
     */
    public static final BlockFace[] SURROUNDING_BLOCK_FACES;

    /**
     * Gets the surrounding block states
     *
     * @param block           the block
     * @param blockStateClass the block state class
     * @param <T>             the block state type
     * @return the surrounding block states
     */
    public static @NotNull <T extends BlockState> List<T> getSurroundingBlockStates(Block block, Class<T> blockStateClass) {
        final List<T> surroundingBlockStates = new ArrayList<>();

        for (BlockFace surroundingBlockFace : SURROUNDING_BLOCK_FACES) {
            final Block relative = block.getRelative(surroundingBlockFace);

            if (blockStateClass.isInstance(relative.getState())) {
                surroundingBlockStates.add((T) relative.getState());
            }
        }

        return surroundingBlockStates;
    }

    public static @NotNull ItemStack constructCreationItem() {
        final ItemStack creationItem = ItemUtils.item(
                Material.CHEST,
                1,
                3,
                Component.text("Shop", MessageManager.PRIMARY)
        );

        creationItem.editMeta(meta -> meta.getPersistentDataContainer().set(Shop.CREATION_ITEM_KEY, PersistentDataType.BYTE, (byte) 1));

        return creationItem;
    }


    static {
        // Initialize the surrounding block faces
        SURROUNDING_BLOCK_FACES = new BlockFace[]{
                BlockFace.NORTH,
                BlockFace.EAST,
                BlockFace.SOUTH,
                BlockFace.WEST,
                BlockFace.UP,
                BlockFace.DOWN
        };
    }
}
