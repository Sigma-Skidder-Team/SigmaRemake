package io.github.sst.remake.manager.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.sst.remake.command.Command;
import io.github.sst.remake.command.impl.HelpCommand;
import io.github.sst.remake.command.impl.SayCommand;
import io.github.sst.remake.manager.Manager;
import io.github.sst.remake.util.IMinecraft;
import net.minecraft.command.CommandSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CommandManager extends Manager implements IMinecraft {
    public static final String PREFIX = ".";

    public List<Command> commands;
    private final Map<String, Command> commandLookup = new LinkedHashMap<>();
    private CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();

    @Override
    public void init() {
        commands = new ArrayList<>();
        commandLookup.clear();
        dispatcher = new CommandDispatcher<>();

        register(new HelpCommand());
        register(new SayCommand());

        super.init();
    }

    public void register(Command command) {
        commands.add(command);

        for (String literal : command.getLiterals()) {
            commandLookup.put(literal.toLowerCase(Locale.ROOT), command);
            dispatcher.register(buildLiteral(command, literal.toLowerCase(Locale.ROOT)));
        }
    }

    public boolean isClientCommand(String input) {
        return input != null && input.startsWith(PREFIX);
    }

    public boolean execute(String input) {
        if (!isClientCommand(input)) {
            return false;
        }

        String raw = input.substring(PREFIX.length()).trim();
        if (raw.isEmpty()) {
            addChatMessage("&cUnknown command.");
            return true;
        }

        String[] parts = raw.split("\\s+");
        Command command = getCommand(parts[0]);

        if (command == null) {
            addChatMessage("&cUnknown command.");
            return true;
        }

        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];

        try {
            command.execute(args);
        } catch (Exception exception) {
            addChatMessage("&cCommand failed: &r" + exception.getMessage());
        }

        return true;
    }

    public Command getCommand(String literal) {
        if (literal == null) {
            return null;
        }
        return commandLookup.get(literal.toLowerCase(Locale.ROOT));
    }

    public CommandDispatcher<CommandSource> getDispatcher() {
        return dispatcher;
    }

    private LiteralArgumentBuilder<CommandSource> buildLiteral(Command command, String literal) {
        return LiteralArgumentBuilder.<CommandSource>literal(PREFIX + literal)
                .executes(context -> executeCommand(command, context.getInput()))
                .then(com.mojang.brigadier.builder.RequiredArgumentBuilder
                        .<CommandSource, String>argument("args", StringArgumentType.greedyString())
                        .suggests((context, builder) -> suggest(command, context, builder))
                        .executes(context -> executeCommand(command, context.getInput())));
    }

    private int executeCommand(Command command, String input) {
        command.execute(parseExecutionArgs(input));
        return 1;
    }

    private CompletableFuture<Suggestions> suggest(Command command, CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        String[] args = parseSuggestionArgs(context.getInput(), builder.getStart());
        List<String> suggestions = command.tab(args);
        String remaining = builder.getRemaining().toLowerCase();

        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(suggestion);
            }
        }

        return builder.buildFuture();
    }

    private String[] parseExecutionArgs(String input) {
        int firstSpace = input.indexOf(' ');
        if (firstSpace < 0 || firstSpace + 1 >= input.length()) {
            return new String[0];
        }
        return Arrays.stream(input.substring(firstSpace + 1).trim().split("\\s+"))
                .filter(part -> !part.isEmpty())
                .toArray(String[]::new);
    }

    private String[] parseSuggestionArgs(String input, int cursor) {
        int firstSpace = input.indexOf(' ');
        if (firstSpace < 0) {
            return new String[0];
        }

        String argumentInput = input.substring(firstSpace + 1, Math.min(cursor, input.length()));

        if (argumentInput.isEmpty()) {
            return new String[]{""};
        }

        String[] split = argumentInput.split("\\s+", -1);
        if (argumentInput.charAt(0) == ' ') {
            split = Arrays.copyOfRange(split, 1, split.length);
        }

        return split.length == 0 ? new String[]{""} : split;
    }
}