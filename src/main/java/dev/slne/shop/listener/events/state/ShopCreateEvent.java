package dev.slne.shop.listener.events.state;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import dev.slne.shop.listener.events.ShopEvent;

public class ShopCreateEvent extends ShopEvent {

    private final Block block;

    /**
     * Constructs a new shop create event.
     *
     * @param block  The block.
     * @param player The player.
     */
    public ShopCreateEvent(Block block, Player player) {
        super(null, player);

        this.block = block;

    }

    /**
     * Gets the block.
     *
     * @return The block.
     */
    public Block getBlock() {
        return block;
    }

}
