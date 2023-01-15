package dev.slne.base.bukkit.command.subcommand;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface BukkitSubCommand {

    /**
     * Executes on command
     * 
     * @param sender The sender of the command
     * @param args   The arguments of the command
     */
    public boolean execute(CommandSender sender, String[] args);

    /**
     * Suggestions on command
     * 
     * @param sender The sender of the command
     * @param args   The arguments of the command
     */
    public List<String> suggest(CommandSender sender, String[] args);

}
