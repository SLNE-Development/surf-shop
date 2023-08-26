package dev.slne.shop.util;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
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
