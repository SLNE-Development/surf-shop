package dev.slne.surf.shop.server.shop;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.slne.data.api.DataApi;
import dev.slne.data.api.web.WebRequest;
import dev.slne.surf.shop.api.events.transaction.buy.ShopItemBuyEvent;
import dev.slne.surf.shop.api.events.transaction.sell.ShopItemSellEvent;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.member.ShopMember;
import dev.slne.surf.shop.server.BukkitMain;
import dev.slne.surf.shop.server.api.API;
import dev.slne.surf.shop.server.api.BukkitGsonConverter;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.inventory.CouldNotAddAllItemsToInventoryException;
import dev.slne.surf.shop.server.shop.gui._2_0.inventory.PlayerInventoryItemsTransfer;
import dev.slne.surf.shop.server.shop.gui._2_0.util.GuiSound;
import dev.slne.surf.shop.server.shop.gui._2_0.util.GuiUtils;
import dev.slne.surf.shop.server.shop.member.ServerShopMember;
import dev.slne.transaction.api.TransactionApi;
import dev.slne.transaction.api.currency.Currency;
import dev.slne.transaction.api.player.TransactionPlayer;
import dev.slne.transaction.api.transaction.Transaction;
import dev.slne.transaction.api.transaction.result.TransactionAddResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.*;

public class ServerShop implements Shop {

    private static final BukkitGsonConverter GSON_CONVERTER = new BukkitGsonConverter();

    @SerializedName("id")
    private long id;

    @SerializedName("uuid")
    private UUID uuid;

    @SerializedName("owner_uuid")
    private UUID ownerUuid;

    @SerializedName("shop_itemstack")
    private ItemStack itemStack;

    @SerializedName("shop_amount")
    private int amount;

    @SerializedName("location_world")
    private UUID worldUUID;

    @SerializedName("location_x")
    private int x;

    @SerializedName("location_y")
    private int y;

    @SerializedName("location_z")
    private int z;

    @SerializedName("sell_amount")
    private int sellAmount;

    @SerializedName("sell_price")
    private double sellPrice;

    @SerializedName("buy_amount")
    private int buyAmount; // TODO: Add to database

    @SerializedName("buy_price")
    private double buyPrice; // TODO: Add to database

    @SerializedName("members")
    private List<ServerShopMember> members;

    @SerializedName("currency")
    private Currency currency;

    private boolean locked;
    private Player lockedByPlayer;

    private boolean deleting = false;
    private boolean isAdminShop = false;

    @Deprecated
    public ServerShop() {
        this.members = new ArrayList<>();
    }

    /**
     * A new {@link ServerShop} instance
     *
     * @param owner     the owner
     * @param itemStack the itemstack
     * @param location  the location
     */
    public ServerShop(@NotNull Currency currency, @NotNull UUID owner, @Nullable ItemStack itemStack, @Nullable Location location) {
        checkNotNull(currency, "currency");
        checkNotNull(owner, "owner");

        this.ownerUuid = owner;
        this.currency = currency;

        if (itemStack != null) {
            this.itemStack = itemStack;
        }

        if (location != null) {
            this.worldUUID = location.getWorld().getUID();
            this.x = location.getBlockX();
            this.y = location.getBlockY();
            this.z = location.getBlockZ();
        }

        this.uuid = UUID.randomUUID();
        this.members = new ArrayList<>();

        this.locked = false;
    }

    /**
     * Returns all shops from the database
     *
     * @return the shops
     */
    public static CompletableFuture<List<Shop>> shops() {
        WebRequest request = WebRequest.builder().json(true).url(API.SHOPS).build();
        List<Shop> shops = new ArrayList<>();

        return request.executeGet().thenApplyAsync(response -> {

            JsonArray bodyArray = response.bodyArray(GSON_CONVERTER);
            for (JsonElement element : bodyArray) {
                Shop shop = fromBodyElement(element, false);

                if (shop == null) {
                    continue;
                }

                shops.add(shop);
            }

            return shops;
        }).exceptionally(exception -> {
            DataApi.getDataInstance().logError(ServerShop.class, "Failed to fetch shops", exception);
            return null;
        });
    }

