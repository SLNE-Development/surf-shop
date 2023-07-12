package dev.slne.shop.api.gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ItemStackGsonAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json.isJsonNull() || !json.isJsonObject()) {
            return null;
        }

        Map<String, Object> serialized = context.deserialize(json, Map.class);

        System.out.println("Deserialized: ");
        System.out.println(serialized);

        return ItemStack.deserialize(serialized);
    }

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return null;
        }

        Map<String, Object> serialized = src.serialize();
        Map<String, Object> toSave = new HashMap<>();

        for (Entry<String, Object> entry : serialized.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof ItemMeta itemMeta) {
                toSave.put(key, itemMeta.serialize());
            } else {
                toSave.put(key, value);
            }
        }

        System.out.println("Serialized: ");
        System.out.println(toSave);

        return context.serialize(toSave);
    }

}
