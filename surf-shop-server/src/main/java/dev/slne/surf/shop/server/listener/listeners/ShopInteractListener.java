package dev.slne.surf.shop.server.listener.listeners;

import java.util.Objects;
import java.util.UUID;

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
import dev.slne.surf.shop.server.shop.Shop;
import dev.slne.surf.shop.server.shop.gui.ShopMainMenu;

public class ShopInteractListener implements Listener {

    @EventHandler
    public void onShopInteract(PlayerInteractEvent event) {
        Action action = event.getAction();

        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        EquipmentSlot hand = event.getHand();
        if (!Objects.equals(hand, EquipmentSlot.HAND)) {
            return;
        }

        final Block block = event.getClickedBlock();
        if (block == null) {
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
            return;
        }

        final Player player = event.getPlayer();
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

        if (shop.getItemStack() == null && !isOwner) {
            player.sendMessage(MessageManager.getShopIsNotSetupComponent());
            event.setCancelled(true);
            return;
        }

        ShopMainMenu mainMenu = new ShopMainMenu(shop, player);

        event.setCancelled(true);
        shop.lock(player);
        mainMenu.show(player);
    }
}
