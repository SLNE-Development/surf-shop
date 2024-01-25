package dev.slne.surf.shop.api.events.state;

import dev.slne.surf.shop.api.events.CancellableShopEvent;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

public final class ShopCreateEvent extends CancellableShopEvent {

    private final Block block;
    private final Chest chest;

    /**
     * Constructs a new shop execute event.
     *
     * @param block  The block.
     * @param player The player.
     */
    @ApiStatus.Internal
    public ShopCreateEvent(Block block, Chest chest, Player player, boolean async) {
        super(null, player, async);

        this.block = block;
        this.chest = chest;
    }

    /**
     * Gets the block.
     *
     * @return The block.
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Gets the chest.
     *
     * @return The chest.
     */
    public Chest getChest() {
        return chest;
    }
}
