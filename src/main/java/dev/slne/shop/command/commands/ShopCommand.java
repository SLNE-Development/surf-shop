package dev.slne.shop.command.commands;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import dev.jorel.commandapi.CommandTree;
import dev.slne.shop.message.MessageManager;
import dev.slne.shop.shop.Shop;
import net.kyori.adventure.text.Component;

public class ShopCommand extends CommandTree {

    /**
     * A new {@link ShopCommand} instance
     */
    public ShopCommand() {
        super("shop");

        withPermission("surf.shop");

        executesPlayer((player, args) -> {
            ItemStack chest = new ItemStack(Material.CHEST);
            ItemMeta meta = chest.getItemMeta();

            meta.displayName(Component.text("Shop", MessageManager.PRIMARY));
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(Shop.SHOP_KEY, PersistentDataType.STRING, player.getUniqueId().toString());
            chest.setItemMeta(meta);

            player.getInventory().addItem(chest);
        });

        register();
    }

}
