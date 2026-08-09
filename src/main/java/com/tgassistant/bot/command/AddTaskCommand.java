package com.tgassistant.bot.command;

import java.util.Arrays;
import java.util.List;

import com.tgassistant.domain.TaskType;
import com.tgassistant.service.TaskService;

/**
 * Shared behaviour for commands that store tasks of one {@link TaskType}: everything
 * the user types after the command is a comma-separated list of descriptions, e.g.
 * {@code /day buy milk, walk the dog}.
 *
 * <p>A new recurrence only needs a subclass passing its own name and type.
 */
public abstract class AddTaskCommand implements BotCommand {

    private final TaskService taskService;
    private final TaskType taskType;

    protected AddTaskCommand(TaskService taskService, TaskType taskType) {
        this.taskService = taskService;
        this.taskType = taskType;
    }

    @Override
    public String label() {
        String type = typeLabel();
        return Character.toUpperCase(type.charAt(0)) + type.substring(1) + " tasks";
    }

    @Override
    public String description() {
        return "Add %s tasks: %s <task>[, <task>, ...]".formatted(typeLabel(), name());
    }

    @Override
    public CommandReply execute(CommandRequest request) {
        List<String> descriptions = splitDescriptions(request.arguments());
        if (descriptions.isEmpty()) {
            return CommandReply.text("Usage: %s <task>[, <task>, ...]".formatted(name()));
        }
        taskService.addTasks(descriptions, taskType);
        return CommandReply.text(formatConfirmation(descriptions));
    }

    private String typeLabel() {
        return taskType.label();
    }

    private static List<String> splitDescriptions(String arguments) {
        return Arrays.stream(arguments.split(","))
                .map(String::strip)
                .filter(description -> !description.isEmpty())
                .toList();
    }

    private String formatConfirmation(List<String> descriptions) {
        String plural = descriptions.size() == 1 ? "task" : "tasks";
        StringBuilder reply = new StringBuilder(
                "Added %d %s %s:".formatted(descriptions.size(), typeLabel(), plural));
        descriptions.forEach(description -> reply.append("\n- ").append(description));
        return reply.toString();
    }
}