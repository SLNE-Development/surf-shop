package dev.slne.shop.listener.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

public class ShopPlaceListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        Material type = block.getType();
        if (!type.equals(Material.CHEST)) {
            return;
        }

        Chest chest = (Chest) block.getState();
        ItemStack handItem = event.getItemInHand();
        PersistentDataContainer handItemContainer = handItem.getItemMeta().getPersistentDataContainer();

        boolean handItemIsShop = handItem.hasItemMeta() && handItemContainer.has(Shop.SHOP_KEY);
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

        if (handItemIsShop) {
            if (nextToShop) {
                player.sendMessage(MessageManager.getCannotPlaceShopNextToShopComponent());
                event.setCancelled(true);
                return;
            } else if (nextToChest) {
                player.sendMessage(MessageManager.getCannotPlaceShopNextToChestComponent());
                event.setCancelled(true);
                return;
            }

            handleFinalPlace(player, block, chest);
        } else {
            if (nextToShop) {
                player.sendMessage(MessageManager.getCannotPlaceChestNextToShopComponent());
                event.setCancelled(true);
            }
        }
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
                player.sendMessage(MessageManager.getShopCreatedFailureComponent());
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

            player.sendMessage(MessageManager.getShopCreatedSuccessfullyComponent());
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            player.sendMessage(MessageManager.getShopCreatedFailureComponent());
            return null;
        });
    }

}
