package dev.slne.shop.api;

import dev.slne.data.api.gson.GsonConverter;
import org.bukkit.inventory.ItemStack;

import dev.slne.shop.api.gson.ItemStackGsonAdapter;

public class BukkitGsonConverter extends GsonConverter {

    /**
     * A new {@link BukkitGsonConverter} instance
     */
    public BukkitGsonConverter() {
        super();

        builder.registerTypeAdapter(ItemStack.class, new ItemStackGsonAdapter());
    }

}
