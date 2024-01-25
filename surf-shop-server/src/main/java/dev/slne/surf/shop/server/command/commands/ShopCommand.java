package dev.slne.surf.shop.server.command.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.slne.surf.shop.api.ShopApi;

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
