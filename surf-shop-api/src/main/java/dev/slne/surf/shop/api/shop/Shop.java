package dev.slne.surf.shop.api.shop;


import dev.slne.surf.shop.api.ShopApi;
import dev.slne.surf.shop.api.shop.member.ShopMember;
import dev.slne.surf.shop.api.shop.transaction.ShopTransaction;
import dev.slne.surf.shop.api.shop.transaction.ShopTransactionResult;
import dev.slne.surf.shop.api.util.ApiUtils;
import dev.slne.surf.shop.api.util.Interable;
import dev.slne.transaction.api.currency.Currency;
import io.papermc.paper.math.BlockPosition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
     * The may price a player can set
     */
    double MAX_SELL_PRICE = 1_000_000;

    double MAX_BUY_PRICE = 1_000_000;

    double MINIMUM_BUY_PRICE = 1.0;

    double MINIMUM_SELL_PRICE = 1.0;

    int MAX_DESCRIPTION_LENGTH = 64;

    char INFINITE_CHAR = '\u221E';

    /**
     * Creates the shop
     *
     * @return the shop
     */
    default CompletableFuture<Shop> create() {
        return ShopApi.getShopManager().createShop(this);
    }

    /**
     * Updates the shop
     *
     * @return the shop
     */
    CompletableFuture<Shop> save();

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
    int amount();

    /**
     * Gets the currency of this shop
     *
     * @return the currency
     */
    Currency currency();

    /**
     * Sets the currency of this shop
     *
     * @param currency the currency
     * @return the shop
     */
    CompletableFuture<Shop> currency(@NotNull Currency currency);

    /**
     * Decreases the amount of items in the shop
     *
     * @param remover the remover
     * @param amount  the amount to remove (must be positive)
     * @return the shop
     */
    CompletableFuture<ShopTransactionResult> decreaseAmount(@Nullable UUID remover, @Range(from = 1, to = Integer.MAX_VALUE) int amount);

    /**
     * Increases the amount of items in the shop
     *
     * @param adder  the adder
     * @param amount the amount to add (must be positive)
     * @return the shop
     */
    CompletableFuture<ShopTransactionResult> increaseAmount(@Nullable UUID adder, @Range(from = 1, to = Integer.MAX_VALUE) int amount);

    /**
     * Gets the id of this shop
     *
     * @return the id
     */
    long getId();

    /**
     * Gets a copy of the item of this shop
     *
     * @return the item
     */
    Optional<ItemStack> item();

    /**
     * Renders the item of this shop
     *
     * @return the rendered item
     */
    Component renderItem();

    /**
     * Renders the item display name of this shop
     *
     * @return the rendered item display name
     */
    Component renderItemDisplayName();

    /**
     * Sets the item of this shop
     *
     * @param item the item
     * @return the shop
     */
    CompletableFuture<Shop> item(@Nullable ItemStack item);

    /**
     * Gets the owner of this shop
     *
     * @return the owner
     * @throws IllegalStateException if the shop is an admin shop
     */
    OfflinePlayer getOwner();

    /**
     * Gets the uuid of this shop
     *
     * @return the uuid
     */
    UUID getUuid();

    /**
     * Gets the owner uuid of this shop
     *
     * @return the owner uuid
     * @throws IllegalStateException if the shop is an admin shop
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
     * @return the shop
     */
    CompletableFuture<Shop> addMember(OfflinePlayer player);

    /**
     * Adds a member to this shop
     *
     * @param uuid the player uuid
     * @return the shop
     */
    CompletableFuture<Shop> addMember(UUID uuid);

    /**
     * Removes a member from this shop
     *
     * @param player the player
     * @return the shop
     */
    CompletableFuture<Shop> removeMember(OfflinePlayer player);

    /**
     * Removes a member from this shop
     *
     * @param uuid the player uuid
     * @return the shop
     */
    CompletableFuture<Shop> removeMember(UUID uuid);

    /**
     * Gets the quantity of this shop
     *
     * @return the quantity
     */
    @Range(from = 1, to = 64)
    int quantity();

    /**
     * Sets the quantity of this shop
     *
     * @param amount the amount
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
     * @return the shop
     */
    CompletableFuture<Shop> sellPrice(@Range(from = 1, to = (long) MAX_SELL_PRICE) double price);

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
     * @return the shop
     */
    CompletableFuture<Shop> buyPrice(@Range(from = -1, to = (long) MAX_BUY_PRICE) double price);

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
     * @return the shop
     */
    CompletableFuture<Shop> buyLimit(@Range(from = -1, to = Integer.MAX_VALUE) int limit);

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
     * @return true if owner
     */
    boolean isOwner(OfflinePlayer player);

    /**
     * Returns if the given uuid is the owner of this shop
     *
     * @param uuid the uuid
     * @return true if owner
     */
    boolean isOwner(UUID uuid);

    /**
     * Returns if the given player is a member of this shop
     *
     * @param player the player
     * @return true if member
     */
    boolean isMember(OfflinePlayer player);

    /**
     * Returns if the given uuid is a member of this shop
     *
     * @param uuid the uuid
     * @return true if member
     */
    boolean isMember(UUID uuid);

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
     * @return the shop
     */
    CompletableFuture<Shop> description(Component description);

    /**
     * Sets the description of this shop
     *
     * @param description the description
     * @return the shop
     */
    CompletableFuture<Shop> description(String description);

    /**
     * Gets the transactions of this shop
     *
     * @return the transactions
     */
    @Unmodifiable
    Set<ShopTransaction> getTransactions();

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
     * Tests if the shop can sell the specified amount of items
     *
     * @param amountOfSellingItem the amount of items
     * @return true if the shop can sell the items (has enough items in stock) otherwise false
     */
    boolean canSell(@Range(from = 1, to = Integer.MAX_VALUE) int amountOfSellingItem);

    /**
     * Buys the specified amount of items from the player who is selling to the shop.
     * <p>
     * This will also call the {@link dev.slne.surf.shop.api.events.transaction.buy.ShopItemBuyEvent} event
     *
     * @param buyFrom            the player who is selling items to the shop
     * @param amountOfBuyingItem the amount of the buying item <b>not</b> the total amount of items.
     *                           <p>
     *                           <b>EXAMPLE:</b> If the {@link #quantity()} is 2 and the
     *                           {@code amountOfBuyingItem} is 3 then the player will sell 6
     *                           items in total to the shop
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
     * Tests if the shop can buy the specified amount of items
     *
     * @param amountOfBuyingItem the amount of items
     * @return true if the shop can buy the items (has enough money) otherwise false
     */
    boolean canBuy(@Range(from = 1, to = Integer.MAX_VALUE) int amountOfBuyingItem); // TODO: 03.11.2023 useless?

    /**
     * Returns the mini message instance
     *
     * @return the mini message
     */
    MiniMessage getMiniMessage();

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

    interface ShopTags {
        ClosingShopTag SHOP_ITEM_DISPLAYNAME = shop -> new KeyComponent("shop_item_displayname", shop.renderItemDisplayName());
        NormalShopTag SHOP_AMOUNT = shop -> new KeyComponent("shop_amount", Component.text(shop.isAdminShop() ? INFINITE_CHAR : shop.amount()));
        NormalShopTag SHOP_QUANTITY = shop -> new KeyComponent("shop_quantity", Component.text(shop.quantity()));
        ClosingShopTag SHOP_SELL_PRICE = shop -> new KeyComponent("shop_sell_price", shop.renderSellPrice());
        NormalShopTag SHOP_BUY_LIMIT = shop -> new KeyComponent("shop_buy_limit", Component.text(shop.buyLimit()));
        ClosingShopTag SHOP_BUY_PRICE = shop -> new KeyComponent("shop_buy_price", shop.renderBuyPrice());
        ClosingShopTag SHOP_CURRENCY_DISPLAYNAME = shop -> new KeyComponent("shop_currency_displayname", shop.currency().getDisplayName());
        ClosingShopTag SHOP_OWNER_NAME = shop -> new KeyComponent("shop_owner_name", shop.isAdminShop() ? Component.text("Admin Shop") : ApiUtils.getOfflineDisplayName(shop.getOwner()));
        NormalShopTag SHOP_IS_SELLING = shop -> new KeyComponent("shop_is_selling", Component.text(shop.isSelling() ? "Ja" : "Nein"));
        NormalShopTag SHOP_IS_BUYING = shop -> new KeyComponent("shop_is_buying", Component.text(shop.isBuying() ? "Ja" : "Nein"));

        interface ShopTag {
            void resolve(TagResolver.Builder builder, Shop shop);
        }

        interface ClosingShopTag extends ShopTag {
            KeyComponent resolve(Shop shop);

            @Override
            default void resolve(TagResolver.Builder builder, Shop shop) {
                KeyComponent resolved = resolve(shop);
                builder.tag(resolved.key(), Tag.selfClosingInserting(resolved.component()));
            }
        }

        interface NormalShopTag extends ShopTag {
            KeyComponent resolve(Shop shop);

            @Override
            default void resolve(TagResolver.Builder builder, Shop shop) {
                KeyComponent resolved = resolve(shop);
                builder.tag(resolved.key(), Tag.inserting(resolved.component()));
            }
        }

        record KeyComponent(@TagPattern String key, Component component) {
        }
    }
}
