package dev.slne.surf.shop.server.api;

import com.google.gson.InstanceCreator;
import dev.slne.data.api.gson.GsonConverter;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.shop.ServerShop;
import org.bukkit.inventory.ItemStack;

import dev.slne.surf.shop.server.api.gson.ItemStackGsonAdapter;

import java.lang.reflect.Type;

public class BukkitGsonConverter extends GsonConverter {

    /**
     * A new {@link BukkitGsonConverter} instance
     */
    public BukkitGsonConverter() {
        super();

        builder.registerTypeAdapter(ItemStack.class, new ItemStackGsonAdapter());
        builder.registerTypeAdapter(Shop.class, (InstanceCreator<Shop>) type -> new ServerShop());
    }
}
