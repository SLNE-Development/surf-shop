package dev.slne.surf.shop.server.listener.listeners;

import java.util.Objects;
import java.util.UUID;

import dev.slne.surf.shop.api.ShopApi;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.shop.gui._2_0.ShopMainMenu;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent.Reason;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import dev.slne.surf.shop.server.instance.BukkitApi;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.ServerShop;

public class ShopInteractListener implements Listener {

    @EventHandler
    public void onShopInteract(PlayerInteractEvent event) {
        final Action action = event.getAction();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        final EquipmentSlot hand = event.getHand();
        if (!Objects.equals(hand, EquipmentSlot.HAND)) {
            return;
        }

        final Block block = event.getClickedBlock();
        if (!ShopApi.isShop(block)) {
            return;
        }

        final Shop shop = ShopApi.getShop(block);

        if (shop == null) {
            return;
        }

        final Player player = event.getPlayer();
        boolean isOwner = shop.isOwner(player);
        boolean isMember = shop.isMember(player);

        if (isOwner || isMember && shop.locked()) {
            shop.getLockedBy().ifPresent(lockedBy -> {
                if (lockedBy.isOnline()) {
                    lockedBy.closeInventory(Reason.UNKNOWN);
                    lockedBy.sendMessage(MessageManager.getClosedDueToEditorRequest(isOwner, isMember, player));
                }
            });
        }

        if (shop.locked()) {
            shop.getLockedBy().ifPresent(lockedBy -> {
                player.sendMessage(MessageManager.getShopIsLockedComponent(shop.getLockedBy().orElseThrow().getPlayer()));
                event.setCancelled(true);
            });
            return;
        }

        if (shop.item() == null && !isOwner) {
            player.sendMessage(MessageManager.getShopIsNotSetupComponent());
            event.setCancelled(true);
            return;
        }

        final ShopMainMenu mainMenu = new ShopMainMenu(shop, player);

        event.setCancelled(true);
        shop.lock(player);
        mainMenu.show(player);
    }
}
