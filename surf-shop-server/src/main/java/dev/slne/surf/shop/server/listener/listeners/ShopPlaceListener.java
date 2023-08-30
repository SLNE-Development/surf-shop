package dev.slne.surf.shop.server.listener.listeners;

import dev.slne.data.api.DataApi;
import dev.slne.surf.shop.api.ShopApi;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.shop.ServerShop;
import dev.slne.surf.shop.server.util.ShopUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import dev.slne.surf.shop.server.instance.BukkitApi;
import dev.slne.surf.shop.api.events.state.ShopCreateEvent;
import dev.slne.surf.shop.server.message.MessageManager;

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

        boolean handItemIsShop = ShopApi.isShopItem(handItem);
        boolean nextToChest = false;
        boolean nextToShop = false;

        for (Chest surroundingChest : ShopUtils.getSurroundingBlockStates(block, Chest.class)) {
            nextToChest = true;

            if (ShopApi.isShop(surroundingChest)) {
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
        final ServerShop shop = new ServerShop(player.getUniqueId(), new ItemStack(Material.STRUCTURE_VOID), location);

        System.out.println("shop = " + shop);

        shop.amount(100_000); // TODO: 26.08.2023 remove this line when finished with testing

        shop.create().thenAcceptAsync(created -> {
            if (created == null) {
                player.sendMessage(MessageManager.getShopCreatedFailureComponent());
                return;
            }

            ShopApi.getShopManager().addShop(shop);
            ShopApi.getShopManager().makeShop(chest, shop);

            player.sendMessage(MessageManager.getShopCreatedSuccessfullyComponent());
        }).exceptionally(throwable -> {
            DataApi.getDataInstance().logError(getClass(), "Failed to create shop", throwable);
            player.sendMessage(MessageManager.getShopCreatedFailureComponent());
            return null;
        });
    }

}
