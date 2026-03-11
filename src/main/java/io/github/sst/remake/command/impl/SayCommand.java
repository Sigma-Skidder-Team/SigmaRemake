package io.github.sst.remake.command.impl;

import io.github.sst.remake.command.Command;

public class SayCommand extends Command {
    public SayCommand() {
        super("say", "Say stuff.", "me");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            addChatMessage("&cA message must be typed!");
            return;
        }
        sendChatMessage(String.join(" ", args));
    }
}