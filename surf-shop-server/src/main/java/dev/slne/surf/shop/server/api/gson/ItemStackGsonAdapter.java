package dev.slne.surf.shop.server.api.gson;

import com.google.gson.*;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;

public class ItemStackGsonAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonNull() || !json.isJsonObject()) {
            return null;
        }

        return ItemStack.deserializeBytes(context.deserialize(json, byte[].class));
    }

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return null;
        }

        return context.serialize(src.serializeAsBytes()); // TODO: 26.08.2023 Test this
    }

}
