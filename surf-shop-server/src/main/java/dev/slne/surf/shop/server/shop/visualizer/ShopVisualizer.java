package dev.slne.surf.shop.server.shop.visualizer;

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
import com.google.common.collect.Lists;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.visualizer.VisualizerSettings;
import dev.slne.surf.shop.api.shop.visualizer.VisualizerSettings.LineSettings;
import dev.slne.surf.shop.api.shop.visualizer.VisualizerSettings.MaterialSettings;
import dev.slne.surf.shop.api.shop.visualizer.settings.BillboardConstraint;
import dev.slne.surf.shop.api.shop.visualizer.settings.DisplayType;
import dev.slne.surf.shop.server.BukkitMain;
import dev.slne.surf.shop.server.util.ShopUtils;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ShopVisualizer {

    private static final GsonComponentSerializer gsonComponentSerializer = GsonComponentSerializer.gson();

    private final Shop shop;
    private final Map<UUID, List<Integer>> playerEntityIds;

    /**
     * A new {@link ShopVisualizer} instance
     */
    public ShopVisualizer(Shop shop) {
        this.shop = shop;
        this.playerEntityIds = new ConcurrentHashMap<>();
    }

    /**
     * Updates the shop visualizer
     */
    public void update() {
        final int rangeSquared = VisualizerSettings.get().getRangeSquared();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isInRange(player, rangeSquared) && !hasBeenSpawned(player)) {
                spawn(player);
            } else if (!isInRange(player, rangeSquared) && hasBeenSpawned(player)) {
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
        final Location shopLocation = getShopLocation();

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
        final Location shopLocation = getShopLocation();

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
        final Material sellItemMaterial = getSellItemMaterial();
        if (sellItemMaterial != null) {
            double materialY = shopY + 1.025;

            spawnMaterial(player, shopX, materialY, shopZ, sellItemMaterial);
        }

        // Lines
        List<Component> linesReversed = Lists.reverse(getLines());

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
        for (Entry<UUID, List<Integer>> entry : new ArrayList<>(playerEntityIds.entrySet())) {
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
        final VisualizerSettings visualizerSettings = VisualizerSettings.get();

        return visualizerSettings.getAboveShopHeight() + visualizerSettings.getLineHeight() * getLines().size() + visualizerSettings.getMaterialLineSpacing();
    }

    /**
     * Shows the shop to the given {@link Player}
     *
     * @param index the index
     * @return the {@link CompletableFuture} instance
     */
    private double calculateLineHeight(int index) {
        final VisualizerSettings visualizerSettings = VisualizerSettings.get();

        return visualizerSettings.getAboveShopHeight() + visualizerSettings.getLineHeight() * index;
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
        final MaterialSettings materialSettings = MaterialSettings.get();
        final float materialScaleSetting = materialSettings.getMaterialScale();

        final int entityId = spawnPacketEntity(player, EntityTypes.ITEM_DISPLAY, x, y, z);

        // Defaults
        final Vector3f scale = new Vector3f(materialScaleSetting, materialScaleSetting, materialScaleSetting);
        final BillboardConstraint billboardConstraint = materialSettings.getBillboardConstraint();

        // Slot
        final ItemStack bukkitItemStack = new ItemStack(material);
        final com.github.retrooper.packetevents.protocol.item.ItemStack packetItemStack = SpigotConversionUtil
                .fromBukkitItemStack(bukkitItemStack);

        // Display Type
        final DisplayType displayType = materialSettings.getDisplayType();

        final List<EntityData> entityData = new ArrayList<>();

        // see https://wiki.vg/Entity_metadata#Display and https://wiki.vg/Entity_metadata#Item_Display
        entityData.add(new EntityData(11, EntityDataTypes.VECTOR3F, scale));
        entityData.add(new EntityData(14, EntityDataTypes.BYTE, billboardConstraint.getId()));
        entityData.add(new EntityData(22, EntityDataTypes.ITEMSTACK, packetItemStack));
        entityData.add(new EntityData(23, EntityDataTypes.BYTE, displayType.getId()));


        /*
        // Slot
        final ItemStack bukkitItemStack = new ItemStack(material);
        final com.github.retrooper.packetevents.protocol.item.ItemStack packetItemStack = SpigotConversionUtil
                .fromBukkitItemStack(bukkitItemStack);
        final List<EntityData> entityData = new ArrayList<>();

        entityData.add(new EntityData(5, EntityDataTypes.BOOLEAN, true));
        entityData.add(new EntityData(8, EntityDataTypes.ITEMSTACK, packetItemStack));

         */


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
    @SuppressWarnings({"java:S2583", "java:S2589"})
    private void spawnLine(Player player, double x, double y, double z, Component component) {
        final LineSettings lineSettings = LineSettings.get();

        final int entityId = spawnPacketEntity(player, EntityTypes.TEXT_DISPLAY, x, y, z);

        // Defaults
        final float lineScaleSetting = lineSettings.getLineScale();
        final Vector3f scale = new Vector3f(lineScaleSetting, lineScaleSetting, lineScaleSetting);
        final BillboardConstraint billboardConstraint = lineSettings.getBillboardConstraint();

        // Text
        final Component text = component != null ? component : Component.empty();
        final String textJson = ShopUtils.GSON_COMPONENT_SERIALIZER.serialize(text);
        final int lineWidth = lineSettings.getLineWidth();
        final int backgroundColor = lineSettings.getBackgroundColor().value();
        final byte textOpacity = lineSettings.getTextOpacity();
        final byte bitMask = TextDisplayBitMask.createBitMask(lineSettings);
        final List<EntityData> entityData = new ArrayList<>();

        entityData.add(new EntityData(11, EntityDataTypes.VECTOR3F, scale));
        entityData.add(new EntityData(14, EntityDataTypes.BYTE, billboardConstraint.getId()));

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
        final List<Integer> entityIds = playerEntityIds.get(player.getUniqueId());

        if (entityIds == null) {
            return;
        }

        final int[] entityIdArray = entityIds.stream().mapToInt(i -> i).toArray();
        final WrapperPlayServerDestroyEntities destroyEntities = new WrapperPlayServerDestroyEntities(entityIdArray);

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
        final WrapperPlayServerEntityMetadata entityMetadata = new WrapperPlayServerEntityMetadata(entityId, entityData);
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
        final int entityId = getRandomEntityId();
        final UUID uuid = UUID.randomUUID();

        final int intX = (int) x, intZ = (int) z;

        final Vector3d position = new Vector3d(intX + 0.5, y, intZ + 0.5);
        final float pitch = 0, yaw = 0, headYaw = 0;
        final int data = 0;
        final Vector3d velocity = new Vector3d(0, 0, 0);

        final WrapperPlayServerSpawnEntity spawnEntity = new WrapperPlayServerSpawnEntity(entityId, Optional.of(uuid),
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
        final AtomicInteger randomId = new AtomicInteger(BukkitMain.getInstance().getRandom().nextInt(1_000_000));

        while (playerEntityIds.values().stream().flatMap(List::stream).anyMatch(integer -> integer.equals(randomId.get()))) { // Get a new random id if the id is already used
            randomId.set(BukkitMain.getInstance().getRandom().nextInt(1_000_000));
        }

        return randomId.get();
    }

    /**
     * Puts an entity id to the player
     *
     * @param player   the player
     * @param entityId the entity id
     */
    private void putEntityId(Player player, int entityId) { // TODO: 05.11.2023 Blame Simon
//        final Entry<UUID, List<Integer>> playerEntry = this.playerEntityIds.entrySet().stream()
//                .filter(entry -> entry.getKey().equals(player.getUniqueId())).findFirst().orElse(null);

        this.playerEntityIds.computeIfAbsent(player.getUniqueId(), uuid -> new ArrayList<>()).add(entityId);

//        if (playerEntry == null) {
//            List<Integer> entityIds = new ArrayList<>();
//            entityIds.add(entityId);
//            this.playerEntityIds.put(player.getUniqueId(), entityIds);
//
//            return;
//        }
//
//        playerEntry.getValue().add(entityId);
    }

    /**
     * Removes all entity ids from the player
     *
     * @param player the player
     */
    public void removeEntityIds(Player player) {
        if (player == null) {
            return;
        }

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
    public Material getSellItemMaterial() {
        return shop.item().map(ItemStack::getType).orElse(Material.BARRIER);
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
     * @return the playerEntityIds
     */
    public Map<UUID, List<Integer>> getPlayerEntityIds() {
        return playerEntityIds;
    }

}
