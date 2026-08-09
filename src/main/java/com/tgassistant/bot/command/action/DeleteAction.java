package com.tgassistant.bot.command.action;

import java.util.List;
import java.util.Optional;

import com.tgassistant.bot.command.CommandReply;
import com.tgassistant.bot.command.ReplyButton;
import com.tgassistant.domain.Task;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Deletes one task. Bare, it offers a button per task; with an id it removes that one and
 * offers whatever is left, so several can go without reopening the menu.
 */
@Component
@Order(30)
public class DeleteAction implements TaskAction {

    /**
     * Telegram renders long button faces poorly, and a task description has no length limit.
     */
    private static final int MAX_BUTTON_LABEL = 40;

    @Override
    public String token() {
        return ":delete";
    }

    @Override
    public Optional<String> menuLabel() {
        return Optional.of("Delete");
    }

    @Override
    public CommandReply apply(TaskActionContext context, String arguments) {
        return arguments.isEmpty() ? chooseTask(context) : deleteTask(context, arguments);
    }

    private CommandReply chooseTask(TaskActionContext context) {
        List<Task> tasks = context.tasks();
        if (tasks.isEmpty()) {
            return CommandReply.text("No %s tasks yet.".formatted(context.typeLabel()));
        }
        return CommandReply.withButtons(
                "Which %s task should go?".formatted(context.typeLabel()), buttons(context, tasks));
    }

    private CommandReply deleteTask(TaskActionContext context, String rawId) {
        Optional<Long> id = parseId(rawId);
        if (id.isEmpty()) {
            return CommandReply.text("That is not a task I can delete.");
        }
        if (!context.taskService().deleteTask(id.get(), context.taskType())) {
            // The button outlived the task — deleted from another chat, or tapped twice.
            return CommandReply.text("That task is already gone.");
        }
        List<Task> remaining = context.tasks();
        if (remaining.isEmpty()) {
            return CommandReply.text("Deleted. No %s tasks left.".formatted(context.typeLabel()));
        }
        return CommandReply.withButtons("Deleted. Tap another to delete:", buttons(context, remaining));
    }

    private List<ReplyButton> buttons(TaskActionContext context, List<Task> tasks) {
        return tasks.stream()
                .map(task -> new ReplyButton(
                        buttonLabel(task.getDescription()),
                        context.commandFor(this, task.getId())))
                .toList();
    }

    private static String buttonLabel(String description) {
        return description.length() <= MAX_BUTTON_LABEL
                ? description
                : description.substring(0, MAX_BUTTON_LABEL - 1) + "…";
    }

    private static Optional<Long> parseId(String rawId) {
        try {
            return Optional.of(Long.valueOf(rawId));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