    /**
     * Creates a shop in the database
     *
     * @return the shop
     */
    @Override
    public CompletableFuture<Shop> create() {
        System.out.println("Creating shop");
        WebRequest request = WebRequest.builder()
                .json(true)
                .parameters(toParameters())
                .url(API.SHOPS)
                .build();

        return request.executePost().thenApplyAsync(response -> {
            Shop newShop = fromBodyElement(response.bodyElement(GSON_CONVERTER), true);

            if (newShop == null) {
                System.out.println("newShop = " + null);

                BukkitMain.getInstance().getLogger().severe("Failed to create shop: " + uuid.toString());
                BukkitMain.getInstance().getLogger().severe(response.body().toString());
                return null;
            }

            id = newShop.getId();

            return this.inter();
        }).exceptionally(exception -> {
            exception.printStackTrace();
            DataApi.getDataInstance().logError(getClass(), "Failed to create shop: " + uuid.toString(), exception);
            return null;
        });
    }

    /**
     * Updates the shop in the database
     *
     * @return the shop
     */
    public CompletableFuture<Shop> update() {
        String url = String.format(API.SHOP, uuid.toString());
        WebRequest request = WebRequest.builder().json(true).parameters(toParameters()).url(url).build();

        return request.executePut().thenApplyAsync(response -> {
            Shop updatedShop = fromBodyElement(response.bodyElement(GSON_CONVERTER), true);

            if (updatedShop == null) {
                BukkitMain.getInstance().getLogger().severe("Failed to update shop: " + uuid.toString());
                BukkitMain.getInstance().getLogger().severe(response.body().toString());
                return null;
            }

            return this.inter();
        }).exceptionally(exception -> {
            DataApi.getDataInstance().logError(getClass(), "Failed to update shop: " + uuid.toString(), exception);
            return null;
        });
    }

    /**
     * Deletes the shop in the database
     *
     * @return the shop
     */
    public CompletableFuture<Shop> delete() {
        deleting = true;

        String url = String.format(API.SHOP, uuid.toString());
        WebRequest request = WebRequest.builder().json(true).parameters(toParameters()).url(url).build();

        return request.executeDelete().thenApplyAsync(response -> {
            Shop deletedShop = fromBodyElement(response.bodyElement(GSON_CONVERTER), true);

            if (deletedShop == null) {
                BukkitMain.getInstance().getLogger().severe("Failed to delete shop: " + uuid.toString());
                BukkitMain.getInstance().getLogger().severe(response.body().toString());

                deleting = false;
                return null;
            }

            deleting = false;
            return this.inter();
        }).exceptionally(exception -> {
            DataApi.getDataInstance().logError(getClass(), "Failed to delete shop: " + uuid.toString(), exception);

            deleting = false;
            return null;
        });
    }

    /**
     * Creates a parameter map
     *
     * @return the parameter map
     */
    private Map<String, Object> toParameters() {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("uuid", uuid.toString());
        parameters.put("owner_uuid", ownerUuid.toString());
        parameters.put("shop_itemstack", itemStack);
        parameters.put("shop_amount", String.valueOf(amount));
        parameters.put("currency_id", currency.getId());

        parameters.put("location_world", worldUUID.toString());
        parameters.put("location_x", String.valueOf(x));
        parameters.put("location_y", String.valueOf(y));
        parameters.put("location_z", String.valueOf(z));

        parameters.put("sell_amount", String.valueOf(sellAmount));
        parameters.put("sell_price", String.valueOf(sellPrice));

        return parameters;
    }

    /**
     * Returns a shop by the body json element
     *
     * @param bodyElement the body json element
     * @return the shop
     */
    private static Shop fromBodyElement(JsonElement bodyElement, boolean isRootElement) {
        JsonObject dataObject = bodyElement.getAsJsonObject();

        return GSON_CONVERTER.fromJson(dataObject.toString(), Shop.class);
    }

