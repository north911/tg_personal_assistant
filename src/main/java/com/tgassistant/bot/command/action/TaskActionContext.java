package com.tgassistant.bot.command.action;

import java.util.List;

import com.tgassistant.domain.Task;
import com.tgassistant.domain.TaskType;
import com.tgassistant.service.TaskService;

/**
 * What a {@link TaskAction} is acting on: which task type, and how to reach the command that
 * owns it. Passed at call time so actions stay stateless singletons shared by every task type.
 *
 * @param commandName the owning command as typed, e.g. {@code /day} — actions build their
 *                    buttons' callback data from it
 * @param chatId      chat the tap came from, for actions that have to remember something
 *                    about it until the next message
 */
public record TaskActionContext(TaskService taskService, TaskType taskType, String commandName, long chatId) {

    public List<Task> tasks() {
        return taskService.tasksOfType(taskType);
    }

    public String typeLabel() {
        return taskType.label();
    }

    /**
     * Callback data that runs {@code action} on this task type, e.g. {@code /day :add}.
     */
    public String commandFor(TaskAction action) {
        return "%s %s".formatted(commandName, action.token());
    }

    /**
     * As {@link #commandFor(TaskAction)}, with an argument for the action, e.g.
     * {@code /day :delete 7}.
     */
    public String commandFor(TaskAction action, Object argument) {
        return "%s %s %s".formatted(commandName, action.token(), argument);
    }
}
