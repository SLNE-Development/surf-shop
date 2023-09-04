package dev.slne.surf.shop.server.shop;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.annotations.SerializedName;
import dev.slne.data.api.DataApi;
import dev.slne.data.api.gson.GsonConverter;
import dev.slne.data.api.web.WebRequest;
import dev.slne.surf.shop.api.ShopApi;
import dev.slne.surf.shop.api.events.state.ShopChangeDescriptionEvent;
import dev.slne.surf.shop.api.events.state.ShopEditQuantityEvent;
import dev.slne.surf.shop.api.events.state.member.ShopAddMemberEvent;
import dev.slne.surf.shop.api.events.state.member.ShopRemoveMemberEvent;
import dev.slne.surf.shop.api.events.state.price.ShopChangeBuyPriceEvent;
import dev.slne.surf.shop.api.events.state.price.ShopChangeSellPriceEvent;
import dev.slne.surf.shop.api.events.transaction.sell.ShopItemSellEvent;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.member.ShopMember;
import dev.slne.surf.shop.api.shop.transaction.ShopTransaction;
import dev.slne.surf.shop.api.shop.transaction.ShopTransactionResult;
import dev.slne.surf.shop.server.BukkitMain;
import dev.slne.surf.shop.server.api.API;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.inventory.InventoryItemsTransfer;
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
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class ServerShop implements Shop {

    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.builder()
            .hexColors()
            .character('&')
            .extractUrls()
            .build();
    private transient final boolean adminShop = false;
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
    @SerializedName("stack_size")
    private int stackSize;
    @SerializedName("sell_price")
    private double sellPrice;
    @SerializedName("buy_limit")
    private int buyLimit;
    @SerializedName("buy_price")
    private double buyPrice;
    @SerializedName("members")
    private List<ServerShopMember> members;
    @SerializedName("currency")
    private Currency currency;
    @SerializedName("description")
    @Nullable
    private Component description;
    @SerializedName("transactions")
    private List<ShopTransaction> transactions;
    private transient boolean locked;
    private transient Player lockedByPlayer;
    private transient boolean deleting = false;

    @Deprecated
    public ServerShop() {
        this.members = new ArrayList<>();
        this.transactions = new ArrayList<>();
    }

    /**
     * A new {@link ServerShop} instance
     *
     * @param owner     the owner
     * @param itemStack the itemstack
     * @param location  the location
     */
    public ServerShop(@NotNull Currency currency, @NotNull UUID owner, @Nullable ItemStack itemStack,
                      @Nullable Location location) {
        this();

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

            JsonArray bodyArray = response.bodyArray(ShopApi.getInstance().getGsonConverter());
            for (JsonElement element : bodyArray) {
                Shop shop = ShopApi.getInstance().getGsonConverter().fromJson(element.toString(), ServerShop.class);

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

    @Override
    public CompletableFuture<Shop> create() {
        System.out.println("Creating shop");
        WebRequest request = WebRequest.builder()
                .json(true)
                .parameters(toParameters())
                .url(API.SHOPS)
                .build();

        return request.executePost().thenApplyAsync(response -> {
            Shop newShop = ShopApi.getInstance().getGsonConverter()
                    .fromJson(response.bodyElement(ShopApi.getInstance().getGsonConverter()).toString(),
                            ServerShop.class);

            if (newShop == null) {
                System.out.println("newShop = " + null);

                BukkitMain.getInstance().getLogger().severe("Failed to create shop: " + uuid.toString());
                BukkitMain.getInstance().getLogger().severe(response.body().toString());
                return null;
            }

            id = newShop.getId();

            return this.inter();
        }).exceptionally(exception -> {
            DataApi.getDataInstance().logError(getClass(), "Failed to create shop: " + uuid.toString(), exception);
            return null;
        });
    }

    @Override
    public CompletableFuture<Shop> update() {
        String url = String.format(API.SHOP, uuid.toString());
        WebRequest request = WebRequest.builder().json(true).parameters(toParameters()).url(url).build();

        System.out.println("Updating shop");
        return request.executePut().thenApplyAsync(response -> {
            System.out.println("response = " + response.body());
            Shop updatedShop = ShopApi.getInstance().getGsonConverter()
                    .fromJson(response.bodyElement(ShopApi.getInstance().getGsonConverter()).toString(),
                            ServerShop.class);

            if (updatedShop == null) {
                System.out.println("updatedShop = " + null);
                BukkitMain.getInstance().getLogger().severe("Failed to update shop: " + uuid.toString());
                BukkitMain.getInstance().getLogger().severe(response.body().toString());
                return null;
            }

            System.out.println("updatedShop = " + updatedShop);
            return updatedShop;
        }).exceptionally(exception -> {
            DataApi.getDataInstance().logError(getClass(), "Failed to update shop: " + uuid.toString(), exception);
            return null;
        });
    }

    @Override
    public CompletableFuture<Shop> delete() {
        deleting = true;

        String url = String.format(API.SHOP, uuid.toString());
        WebRequest request = WebRequest.builder().json(true).parameters(toParameters()).url(url).build();

        return request.executeDelete().thenApplyAsync(response -> {
            Shop deletedShop = ShopApi.getInstance().getGsonConverter()
                    .fromJson(response.bodyElement(ShopApi.getInstance().getGsonConverter()).toString(),
                            ServerShop.class);

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

    @Override
    public CompletableFuture<ShopTransactionResult> decreaseAmount(UUID remover, int amount) {
        return ShopApi.getInstance().createShopTransaction(this, remover, amount).create();
    }

    @Override
    public CompletableFuture<ShopTransactionResult> increaseAmount(UUID adder, int amount) {
        return ShopApi.getInstance().createShopTransaction(this, adder, amount).create();
    }

    @Override
    public List<ShopTransaction> getTransactions() {
        return transactions;
    }

    /**
     * Creates a parameter map
     *
     * @return the parameter map
     */
    private Map<String, Object> toParameters() {
        GsonConverter GSON_CONVERTER = ShopApi.getInstance().getGsonConverter();
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("uuid", uuid.toString());
        parameters.put("owner_uuid", ownerUuid.toString());
        parameters.put("shop_itemstack", GSON_CONVERTER.toJsonElement(itemStack).getAsString());
        parameters.put("shop_amount", String.valueOf(amount));
        parameters.put("currency_id", String.valueOf(currency.getId()));
        parameters.put("description",
                description != null ? GSON_CONVERTER.toJsonElement(description).getAsString() : JsonNull.INSTANCE);

        parameters.put("location_world", worldUUID.toString());
        parameters.put("location_x", String.valueOf(x));
        parameters.put("location_y", String.valueOf(y));
        parameters.put("location_z", String.valueOf(z));

        parameters.put("stack_size", String.valueOf(stackSize));
        parameters.put("sell_price", String.valueOf(sellPrice));
        parameters.put("buy_limit", String.valueOf(buyLimit));
        parameters.put("buy_price", String.valueOf(buyPrice));

        return parameters;
    }

    @Override
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

        if (description != null) {
            lines.add(description);
        }

        return lines;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public ItemStack item() {
        return itemStack;
    }

    @Override
    public Component renderItem() {
        return MessageManager.getItemStackComponent(itemStack);
    }

    @Override
    public CompletableFuture<Shop> item(ItemStack item) {
        this.itemStack = item;

        return update();
    }

    @Override
    public UUID getOwnerUUID() {
        checkState(!adminShop, "shop is an admin shop");
        return ownerUuid;
    }

    @Override
    public OfflinePlayer getOwner() {
        checkState(!adminShop, "shop is an admin shop");
        return Bukkit.getOfflinePlayer(ownerUuid);
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

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

    @Override
    public List<ShopMember> getMembers() {
        return members.stream().map(ShopMember::inter).toList();
    }

    @Override
    public CompletableFuture<Shop> addMember(UUID uuid) {
        checkState(!isMember(uuid), "player is already a member");

        final ServerShopMember memberToAdd = new ServerShopMember(this, uuid);
        new ShopAddMemberEvent(this, memberToAdd, !Bukkit.isPrimaryThread()).callEvent();

        members.add(memberToAdd);
        return update();
    }

    @Override
    public CompletableFuture<Shop> removeMember(OfflinePlayer player) {
        return removeMember(player.getUniqueId());
    }

    @Override
    public CompletableFuture<Shop> removeMember(UUID uuid) {
        checkState(isMember(uuid), "player is not a member");

        members.removeIf(member -> {
            final boolean equals = member.getUUID().equals(uuid);

            if (equals) {
                new ShopRemoveMemberEvent(this, member, !Bukkit.isPrimaryThread()).callEvent();
            }

            return equals;
        });

        return update();
    }

    @Override
    public CompletableFuture<Shop> quantity(int amount) {
        checkArgument(amount > 0, "amount must be greater than 0");

        final ShopEditQuantityEvent event = new ShopEditQuantityEvent(this, amount, !Bukkit.isPrimaryThread());

        event.callEvent();

        this.stackSize = event.getNewQuantity();
        return update();
    }

    @Override
    public double sellPrice() {
        return sellPrice;
    }

    @Override
    public int quantity() {
        return stackSize;
    }

    @Override
    public Component renderSellPrice() {
        return Component.text(sellPrice, MessageManager.VARIABLE_VALUE)
                .append(Component.space())
                .append(currency.getDisplayName());
    }

    @Override
    public CompletableFuture<Shop> sellPrice(double price) {
        checkArgument(price > 0, "price must be greater than 0");

        final ShopChangeSellPriceEvent event = new ShopChangeSellPriceEvent(this, price, !Bukkit.isPrimaryThread());
        event.callEvent();

        this.sellPrice = price;
        return update();
    }

    @Override
    public double buyPrice() {
        return buyPrice;
    }

    @Override
    public Component renderBuyPrice() {
        return Component.text(buyPrice, MessageManager.VARIABLE_VALUE)
                .append(Component.space())
                .append(currency.getDisplayName());
    }

    @Override
    public CompletableFuture<Shop> buyPrice(double price) {
        checkArgument(price > 0, "price must be greater than 0");

        final ShopChangeBuyPriceEvent event = new ShopChangeBuyPriceEvent(this, price, !Bukkit.isPrimaryThread());
        event.callEvent();

        this.buyPrice = event.getNewPrice();
        return update();
    }

    @Override
    public int buyLimit() {
        return buyLimit;
    }

    @Override
    public CompletableFuture<Shop> buyLimit(int limit) {
        checkArgument(limit > 0, "limit must be greater than 0");

        this.buyLimit = limit;
        return update();
    }

    @Override
    public UUID getWorldUUID() {
        return worldUUID;
    }

    @Override
    public Optional<World> getWorld() {
        return Optional.ofNullable(Bukkit.getWorld(worldUUID));
    }

    @Override
    public void lock(Player player) {
        this.locked = true;
        this.lockedByPlayer = player;
    }

    @Override
    public void unlock() {
        this.locked = false;
        this.lockedByPlayer = null;
    }

    @Override
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

    @Override
    public Optional<Component> description() {
        return Optional.ofNullable(description);
    }

    @Override
    public CompletableFuture<Shop> description(Component description) {
        final Component originDescription =
                description != null && !LEGACY_COMPONENT_SERIALIZER.serialize(description).isEmpty() ? description :
                        null;
        final ShopChangeDescriptionEvent shopChangeDescriptionEvent =
                new ShopChangeDescriptionEvent(this, originDescription, !Bukkit.isPrimaryThread());

        this.description = shopChangeDescriptionEvent.getNewDescription().orElse(null);
        return update();
    }

    @Override
    public CompletableFuture<Shop> description(String description) {
        return description(description.isEmpty() ? LEGACY_COMPONENT_SERIALIZER.deserialize(description) : null);
    }

    @Override
    public CompletableFuture<Boolean> sell(Player player, int amountOfSellingItem) {
        checkNotNull(player, "player");
        checkArgument(amountOfSellingItem > 0, "amountOfSellingItem must be greater than 0");
        checkState(!isDeleting(), "shop is currently deleting");

        if (!isSelling()) {
            player.sendMessage(MessageManager.getShopNotSellingComponent());
            return CompletableFuture.completedFuture(false);
        }

        if (amount < (amountOfSellingItem * stackSize)) {
            return CompletableFuture.completedFuture(false);
        }

        final ShopItemSellEvent event =
                new ShopItemSellEvent(this, player, itemStack, amountOfSellingItem, !Bukkit.isPrimaryThread());

        if (!event.callEvent()) {
            event.applyCancelled(player);
            return CompletableFuture.completedFuture(false);
        }

        final int finalAmount = event.getBoughtAmount();
        final int totalItems = finalAmount * stackSize;
        final InventoryItemsTransfer itemTransfer =
                new InventoryItemsTransfer(item(), totalItems, player.getInventory());

        List<ItemStack> leftOvers = itemTransfer.addItems();
        int leftOverAmount = leftOvers.stream().mapToInt(ItemStack::getAmount).sum();
        int itemsToTransfer = totalItems - leftOverAmount;

        // Items to transfer now equals the addable amount - the left over amount which could not be added including the stack size of the item
        if (itemsToTransfer % stackSize != 0) {
            int leftOverModulo = itemsToTransfer % stackSize;
            itemsToTransfer = itemsToTransfer - leftOverModulo;
        }

        final int finalItemsToTransfer = itemsToTransfer;
        // Charge player
        return executeSellTransaction(player, sellPrice * finalItemsToTransfer).thenComposeAsync(transactionResult -> {
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

            return decreaseAmount(player.getUniqueId(), finalItemsToTransfer).thenApplyAsync(__ -> {
                Bukkit.getScheduler().runTask(BukkitMain.getInstance(), () -> {
                    final ItemStack itemStack = item();

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
                        owner.sendMessage(
                                MessageManager.getShopBoughtAmountOwnerComponent(player, itemStack, totalItems));
                    }
                });

                // Transfer items
                itemTransfer.commit(finalItemsToTransfer).forEach(leftOver -> {
                    final Location playerLocation = player.getLocation();
                    final World playerWorld = player.getWorld();

                    playerWorld.dropItem(playerLocation, leftOver, item -> {
                        item.setOwner(player.getUniqueId());
                        item.setThrower(player.getUniqueId());
                    });
                });

                return true;
            });
        });
    }

    /**
     * {@inheritDoc}
     *
     * @param buyFrom            {@inheritDoc}
     * @param amountOfBuyingItem {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public CompletableFuture<Boolean> buy(Player buyFrom, int amountOfBuyingItem) {
        checkNotNull(buyFrom, "buyFrom");
        checkArgument(amountOfBuyingItem > 0, "amountOfBuyingItem must be greater than 0");

        if (!isBuying()) {
            buyFrom.sendMessage(MessageManager.getShopNotBuyingComponent());
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.completedFuture(false); // TODO: Implement
    }

    private CompletableFuture<TransactionAddResult> executeSellTransaction(Player buyer, double price) {
        final TransactionPlayer buyerTransactionPlayer = TransactionApi.getTransactionPlayer(buyer.getUniqueId(), true);
        final Transaction buyerTransaction =
                TransactionApi.createTransaction(ownerUuid, buyerTransactionPlayer.uuid(), this.currency,
                        BigDecimal.valueOf(-price));

        buyerTransaction.setTransactionData(new ShopTransactionData(this));
        final CompletableFuture<TransactionAddResult> transactionResult =
                buyerTransactionPlayer.addTransaction(buyerTransaction);

        if (!adminShop) {
            final TransactionPlayer ownerTransactionPlayer = TransactionApi.getTransactionPlayer(ownerUuid, false);
            final Transaction ownerTransaction =
                    TransactionApi.createTransaction(buyer.getUniqueId(), ownerTransactionPlayer.uuid(), this.currency,
                            BigDecimal.valueOf(price));

            ownerTransaction.setTransactionData(new ShopTransactionData(this));

            transactionResult.thenComposeAsync(buyerTransactionResult -> {
                if (buyerTransactionResult == TransactionAddResult.SUCCESS) {
                    return ownerTransactionPlayer.addTransaction(ownerTransaction)
                            .thenApplyAsync(ownerTransactionResult -> buyerTransactionResult);
                }

                return CompletableFuture.completedFuture(buyerTransactionResult);
            });
        }

        return transactionResult;
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
                .add("quantity", stackSize)
                .add("quantity", sellPrice)
                .add("members", members)
                .add("locked", locked)
                .add("lockedByPlayer", lockedByPlayer)
                .add("deleting", deleting)
                .add("isAdminShop", adminShop)
                .add("buyAmount", buyLimit)
                .add("buyPrice", buyPrice)
                .add("currency", currency)
                .add("description", description)
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
     *
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     *
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
        return adminShop;
    }
}
