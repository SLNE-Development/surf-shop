package dev.slne.surf.shop.server.instance;

import dev.slne.data.api.DataApi;
import dev.slne.surf.shop.api.ShopApi;
import dev.slne.surf.shop.api.instance.ShopInstance;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.transaction.ShopTransaction;
import dev.slne.surf.shop.api.shop.visualizer.VisualizerSettings;
import dev.slne.surf.shop.server.BukkitMain;
import dev.slne.surf.shop.server.api.BukkitGsonConverter;
import dev.slne.surf.shop.server.command.BukkitCommandManager;
import dev.slne.surf.shop.server.listener.BukkitListenerManager;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.ServerShop;
import dev.slne.surf.shop.server.shop.ServerShopManager;
import dev.slne.surf.shop.server.shop.transaction.ServerShopTransaction;
import dev.slne.surf.shop.server.shop.visualizer.setting.VisualizerSettingsImpl;
import dev.slne.surf.shop.server.spring.ShopApplication;
import dev.slne.surf.shop.server.util.Permissions;
import dev.slne.surf.shop.server.util.ShopUtils;
import dev.slne.surf.shop.server.util.UuidDataType;
import dev.slne.transaction.api.currency.Currency;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.*;

public class BukkitInstance implements ShopInstance {

    private final ClassLoader classLoader;
    private final VisualizerSettings visualizerSettings;
    @Getter
    private ConfigurableApplicationContext context;
    private BukkitCommandManager commandManager;

    /**
     * The {@link BukkitListenerManager} instance
     */
    @Getter
    private BukkitListenerManager listenerManager;

    /**
     * The {@link ServerShopManager} instance
     */
    @Getter
    private ServerShopManager shopManager;
    @Getter
    private BukkitGsonConverter gsonConverter;

    public BukkitInstance(ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.visualizerSettings = new VisualizerSettingsImpl();
    }

    /**
     * Called when the plugin is loaded
     */
    @Override
    public void onLoad() {
        new ShopApi(this);

        context = ShopApplication.run(classLoader);

        commandManager = new BukkitCommandManager();
        listenerManager = new BukkitListenerManager();
        gsonConverter = new BukkitGsonConverter();

        Permissions.invoke();

        shopManager = new ServerShopManager();
        shopManager.onLoad();
    }

    /**
     * Called when the plugin is enabled
     */
    @Override
    public void onEnable() {
        commandManager.registerCommands();
        listenerManager.registerListeners();

        shopManager.onEnable();
    }

    /**
     * Called when the plugin is disabled
     */
    public void onDisable() {
        listenerManager.unregisterListeners();

        shopManager.onDisable();
        context.close();
    }

    @Override
    public CompletableFuture<Shop> createShop(@NotNull Currency currency,
                                              @NotNull Player owner,
                                              ItemStack itemStack,
                                              @NotNull Location location) {
        checkNotNull(currency, "Currency cannot be null");
        checkNotNull(owner, "Owner cannot be null");

        final BlockState blockState = location.getBlock().getState();

        checkArgument(blockState instanceof Chest, "Location is not a chest");

        final ServerShop shop = new ServerShop(currency, owner.getUniqueId(), itemStack, location);


        return shop.create().thenComposeAsync(created -> {
            if (created == null) {
                owner.sendMessage(MessageManager.getShopCreatedFailureComponent());
                return null;
            }

            ShopApi.getShopManager().addShop(shop);
            ShopApi.getShopManager().makeShop(((Chest) blockState), shop);

            owner.sendMessage(MessageManager.getShopCreatedSuccessfullyComponent());

            return shop.item(new ItemStack(Material.DIAMOND)).thenComposeAsync(
                    shop1 -> shop.increaseAmount(owner.getUniqueId(), 100_000)
                            .thenApplyAsync(result -> shop.inter())); // TODO: 26.08.2023 remove this
            // line when finished with
            // testing
        }).exceptionally(throwable -> {
            DataApi.getDataInstance().logError(getClass(), "Failed to execute shop", throwable);
            owner.sendMessage(MessageManager.getShopCreatedFailureComponent());
            return null;
        });
    }

    @Override
    public CompletableFuture<Shop> createShop(@NotNull Currency currency, @NotNull Player owner, @Nullable ItemStack itemStack, @Nullable Location location, @NotNull String server) {

    }

    @Override
    public CompletableFuture<Shop> createAdminShop(@NotNull Currency currency, @NotNull ItemStack itemStack, @NotNull Location location) {
        ;
    }

    @Override
    public CompletableFuture<Shop> createAdminShop(@NotNull Currency currency, @NotNull ItemStack itemStack, @NotNull Location location, @NotNull String server) {

    }

    @Override
    public ItemStack constructCreationItem() {
        return ShopUtils.constructCreationItem();
    }

    @Override
    public boolean isShop(Location location) {
        return isShop(location.getBlock());
    }

    @Override
    public boolean isShop(Block block) {
        if (block == null) {
            return false;
        }

        return isShop(block.getState());
    }

    @Override
    public boolean isShop(BlockState blockState) {
        if (!(blockState instanceof Chest chest)) {
            return false;
        }

        return chest.getPersistentDataContainer().has(Shop.CREATED_SHOP_KEY, UuidDataType.UUID);
    }

    @Override
    public boolean isShop(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        if (!itemStack.hasItemMeta()) {
            return false;
        }

        return itemStack.getItemMeta().getPersistentDataContainer()
                .has(ServerShop.CREATION_ITEM_KEY, PersistentDataType.BYTE);
    }

    @Override
    public Shop getShop(@NotNull Location location) {
        return getShop(location.getBlock());
    }

    @Override
    public Shop getShop(@NotNull Block block) {
        checkState(block.getState() instanceof Chest, "Block is not a chest");

        final Chest chest = (Chest) block.getState();
        final PersistentDataContainer dataContainer = chest.getPersistentDataContainer();
        final UUID shopUuid = dataContainer.get(Shop.CREATED_SHOP_KEY, UuidDataType.UUID);

        checkState(shopUuid != null, "Block is not a shop");

        return shopManager.getShop(shopUuid);
    }

    @Override
    public Currency getDefaultCurrency() {
        return BukkitMain.getInstance().getDefaultCurrency();
    }

    @Override
    public List<Currency> getOtherCurrencies() {
        return BukkitMain.getInstance().getOtherCurrencies();
    }

    @Override
    public VisualizerSettings getVisualizerSettings() {
        return visualizerSettings;
    }

    /**
     * Creates a shop transaction with a reason but does not execute it use {@link ShopTransaction#execute()}
     *
     * @param shop   the shop to create the transaction for
     * @param uuid   the sender of the transaction (can be null)
     * @param amount the amount of the transaction
     * @param reason the reason for the transaction (can be null)
     * @return a {@link CompletableFuture} that completes with the created shop transaction
     */
    @Override
    public ShopTransaction createShopTransaction(@NotNull Shop shop, @Nullable UUID uuid, int amount, @Nullable String reason) {
        checkNotNull(shop, "Shop cannot be null");

        return new ServerShopTransaction(shop, uuid, amount, reason);
//        shop.getTransactions().add(transaction); TODO: necessary?
    }

    @Override
    public ShopTransaction createShopTransaction(@NotNull Shop shop, @Nullable UUID uuid, int amount) {
        checkNotNull(shop, "Shop cannot be null");

        return createShopTransaction(shop, uuid, amount, null);
    }
}
