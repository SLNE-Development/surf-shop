package dev.slne.base.velocity.command;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;

import dev.slne.base.velocity.VelocityMain;

public class VelocityCommandManager {

    /**
     * Register a command
     * 
     * @param command     the command to register
     * @param commandName the command name
     * @param aliases     the command aliases
     */
    public void registerCommand(SimpleCommand command, String commandName, String... aliases) {
        CommandManager commandManager = VelocityMain.getInstance().getProxyServer().getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder(commandName).aliases(aliases).build();

        commandManager.register(commandMeta, command);
    }

    /**
     * Register all commands
     */
    public void registerCommands() {
    }

}
