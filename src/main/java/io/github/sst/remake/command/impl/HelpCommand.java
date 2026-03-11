package io.github.sst.remake.command.impl;

import io.github.sst.remake.Client;
import io.github.sst.remake.command.Command;
import io.github.sst.remake.manager.impl.CommandManager;

import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "See information about the client.", "?", "sos");
    }

    @Override
    public void execute(String[] args) {
        List<String> commands = Client.INSTANCE.commandManager.commands.stream()
                .map(command -> "&e" + CommandManager.PREFIX + "&6" + command.getPrimaryLiteral() + " &7- &f" + command.description)
                .collect(Collectors.toList());

        addChatMessage("Available commands: \n" + String.join("\n", commands));
    }
}