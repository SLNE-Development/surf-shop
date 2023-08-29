package dev.slne.surf.shop.server.shop;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.slne.data.api.DataApi;
import dev.slne.data.api.web.WebRequest;
import dev.slne.surf.shop.server.BukkitMain;
import dev.slne.surf.shop.server.api.API;
import dev.slne.surf.shop.server.api.BukkitGsonConverter;
import dev.slne.surf.shop.server.api.buffer.ItemBuffer;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.member.ShopMember;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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

    private boolean locked;
    private Player lockedByPlayer;

    private boolean deleting = false;

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

        this.locked = false;
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
            int statusCode = response.statusCode();

            if (!(statusCode >= 200 || statusCode < 300)) { // TODO: Condition '!(statusCode >= 200 || statusCode < 300)' is always 'false'
                DataApi.getDataInstance().logError(Shop.class, "Failed to fetch shops");
                DataApi.getDataInstance().logError(Shop.class, response.body().toString());
                return null;
            }

            BukkitGsonConverter gson = new BukkitGsonConverter();
            Object body = response.body();
            String bodyString = body.toString();
            JsonElement bodyElement = gson.fromJson(bodyString, JsonElement.class);

            if (!bodyElement.isJsonObject()) {
                DataApi.getDataInstance().logError(Shop.class, "Failed to fetch shops");
                DataApi.getDataInstance().logError(Shop.class, response.body().toString());
                return null;
            }

            JsonObject bodyObject = bodyElement.getAsJsonObject();
            if (!bodyObject.has("data")) {
                DataApi.getDataInstance().logError(Shop.class, "Failed to fetch shops");
                DataApi.getDataInstance().logError(Shop.class, response.body().toString());
                return null;
            }

            JsonElement dataElement = bodyObject.get("data");
            if (!dataElement.isJsonArray()) {
                DataApi.getDataInstance().logError(Shop.class, "Failed to fetch shops");
                DataApi.getDataInstance().logError(Shop.class, response.body().toString());
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
            DataApi.getDataInstance().logError(Shop.class, "Failed to fetch shops", exception);
            return null;
        });
    }

    /**
     * Creates a shop in the database
     *
     * @return the shop
     */
    public CompletableFuture<Shop> create() {
        System.out.println("Creating shop");
        WebRequest request = WebRequest.builder()
                .json(true)
                .parameters(toParameters())
                .url(API.SHOPS)
                .build();

        return request.executePost().thenApplyAsync(response -> {
            int statusCode = response.statusCode();

            System.out.println("statusCode = " + statusCode);

            if (!(statusCode >= 200 || statusCode < 300)) { // TODO: 26.08.2023 Condition '!(statusCode >= 200 || statusCode < 300)' is always 'false'
                BukkitMain.getInstance().getLogger().severe("Failed to create shop: " + uuid.toString());
                BukkitMain.getInstance().getLogger().severe(response.body().toString());
                return null;
            }

            BukkitGsonConverter gson = new BukkitGsonConverter();
            Object body = response.body();
            String bodyString = body.toString();
            JsonElement bodyElement = gson.fromJson(bodyString, JsonElement.class);

            Shop newShop = fromBodyElement(bodyElement, true);

            if (newShop == null) {
                System.out.println("newShop = " + null);

                BukkitMain.getInstance().getLogger().severe("Failed to create shop: " + uuid.toString());
                BukkitMain.getInstance().getLogger().severe(response.body().toString());
                return null;
            }

            id = newShop.id;

            return this;
        }).exceptionally(exception -> {
            exception.printStackTrace();
            DataApi.getDataInstance().logError(getClass(), "Failed to create shop: " + uuid.toString(), exception);
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
            int statusCode = response.statusCode();

            if (!(statusCode >= 200 || statusCode < 300)) { // TODO: 26.08.2023 Condition '!(statusCode >= 200 || statusCode < 300)' is always 'false'
                BukkitMain.getInstance().getLogger().severe("Failed to update shop: " + uuid.toString());
                BukkitMain.getInstance().getLogger().severe(response.body().toString());
                return null;
            }

            BukkitGsonConverter gson = new BukkitGsonConverter();
            Object body = response.body();
            String bodyString = body.toString();
            JsonElement bodyElement = gson.fromJson(bodyString, JsonElement.class);

            Shop updatedShop = fromBodyElement(bodyElement, true);

            if (updatedShop == null) {
                BukkitMain.getInstance().getLogger().severe("Failed to update shop: " + uuid.toString());
                BukkitMain.getInstance().getLogger().severe(response.body().toString());
                return null;
            }

            return this;
        }).exceptionally(exception -> {
            DataApi.getDataInstance().logError(getClass(), "Failed to update shop: " + uuid.toString(), exception);
            return null;
        });
    }

    /**
     * Deletes the shop in the database
     *
     * @return the shop
     */
    public CompletableFuture<Shop> delete() {
        deleting = true;

        String url = String.format(API.SHOP, uuid.toString());
        WebRequest request = WebRequest.builder().json(true).parameters(toParameters()).url(url).build();

        return request.executeDelete().thenApplyAsync(response -> {
            int statusCode = response.statusCode();

            if (!(statusCode >= 200 || statusCode < 300)) {
                BukkitMain.getInstance().getLogger().severe("Failed to delete shop: " + uuid.toString());
                BukkitMain.getInstance().getLogger().severe(response.body().toString());

                deleting = false;
                return null;
            }

            BukkitGsonConverter gson = new BukkitGsonConverter();
            Object body = response.body();
            String bodyString = body.toString();
            JsonElement bodyElement = gson.fromJson(bodyString, JsonElement.class);

            Shop deletedShop = fromBodyElement(bodyElement, true);

            if (deletedShop == null) {
                BukkitMain.getInstance().getLogger().severe("Failed to delete shop: " + uuid.toString());
                BukkitMain.getInstance().getLogger().severe(response.body().toString());

                deleting = false;
                return null;
            }

            deleting = false;
            return this;
        }).exceptionally(exception -> {
            DataApi.getDataInstance().logError(getClass(), "Failed to delete shop: " + uuid.toString(), exception);

            deleting = false;
            return null;
        });
    }

    /**
     * Creates a parameter map
     *
     * @return the parameter map
     */
    private Map<String, Object> toParameters() {
        Map<String, Object> parameters = new HashMap<>();

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
            TextComponent.Builder builder = Component.text();
            ItemMeta itemMeta = itemStack.clone().getItemMeta();

            builder.append(Component.text(amount + "x", MessageManager.VARIABLE_VALUE));

            if (itemMeta != null) {
                if (itemMeta.hasDisplayName()) {
                    builder.append(itemMeta.displayName().colorIfAbsent(MessageManager.VARIABLE_VALUE));
                } else {
                    builder.append(Component.text(itemStack.getType().name(), MessageManager.VARIABLE_VALUE));
                }
            }
        }

        return lines;
    }

    /**
     * Decreases the amount
     */
    public CompletableFuture<Shop> decreaseAmount(int amount) {
        this.amount -= amount;

        return update();
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
     * Sets the amount
     *
     * @param amount the amount
     */
    public void setAmount(int amount) {
        this.amount = amount;
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

    /**
     * @return the lockedByPlayer
     */
    public Player getLockedByPlayer() {
        return lockedByPlayer;
    }

    /**
     * @return the locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * @param locked the locked to set
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * @param itemStack the itemStack to set
     */
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * @param lockedByPlayer the lockedByPlayer to set
     */
    public void setLockedByPlayer(Player lockedByPlayer) {
        this.lockedByPlayer = lockedByPlayer;
    }

    /**
     * @param sellAmount the sellAmount to set
     */
    public void setSellAmount(int sellAmount) {
        this.sellAmount = sellAmount;
    }

    /**
     * @param sellPrice the sellPrice to set
     */
    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    /**
     * Locks the shop
     *
     * @param player the player
     */
    public void lock(Player player) {
        this.locked = true;
        this.lockedByPlayer = player;
    }

    /**
     * Unlocks the shop
     */
    public void unlock() {
        this.locked = false;
        this.lockedByPlayer = null;
    }

    /**
     * Returns if the player is the owner
     *
     * @param player the player
     * @return if the player is the owner
     */
    public boolean isOwner(OfflinePlayer player) {
        return ownerUuid != null && ownerUuid.equals(player.getUniqueId());
    }

    /**
     * Returns if the player is a member
     *
     * @param player the player
     * @return if the player is a member
     */
    public boolean isMember(OfflinePlayer player) {
        return members != null
                && members.stream().anyMatch(member -> member.getMemberUuid().equals(player.getUniqueId()));
    }

    /**
     * Returns if the shop is currently deleting
     *
     * @return if the shop is currently deleting
     * @since 1.0.0
     */
    public boolean isDeleting() {
        return deleting;
    }

    @Override
    public String toString() {

        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("uuid", uuid)
                .add("ownerUuid", ownerUuid)
                .add("itemStack", itemStack)
                .add("amount", amount)
                .add("worldName", worldName)
                .add("x", x)
                .add("y", y)
                .add("z", z)
                .add("sellAmount", sellAmount)
                .add("sellPrice", sellPrice)
                .add("members", members)
                .add("locked", locked)
                .add("lockedByPlayer", lockedByPlayer)
                .add("deleting", deleting)
                .toString();
    }
}
