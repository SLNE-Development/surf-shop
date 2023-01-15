package dev.slne.base.velocity.command.subcommand;

import java.util.List;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand.Invocation;

public interface VelocitySubCommand {

    /**
     * Executes the subcommand
     * 
     * @param invocation The invocation of the command
     * @param source     The source of the command
     * @param args       The arguments of the command
     * @return If the command was executed successfully
     */
    public boolean execute(Invocation invocation, CommandSource source, String[] args);

    /**
     * Suggestions on command
     * 
     * @param invocation The invocation of the command
     * @param source     The source of the command
     * @param args       The arguments of the command
     * @return The suggestions
     */
    public List<String> suggest(Invocation invocation, CommandSource source, String[] args);

}
