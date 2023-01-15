package dev.slne.base.bukkit.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

import dev.slne.base.bukkit.command.subcommand.BukkitSubCommand;

public abstract class BukkitCommand implements CommandExecutor, TabCompleter {

    private List<BukkitSubCommand> subCommands;

    /**
     * A {@link BukkitCommand}
     * 
     * @param command the {@link PluginCommand} to register the
     *                {@link CommandExecutor} and {@link TabCompleter} to
     */
    public BukkitCommand(PluginCommand command) {
        command.setExecutor(this);
        command.setTabCompleter(this);

        this.subCommands = new ArrayList<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean handled = false;

        for (BukkitSubCommand subCommand : subCommands) {
            boolean subHandled = subCommand.execute(sender, args);

            if (subHandled) {
                handled = true;
            }
        }

        if (!handled) {
            supplyHelp(sender, args);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();

        for (BukkitSubCommand subCommand : subCommands) {
            List<String> subSuggestions = subCommand.suggest(sender, args);
            suggestions.addAll(subSuggestions);
        }

        return suggestions;
    }

    /**
     * Supplies help to the command source
     * 
     * @param sender the {@link CommandSender}
     * @param args   the arguments
     */
    public abstract void supplyHelp(CommandSender sender, String[] args);

    /**
     * Returns the {@link List} of {@link BukkitSubCommand}
     * 
     * @return the {@link List} of {@link BukkitSubCommand}
     */
    public List<BukkitSubCommand> getSubCommands() {
        return subCommands;
    }

}
