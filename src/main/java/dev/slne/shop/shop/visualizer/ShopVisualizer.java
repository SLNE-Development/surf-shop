package dev.slne.shop.shop.visualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;

import dev.slne.shop.BukkitMain;
import dev.slne.shop.shop.Shop;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class ShopVisualizer {

    private static final double ABOVE_SHOP_HEIGHT = 1.25;
    private static final double LINE_HEIGHT = 0.35;
    private static final double MATERIAL_LINES_SPACING = 0.35;

    private static final float MATERIAL_SCALE = 0.5f;
    private static final float LINE_SCALE = 0.75f;

    private static final int RANGE_SQUARED = (int) Math.pow(10, 2);

    private Shop shop;
    private Map<UUID, List<Integer>> playerEntityIds;

    /**
     * A new {@link ShopVisualizer} instance
     */
    public ShopVisualizer(Shop shop) {
        this.shop = shop;
        this.playerEntityIds = new HashMap<>();
    }

    /**
     * Updates the shop visualizer
     */
    public void update() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isInRange(player, RANGE_SQUARED) && !hasBeenSpawned(player)) {
                spawn(player);
            } else if (!isInRange(player, RANGE_SQUARED) && hasBeenSpawned(player)) {
                despawn(player);
            }
        }
    }

    /**
     * Checks if the shop visualizer is spawned for the given Player
     *
     * @param player the player
     * @return true if the shop visualizer is spawned, otherwise false
     */
    private boolean hasBeenSpawned(Player player) {
        return playerEntityIds.containsKey(player.getUniqueId());
    }

    /**
     * Checks if the player is in range for the shop
     *
     * @param player       the player
     * @param rangeSquared the range squared
     * @return true if the player is in range, otherwise false
     */
    private boolean isInRange(Player player, int rangeSquared) {
        Location shopLocation = getShopLocation();

        if (shopLocation == null) {
            return false;
        }

        if (shopLocation.getWorld() == null || shopLocation.getWorld() != player.getWorld()) {
            return false;
        }

        return shopLocation.distanceSquared(player.getLocation()) <= rangeSquared;
    }

    /**
     * Spawns the shop visualizer for the given Player
     *
     * @param player the player
     */
    public void spawn(Player player) {
        Location shopLocation = getShopLocation();

        if (shopLocation == null) {
            return;
        }

        if (shopLocation.getWorld() == null || shopLocation.getWorld() != player.getWorld()) {
            return;
        }

        int shopX = shopLocation.getBlockX();
        int shopY = shopLocation.getBlockY();
        int shopZ = shopLocation.getBlockZ();

        // Material
        if (getMaterial() != null) {
            double materialY = shopY + calculateMaterialHeight();

            spawnMaterial(player, shopX, materialY, shopZ, getMaterial());
        }

        // Lines
        List<Component> lines = getLines();
        List<Component> linesReversed = new ArrayList<>();

        for (int i = lines.size() - 1; i >= 0; i--) {
            linesReversed.add(lines.get(i));
        }

        for (int i = 0; i < linesReversed.size(); i++) {
            double lineY = shopY + calculateLineHeight(i);

            spawnLine(player, shopX, lineY, shopZ, linesReversed.get(i));
        }
    }

    /**
     * Despawns the shop visualizer for the given Player
     *
     * @param player the player
     */
    public void despawn(Player player) {
        destroyEntities(player);
    }

    /**
     * Despawns the shop visualizer for all players
     */
    public void despawnAll() {
        for (Map.Entry<UUID, List<Integer>> entry : new ArrayList<>(playerEntityIds.entrySet())) {
            UUID uuid = entry.getKey();
            Player player = Bukkit.getPlayer(uuid);

            if (player == null) {
                removeEntityIds(player);
                continue;
            }

            destroyEntities(player);
        }
    }

    /**
     * Calculates the material height
     *
     * @return the material height
     */
    private double calculateMaterialHeight() {
        return ABOVE_SHOP_HEIGHT + LINE_HEIGHT * getLines().size() + MATERIAL_LINES_SPACING;
    }

    /**
     * Shows the shop to the given {@link Player}
     *
     * @param index the index
     * @return the {@link CompletableFuture} instance
     */
    private double calculateLineHeight(int index) {
        return ABOVE_SHOP_HEIGHT + LINE_HEIGHT * index;
    }

    /**
     * Spawn the material
     *
     * @param player   the player
     * @param x        the x coordinate
     * @param y        the y coordinate
     * @param z        the z coordinate
     * @param material the material
     */
    private void spawnMaterial(Player player, double x, double y, double z, Material material) {
        int entityId = spawnPacketEntity(player, EntityTypes.ITEM_DISPLAY, x, y, z);

        // Defaults
        Vector3f scale = new Vector3f(MATERIAL_SCALE, MATERIAL_SCALE, MATERIAL_SCALE);
        BillboardConstraint billboardConstraint = BillboardConstraint.CENTER;
        int billboardConstraintOrdinal = billboardConstraint.ordinal();
        byte billboardConstraintByte = (byte) billboardConstraintOrdinal;

        // Slot
        ItemStack bukkitItemStack = new ItemStack(material);
        com.github.retrooper.packetevents.protocol.item.ItemStack packetItemStack = SpigotConversionUtil
                .fromBukkitItemStack(bukkitItemStack);

        // Display Type
        DisplayType displayType = DisplayType.NONE;
        int displayTypeOrdinal = displayType.ordinal();
        byte displayTypeByte = (byte) displayTypeOrdinal;

        List<EntityData> entityData = new ArrayList<>();

        entityData.add(new EntityData(11, EntityDataTypes.VECTOR3F, scale));
        entityData.add(new EntityData(14, EntityDataTypes.BYTE, billboardConstraintByte));
        entityData.add(new EntityData(22, EntityDataTypes.ITEMSTACK, packetItemStack));
        entityData.add(new EntityData(23, EntityDataTypes.BYTE, displayTypeByte));

        sendMetadata(player, entityId, entityData);
    }

    /**
     * Spawns a line
     *
     * @param player    the player
     * @param x         the x coordinate
     * @param y         the y coordinate
     * @param z         the z coordinate
     * @param component the component
     */
    @SuppressWarnings({ "java:S2583", "java:S2589" })
    private void spawnLine(Player player, double x, double y, double z, Component component) {
        int entityId = spawnPacketEntity(player, EntityTypes.TEXT_DISPLAY, x, y, z);

        // Defaults
        Vector3f scale = new Vector3f(LINE_SCALE, LINE_SCALE, LINE_SCALE);
        BillboardConstraint billboardConstraint = BillboardConstraint.CENTER;
        int billboardConstraintOrdinal = billboardConstraint.ordinal();
        byte billboardConstraintByte = (byte) billboardConstraintOrdinal;

        // Text
        GsonComponentSerializer gsonComponentSerializer = GsonComponentSerializer.gson();
        Component text = component != null ? component : Component.empty();
        String textJson = gsonComponentSerializer.serialize(text);
        int lineWidth = 200;
        int backgroundColor = 0x40000000;
        byte textOpacity = (byte) -1;

        // Bit Mask 0x01 has shadow
        // Bit Mask 0x02 is see through
        // Bit Mask 0x04 use default background color
        // Bit Mask 0x08 alignment 0 = center, 1 or 3 left, 2 right
        boolean hasShadow = false;
        boolean isSeeThrough = false;
        boolean useDefaultBackgroundColor = false;
        int alignment = 0;
        byte bitMask = 0;

        if (hasShadow) {
            bitMask |= 0x01;
        }

        if (isSeeThrough) {
            bitMask |= 0x02;
        }

        if (useDefaultBackgroundColor) {
            bitMask |= 0x04;
        }

        bitMask |= alignment << 3;
        List<EntityData> entityData = new ArrayList<>();

        entityData.add(new EntityData(11, EntityDataTypes.VECTOR3F, scale));
        entityData.add(new EntityData(14, EntityDataTypes.BYTE, billboardConstraintByte));

        entityData.add(new EntityData(22, EntityDataTypes.COMPONENT, textJson));
        entityData.add(new EntityData(23, EntityDataTypes.INT, lineWidth));
        entityData.add(new EntityData(24, EntityDataTypes.INT, backgroundColor));
        entityData.add(new EntityData(25, EntityDataTypes.BYTE, textOpacity));
        entityData.add(new EntityData(26, EntityDataTypes.BYTE, bitMask));

        sendMetadata(player, entityId, entityData);
    }

    /**
     * Destroys all entities
     *
     * @param player the player
     */
    private void destroyEntities(Player player) {
        Entry<UUID, List<Integer>> playerEntry = this.playerEntityIds.entrySet().stream()
                .filter(entry -> entry.getKey().equals(player.getUniqueId())).findFirst().orElse(null);

        if (playerEntry == null) {
            return;
        }

        List<Integer> entityIds = playerEntry.getValue();

        if (entityIds == null) {
            return;
        }

        int[] entityIdArray = entityIds.stream().mapToInt(i -> i).toArray();
        WrapperPlayServerDestroyEntities destroyEntities = new WrapperPlayServerDestroyEntities(entityIdArray);

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, destroyEntities);
        removeEntityIds(player);
    }

    /**
     * Sends the metadata
     *
     * @param player     the player
     * @param entityId   the entity id
     * @param entityData the entity data
     */
    private void sendMetadata(Player player, int entityId, List<EntityData> entityData) {
        WrapperPlayServerEntityMetadata entityMetadata = new WrapperPlayServerEntityMetadata(entityId, entityData);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, entityMetadata);
    }

    /**
     * Spawns a packet entity
     *
     * @param player     the player
     * @param entityType the entity type
     * @param x          the x coordinate
     * @param y          the y coordinate
     * @param z          the z coordinate
     * @return the spawned entity id
     */
    private int spawnPacketEntity(Player player, EntityType entityType, double x, double y, double z) {
        int entityId = getRandomEntityId();
        UUID uuid = UUID.randomUUID();

        int intX = (int) x;
        int intZ = (int) z;

        Vector3d position = new Vector3d(intX + 0.5, y, intZ + 0.5);
        float pitch = 0;
        float yaw = 0;
        float headYaw = 0;
        int data = 0;
        Vector3d velocity = new Vector3d(0, 0, 0);

        WrapperPlayServerSpawnEntity spawnEntity = new WrapperPlayServerSpawnEntity(entityId, Optional.of(uuid),
                entityType, position, pitch, yaw, headYaw, data, Optional.of(velocity));

        PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnEntity);
        putEntityId(player, entityId);

        return entityId;
    }

    /**
     * Returns a random entity id
     *
     * @return the random entity id
     */
    private int getRandomEntityId() {
        return BukkitMain.getInstance().getRandom().nextInt(1000000);
    }

    /**
     * Puts an entity id to the player
     *
     * @param player   the player
     * @param entityId the entity id
     */
    private void putEntityId(Player player, int entityId) {
        Entry<UUID, List<Integer>> playerEntry = this.playerEntityIds.entrySet().stream()
                .filter(entry -> entry.getKey().equals(player.getUniqueId())).findFirst().orElse(null);

        if (playerEntry == null) {
            List<Integer> entityIds = new ArrayList<>();
            entityIds.add(entityId);
            this.playerEntityIds.put(player.getUniqueId(), entityIds);

            return;
        }

        playerEntry.getValue().add(entityId);
    }

    /**
     * Removes all entity ids from the player
     *
     * @param player the player
     */
    public void removeEntityIds(Player player) {
        this.playerEntityIds.remove(player.getUniqueId());
    }

    /**
     * @return the lines
     */
    public List<Component> getLines() {
        return shop.getShopLines();
    }

    /**
     * @return the material
     */
    public Material getMaterial() {
        return shop.getItemStack() != null ? shop.getItemStack().getType() : Material.BARRIER;
    }

    /**
     * @return the shop
     */
    public Shop getShop() {
        return shop;
    }

    /**
     * @return the shopLocation
     */
    public Location getShopLocation() {
        return shop.getLocation();
    }

    /**
     * @return the aboveShopHeight
     */
    public static double getAboveShopHeight() {
        return ABOVE_SHOP_HEIGHT;
    }

    /**
     * @return the lineHeight
     */
    public static double getLineHeight() {
        return LINE_HEIGHT;
    }

    /**
     * @return the materialLinesSpacing
     */
    public static double getMaterialLinesSpacing() {
        return MATERIAL_LINES_SPACING;
    }

    /**
     * @return the playerEntityIds
     */
    public Map<UUID, List<Integer>> getPlayerEntityIds() {
        return playerEntityIds;
    }

}
