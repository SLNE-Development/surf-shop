package dev.slne.shop.listener.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import dev.slne.shop.BukkitMain;
import dev.slne.shop.instance.BukkitApi;
import dev.slne.shop.message.MessageManager;
import dev.slne.shop.shop.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ShopPlaceListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Material type = block.getType();
        if (!type.equals(Material.CHEST)) {
            return;
        }

        Chest chest = (Chest) block.getState();
        ItemStack handItem = event.getItemInHand();

        if (!handleBadThings(block, handItem, player)) {
            return;
        }

        handleFinalPlace(player, block, chest);
    }

    /**
     * Checks if the item in the player's hand is a shop
     *
     * @param handItem the hand item
     * @return true if the item is a shop
     */
    private boolean isShopItem(ItemStack handItem) {
        PersistentDataContainer handItemContainer = handItem.getItemMeta().getPersistentDataContainer();

        return handItem.hasItemMeta() && !handItemContainer.has(Shop.SHOP_KEY);
    }

    /**
     * Handles the chest being placed next to a chest
     *
     * @param block    the block
     * @param handItem the hand item
     * @param player   the player
     * @return true if the chest is next to a chest
     */
    @SuppressWarnings("java:S3776")
    private boolean handleBadThings(Block block, ItemStack handItem, Player player) {
        boolean handItemIsShop = isShopItem(handItem);
        boolean nextToChest = false;
        boolean nextToShop = false;

        BlockFace[] possibleNextTo = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH,
                BlockFace.WEST };

        for (BlockFace face : possibleNextTo) {
            Block relative = block.getRelative(face);
            if (relative.getType().equals(Material.CHEST)) {
                nextToChest = true;

                Chest relativeChest = (Chest) relative.getState();
                PersistentDataContainer container = relativeChest.getPersistentDataContainer();

                if (container.has(Shop.SHOP_KEY, PersistentDataType.STRING)) {
                    nextToShop = true;
                }

                break;
            }
        }

        Component message = null;

        if (handItemIsShop) {
            if (nextToShop) {
                message = MessageManager.getCannotPlaceShopNextToShopComponent();
                player.sendMessage(message);
            } else if (nextToChest) {
                message = MessageManager.getCannotPlaceShopNextToChestComponent();
                player.sendMessage(message);
            }
        } else {
            if (nextToShop) {
                message = MessageManager.getCannotPlaceChestNextToShopComponent();
                player.sendMessage(message);
            }

            return false;
        }

        return nextToChest;
    }

    /**
     * Handles the final place of the shop
     *
     * @param player the player
     * @param block  the block
     * @param chest  the chest
     */
    private void handleFinalPlace(Player player, Block block, Chest chest) {
        PersistentDataContainer container = chest.getPersistentDataContainer();

        Location location = block.getLocation();
        Shop shop = new Shop(player.getUniqueId(), null, location);
        shop.create().thenAcceptAsync(created -> {
            if (created == null) {
                printShopCreationFailed(player);
                return;
            }

            BukkitApi.getInstance().getShopManager().getVisualizerTask().addVisualizer(shop);
            BukkitApi.getInstance().getShopManager().getVisualizerTask().addPlayer(player, shop);
            BukkitApi.getInstance().getShopManager().addShop(shop);

            container.set(Shop.SHOP_KEY, PersistentDataType.STRING, shop.getUuid().toString());
            new BukkitRunnable() {
                @Override
                public void run() {
                    chest.update();
                }
            }.runTask(BukkitMain.getInstance());

            player.sendMessage(Component.text("Created shop", NamedTextColor.GREEN));
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            printShopCreationFailed(player);
            return null;
        });
    }

    private void printShopCreationFailed(Player player) {
        player.sendMessage(Component.text("Failed to create shop", NamedTextColor.RED));
    }

}
