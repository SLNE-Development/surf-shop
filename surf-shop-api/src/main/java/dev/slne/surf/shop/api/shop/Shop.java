package dev.slne.surf.shop.api.shop;


import dev.slne.surf.shop.api.shop.member.ShopMember;
import dev.slne.surf.shop.api.shop.transaction.ShopTransaction;
import dev.slne.surf.shop.api.shop.transaction.ShopTransactionResult;
import dev.slne.surf.shop.api.util.Interable;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.FinePosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

    /**
     * Creates the shop
     *
     * @return the shop
     */
    CompletableFuture<Shop> create();

    /**
     * Updates the shop
     *
     * @return the shop
     */
    CompletableFuture<Shop> update();

    /**
     * Deletes the shop
     *
     * @return the shop
     */
    CompletableFuture<Shop> delete();

    /**
     * Gets the shop lines
     *
     * @return the shop lines
     */
    List<Component> getShopLines();

    /**
     * Gets the amount of items that are in the shop
     *
     * @return the amount of items
     */
    default int amount() {
        return getTransactions().stream().mapToInt(ShopTransaction::getAmount).sum();
    }

    /**
     * Decreases the amount of items in the shop
     *
     * @param remover the remover
     * @param amount  the amount
     *
     * @return the shop
     */
    CompletableFuture<ShopTransactionResult> decreaseAmount(UUID remover, int amount);

    /**
     * Increases the amount of items in the shop
     *
     * @param adder  the adder
     * @param amount the amount
     *
     * @return the shop
     */
    CompletableFuture<ShopTransactionResult> increaseAmount(UUID adder, int amount);

    /**
     * Gets the id of this shop
     *
     * @return the id
     */
    long getId();

    /**
     * Gets the item of this shop
     *
     * @return the item
     */
    ItemStack item();

    /**
     * Renders the item of this shop
     *
     * @return the rendered item
     */
    Component renderItem();

    /**
     * Sets the item of this shop
     *
     * @param item the item
     *
     * @return the shop
     */
    CompletableFuture<Shop> item(ItemStack item);

    /**
     * Gets the owner of this shop
     *
     * @return the owner
     */
    OfflinePlayer getOwner();

    /**
     * Gets the uuid of this shop
     *
     * @return the uuid
     */
    UUID getUUID();

    /**
     * Gets the owner uuid of this shop
     *
     * @return the owner uuid
     */
    UUID getOwnerUUID();

    /**
     * Gets the members of this shop
     *
     * @return the members
     */
    @Unmodifiable
    List<ShopMember> getMembers();

    /**
     * Adds a member to this shop
     *
     * @param player the player
     *
     * @return the shop
     */
    default CompletableFuture<Shop> addMember(OfflinePlayer player) {
        return addMember(player.getUniqueId());
    }

    /**
     * Adds a member to this shop
     *
     * @param uuid the player uuid
     *
     * @return the shop
     */
    CompletableFuture<Shop> addMember(UUID uuid);

    /**
     * Removes a member from this shop
     *
     * @param player the player
     *
     * @return the shop
     */
    default CompletableFuture<Shop> removeMember(OfflinePlayer player) {
        return removeMember(player.getUniqueId());
    }

    /**
     * Removes a member from this shop
     *
     * @param uuid the player uuid
     *
     * @return the shop
     */
    CompletableFuture<Shop> removeMember(UUID uuid);

    /**
     * Gets the quantity of this shop
     *
     * @return the quantity
     */
    int quantity();

    /**
     * Sets the quantity of this shop
     *
     * @param amount the amount
     *
     * @return the shop
     */
    CompletableFuture<Shop> quantity(@Range(from = 1, to = 64) int amount);

    /**
     * Gets the sell price of this shop
     *
     * @return the sell price
     */
    double sellPrice();

    /**
     * Renders the sell price of this shop
     *
     * @return the rendered sell price
     */
    Component renderSellPrice();

    /**
     * Sets the sell price of this shop
     *
     * @param price the price
     *
     * @return the shop
     */
    CompletableFuture<Shop> sellPrice(double price);

    /**
     * Gets the buy price of this shop
     *
     * @return the buy price
     */
    double buyPrice();

    /**
     * Renders the buy price of this shop
     *
     * @return the rendered buy price
     */
    Component renderBuyPrice();

    /**
     * Sets the buy price of this shop
     *
     * @param price the price
     *
     * @return the shop
     */
    CompletableFuture<Shop> buyPrice(double price);

    /**
     * Gets the limit of items that can be sold to the shop
     *
     * @return the limit
     */
    @Range(from = 1, to = Integer.MAX_VALUE)
    int buyLimit();

    /**
     * Sets the limit of items that can be sold to the shop
     *
     * @param limit the limit
     *
     * @return the shop
     */
    CompletableFuture<Shop> buyLimit(@Range(from = 1, to = Integer.MAX_VALUE) int limit);

    /**
     * Returns the uuid of the world
     *
     * @return the uuid
     */
    UUID getWorldUUID();

    /**
     * Gets the world of this shop
     *
     * @return the world
     */
    Optional<World> getWorld();

    /**
     * Returns if the inventory of this shop is empty
     *
     * @return true if empty
     */
    boolean isInventoryEmpty();

    /**
     * Returns the player the shop is locked by
     *
     * @return the player
     */
    Optional<Player> getLockedBy();

    /**
     * Sets the player the shop is locked by
     *
     * @param player the player
     */
    void setLockedBy(Player player);

    /**
     * Returns if the shop is locked
     *
     * @return true if locked
     */
    boolean locked();

    /**
     * Sets if the shop is locked
     *
     * @param locked true if locked
     */
    void locked(boolean locked);

    /**
     * Locks the shop
     *
     * @param player the player
     */
    void lock(Player player);

    /**
     * Unlocks the shop
     */
    void unlock();

    /**
     * Returns if the given player is the owner of this shop
     *
     * @param player the player
     *
     * @return true if owner
     */
    default boolean isOwner(OfflinePlayer player) {
        return isOwner(player.getUniqueId());
    }

    /**
     * Returns if the given uuid is the owner of this shop
     *
     * @param uuid the uuid
     *
     * @return true if owner
     */
    default boolean isOwner(UUID uuid) {
        return getOwnerUUID().equals(uuid);
    }

    /**
     * Returns if the given player is a member of this shop
     *
     * @param player the player
     *
     * @return true if member
     */
    default boolean isMember(OfflinePlayer player) {
        return isMember(player.getUniqueId());
    }

    /**
     * Returns if the given uuid is a member of this shop
     *
     * @param uuid the uuid
     *
     * @return true if member
     */
    default boolean isMember(UUID uuid) {
        return getMembers().stream().anyMatch(member -> member.getUUID().equals(uuid));
    }

    /**
     * Returns if the shop is deleting
     *
     * @return true if deleting
     */
    boolean isDeleting();

    /**
     * Returns if the shop is selling
     *
     * @return true if selling
     */
    boolean isSelling();

    /**
     * Returns the description of this shop
     *
     * @return the description
     */
    Optional<Component> description();

    /**
     * Sets the description of this shop
     *
     * @param description the description
     *
     * @return the shop
     */
    CompletableFuture<Shop> description(Component description);

    /**
     * Sets the description of this shop
     *
     * @param description the description
     *
     * @return the shop
     */
    default CompletableFuture<Shop> description(String description) {
        return description(
                LegacyComponentSerializer.builder().hexColors().character('&').build().deserialize(description));
    }

    /**
     * Gets the transactions of this shop
     *
     * @return the transactions
     */
    List<ShopTransaction> getTransactions();

    /**
     * Returns if this shop is a buying shop
     *
     * @return true if buying
     */
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
     *
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
     *
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
     *
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

    /**
     * Returns the string representation of this shop
     *
     * @return the string representation
     */
    String toString();
}
