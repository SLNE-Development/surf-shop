package dev.slne.surf.shop.server.listener.listeners;

import dev.slne.data.api.DataApi;
import dev.slne.surf.shop.api.ShopApi;
import dev.slne.surf.shop.api.events.state.ShopRemoveEvent;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.instance.BukkitApi;
import dev.slne.surf.shop.server.message.MessageManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Objects;
import java.util.UUID;

public class ShopBreakListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Player player = event.getPlayer();

        if (!ShopApi.getInstance().isShop(block)) {
            return;
        }

        final Shop shop = ShopApi.getShop(block);

        if (shop == null) {
            player.sendMessage(MessageManager.getShopRemovedFailureComponent());
            event.setCancelled(true);
            return;
        }

        if (!shop.isOwner(player)) {
            player.sendMessage(MessageManager.getPlayerNotOwningShopComponent());
            event.setCancelled(true);
            return;
        }

        if (!shop.isInventoryEmpty()) {
            player.sendMessage(MessageManager.getShopNotEmptiedComponent());
            event.setCancelled(true);
            return;
        }

        final ShopRemoveEvent shopRemoveEvent = new ShopRemoveEvent(shop, player, event.isAsynchronous());

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
                event.setCancelled(true); // TODO: Can you cancel an event later?
            }
        }).exceptionally(throwable -> {
            DataApi.getDataInstance().logError(getClass(), "Failed to delete shop", throwable);
            player.sendMessage(MessageManager.getShopRemovedFailureComponent());
            event.setCancelled(true);
            return null;
        });
    }

}