    /**
     * Returns the shop lines
     *
     * @return the shop lines
     */
    public List<Component> getShopLines() {
        List<Component> lines = new ArrayList<>();

        OfflinePlayer owner = getOwner();

        if (owner.getName() != null) {
            lines.add(Component.text(owner.getName(), MessageManager.VARIABLE_VALUE));
        }

        if (itemStack != null) {
            TextComponent.Builder builder = Component.text();
            ItemMeta itemMeta = itemStack.clone().getItemMeta();

            builder.append(Component.text(amount + "x", MessageManager.VARIABLE_VALUE));

            if (itemMeta != null) {
                if (itemMeta.hasDisplayName()) {
                    final Component displayName = itemMeta.displayName();
                    assert displayName != null : "What is happening???";

                    builder.append(displayName.colorIfAbsent(MessageManager.VARIABLE_VALUE));
                } else {
                    builder.append(Component.text(itemStack.getType().name(), MessageManager.VARIABLE_VALUE));
                }
            }
        }

        return lines;
    }

    @Override
    public int amount() {
        return amount;
    }

    @Override
    public CompletableFuture<Shop> amount(int amount) {
        this.amount = amount;

        return update();
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    @Override
    public ItemStack item() {
        return itemStack;
    }

    @Override
    public void item(ItemStack item) {
        this.itemStack = item;
    }

    /**
     * @return the owner
     */
    @Override
    public UUID getOwnerUUID() {
        return ownerUuid;
    }

    /**
     * Returns the owner
     *
     * @return the owner
     */
    public OfflinePlayer getOwner() {
        return Bukkit.getOfflinePlayer(ownerUuid);
    }

    /**
     * @return the uuid
     */
    @Override
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Returns if the shop is empty
     *
     * @return if the shop is empty
     */
    @Override
    public boolean isInventoryEmpty() {
        return amount() == 0;
    }

    @Override
    public Optional<Player> getLockedBy() {
        return Optional.ofNullable(lockedByPlayer);
    }

    @Override
    public void setLockedBy(Player player) {
        this.lockedByPlayer = player;
    }

    @Override
    public boolean locked() {
        return locked;
    }

    @Override
    public void locked(boolean locked) {
        this.locked = locked;
    }

    /**
     * @return the members
     */
    @Override
    public List<ShopMember> getMembers() {
        return members.stream().map(ShopMember::inter).toList();
    }

    @Override
    public int sellAmount() {
        return sellAmount;
    }

    @Override
    public void sellAmount(int amount) {
        this.sellAmount = amount;
    }

    @Override
    public double sellPrice() {
        return sellPrice;
    }

    @Override
    public void sellPrice(double price) {
        this.sellPrice = price;
    }

    @Override
    public UUID getWorldUUID() {
        return worldUUID;
    }

    @Override
    public Optional<World> getWorld() {
        return Optional.ofNullable(Bukkit.getWorld(worldUUID));
    }

    /**
     * Locks the shop
     *
     * @param player the player
     */
    @Override
    public void lock(Player player) {
        this.locked = true;
        this.lockedByPlayer = player;
    }

    /**
     * Unlocks the shop
     */
    @Override
    public void unlock() {
        this.locked = false;
        this.lockedByPlayer = null;
    }

    /**
     * Returns if the player is the owner
     *
     * @param player the player
     * @return if the player is the owner
     */
    public boolean isOwner(OfflinePlayer player) {
        return isOwner(player.getUniqueId());
    }

    @Override
    public boolean isOwner(UUID player) {
        return Objects.equals(ownerUuid, player);
    }

    /**
     * Returns if the player is a member
     *
     * @param player the player
     * @return if the player is a member
     */
    public boolean isMember(OfflinePlayer player) {
        return isMember(player.getUniqueId());
    }

    @Override
    public boolean isMember(UUID player) {
        return members != null
                && members.stream().anyMatch(member -> member.getUUID().equals(player));
    }

    /**
     * Returns if the shop is currently deleting
     *
     * @return if the shop is currently deleting
     * @since 1.0.0
     */
    public boolean isDeleting() {
        return deleting;
    }

    @Override
    public boolean isSelling() {
        return sellPrice > 0;
    }

    @Override
    public boolean isBuying() {
        return buyPrice > 0;
    }

    /**
     * Sells the specified amount of the selling item to the shop.
     * <p>
     * This will also call the {@link ShopItemBuyEvent} event
     *
     * @param player              the player who is buying
     * @param amountOfSellingItem the amount of the selling item <b>not</b> the total amount of items.
     *                            <p>
     *                            <b>EXAMPLE:</b> If the {@link #sellAmount()} is 2 and the
     *                            {@code amountOfSellingItem} is 3 then the player will become 6
     *                            items in total
     *                            </p>
     * @return true if everything went fine and the player has been charged
     *
     * <li>
     * If the player inventory is full then the action will be cancelled
     * and {@code  false} will be returned
     * </li>
     * <li>
     * If the player don´t have enough space in his invenvotry and the
     * check was broken for some reasons than the leftover items will be
     * dropped at the players position
     * </li>
     * <li>
     * If the player does not have enough money then the action will be
     * cancelled and {@code false} will be returned
     * </li>
     */
    @Override
    public CompletableFuture<Boolean> buy(Player player, int amountOfSellingItem) {
        checkNotNull(player, "player");
        checkArgument(amountOfSellingItem > 0, "amountOfSellingItem must be greater than 0");
        checkState(player.isConnected(), "player is not connected");
        checkState(!isDeleting(), "shop is currently deleting");

        if (amount < (amountOfSellingItem * sellAmount)) {
            return CompletableFuture.completedFuture(false);
        }

        final ShopItemSellEvent event = new ShopItemSellEvent(this, player, itemStack, amountOfSellingItem);

        if (!event.callEvent()) {
            event.applyCancelled(player);
            return CompletableFuture.completedFuture(false);
        }

        final int finalAmount = event.getBoughtAmount();
        final int totalItems = finalAmount * sellAmount;

        // Charge player
        executeTransaction(player, sellPrice * finalAmount).thenComposeAsync(transactionResult -> {
            if (transactionResult == TransactionAddResult.NOT_ENOUGH_MONEY) {
                player.sendMessage(MessageManager.getNotEnoughMoneyComponent());
                GuiUtils.playGuiSound(GuiSound.DENY_ACTION, player);
                return CompletableFuture.completedFuture(false);
            }

            if (transactionResult != TransactionAddResult.SUCCESS) {
                player.sendMessage(MessageManager.getTransactionErrorComponent());
                GuiUtils.playGuiSound(GuiSound.DENY_ACTION, player);
                return CompletableFuture.completedFuture(false);
            }

            return decreaseAmount(totalItems).thenApplyAsync(shop -> {
                Bukkit.getScheduler().runTask(BukkitMain.getInstance(), () -> {
                    final ItemStack itemStack = shop.item();

                    if (itemStack == null) {
                        return;
                    }

                    final Player playerNow = Bukkit.getPlayer(player.getUniqueId());

                    if (playerNow != null) {
                        playerNow.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                        playerNow.sendMessage(MessageManager.getShopBoughtAmountBuyerComponent(itemStack, totalItems));
                    }

                    final Player owner = getOwner().getPlayer();
                    if (owner != null && owner.isConnected()) {
                        owner.sendMessage(MessageManager.getShopBoughtAmountOwnerComponent(player, itemStack, totalItems));
                    }
                });

                // Transfer items
                return transferItems(player, finalAmount);
            });
        });

        return CompletableFuture.completedFuture(true);
    }

    /**
     * Tries to transfer the items to the player.
     *
     * @param player the player
     * @param amount the amount of items to transfer
     * @return whether the transfer was successful
     */
    private boolean transferItems(@NotNull Player player, int amount) {
        checkNotNull(player, "player");
        checkArgument(amount > 0, "amount must be greater than 0");

        final PlayerInventoryItemsTransfer itemTransfer = new PlayerInventoryItemsTransfer(
                item(),
                amount,
                player.getInventory()
        );

        try {
            if (!itemTransfer.transferItems()) {
                player.sendMessage(MessageManager.getInventoryCannotAcceptNItemsComponent(amount));
                GuiUtils.playGuiSound(GuiSound.DENY_ACTION, player);
                return false;
            }

        } catch (CouldNotAddAllItemsToInventoryException exception) {
            final Location playerLocation = player.getLocation();
            final World playerWorld = player.getWorld();

            for (Map.Entry<Integer, ItemStack> leftOver : exception.getLeftOver().entrySet()) {
                playerWorld.dropItem(playerLocation, leftOver.getValue(), item -> {
                    item.setOwner(player.getUniqueId());
                    item.setThrower(player.getUniqueId());
                });
            }
        }

        return true;
    }

    private CompletableFuture<TransactionAddResult> executeTransaction(Player buyer, double price) {
        final TransactionPlayer ownerTransactionPlayer = TransactionApi.getTransactionPlayer(ownerUuid, false);
        final TransactionPlayer buyerTransactionPlayer = TransactionApi.getTransactionPlayer(buyer.getUniqueId(), true);
        final Transaction buyerTransaction = TransactionApi.createTransaction(null, buyerTransactionPlayer.uuid(), this.currency, BigDecimal.valueOf(-price));
        final Transaction ownerTransaction = TransactionApi.createTransaction(null, ownerTransactionPlayer.uuid(), this.currency, BigDecimal.valueOf(price));

        buyerTransaction.setTransactionData(new ShopTransactionData(this, buyer));
        ownerTransaction.setTransactionData(new ShopTransactionData(this, buyer));

        return buyerTransactionPlayer.addTransaction(buyerTransaction).thenComposeAsync(buyerTransactionResult -> {
            if (buyerTransactionResult == TransactionAddResult.SUCCESS) {
                return ownerTransactionPlayer.addTransaction(ownerTransaction).thenApplyAsync(ownerTransactionResult -> buyerTransactionResult);
            }

            return CompletableFuture.completedFuture(buyerTransactionResult);
        });
    }

    /**
     * Gets the block x value for this shop
     *
     * @return the block x value
     */
    @Override
    public int blockX() {
        return x;
    }

    /**
     * Gets the block x value for this shop
     *
     * @return the block x value
     */
    @Override
    public int blockY() {
        return y;
    }

    /**
     * Gets the block x value for this shop
     *
     * @return the block x value
     */
    @Override
    public int blockZ() {
        return z;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("uuid", uuid)
                .add("ownerUuid", ownerUuid)
                .add("itemStack", itemStack)
                .add("amount", amount)
                .add("worldUUID", worldUUID)
                .add("x", x)
                .add("y", y)
                .add("z", z)
                .add("sellAmount", sellAmount)
                .add("sellPrice", sellPrice)
                .add("members", members)
                .add("locked", locked)
                .add("lockedByPlayer", lockedByPlayer)
                .add("deleting", deleting)
                .toString();
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure {@link Integer#signum
     * signum}{@code (x.compareTo(y)) == -signum(y.compareTo(x))} for
     * all {@code x} and {@code y}.  (This implies that {@code
     * x.compareTo(y)} must throw an exception if and only if {@code
     * y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code
     * x.compareTo(y)==0} implies that {@code signum(x.compareTo(z))
     * == signum(y.compareTo(z))}, for all {@code z}.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     * @apiNote It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     */
    @Override
    public int compareTo(@NotNull Shop o) {
        return uuid.compareTo(o.getUUID());
    }

    @Override
    public Shop inter() {
        return this;
    }

    /**
     * Checks if this shop is an admin shop (Currently not implemented)
     *
     * @return true if admin shop
     */
    @Override
    public boolean isAdminShop() {
        return isAdminShop;
    }
}
