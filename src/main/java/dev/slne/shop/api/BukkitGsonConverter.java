package dev.slne.shop.api;

import org.bukkit.inventory.ItemStack;

import dev.slne.data.core.gson.GsonConverter;
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
