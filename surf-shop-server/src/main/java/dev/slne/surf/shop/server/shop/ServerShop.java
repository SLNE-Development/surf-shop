package dev.slne.surf.shop.server.shop;

import dev.slne.data.api.DataApi;
import dev.slne.data.api.spring.converter.ComponentConverter;
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
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.inventory.InventoryItemsTransfer;
import dev.slne.surf.shop.server.shop.gui._2_0.util.GuiSound;
import dev.slne.surf.shop.server.shop.gui._2_0.util.GuiUtils;
import dev.slne.surf.shop.server.shop.member.ServerShopMember;
import dev.slne.surf.shop.server.shop.transaction.ServerShopTransaction;
import dev.slne.surf.shop.server.spring.converter.CurrencyConverter;
import dev.slne.surf.shop.server.spring.converter.ItemStackConverter;
import dev.slne.surf.shop.server.spring.converter.LocationConverter;
import dev.slne.surf.shop.server.spring.repository.jpa.ShopRepository;
import dev.slne.surf.shop.server.util.ShopUtils;
import dev.slne.transaction.api.TransactionApi;
import dev.slne.transaction.api.currency.Currency;
import dev.slne.transaction.api.player.TransactionPlayer;
import dev.slne.transaction.api.transaction.Transaction;
import dev.slne.transaction.api.transaction.fee.TransactionFee;
import dev.slne.transaction.api.transaction.result.TransactionAddResult;
import jakarta.persistence.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.*;
import static org.springframework.util.Assert.hasText;

