package dev.slne.surf.shop.server.shop;

import com.google.common.base.MoreObjects;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.slne.data.api.DataApi;
import dev.slne.data.api.web.WebRequest;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.member.ShopMember;
import dev.slne.surf.shop.server.BukkitMain;
import dev.slne.surf.shop.server.api.API;
import dev.slne.surf.shop.server.api.BukkitGsonConverter;
import dev.slne.surf.shop.server.api.buffer.ItemBuffer;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.member.ServerShopMember;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ServerShop implements Shop {

    private static final BukkitGsonConverter GSON_CONVERTER = new BukkitGsonConverter();

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
    private UUID worldUUID;

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
    private List<ServerShopMember> members;

    private boolean locked;
    private Player lockedByPlayer;

    private boolean deleting = false;

    /**
     * A new {@link ServerShop} instance
     *
     * @param owner     the owner
     * @param itemStack the itemstack
     * @param location  the location
     */
    public ServerShop(UUID owner, ItemStack itemStack, Location location) {
        this.ownerUuid = owner;

        if (itemStack != null) {
            this.itemStack = itemStack;
        }

        if (location != null) {
            this.worldUUID = location.getWorld().getUID();
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
    public static CompletableFuture<List<ServerShop>> shops() {
        WebRequest request = WebRequest.builder().json(true).url(API.SHOPS).build();
        List<ServerShop> shops = new ArrayList<>();

        return request.executeGet().thenApplyAsync(response -> {

            JsonArray bodyArray = response.bodyArray(GSON_CONVERTER);
            for (JsonElement element : bodyArray) {
                ServerShop shop = fromBodyElement(element, false);

                if (shop == null) {
                    continue;
                }

                shops.add(shop);
            }

            return shops;
        }).exceptionally(exception -> {
            DataApi.getDataInstance().logError(ServerShop.class, "Failed to fetch shops", exception);
            return null;
        });
    }

    /**
     * Creates a shop in the database
     *
     * @return the shop
     */
    @Override
    public CompletableFuture<Shop> create() {
        System.out.println("Creating shop");
        WebRequest request = WebRequest.builder()
                .json(true)
                .parameters(toParameters())
                .url(API.SHOPS)
                .build();

        return request.executePost().thenApplyAsync(response -> {
            ServerShop newShop = fromBodyElement(response.bodyElement(GSON_CONVERTER), true);

            if (newShop == null) {
                System.out.println("newShop = " + null);

                BukkitMain.getInstance().getLogger().severe("Failed to create shop: " + uuid.toString());
                BukkitMain.getInstance().getLogger().severe(response.body().toString());
                return null;
            }

            id = newShop.id;

            return this.inter();
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
            ServerShop updatedShop = fromBodyElement(response.bodyElement(GSON_CONVERTER), true);

            if (updatedShop == null) {
                BukkitMain.getInstance().getLogger().severe("Failed to update shop: " + uuid.toString());
                BukkitMain.getInstance().getLogger().severe(response.body().toString());
                return null;
            }

            return this.inter();
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
            ServerShop deletedShop = fromBodyElement(response.bodyElement(GSON_CONVERTER), true);

            if (deletedShop == null) {
                BukkitMain.getInstance().getLogger().severe("Failed to delete shop: " + uuid.toString());
                BukkitMain.getInstance().getLogger().severe(response.body().toString());

                deleting = false;
                return null;
            }

            deleting = false;
            return this.inter();
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

        parameters.put("location_world", worldUUID);
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
    private static ServerShop fromBodyElement(JsonElement bodyElement, boolean isRootElement) {
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

        return gson.fromJson(dataObject.toString(), ServerShop.class);
    }

    /**
     * Returns the shop lines
     *
     * @return the shop lines
     */
    public List<Component> getShopLines() {
        List<Component> lines = new ArrayList<>();

        OfflinePlayer owner = getOwner();

        if (owner.getName() != null) {
            lines.add(Component.text(owner.getName(), MessageManager.VARIABLE_VALUE));
        }

        if (itemStack != null) {
            TextComponent.Builder builder = Component.text();
            ItemMeta itemMeta = itemStack.clone().getItemMeta();

            builder.append(Component.text(amount + "x", MessageManager.VARIABLE_VALUE));

            if (itemMeta != null) {
                if (itemMeta.hasDisplayName()) {
                    final Component displayName = itemMeta.displayName();
                    assert displayName != null : "???";

                    builder.append(displayName.colorIfAbsent(MessageManager.VARIABLE_VALUE));
                } else {
                    builder.append(Component.text(itemStack.getType().name(), MessageManager.VARIABLE_VALUE));
                }
            }
        }

        return lines;
    }

    @Override
    public int amount() {
        return amount;
    }

    @Override
    public CompletableFuture<Shop> amount(int amount) {
        this.amount = amount;

        return update();
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    @Override
    public ItemStack item() {
        return itemStack;
    }

    @Override
    public void item(ItemStack item) {
        this.itemStack = item;
    }

    /**
     * @return the owner
     */
    @Override
    public UUID getOwnerUUID() {
        return ownerUuid;
    }

    /**
     * Returns the owner
     *
     * @return the owner
     */
    public OfflinePlayer getOwner() {
        return Bukkit.getOfflinePlayer(ownerUuid);
    }

    /**
     * @return the uuid
     */
    @Override
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Returns if the shop is empty
     *
     * @return if the shop is empty
     */
    @Override
    public boolean isInventoryEmpty() {
        return amount() == 0;
    }

    @Override
    public Optional<Player> getLockedBy() {
        return Optional.ofNullable(lockedByPlayer);
    }

    @Override
    public void setLockedBy(Player player) {
        this.lockedByPlayer = player;
    }

    @Override
    public boolean locked() {
        return locked;
    }

    @Override
    public void locked(boolean locked) {
        this.locked = locked;
    }

    /**
     * @return the members
     */
    @Override
    public List<ShopMember> getMembers() {
        return members.stream().map(ShopMember::inter).toList();
    }

    @Override
    public int sellAmount() {
        return sellAmount;
    }

    @Override
    public void sellAmount(int amount) {
        this.sellAmount = amount;
    }

    @Override
    public double sellPrice() {
        return sellPrice;
    }

    @Override
    public void sellPrice(double price) {
        this.sellPrice = price;
    }

    @Override
    public UUID getWorldUUID() {
        return worldUUID;
    }

    @Override
    public Optional<World> getWorld() {
        return Optional.ofNullable(Bukkit.getWorld(worldUUID));
    }

    /**
     * Locks the shop
     *
     * @param player the player
     */
    @Override
    public void lock(Player player) {
        this.locked = true;
        this.lockedByPlayer = player;
    }

    /**
     * Unlocks the shop
     */
    @Override
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
        return isOwner(player.getUniqueId());
    }

    @Override
    public boolean isOwner(UUID player) {
        return ownerUuid != null && ownerUuid.equals(player);
    }

    /**
     * Returns if the player is a member
     *
     * @param player the player
     * @return if the player is a member
     */
    public boolean isMember(OfflinePlayer player) {
        return isMember(player.getUniqueId());
    }

    @Override
    public boolean isMember(UUID player) {
        return members != null
                && members.stream().anyMatch(member -> member.getUUID().equals(player));
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

    /**
     * Gets the block x value for this shop
     *
     * @return the block x value
     */
    @Override
    public int blockX() {
        return x;
    }

    /**
     * Gets the block x value for this shop
     *
     * @return the block x value
     */
    @Override
    public int blockY() {
        return y;
    }

    /**
     * Gets the block x value for this shop
     *
     * @return the block x value
     */
    @Override
    public int blockZ() {
        return z;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("uuid", uuid)
                .add("ownerUuid", ownerUuid)
                .add("itemStack", itemStack)
                .add("amount", amount)
                .add("worldName", worldUUID)
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

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure {@link Integer#signum
     * signum}{@code (x.compareTo(y)) == -signum(y.compareTo(x))} for
     * all {@code x} and {@code y}.  (This implies that {@code
     * x.compareTo(y)} must throw an exception if and only if {@code
     * y.compareTo(x)} throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * {@code (x.compareTo(y) > 0 && y.compareTo(z) > 0)} implies
     * {@code x.compareTo(z) > 0}.
     *
     * <p>Finally, the implementor must ensure that {@code
     * x.compareTo(y)==0} implies that {@code signum(x.compareTo(z))
     * == signum(y.compareTo(z))}, for all {@code z}.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     * @apiNote It is strongly recommended, but <i>not</i> strictly required that
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.  Generally speaking, any
     * class that implements the {@code Comparable} interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     */
    @Override
    public int compareTo(@NotNull Shop o) {
        return uuid.compareTo(o.getUUID());
    }

    @Override
    public Shop inter() {
        return this;
    }
}
