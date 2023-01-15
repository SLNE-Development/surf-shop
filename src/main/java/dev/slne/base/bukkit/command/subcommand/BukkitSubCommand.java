package dev.slne.base.bukkit.command.subcommand;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface BukkitSubCommand {

    public boolean execute(CommandSender sender, String[] args);

    public List<String> suggest(CommandSender sender, String[] args);

}
