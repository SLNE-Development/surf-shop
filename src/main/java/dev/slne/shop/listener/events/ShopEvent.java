package dev.slne.shop.listener.events;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import dev.slne.shop.shop.Shop;
import dev.slne.shop.shop.gui.utils.GuiUtils;
import net.kyori.adventure.text.Component;

public class ShopEvent extends Event implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Shop shop;
    private final Player player;

    private boolean cancelled;
    private Component cancelReason;
    private Sound cancelSound;

    /**
     * Constructs a new shop event.
     *
     * @param shop   The shop.
     * @param player the player
     * @param async  Whether the event is asynchronous.
     */
    public ShopEvent(Shop shop, Player player, boolean async) {
        super(async);

        this.shop = shop;
        this.player = player;

        this.cancelled = false;
        this.cancelReason = null;
        this.cancelSound = null;
    }

    /**
     * Constructs a new shop event.
     *
     * @param shop   The shop.
     * @param player the player
     */
    public ShopEvent(Shop shop, Player player) {
        this(shop, player, false);
    }

    /**
     * Gets the shop.
     *
     * @return The shop.
     */
    public Shop getShop() {
        return shop;
    }

    /**
     * @return the handlerList
     */
    @SuppressWarnings("java:S4144")
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    @SuppressWarnings("java:S4144")
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
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
    public Component getCancelReason() {
        return cancelReason;
    }

    /**
     * @return the cancelSound
     */
    public Sound getCancelSound() {
        return cancelSound;
    }

    /**
     * @param cancelReason the cancelReason to set
     */
    public void setCancelReason(Component cancelReason) {
        this.cancelReason = cancelReason;
    }

    /**
     * @param cancelSound the cancelSound to set
     */
    public void setCancelSound(Sound cancelSound) {
        this.cancelSound = cancelSound;
    }

    /**
     * Called when the event is cancelled.
     *
     * @param player The player.
     */
    public void applyCancelled(Player player) {
        if (cancelReason != null) {
            player.sendMessage(cancelReason);
        }

        if (cancelSound != null) {
            GuiUtils.playSound(cancelSound, player);
        }
    }

    /**
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

}
