package com.tgassistant.bot.command;

import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Lists every registered command. Takes an {@link ObjectProvider} rather than a
 * {@code List<BotCommand>} so it does not depend on itself while the context starts up.
 */
@Component
public class HelpCommand implements BotCommand {

    private final ObjectProvider<BotCommand> commands;

    public HelpCommand(ObjectProvider<BotCommand> commands) {
        this.commands = commands;
    }

    @Override
    public String name() {
        return "/help";
    }

    @Override
    public String description() {
        return "Show the available commands";
    }

    @Override
    public String execute(CommandRequest request) {
        return commands.orderedStream()
                .sorted(Comparator.comparing(BotCommand::name))
                .map(command -> "%s - %s".formatted(command.name(), command.description()))
                .collect(Collectors.joining("\n", "Available commands:\n", ""));
    }
}