package dev.slne.shop.command;

import dev.slne.shop.command.commands.ShopCommand;

public class BukkitCommandManager {

    /**
     * Register all commands
     */
    public void registerCommands() {
        new ShopCommand();
    }

}
