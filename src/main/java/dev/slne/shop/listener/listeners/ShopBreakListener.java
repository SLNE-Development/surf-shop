package dev.slne.shop.listener.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import dev.slne.shop.instance.BukkitApi;
import dev.slne.shop.listener.events.state.ShopRemoveEvent;
import dev.slne.shop.message.MessageManager;
import dev.slne.shop.shop.Shop;

public class ShopBreakListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        Material type = block.getType();

        if (!type.equals(Material.CHEST)) {
            return;
        }

        Chest chest = (Chest) block.getState();
        PersistentDataContainer container = chest.getPersistentDataContainer();

        if (!container.has(Shop.SHOP_KEY, PersistentDataType.STRING)) {
            return;
        }

        String shopUuidString = container.get(Shop.SHOP_KEY, PersistentDataType.STRING);
        UUID shopUuid = UUID.fromString(shopUuidString);

        Shop shop = BukkitApi.getInstance().getShopManager().getShop(shopUuid);

        if (shop == null) {
            player.sendMessage(MessageManager.getShopRemovedFailureComponent());
            event.setCancelled(true);
            return;
        }

        UUID ownerUuid = shop.getOwnerUuid();
        if (ownerUuid != null && !ownerUuid.equals(player.getUniqueId())) {
            player.sendMessage(MessageManager.getPlayerNotOwningShopComponent());
            event.setCancelled(true);
            return;
        }

        // if (!shop.isInventoryEmpty()) {
        // player.sendMessage(MessageManager.getShopNotEmptiedComponent());
        // event.setCancelled(true);
        // return;
        // }

        ShopRemoveEvent shopRemoveEvent = new ShopRemoveEvent(shop, player);
        Bukkit.getPluginManager().callEvent(shopRemoveEvent);

        if (shopRemoveEvent.isCancelled()) {
            shopRemoveEvent.applyCancelled(event.getPlayer());
            event.setCancelled(true);
            return;
        }

        shop.delete().thenAcceptAsync(deleted -> {
            if (deleted != null) {
                player.sendMessage(MessageManager.getShopRemovedSuccessfullyComponent());
                BukkitApi.getInstance().getShopManager().removeShop(shop);
            } else {
                player.sendMessage(MessageManager.getShopRemovedFailureComponent());
                event.setCancelled(true);
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            player.sendMessage(MessageManager.getShopRemovedFailureComponent());
            event.setCancelled(true);
            return null;
        });
    }

}
