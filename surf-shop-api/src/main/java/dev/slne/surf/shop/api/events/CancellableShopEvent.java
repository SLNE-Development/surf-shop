package dev.slne.surf.shop.api.events;

import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.util.ApiUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public abstract class CancellableShopEvent extends ShopEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;
    private @Nullable Component cancelReason = null;
    private @Nullable Sound cancelSound = null;

    /**
     * Constructs a new shop event.
     *
     * @param shop   The shop.
     * @param player the player
     */
    public CancellableShopEvent(@Nullable Shop shop, Player player) {
        super(shop, player);
    }

    /**
     * Constructs a new shop event.
     *
     * @param shop   The shop.
     * @param player the player
     * @param async  Whether the event is asynchronous.
     */
    public CancellableShopEvent(@Nullable Shop shop, Player player, boolean async) {
        super(shop, player, async);
    }



    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * @return the cancelReason
     */
    public @Nullable Component getCancelReason() {
        return cancelReason;
    }

    /**
     * @return the cancelSound
     */
    public @Nullable Sound getCancelSound() {
        return cancelSound;
    }

    /**
     * @param cancelReason the cancelReason to set
     */
    public void setCancelReason(@Nullable Component cancelReason) {
        this.cancelReason = cancelReason;
    }

    /**
     * @param cancelSound the cancelSound to set
     */
    public void setCancelSound(@Nullable Sound cancelSound) {
        this.cancelSound = cancelSound;
    }

    /**
     * Called when the event is cancelled.
     *
     * @param player The player.
     */
    @ApiStatus.Internal // Only for internal use
    public void applyCancelled(Player player) {
        if (cancelReason != null) {
            player.sendMessage(cancelReason);
        }

        if (cancelSound != null) {
            ApiUtils.playSound(cancelSound, player);
        }
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
