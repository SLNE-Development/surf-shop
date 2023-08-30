package dev.slne.surf.shop.api.shop;


import dev.slne.surf.shop.api.shop.member.ShopMember;
import dev.slne.surf.shop.api.util.Interable;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.FinePosition;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Shop extends BlockPosition, Comparable<Shop>, Interable<Shop> {
    /**
     * The created shop key
     */
    NamespacedKey CREATED_SHOP_KEY = new NamespacedKey("surf-shops", "placed_shop");

    /**
     * The creation item key
     */
    NamespacedKey CREATION_ITEM_KEY = new NamespacedKey("surf-shops", "creation_item");

    CompletableFuture<Shop> create();

    CompletableFuture<Shop> update();

    CompletableFuture<Shop> delete();

    List<Component> getShopLines();

    int amount();

    CompletableFuture<Shop> amount(int amount);

    default CompletableFuture<Shop> decreaseAmount(int amount) {
        return this.amount(this.amount() - amount);
    }

    default CompletableFuture<Shop> increaseAmount(int amount) {
        return this.amount(this.amount() + amount);
    }

    long getId();

    ItemStack item();

    void item(ItemStack item);

    OfflinePlayer getOwner();

    UUID getUUID();

    UUID getOwnerUUID();

    List<ShopMember> getMembers();

    int sellAmount();

    void sellAmount(int amount);

    double sellPrice();

    void sellPrice(double price);

    UUID getWorldUUID();

    Optional<World> getWorld();

    boolean isInventoryEmpty();

    Optional<Player> getLockedBy();

    void setLockedBy(Player player);

    boolean locked();

    void locked(boolean locked);

    void lock(Player player);

    void unlock();

    boolean isOwner(OfflinePlayer player);

    boolean isOwner(UUID player);

    boolean isMember(OfflinePlayer player);

    boolean isMember(UUID player);

    boolean isDeleting();

    /**
     * Gets the block x value for this shop
     *
     * @return the block x value
     */
    @Override
    int blockX();

    /**
     * Gets the block x value for this shop
     *
     * @return the block x value
     */
    @Override
    int blockY();

    /**
     * Gets the block x value for this shop
     *
     * @return the block x value
     */
    @Override
    int blockZ();

    /**
     * Gets the x value for this shop
     *
     * @return the x value
     */
    @Override
    default double x() {
        return blockX();
    }

    /**
     * Gets the y value for this shop
     *
     * @return the y value
     */
    @Override
    default double y() {
        return blockY();
    }

    /**
     * Gets the z value for this shop
     *
     * @return the z value
     */
    @Override
    default double z() {
        return blockZ();
    }


    /**
     * Checks if this position represents a {@link FinePosition}
     *
     * @return true if fine
     */
    @Override
    default boolean isFine() {
        return false;
    }

    /**
     * Returns the block position of this shop
     * or itself if it already is a block position
     *
     * @return the block position
     */
    @Override
    default @NotNull BlockPosition toBlock() {
        return this;
    }

    /**
     * Converts this position to a vector
     *
     * @return a new vector
     */
    @Override
    default @NotNull Vector toVector() {
        return new Vector(this.x(), this.y(), this.z());
    }

    /**
     * Creates a new location object at this position with the specified world
     *
     * @param world the world for the location object
     * @return a new location
     */
    @Override
    default @NotNull Location toLocation(@NotNull World world) {
        return new Location(world, this.x(), this.y(), this.z());
    }

    /**
     * Gets the location of this shop or null if the world is not loaded
     *
     * @return the location
     */
    default Location getLocation() {
        return getWorld().map(this::toLocation).orElse(null);
    }

    String toString();
}
