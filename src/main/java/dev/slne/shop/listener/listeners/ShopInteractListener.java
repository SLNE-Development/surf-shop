package dev.slne.shop.listener.listeners;

import java.util.UUID;

import org.bukkit.Material;
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

import dev.slne.shop.instance.BukkitApi;
import dev.slne.shop.message.MessageManager;
import dev.slne.shop.shop.Shop;
import dev.slne.shop.shop.gui.ShopMainMenu;

public class ShopInteractListener implements Listener {

    @EventHandler
    public void onShopInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        EquipmentSlot hand = event.getHand();
        if (!hand.equals(EquipmentSlot.HAND)) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        if (!block.getType().equals(Material.CHEST)) {
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
            return;
        }

        Player player = event.getPlayer();
        boolean isOwner = shop.isOwner(player);
        boolean isMember = shop.isMember(player);

        if (isOwner || isMember && shop.isLocked()) {
            Player lockedByPlayer = shop.getLockedByPlayer();

            if (lockedByPlayer != null && lockedByPlayer.isOnline()) {
                shop.unlock();
                lockedByPlayer.closeInventory(Reason.UNKNOWN);
                lockedByPlayer.sendMessage(MessageManager.getClosedDueToEditorRequest(isOwner, isMember, player));
            }
        }

        if (shop.isLocked()) {
            player.sendMessage(MessageManager.getShopIsLockedComponent(shop.getLockedByPlayer()));
            event.setCancelled(true);
            return;
        }

        ShopMainMenu mainMenu = new ShopMainMenu(shop);

        event.setCancelled(true);
        shop.lock(player);
        mainMenu.show(player);
    }
}
