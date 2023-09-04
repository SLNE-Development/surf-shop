package dev.slne.surf.shop.server.api;

import com.google.gson.InstanceCreator;
import dev.slne.data.api.gson.GsonConverter;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.shop.ServerShop;
import dev.slne.transaction.api.TransactionApi;
import dev.slne.transaction.api.currency.Currency;
import org.bukkit.inventory.ItemStack;

import dev.slne.surf.shop.server.api.gson.ItemStackGsonAdapter;

import java.lang.reflect.Type;

public class BukkitGsonConverter extends GsonConverter {

    /**
     * A new {@link BukkitGsonConverter} instance
     */
    public BukkitGsonConverter() {
        super();

        builder.registerTypeHierarchyAdapter(ItemStack.class, new ItemStackGsonAdapter());
        builder.registerTypeAdapter(Currency.class, TransactionApi.getTransactionInstance().getCurrencyInstanceCreator());
        builder.registerTypeAdapter(Shop.class, (InstanceCreator<Shop>) type -> {
            System.out.println("Creating new shop");

            return new ServerShop();
        });

    }
}
