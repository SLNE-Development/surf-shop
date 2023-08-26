package dev.slne.shop.listener.listeners;

import dev.slne.data.api.DataApi;
import dev.slne.shop.util.ShopUtils;
import org.bukkit.Bukkit;
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

import dev.slne.shop.instance.BukkitApi;
import dev.slne.shop.listener.events.state.ShopCreateEvent;
import dev.slne.shop.message.MessageManager;
import dev.slne.shop.shop.Shop;

public class ShopPlaceListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    @SuppressWarnings("java:S3776")
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();

        if (!(block.getState() instanceof Chest chest)) {
            return;
        }

        final ItemStack handItem = event.getItemInHand();
        final PersistentDataContainer handItemContainer = handItem.getItemMeta().getPersistentDataContainer();

        boolean handItemIsShop = handItem.hasItemMeta() && handItemContainer.has(Shop.SHOP_KEY);
        boolean nextToChest = false;
        boolean nextToShop = false;

        for (Chest surroundingChests : ShopUtils.getSurroundingBlockStates(block, Chest.class)) {
            nextToChest = true;

            if (surroundingChests.getPersistentDataContainer().has(Shop.SHOP_KEY, PersistentDataType.STRING)) {
                nextToShop = true;
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

            ShopCreateEvent shopCreateEvent = new ShopCreateEvent(block, player);

            System.out.println("shopCreateEvent = " + shopCreateEvent);

            if (!shopCreateEvent.callEvent()) {
                System.out.println("shopCreateEvent.isCancelled() = " + shopCreateEvent.isCancelled());
                shopCreateEvent.applyCancelled(event.getPlayer());
                event.setCancelled(true);
                return;
            }

            System.out.println("shopCreateEvent.isCancelled() = " + shopCreateEvent.isCancelled());
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
        System.out.println("handleFinalPlace");
        final PersistentDataContainer container = chest.getPersistentDataContainer();
        final Location location = block.getLocation();
        final Shop shop = new Shop(player.getUniqueId(), new ItemStack(Material.STONE), location);

        System.out.println("shop = " + shop);

        shop.setAmount(100000); // TODO: 26.08.2023 remove this line when finished with testing

        shop.create().thenAcceptAsync(created -> {
            if (created == null) {
                player.sendMessage(MessageManager.getShopCreatedFailureComponent());
                return;
            }

            BukkitApi.getInstance().getShopManager().addShop(shop);
            container.set(Shop.SHOP_KEY, PersistentDataType.STRING, shop.getUuid().toString());
            chest.update();

            player.sendMessage(MessageManager.getShopCreatedSuccessfullyComponent());
        }).exceptionally(throwable -> {
            DataApi.getDataInstance().logError(getClass(), "Failed to create shop", throwable);
            player.sendMessage(MessageManager.getShopCreatedFailureComponent());
            return null;
        });
    }

}
