package dev.slne.surf.shop.server.api;

import com.google.gson.InstanceCreator;
import dev.slne.data.api.gson.GsonConverter;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.transaction.ShopTransaction;
import dev.slne.surf.shop.server.api.gson.ItemStackGsonAdapter;
import dev.slne.surf.shop.server.shop.ServerShop;
import dev.slne.surf.shop.server.shop.transaction.ServerShopTransaction;
import dev.slne.transaction.api.TransactionApi;
import dev.slne.transaction.api.currency.Currency;
import org.bukkit.inventory.ItemStack;

public class BukkitGsonConverter extends GsonConverter { // TODO: 25.01.2024 21:16 - remove

    /**
     * A new {@link BukkitGsonConverter} instance
     */
    public BukkitGsonConverter() {
        super();

        builder.registerTypeHierarchyAdapter(ItemStack.class, new ItemStackGsonAdapter());
        builder.registerTypeAdapter(Currency.class,
                TransactionApi.getTransactionInstance().getCurrencyInstanceCreator());
        builder.registerTypeAdapter(Shop.class, (InstanceCreator<Shop>) type -> new ServerShop());
        builder.registerTypeAdapter(ShopTransaction.class, (InstanceCreator<ShopTransaction>) type -> new ServerShopTransaction());

    }
}
