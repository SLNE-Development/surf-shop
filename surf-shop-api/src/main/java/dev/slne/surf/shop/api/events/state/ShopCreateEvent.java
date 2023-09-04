package dev.slne.surf.shop.api.events.state;

import dev.slne.surf.shop.api.events.CancellableShopEvent;
import dev.slne.surf.shop.api.events.ShopEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class ShopCreateEvent extends CancellableShopEvent {

    private final Block block;

    /**
     * Constructs a new shop create event.
     *
     * @param block  The block.
     * @param player The player.
     */
    public ShopCreateEvent(Block block, Player player, boolean async) {
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
