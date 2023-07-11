package dev.slne.shop.listener.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
        PersistentDataContainer container = chest.getPersistentDataContainer();

        Location location = block.getLocation();
        Shop shop = new Shop(player.getUniqueId(), new ItemStack(Material.ACACIA_BOAT), location);
        shop.create().thenAcceptAsync(created -> {
            if (created == null) {
                printShopCreationFailed(player);
                return;
            }

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