@Entity(name = "Shop")
@Table(name = "shops")
public class ServerShop implements Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    private Long id = -1L;

    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    @JdbcTypeCode(SqlTypes.CHAR)
    @NotNull
    private UUID uuid;

    @Convert(converter = CurrencyConverter.class)
    @Column(name = "currency_id", nullable = false)
    @JdbcTypeCode(SqlTypes.BIGINT)
    @NotNull
    private Currency currency;

    @Column(name = "admin_shop", nullable = false, length = 1)
    @JdbcTypeCode(SqlTypes.BIT)
    private boolean adminShop = false;

    @Column(name = "owner_uuid")
    @JdbcTypeCode(SqlTypes.CHAR)
    @Nullable
    private UUID ownerUuid;

    @Column(name = "shop_itemstack")
    @Lob
    @JdbcTypeCode(SqlTypes.BLOB)
    @Convert(converter = ItemStackConverter.class)
    @Nullable
    private ItemStack shopItemstack;

    @Column(name = "stack_size", nullable = false, length = 2)
    @JdbcTypeCode(SqlTypes.INTEGER)
    @Range(from = 1, to = 64)
    private int stackSize = 1;

    @Column(name = "location")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Convert(converter = LocationConverter.class)
    @NotNull
    private Location location;

    @Column(name = "server")
    @JdbcTypeCode(SqlTypes.CHAR)
    @NotNull
    private String server;

    @Column(name = "sell_price", nullable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    @Range(from = 0, to = (long) Shop.MAX_SELL_PRICE)
    private double sellPrice = -1;

    @Column(name = "buy_limit", nullable = false)
    @JdbcTypeCode(SqlTypes.INTEGER)
    private int buyLimit = 0;

    @Column(name = "buy_price", nullable = false)
    @JdbcTypeCode(SqlTypes.DOUBLE)
    private double buyPrice = -1;

    @Convert(converter = ComponentConverter.class)
    @Column(name = "description")
    @JdbcTypeCode(SqlTypes.LONGNVARCHAR)
    @Nullable
    private Component description;

    @OneToMany(mappedBy = "serverShop", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ServerShopMember> members = new LinkedHashSet<>();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ServerShopTransaction> transactions = new LinkedHashSet<>();

    private transient @Nullable Player lockedByPlayer;
    private transient boolean deleting = false;
    private transient boolean deleted = false;
    private transient boolean locked = false;

    public boolean getAdminShop() {
        return adminShop;
    }

    protected ServerShop() {
        // JPA
    }

    /**
     * A new {@link ServerShop} with an owner
     *
     * @param currency  the currency
     * @param owner     the owner
     * @param itemStack the itemstack
     * @param location  the location
     * @param server    the server
     */
    public ServerShop(@NotNull Currency currency,
                      @NotNull UUID owner,
                      @Nullable ItemStack itemStack,
                      @NotNull Location location,
                      @NotNull String server) {

        checkNotNull(currency, "currency");
        checkNotNull(owner, "owner");
        checkNotNull(server, "server");
        checkNotNull(location, "location");
        hasText(server, "server cannot be empty");

        this.ownerUuid = owner;
        this.currency = currency;
        this.shopItemstack = itemStack;
        this.location = location;
        this.server = server;

        this.uuid = UUID.randomUUID();
        this.locked = false;
    }

    /**
     * A new {@link ServerShop} with an owner and the current server where the instance is created on
     *
     * @param currency  the currency
     * @param owner     the owner
     * @param itemStack the itemstack
     * @param location  the location
     */
    public ServerShop(@NotNull Currency currency,
                      @NotNull UUID owner,
                      @Nullable ItemStack itemStack,
                      @NotNull Location location) {

        this(currency, owner, itemStack, location, DataApi.getDataInstance().getServerName());
    }

    /**
     * A new {@link ServerShop} with no owner (also known as an admin shop)
     *
     * @param currency  the currency
     * @param itemStack the itemstack
     * @param location  the location
     * @param server    the server
     */
    public ServerShop(@NotNull Currency currency,
                      @Nullable ItemStack itemStack,
                      @NotNull Location location,
                      @NotNull String server) {

        checkNotNull(currency, "currency");
        checkNotNull(location, "location");
        checkNotNull(server, "server");

        this.currency = currency;
        this.shopItemstack = itemStack;
        this.location = location;
        this.server = server;

        this.uuid = UUID.randomUUID();
        this.locked = false;
        this.adminShop = true;
    }

    /**
     * A new {@link ServerShop} with no owner (also known as an admin shop) and the current server where the instance is
     * created on
     *
     * @param currency  the currency
     * @param itemStack the itemstack
     * @param location  the location
     */
    public ServerShop(@NotNull Currency currency,
                      @Nullable ItemStack itemStack,
                      @Nullable Location location) {

        this(currency, itemStack, location, DataApi.getDataInstance().getServerName());
    }

    /**
     * Returns all shops from the database
     *
     * @return the shops
     */
    public static CompletableFuture<List<Shop>> shops() {
        return CompletableFuture.supplyAsync(() -> {
            final List<ServerShop> all = ShopApi.getContext().getBean(ShopRepository.class).findAll();

            return all.stream()
                    .filter(serverShop -> serverShop.server.equals(DataApi.getDataInstance().getServerName()))
                    .map(ServerShop::inter)
                    .collect(Collectors.toList());
        }).exceptionally(exception -> {
            DataApi.getDataInstance().logError(ServerShop.class, "Failed to fetch shops", exception);
            return null;
        });
    }


    @Override
    public CompletableFuture<Shop> save() {
        checkNotDeleted();
        return CompletableFuture.supplyAsync(() -> ShopApi.getContext().getBean(ShopRepository.class).save(this).inter())
                .exceptionally(exception -> {
                    DataApi.getDataInstance().logError(getClass(), "Failed to save shop: " + uuid.toString(), exception);
                    return null;
                });
    }

    @Override
    public CompletableFuture<Shop> delete() {
        checkNotDeleted();
        checkState(!deleting, "shop is already deleting");

        deleting = true;

        return CompletableFuture.supplyAsync(() -> {
                    ShopApi.getContext().getBean(ShopRepository.class).delete(this);
                    deleting = false;
                    deleted = true;

                    return this.inter();
                })
                .exceptionally(exception -> {
                    DataApi.getDataInstance().logError(getClass(), "Failed to delete shop: " + uuid.toString(), exception);
                    deleting = false;
                    return null;
                });
    }

    @Override
    public CompletableFuture<ShopTransactionResult> decreaseAmount(@Nullable UUID remover, int amount) {
        checkNotDeleted();
        checkArgument(amount > 0, "amount must be greater than 0");

        return ShopApi.getInstance().createShopTransaction(this, remover, -amount).execute();
    }

    @Override
    public CompletableFuture<ShopTransactionResult> increaseAmount(@Nullable UUID adder, int amount) {
        checkNotDeleted();
        checkArgument(amount > 0, "amount must be greater than 0");

        return ShopApi.getInstance().createShopTransaction(this, adder, amount).execute();
    }

    @Override
    public Set<ShopTransaction> getTransactions() {
        return transactions.stream().map(ShopTransaction::inter).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public List<Component> getShopLines() {
        final List<Component> lines = new ArrayList<>();

        if (!adminShop) {
            final OfflinePlayer owner = getOwner();

            if (owner.getName() != null) {
                lines.add(ShopUtils.getOfflineDisplayName(owner));
            }
        }

        if (shopItemstack != null) {
            final TextComponent.Builder builder = Component.text();

            if (adminShop) {
                builder.append(Component.text(INFINITE_CHAR, MessageManager.VARIABLE_VALUE));
            } else {
                builder.append(Component.text(amount() + "x", MessageManager.VARIABLE_VALUE));
            }

            builder.appendSpace();
            builder.append(shopItemstack.displayName().colorIfAbsent(MessageManager.VARIABLE_VALUE));

            lines.add(builder.build());
        }

        if (description != null && ShopUtils.hasComponentText(description)) {
            lines.add(description);
        }

        return lines;
    }

    /**
     * Gets the amount of items that are in the shop
     *
     * @return the amount of items
     */
    @Override
    public int amount() {
        return transactions.stream().mapToInt(ShopTransaction::getTransactionAmount).sum();
    }

    /**
     * Gets the currency of this shop
     *
     * @return the currency
     */
    @Override
    public Currency currency() {
        return currency;
    }

    /**
     * Sets the currency of this shop
     *
     * @param currency the currency
     * @return the shop
     */
    @Override
    public CompletableFuture<Shop> currency(@NotNull Currency currency) {
        checkNotDeleted();
        checkNotNull(currency, "currency");

        this.currency = currency;
        return save();
    }

    @Override
    public long getId() {
        checkNotDeleted();

        return id;
    }

    @Override
    public Optional<ItemStack> item() {
        return Optional.ofNullable(shopItemstack).map(ItemStack::clone);
    }

    @Override
    public Component renderItem() {
        return item().map(MessageManager::getItemStackComponent).orElse(Component.empty());
    }

    @Override
    public Component renderItemDisplayName() {
        return item().map(ItemStack::displayName).orElse(Component.empty());
    }

    @Override
    public CompletableFuture<Shop> item(ItemStack item) {
        checkNotDeleted();

        this.shopItemstack = item;

        return save();
    }

    @Override
    public UUID getOwnerUUID() {
        checkState(!adminShop, "shop is an admin shop");

        return ownerUuid;
    }

    @Override
    public OfflinePlayer getOwner() {
        checkState(!adminShop, "shop is an admin shop");
        assert ownerUuid != null : "This shop is not an admin shop therefore it must have an owner";

        return Bukkit.getOfflinePlayer(ownerUuid);
    }

    @Override
    public UUID getUuid() {
        checkNotDeleted();

        return uuid;
    }

    @Override
    public boolean isInventoryEmpty() {
        return amount() == 0;
    }

    @Override
    public Optional<Player> getLockedBy() {
        checkNotDeleted();

        return Optional.ofNullable(lockedByPlayer);
    }

    @Override
    public void setLockedBy(Player player) {
        checkNotDeleted();

        this.lockedByPlayer = player;
    }

    @Override
    public boolean locked() {
        return locked;
    }

    @Override
    public void locked(boolean locked) {
        checkNotDeleted();

        this.locked = locked;
    }

    @Override
    public List<ShopMember> getMembers() {
        return members.stream().map(ShopMember::inter).toList();
    }

    public ServerShopMember getMember(UUID uuid) {
        return members.stream().filter(member -> member.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    /**
     * Adds a member to this shop
     *
     * @param player the player
     * @return the shop
     */
    @Override
    public CompletableFuture<Shop> addMember(OfflinePlayer player) {
        checkNotDeleted();

        return addMember(player.getUniqueId());
    }

    @Override
    public CompletableFuture<Shop> addMember(UUID uuid) {
        checkNotDeleted();
        checkState(!isMember(uuid), "player is already a member");

        final ServerShopMember memberToAdd = new ServerShopMember(this, uuid);
        new ShopAddMemberEvent(this, memberToAdd).callEvent();

        return memberToAdd.save().thenApplyAsync(__ -> this); // TODO: 02.11.2023 Do we need to add the member to the list?
    }

    @Override
    public CompletableFuture<Shop> removeMember(OfflinePlayer player) {
        checkNotDeleted();

        return removeMember(player.getUniqueId());
    }

    @Override
    public CompletableFuture<Shop> removeMember(UUID uuid) {
        checkNotDeleted();
        checkState(isMember(uuid), "player is not a member");
        final ServerShopMember shopMember = getMember(uuid);
        new ShopRemoveMemberEvent(this, shopMember, !Bukkit.isPrimaryThread()).callEvent();

        return shopMember.delete().thenApplyAsync(__ -> this); // TODO: 02.11.2023 Do we need to remove the member from the list?
    }

    @Override
    public CompletableFuture<Shop> quantity(int amount) {
        checkArgument(amount > 0, "amount must be greater than 0");

        final ShopEditQuantityEvent event = new ShopEditQuantityEvent(this, amount, !Bukkit.isPrimaryThread());

        event.callEvent();

        this.stackSize = event.getNewQuantity();
        return save();
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
        System.err.println(currency);

        return Component.text(sellPrice, MessageManager.VARIABLE_VALUE)
                .append(Component.space())
                .append(currency.getDisplayName());
    }

    @Override
    public CompletableFuture<Shop> sellPrice(double price) {
        checkArgument(price >= -1, "price must be greater than -1");
        checkArgument(price <= MAX_SELL_PRICE, "price must be less than or equal to " + MAX_SELL_PRICE);

        final ShopChangeSellPriceEvent event = new ShopChangeSellPriceEvent(this, price, !Bukkit.isPrimaryThread());
        event.callEvent();

        this.sellPrice = price;
        return save();
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
        checkArgument(price >= -1, "price must be greater than -1");
        checkArgument(price <= MAX_BUY_PRICE, "price must be less than or equal to " + MAX_BUY_PRICE);

        final ShopChangeBuyPriceEvent event = new ShopChangeBuyPriceEvent(this, price, !Bukkit.isPrimaryThread());
        event.callEvent();

        this.buyPrice = event.getNewPrice();
        return save();
    }

    @Override
    public int buyLimit() {
        return buyLimit;
    }

    @Override
    public CompletableFuture<Shop> buyLimit(int limit) {
        checkArgument(limit > 0, "limit must be greater than 0");

        this.buyLimit = limit;
        return save();
    }

    @Override
    public UUID getWorldUUID() {
        return location.getWorld().getUID();
    }

    @Override
    public Optional<World> getWorld() {
        return Optional.ofNullable(Bukkit.getWorld(getWorldUUID()));
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

    /**
     * Returns if the given player is the owner of this shop
     *
     * @param player the player
     * @return true if owner
     */
    @Override
    public boolean isOwner(OfflinePlayer player) {
        return isOwner(player.getUniqueId());
    }

    /**
     * Returns if the given uuid is the owner of this shop
     *
     * @param uuid the uuid
     * @return true if owner
     */
    @Override
    public boolean isOwner(UUID uuid) {
        return Objects.equals(ownerUuid, uuid);
    }

    /**
     * Returns if the given player is a member of this shop
     *
     * @param player the player
     * @return true if member
     */
    @Override
    public boolean isMember(OfflinePlayer player) {
        return isMember(player.getUniqueId());
    }

    /**
     * Returns if the given uuid is a member of this shop
     *
     * @param uuid the uuid
     * @return true if member
     */
    @Override
    public boolean isMember(UUID uuid) {
        return getMembers().stream().anyMatch(member -> Objects.equals(member.getUuid(), uuid));
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
        final Component originDescription = description != null && ShopUtils.hasComponentText(description) ? description : null;
        final ShopChangeDescriptionEvent shopChangeDescriptionEvent = new ShopChangeDescriptionEvent(this, originDescription);

        this.description = shopChangeDescriptionEvent.getNewDescription().filter(ShopUtils::hasComponentText).orElse(null);
        return save();
    }

    @Override
    public CompletableFuture<Shop> description(String description) {
        return description(StringUtils.hasText(description) ? getMiniMessage().deserialize(description) : null);
    }

    @Override
    public CompletableFuture<Boolean> sell(Player player, int amountOfSellingItem) {
        checkNotDeleted();
        checkNotNull(player, "player");
        checkArgument(amountOfSellingItem > 0, "amountOfSellingItem must be greater than 0");
        checkState(!isDeleting(), "shop is currently deleting");

        if (!isSelling()) {
            player.sendMessage(MessageManager.getShopNotSellingComponent());
            return CompletableFuture.completedFuture(false);
        }

        if (!canSell(amountOfSellingItem)) {
            return CompletableFuture.completedFuture(false);
        }

        if (shopItemstack == null) {
            return CompletableFuture.completedFuture(false);
        }

        final ShopItemSellEvent event = new ShopItemSellEvent(this, player, shopItemstack, amountOfSellingItem);

        if (!event.callEvent()) {
            event.applyCancelled(player);
            return CompletableFuture.completedFuture(false);
        }

        final int finalAmount = event.getBoughtAmount();
        final int totalItems = finalAmount * stackSize;
        final InventoryItemsTransfer itemTransfer = new InventoryItemsTransfer(shopItemstack.clone(), totalItems, player.getInventory());

        final List<ItemStack> leftOvers = itemTransfer.addItems();
        final int leftOverAmount = leftOvers.stream().mapToInt(ItemStack::getAmount).sum();
        int itemsToTransfer = totalItems - leftOverAmount;
        final int leftOverModulo = leftOverAmount % stackSize;

        // Items to transfer now equals the addable amount - the left over amount which could not be added including the stack size of the item
        if (leftOverModulo != 0) {
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
                    if (shopItemstack == null) {
                        return;
                    }

                    final Player playerNow = Bukkit.getPlayer(player.getUniqueId());

                    if (playerNow != null) {
                        playerNow.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                        playerNow.sendMessage(MessageManager.getShopBoughtAmountBuyerComponent(shopItemstack, totalItems));
                    }

                    final Player owner = getOwner().getPlayer();
                    if (owner != null && owner.isOnline()) {
                        owner.sendMessage(MessageManager.getShopBoughtAmountOwnerComponent(player, shopItemstack, totalItems));
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
     * Tests if the shop can sell the specified amount of items
     *
     * @param amountOfSellingItem the amount of items
     * @return true if the shop can sell the items (has enough items in stock) otherwise false
     */
    @Override
    public boolean canSell(int amountOfSellingItem) {
        checkArgument(amountOfSellingItem > 0, "amountOfSellingItem must be greater than 0");

        if (!isSelling()) {
            return false;
        }

        if (isAdminShop()) {
            return true;
        }

        return amount() >= (amountOfSellingItem * stackSize);
    }

    /**
     * {@inheritDoc}
     *
     * @param buyFrom            {@inheritDoc}
     * @param amountOfBuyingItem {@inheritDoc}
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

    /**
     * Tests if the shop can buy the specified amount of items
     *
     * @param amountOfBuyingItem the amount of items
     * @return true if the shop can buy the items (has enough money) otherwise false
     */
    @Override
    public boolean canBuy(int amountOfBuyingItem) {
        checkArgument(amountOfBuyingItem > 0, "amountOfBuyingItem must be greater than 0");

        if (!isBuying()) {
            return false;
        }

        return true; // TODO: Implement
    }

    private CompletableFuture<TransactionAddResult> executeSellTransaction(Player buyer, double price) {
        final TransactionPlayer buyerTransactionPlayer = TransactionApi.getTransactionPlayer(buyer.getUniqueId(), true);
        final Transaction buyerTransaction =
                TransactionApi.getTransactionInstance().createTransaction(ownerUuid, buyerTransactionPlayer.uuid(), this.currency,
                        BigDecimal.valueOf(-price));

        buyerTransaction.setTransactionData(new ShopTransactionData(this));
        final CompletableFuture<TransactionAddResult> transactionResult = buyerTransactionPlayer.addTransaction(buyerTransaction);

        if (!adminShop) {
            final TransactionPlayer ownerTransactionPlayer = TransactionApi.getTransactionPlayer(ownerUuid, false);
            final Transaction ownerTransaction =
                    TransactionApi.createTransaction(buyer.getUniqueId(), ownerTransactionPlayer.uuid(), this.currency,
                            BigDecimal.valueOf(price));

            ownerTransaction.setTransactionFeeAmountPercentage(TransactionFee.SHOP_PURCHASE.getFeePercentage()); // TODO: 03.11.2023
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
     * Returns the mini message instance
     *
     * @return the mini message
     */
    @Override
    public MiniMessage getMiniMessage() {
        return ShopUtils.DEFAULT_MINI_MESSAGE_BUILDER
                .editTags(builder ->
                        ReflectionUtils.doWithFields( // Get all fields of ShopTags
                                ShopTags.class,
                                field -> ((ShopTags.ShopTag) field.get(null)).resolve(builder, this), // Resolve the tag
                                field -> field.getType().isAssignableFrom(ShopTags.ShopTag.class))) // Filter all fields which are assignable from ShopTag
                .build();
    }

    /**
     * Gets the block x value for this shop
     *
     * @return the block x value
     */
    @Override
    public int blockX() {
        return location.blockX();
    }

    /**
     * Gets the block x value for this shop
     *
     * @return the block x value
     */
    @Override
    public int blockY() {
        return location.blockY();
    }

    /**
     * Gets the block x value for this shop
     *
     * @return the block x value
     */
    @Override
    public int blockZ() {
        return location.blockZ();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
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
        return Integer.compare(amount(), o.amount());
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

    private void checkNotDeleted() {
        checkState(!deleted, "shop is deleted");
    }
}
