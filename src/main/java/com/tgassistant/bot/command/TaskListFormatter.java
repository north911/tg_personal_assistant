package com.tgassistant.bot.command;

import java.util.List;
import java.util.stream.Collectors;

import com.tgassistant.domain.Task;
import com.tgassistant.domain.TaskType;

/**
 * Renders a task list for the chat. Shared so {@code /tasks daily} and the task-type menu
 * cannot drift into wording each other's answers differently.
 */
public final class TaskListFormatter {

    private TaskListFormatter() {
    }

    public static String format(List<Task> tasks, TaskType type) {
        if (tasks.isEmpty()) {
            return "No %s tasks yet.".formatted(type.label());
        }
        return tasks.stream()
                .map(task -> "- " + task.getDescription())
                .collect(Collectors.joining("\n", "Your %s tasks:\n".formatted(type.label()), ""));
    }
}
