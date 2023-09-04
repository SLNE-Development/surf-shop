package dev.slne.surf.shop.server.shop;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.api.BukkitGsonConverter;
import dev.slne.transaction.api.transaction.data.TransactionData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class ShopTransactionData implements TransactionData {
    private static final BukkitGsonConverter CONVERTER = new BukkitGsonConverter();
    private Shop shop;

    public ShopTransactionData(Shop shop) {
        this.shop = shop;
    }

    /**
     * Convert the transaction data to a json string
     *
     * @return The json string
     */
    @Override
    public String toJson() {
        JsonObject object = new JsonObject();

        object.addProperty("shop", CONVERTER.toJson(shop));

        return CONVERTER.toJson(object);
    }

    /**
     * Load the transaction data from a json string
     *
     * @param json The json string
     */
    @Override
    public void fromJson(String json) {
        final JsonObject object = CONVERTER.fromJson(json, JsonObject.class);

        if (object == null) {
            return;
        }

        final JsonElement shopElement = object.get("shop");

        if (shopElement == null) {
            return;
        }

        shop = CONVERTER.fromJson(shopElement.getAsString(), Shop.class);
    }
}
