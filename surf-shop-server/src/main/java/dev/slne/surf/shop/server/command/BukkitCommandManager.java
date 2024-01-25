package dev.slne.surf.shop.server.command;

import dev.slne.surf.shop.server.command.commands.ShopCommand;

public class BukkitCommandManager {

    /**
     * Register all commands
     */
    public void registerCommands() {
        new ShopCommand();
    }
}
