package com.tgassistant.bot.command.action;

import java.util.Optional;

import com.tgassistant.bot.command.CommandReply;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Adding needs text the buttons cannot carry, so this points at the typed form rather than
 * doing the work — the command itself adds whatever free text follows it.
 */
@Component
@Order(20)
public class AddAction implements TaskAction {

    @Override
    public String token() {
        return ":add";
    }

    @Override
    public Optional<String> menuLabel() {
        return Optional.of("Add");
    }

    @Override
    public CommandReply apply(TaskActionContext context, String arguments) {
        return CommandReply.text("Send: %s <task>[, <task>, ...]".formatted(context.commandName()));
    }
}
