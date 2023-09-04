package dev.slne.surf.shop.api.events;

import dev.slne.surf.shop.api.shop.Shop;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@ApiStatus.Internal
public abstract class ShopEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final @Nullable Shop shop;
    private final OfflinePlayer player;

    /**
     * Constructs a new shop event.
     *
     * @param shop   The shop.
     * @param player the player
     * @param async  Whether the event is asynchronous.
     */
    public ShopEvent(@Nullable Shop shop, OfflinePlayer player, boolean async) {
        super(async);

        this.shop = shop;
        this.player = player;
    }

    /**
     * Constructs a new shop event.
     *
     * @param shop   The shop.
     * @param player the player
     */
    public ShopEvent(@Nullable Shop shop, OfflinePlayer player) {
        this(shop, player, false);
    }

    /**
     * @return the handlerList
     */
    @SuppressWarnings("java:S4144")
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * Gets the shop.
     *
     * @return The shop.
     */
    public Optional<Shop> getShop() {
        return Optional.ofNullable(shop);
    }

    @Override
    @SuppressWarnings("java:S4144")
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Returns the player
     *
     * @return the player
     */
    public OfflinePlayer getPlayer() {
        return player;
    }
}
