package dev.slne.surf.shop.server.listener.listeners;

import dev.slne.data.api.DataApi;
import dev.slne.surf.shop.server.instance.BukkitApi;
import dev.slne.surf.shop.server.listener.events.state.ShopRemoveEvent;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.Shop;
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

import java.util.Objects;
import java.util.UUID;

public class ShopBreakListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Player player = event.getPlayer();
        final Material type = block.getType();

        if (!type.equals(Material.CHEST)) {
            return;
        }

        if (!(block.getState() instanceof Chest chest)) {
            return;
        }

        final PersistentDataContainer container = chest.getPersistentDataContainer();

        if (!container.has(Shop.SHOP_KEY, PersistentDataType.STRING)) {
            return;
        }

        final String shopUuidString = container.get(Shop.SHOP_KEY, PersistentDataType.STRING);

        assert shopUuidString != null;

        final UUID shopUuid = UUID.fromString(shopUuidString);
        final Shop shop = BukkitApi.getInstance().getShopManager().getShop(shopUuid);

        if (shop == null) {
            player.sendMessage(MessageManager.getShopRemovedFailureComponent());
            event.setCancelled(true);
            return;
        }

        final UUID ownerUuid = shop.getOwnerUuid();
        if (!Objects.equals(ownerUuid, player.getUniqueId())) {
            player.sendMessage(MessageManager.getPlayerNotOwningShopComponent());
            event.setCancelled(true);
            return;
        }

        if (!shop.isInventoryEmpty()) {
            player.sendMessage(MessageManager.getShopNotEmptiedComponent());
            event.setCancelled(true);
            return;
        }

        final ShopRemoveEvent shopRemoveEvent = new ShopRemoveEvent(shop, player);

        if (!shopRemoveEvent.callEvent()) {
            shopRemoveEvent.applyCancelled(event.getPlayer());
            event.setCancelled(true);
            return;
        }

        if (shop.isDeleting()) {
            player.sendMessage(MessageManager.getShopAlreadyRemovingComponent());
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
            DataApi.getDataInstance().logError(getClass(), "Failed to delete shop", throwable);
            player.sendMessage(MessageManager.getShopRemovedFailureComponent());
            event.setCancelled(true);
            return null;
        });
    }

}
