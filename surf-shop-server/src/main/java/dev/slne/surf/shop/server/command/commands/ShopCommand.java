package dev.slne.surf.shop.server.command.commands;

import dev.slne.surf.shop.api.ShopApi;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.ServerShop;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;

public class ShopCommand extends CommandAPICommand {

    /**
     * A new {@link ShopCommand} instance
     */
    public ShopCommand() {
        super("shop");

        withPermission("surf.shop.give");

        executesPlayer((player, args) -> {
            player.getInventory().addItem(ShopApi.constructCreationItem());
        });

        register();
    }

}
