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
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage") // Cause of BlockPosition
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

    Component renderItem();

    CompletableFuture<Shop> item(ItemStack item);

    OfflinePlayer getOwner();

    UUID getUUID();

    UUID getOwnerUUID();

    @Unmodifiable
    List<ShopMember> getMembers();

    CompletableFuture<Shop> addMember(OfflinePlayer player);

    CompletableFuture<Shop> addMember(UUID player);

    CompletableFuture<Shop> removeMember(OfflinePlayer player);

    CompletableFuture<Shop> removeMember(UUID player);

    int quantity();

    CompletableFuture<Shop> quantity(@Range(from = 1, to = 64) int amount);

    @Range(from = 1, to = 64)
    double sellPrice();

    Component renderSellPrice();

    CompletableFuture<Shop> sellPrice(double price);

    double buyPrice();

    Component renderBuyPrice();

    CompletableFuture<Shop> buyPrice(double price);

    @Range(from = 1, to = Integer.MAX_VALUE)
    int buyLimit();

    CompletableFuture<Shop> buyLimit(@Range(from = 1, to = Integer.MAX_VALUE) int limit);

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

    boolean isSelling();

    Optional<Component> description();

    CompletableFuture<Shop> description(Component description);

    CompletableFuture<Shop> description(String description);

    default boolean isBuying() {
        return false;
    }

    /**
     * Checks if this shop is an admin shop (Currently not implemented)
     *
     * @return true if admin shop
     */
    default boolean isAdminShop() {
        return false;
    }

    /**
     * Sells the specified amount of the selling item to the player who is buying from the shop.
     * <p>
     * This will also call the {@link dev.slne.surf.shop.api.events.transaction.sell.ShopItemSellEvent} event
     *
     * @param player              the player who is buying from the shop
     * @param amountOfSellingItem the amount of the selling item <b>not</b> the total amount of items.
     *                            <p>
     *                            <b>EXAMPLE:</b> If the {@link #quantity()} is 2 and the
     *                            {@code amountOfSellingItem} is 3 then the player will become 6
     *                            items in total
     *                            </p>
     * @return {@code true} if everything went fine and the player has been charged
     *
     * <li>
     * If the player inventory is full then only the amount of items that
     * fit into the inventory will be sold and {@code true} will be returned
     * </li>
     * <li>
     * If the player don´t have enough space in his inventory and the
     * check was broken for some reasons than the leftover items will be
     * dropped at the players position
     * </li>
     * <li>
     * If the player does not have enough money then the action will be
     * cancelled and {@code false} will be returned
     * </li>
     */
    CompletableFuture<Boolean> sell(Player player, @Range(from = 1, to = Integer.MAX_VALUE) int amountOfSellingItem);

    /**
     * Buys the specified amount of items from the player who is selling to the shop.
     * <p>
     * This will also call the {@link dev.slne.surf.shop.api.events.transaction.buy.ShopItemBuyEvent} event
     *
     * @param buyFrom            the player who is selling items to the shop
     * @param amountOfBuyingItem the amount of the buying item <b>not</b> the total amount of items.
     *                           <p>
     *                           <b>EXAMPLE:</b> If the {@link #quantity()} is 2 and the
     *                           {@code amountOfBuyingItem} is 3 then the player will become 6
     *                           items in total
     *                           </p>
     * @return {@code true} if everything went fine and the player has received the money
     *
     * <li>
     * If the shop inventory is full then only the amount of items that
     * fit into the inventory will be bought and {@code true} will be returned
     * </li>
     * <li>
     * If the maximum amount of items that can be sold to the shop ({@link #buyLimit()})
     * is reached then only the amount of items that fit into the inventory will be bought
     * </li>
     * <li>
     * If the shop does not have enough money then the action will be
     * cancelled and {@code false} will be returned
     * </li>
     */
    CompletableFuture<Boolean> buy(Player buyFrom, @Range(from = 1, to = Integer.MAX_VALUE) int amountOfBuyingItem);

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
