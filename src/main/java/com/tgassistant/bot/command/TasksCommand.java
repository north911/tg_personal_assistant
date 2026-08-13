package com.tgassistant.bot.command;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.tgassistant.domain.TaskType;
import com.tgassistant.service.TaskService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * The bot's entry point, reached by the persistent <em>TASKS</em> button on the
 * {@link ChatKeyboard}.
 *
 * <p>Typed bare it answers with every registered command as an inline button, so the whole
 * bot is reachable by tapping. Given a task type ({@code /tasks daily}) it lists those tasks
 * instead — that branch has no button of its own and is only reachable by typing.
 *
 * <p>Takes an {@link ObjectProvider} rather than a {@code List<BotCommand>} because it is
 * itself a {@code BotCommand} and would otherwise depend on itself while the context starts up.
 */
@Component
public class TasksCommand implements BotCommand {

    static final String NAME = "/tasks";

    private final TaskService taskService;
    private final ObjectProvider<BotCommand> commands;

    public TasksCommand(TaskService taskService, ObjectProvider<BotCommand> commands) {
        this.taskService = taskService;
        this.commands = commands;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String label() {
        return "Tasks";
    }

    @Override
    public String description() {
        return "Show what this bot can do, or list tasks: %s [%s]".formatted(NAME, typeLabels("|"));
    }

    @Override
    public CommandReply execute(CommandRequest request) {
        if (!request.hasArguments()) {
            return commandMenu();
        }
        return parseType(request.arguments())
                .map(this::listTasks)
                .orElseGet(() -> CommandReply.text("Unknown task type '%s'. Expected one of: %s"
                        .formatted(request.arguments(), typeLabels(", "))));
    }

    /**
     * One button per registered command, so a new command shows up here on its own. The
     * button shows the command's {@link BotCommand#label()} but still carries its {@code name()}
     * as callback data, which is what routes when tapped.
     */
    private CommandReply commandMenu() {
        List<ReplyButton> buttons = commands.orderedStream()
                .sorted(Comparator.comparing(BotCommand::label))
                .map(command -> new ReplyButton(command.label(), command.name()))
                .toList();
        return CommandReply.withButtons("Available commands:", buttons);
    }

    private CommandReply listTasks(TaskType type) {
        return CommandReply.text(TaskListFormatter.format(taskService.tasksOfType(type), type));
    }

    private static Optional<TaskType> parseType(String arguments) {
        return Arrays.stream(TaskType.values())
                .filter(type -> type.label().equalsIgnoreCase(arguments))
                .findFirst();
    }

    private static String typeLabels(String separator) {
        return Arrays.stream(TaskType.values())
                .map(TaskType::label)
                .collect(Collectors.joining(separator));
    }
}
