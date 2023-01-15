package dev.slne.base.velocity.command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

import dev.slne.base.velocity.command.subcommand.VelocitySubCommand;

public abstract class VelocityCommand implements SimpleCommand {

    private List<VelocitySubCommand> subCommands;

    public VelocityCommand() {
        this.subCommands = new ArrayList<>();
    }

    @Override
    public void execute(Invocation invocation) {
        boolean handled = false;

        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        for (VelocitySubCommand subCommand : subCommands) {
            boolean subHandled = subCommand.execute(invocation, source, args);

            if (subHandled) {
                handled = true;
            }
        }

        if (!handled) {
            supplyHelp(invocation, source, args);
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        List<String> suggestions = new ArrayList<>();

        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        for (VelocitySubCommand subCommand : subCommands) {
            List<String> subSuggestions = subCommand.suggest(invocation, source, args);
            suggestions.addAll(subSuggestions);
        }

        return CompletableFuture.completedFuture(suggestions);
    }

    /**
     * Supplies help to the command source
     * 
     * @param invocation the {@link Invocation}
     * @param source     the {@link CommandSource}
     * @param args       the arguments
     */
    public abstract void supplyHelp(Invocation invocation, CommandSource source, String[] args);

    /**
     * Returns the {@link List} of {@link VelocitySubCommand}s
     * 
     * @return the {@link List} of {@link VelocitySubCommand}s
     */
    public List<VelocitySubCommand> getSubCommands() {
        return subCommands;
    }
}
