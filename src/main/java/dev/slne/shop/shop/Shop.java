package dev.slne.shop.shop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import dev.slne.data.core.instance.DataApi;
import dev.slne.data.core.web.WebRequest;
import dev.slne.shop.api.API;
import dev.slne.shop.api.BukkitGsonConverter;
import dev.slne.shop.api.buffer.ItemBuffer;
import dev.slne.shop.shop.member.ShopMember;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Shop {

    /**
     * The shop key
     */
    public static final NamespacedKey SHOP_KEY = new NamespacedKey("slne", "shop");

    @SerializedName("id")
    private long id;

    @SerializedName("uuid")
    private UUID uuid;

    @SerializedName("owner_uuid")
    private UUID ownerUuid;

    @SerializedName("shop_itemstack")
    private ItemStack itemStack;

    @SerializedName("shop_amount")
    private int amount;

    @SerializedName("location_world")
    private String worldName;

    @SerializedName("location_x")
    private int x;

    @SerializedName("location_y")
    private int y;

    @SerializedName("location_z")
    private int z;

    @SerializedName("sell_amount")
    private int sellAmount;

    @SerializedName("sell_price")
    private double sellPrice;

    @SerializedName("members")
    private List<ShopMember> members;

    /**
     * A new {@link Shop} instance
     *
     * @param owner     the owner
     * @param itemStack the itemstack
     * @param location  the location
     */
    public Shop(UUID owner, ItemStack itemStack, Location location) {
        this.ownerUuid = owner;

        if (itemStack != null) {
            this.itemStack = itemStack;
        }

        if (location != null) {
            this.worldName = location.getWorld().getName();
            this.x = location.getBlockX();
            this.y = location.getBlockY();
            this.z = location.getBlockZ();
        }

        this.uuid = UUID.randomUUID();
        this.members = new ArrayList<>();
    }

    /**
     * Returns all shops from the database
     *
     * @return the shops
     */
    public static CompletableFuture<List<Shop>> shops() {
        WebRequest request = WebRequest.builder().json(true).url(API.SHOPS).build();
        List<Shop> shops = new ArrayList<>();

        return request.executeGet().thenApplyAsync(response -> {
            int statusCode = response.getStatusCode();

            if (!(statusCode >= 200 || statusCode < 300)) {
                DataApi.getDataInstance().logError(Shop.class, "Failed to fetch shops");
                DataApi.getDataInstance().logError(Shop.class, response.getBody().toString());
                return null;
            }

            BukkitGsonConverter gson = new BukkitGsonConverter();
            Object body = response.getBody();
            String bodyString = body.toString();
            JsonElement bodyElement = gson.fromJson(bodyString, JsonElement.class);

            if (!bodyElement.isJsonObject()) {
                DataApi.getDataInstance().logError(Shop.class, "Failed to fetch shops");
                DataApi.getDataInstance().logError(Shop.class, response.getBody().toString());
                return null;
            }

            JsonObject bodyObject = bodyElement.getAsJsonObject();
            if (!bodyObject.has("data")) {
                DataApi.getDataInstance().logError(Shop.class, "Failed to fetch shops");
                DataApi.getDataInstance().logError(Shop.class, response.getBody().toString());
                return null;
            }

            JsonElement dataElement = bodyObject.get("data");
            if (!dataElement.isJsonArray()) {
                DataApi.getDataInstance().logError(Shop.class, "Failed to fetch shops");
                DataApi.getDataInstance().logError(Shop.class, response.getBody().toString());
                return null;
            }

            JsonArray bodyArray = dataElement.getAsJsonArray();
            for (JsonElement element : bodyArray) {
                Shop shop = fromBodyElement(element, false);

                if (shop == null) {
                    continue;
                }

                shops.add(shop);
            }

            return shops;
        }).exceptionally(exception -> {
            DataApi.getDataInstance().logError(Shop.class, "Failed to fetch shops");
            exception.printStackTrace();
            return null;
        });
    }

    /**
     * Creates a shop in the database
     *
     * @return the shop
     */
    public CompletableFuture<Shop> create() {
        WebRequest request = WebRequest.builder().json(true).parameters(toParameters()).url(API.SHOPS).build();

        return request.executePost().thenApplyAsync(response -> {
            int statusCode = response.getStatusCode();

            if (!(statusCode >= 200 || statusCode < 300)) {
                DataApi.getDataInstance().logError(Shop.class, "Failed to create shop: " + uuid.toString());
                DataApi.getDataInstance().logError(Shop.class, response.getBody().toString());
                return null;
            }

            BukkitGsonConverter gson = new BukkitGsonConverter();
            Object body = response.getBody();
            String bodyString = body.toString();
            JsonElement bodyElement = gson.fromJson(bodyString, JsonElement.class);

            Shop newShop = fromBodyElement(bodyElement, true);

            if (newShop == null) {
                DataApi.getDataInstance().logError(Shop.class, "Failed to create shop: " + uuid.toString());
                DataApi.getDataInstance().logError(Shop.class, response.getBody().toString());
                return null;
            }

            id = newShop.id;

            return this;
        }).exceptionally(exception -> {
            DataApi.getDataInstance().logError(Shop.class, "Failed to create shop: " + uuid.toString());
            exception.printStackTrace();
            return null;
        });
    }

    /**
     * Updates the shop in the database
     *
     * @return the shop
     */
    public CompletableFuture<Shop> update() {
        String url = String.format(API.SHOP, uuid.toString());
        WebRequest request = WebRequest.builder().json(true).parameters(toParameters()).url(url).build();

        return request.executePut().thenApplyAsync(response -> {
            int statusCode = response.getStatusCode();

            if (!(statusCode >= 200 || statusCode < 300)) {
                DataApi.getDataInstance().logError(Shop.class, "Failed to update shop: " + uuid.toString());
                DataApi.getDataInstance().logError(Shop.class, response.getBody().toString());
                return null;
            }

            BukkitGsonConverter gson = new BukkitGsonConverter();
            Object body = response.getBody();
            String bodyString = body.toString();
            JsonElement bodyElement = gson.fromJson(bodyString, JsonElement.class);

            Shop updatedShop = fromBodyElement(bodyElement, true);

            if (updatedShop == null) {
                DataApi.getDataInstance().logError(Shop.class, "Failed to update shop: " + uuid.toString());
                DataApi.getDataInstance().logError(Shop.class, response.getBody().toString());
                return null;
            }

            return this;
        }).exceptionally(exception -> {
            DataApi.getDataInstance().logError(Shop.class, "Failed to update shop: " + uuid.toString());
            exception.printStackTrace();
            return null;
        });
    }

    /**
     * Deletes the shop in the database
     *
     * @return the shop
     */
    public CompletableFuture<Shop> delete() {
        String url = String.format(API.SHOP, uuid.toString());
        WebRequest request = WebRequest.builder().json(true).parameters(toParameters()).url(url).build();

        return request.executeDelete().thenApplyAsync(response -> {
            int statusCode = response.getStatusCode();

            if (!(statusCode >= 200 || statusCode < 300)) {
                DataApi.getDataInstance().logError(Shop.class, "Failed to delete shop: " + uuid.toString());
                DataApi.getDataInstance().logError(Shop.class, response.getBody().toString());
                return null;
            }

            BukkitGsonConverter gson = new BukkitGsonConverter();
            Object body = response.getBody();
            String bodyString = body.toString();
            JsonElement bodyElement = gson.fromJson(bodyString, JsonElement.class);

            Shop deletedShop = fromBodyElement(bodyElement, true);

            if (deletedShop == null) {
                DataApi.getDataInstance().logError(Shop.class, "Failed to delete shop: " + uuid.toString());
                DataApi.getDataInstance().logError(Shop.class, response.getBody().toString());
                return null;
            }

            return this;
        }).exceptionally(exception -> {
            DataApi.getDataInstance().logError(Shop.class, "Failed to delete shop: " + uuid.toString());
            exception.printStackTrace();
            return null;
        });
    }

    /**
     * Creates a parameter map
     *
     * @return the parameter map
     */
    private Map<String, String> toParameters() {
        Map<String, String> parameters = new HashMap<>();

        parameters.put("uuid", uuid.toString());
        parameters.put("owner_uuid", ownerUuid.toString());
        parameters.put("shop_itemstack", ItemBuffer.toString(itemStack));
        parameters.put("shop_amount", String.valueOf(amount));

        parameters.put("location_world", worldName);
        parameters.put("location_x", String.valueOf(x));
        parameters.put("location_y", String.valueOf(y));
        parameters.put("location_z", String.valueOf(z));

        parameters.put("sell_amount", String.valueOf(sellAmount));
        parameters.put("sell_price", String.valueOf(sellPrice));

        return parameters;
    }

    /**
     * Returns a shop by the body json element
     *
     * @param bodyElement the body json element
     * @return the shop
     */
    private static Shop fromBodyElement(JsonElement bodyElement, boolean isRootElement) {
        BukkitGsonConverter gson = new BukkitGsonConverter();

        JsonElement dataElement = bodyElement;
        if (isRootElement) {
            if (!bodyElement.isJsonObject()) {
                return null;
            }

            JsonObject bodyObject = bodyElement.getAsJsonObject();
            if (!bodyObject.has("data")) {
                return null;
            }

            dataElement = bodyObject.get("data");
            if (!dataElement.isJsonObject()) {
                return null;
            }
        }

        JsonObject dataObject = dataElement.getAsJsonObject();

        return gson.fromJson(dataObject.toString(), Shop.class);
    }

    /**
     * Returns the shop lines
     *
     * @return the shop lines
     */
    public List<Component> getShopLines() {
        List<Component> lines = new ArrayList<>();

        Player owner = getOwner();
        if (owner != null) {
            lines.add(owner.displayName().colorIfAbsent(NamedTextColor.YELLOW));
        }

        if (itemStack != null) {
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta != null) {
                if (itemMeta.hasDisplayName()) {
                    lines.add(itemMeta.displayName());
                } else {
                    lines.add(Component.text(itemStack.getType().name(), NamedTextColor.YELLOW));
                }
            }
        }

        return lines;
    }

    /**
     * @return the amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the itemStack
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * @return the owner
     */
    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    /**
     * Returns the owner
     *
     * @return the owner
     */
    public Player getOwner() {
        return Bukkit.getPlayer(ownerUuid);
    }

    /**
     * @return the sellAmount
     */
    public int getSellAmount() {
        return sellAmount;
    }

    /**
     * @return the sellPrice
     */
    public double getSellPrice() {
        return sellPrice;
    }

    /**
     * @return the uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * @return the worldName
     */
    public String getWorldName() {
        return worldName;
    }

    /**
     * @return the x
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y
     */
    public int getY() {
        return y;
    }

    /**
     * @return the z
     */
    public int getZ() {
        return z;
    }

    /**
     * Returns the location
     *
     * @return the location
     */
    public Location getLocation() {
        if (worldName == null) {
            return null;
        }

        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    /**
     * Returns if the shop is empty
     *
     * @return if the shop is empty
     */
    public boolean isInventoryEmpty() {
        return getAmount() == 0;
    }

    /**
     * @return the members
     */
    public List<ShopMember> getMembers() {
        return members;
    }

    /**
     * @return the shopKey
     */
    public static NamespacedKey getShopKey() {
        return SHOP_KEY;
    }

}
