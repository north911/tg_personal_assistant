package com.tgassistant.bot.command.action;

import java.util.Optional;

import com.tgassistant.bot.command.CommandReply;
import com.tgassistant.bot.command.PendingInputStore;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Asks for the task text, since a button cannot carry it.
 *
 * <p>Registers the chat as awaiting input and replies with a prompt; the client focuses the
 * input box, and {@code CommandDispatcher} hands whatever is typed next back to this task
 * type as if it had been typed after the command. The prompt expires, so an abandoned one
 * never swallows an unrelated message.
 */
@Component
@Order(20)
public class AddAction implements TaskAction {

    private final PendingInputStore pendingInput;

    public AddAction(PendingInputStore pendingInput) {
        this.pendingInput = pendingInput;
    }

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
        pendingInput.await(context.chatId(), context.commandName());
        return CommandReply.prompt(
                "What %s task should I add? Separate several with commas.".formatted(context.typeLabel()),
                "e.g. buy milk, walk the dog");
    }
}
