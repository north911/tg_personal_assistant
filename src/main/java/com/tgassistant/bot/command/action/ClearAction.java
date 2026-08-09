package com.tgassistant.bot.command.action;

import java.util.List;
import java.util.Optional;

import com.tgassistant.bot.command.CommandReply;
import com.tgassistant.bot.command.ReplyButton;
import com.tgassistant.domain.Task;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Wiping every task of a type is irreversible and sits one row under the single-task Delete,
 * so this only asks; {@link ClearConfirmedAction} is what actually deletes.
 */
@Component
@Order(40)
public class ClearAction implements TaskAction {

    private final ClearConfirmedAction clearConfirmed;

    public ClearAction(ClearConfirmedAction clearConfirmed) {
        this.clearConfirmed = clearConfirmed;
    }

    @Override
    public String token() {
        return ":clear";
    }

    @Override
    public Optional<String> menuLabel() {
        return Optional.of("Delete all");
    }

    @Override
    public CommandReply apply(TaskActionContext context, String arguments) {
        List<Task> tasks = context.tasks();
        if (tasks.isEmpty()) {
            return CommandReply.text("No %s tasks yet.".formatted(context.typeLabel()));
        }
        String plural = tasks.size() == 1 ? "task" : "tasks";
        return CommandReply.withButtons(
                "Delete all %d %s %s? This cannot be undone."
                        .formatted(tasks.size(), context.typeLabel(), plural),
                List.of(new ReplyButton("Yes, delete all", context.commandFor(clearConfirmed)),
                        new ReplyButton("Cancel", context.commandName())));
    }
}
