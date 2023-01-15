package dev.slne.base.velocity.command.subcommand;

import java.util.List;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand.Invocation;

public interface VelocitySubCommand {

    public boolean execute(Invocation invocation, CommandSource source, String[] args);

    public List<String> suggest(Invocation invocation, CommandSource source, String[] args);

}
