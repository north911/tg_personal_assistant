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
    public String description() {
        return "Add %s tasks: %s <task>[, <task>, ...]".formatted(label(), name());
    }

    @Override
    public String execute(CommandRequest request) {
        List<String> descriptions = splitDescriptions(request.arguments());
        if (descriptions.isEmpty()) {
            return "Usage: %s <task>[, <task>, ...]".formatted(name());
        }
        taskService.addTasks(descriptions, taskType);
        return formatConfirmation(descriptions);
    }

    private String label() {
        return taskType.name().toLowerCase();
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
                "Added %d %s %s:".formatted(descriptions.size(), label(), plural));
        descriptions.forEach(description -> reply.append("\n- ").append(description));
        return reply.toString();
    }
}