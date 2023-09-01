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
    private OfflinePlayer buyer;

    public ShopTransactionData(Shop shop, OfflinePlayer buyer) {
        this.shop = shop;
        this.buyer = buyer;
    }

    /**
     * Convert the transaction data to a json string
     *
     * @return The json string
     */
    @Override
    public String toJson() {
        JsonObject object = new JsonObject();

        object.addProperty("buyer_uuid", buyer.getUniqueId().toString());
        object.addProperty("buyer_name", buyer.getName());
        object.addProperty("owner_uuid", shop.getOwner().getUniqueId().toString());
        object.addProperty("owner_name", shop.getOwner().getName());
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

        final JsonElement buyerUuidElement = object.get("buyer_uuid");
        final JsonElement shopElement = object.get("shop");

        if (buyerUuidElement == null || shopElement == null) {
            return;
        }

        shop = CONVERTER.fromJson(shopElement.getAsString(), Shop.class);
        buyer = Bukkit.getOfflinePlayer(UUID.fromString(buyerUuidElement.getAsString()));
    }
}
