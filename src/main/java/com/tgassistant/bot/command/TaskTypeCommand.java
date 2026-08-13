package com.tgassistant.bot.command;

import java.util.Arrays;
import java.util.List;

import com.tgassistant.bot.command.action.TaskAction;
import com.tgassistant.bot.command.action.TaskActionContext;
import com.tgassistant.domain.TaskType;
import com.tgassistant.service.TaskService;

/**
 * Everything you can do with tasks of one {@link TaskType}.
 *
 * <p>Tapped from the {@code /tasks} menu it lists the tasks of its type and offers one button
 * per {@link TaskAction} under them, so the list is in view while you act on it; typed with a
 * description it adds tasks, e.g. {@code /day buy milk, walk the dog}.
 *
 * <p>The two cannot be confused because every action token starts with {@code :} —
 * {@code /day :add} runs the Add action, while {@code /day add} adds a task called "add".
 * Tokens are only ever produced by buttons, so the marker never has to be typed.
 *
 * <p>Actions are injected rather than enumerated here, so a new one is a {@code @Component}
 * implementing {@code TaskAction} and nothing in this class changes. A new recurrence is
 * likewise just a subclass passing its own name and type.
 */
public abstract class TaskTypeCommand implements BotCommand {

    private final TaskService taskService;
    private final TaskType taskType;
    private final List<TaskAction> actions;

    protected TaskTypeCommand(TaskService taskService, TaskType taskType, List<TaskAction> actions) {
        this.taskService = taskService;
        this.taskType = taskType;
        this.actions = List.copyOf(actions);
    }

    @Override
    public String label() {
        return taskType.buttonLabel();
    }

    @Override
    public String description() {
        return "Manage %s tasks; %s <task>[, <task>, ...] adds".formatted(typeLabel(), name());
    }

    @Override
    public CommandReply execute(CommandRequest request) {
        String arguments = request.arguments();
        if (arguments.isEmpty()) {
            return actionMenu(request.chatId());
        }
        String token = firstWord(arguments);
        return actions.stream()
                .filter(action -> action.token().equals(token))
                .findFirst()
                .map(action -> action.apply(context(request.chatId()), afterFirstWord(arguments)))
                // Anything that is not an action token is a task to add.
                .orElseGet(() -> addTasks(arguments));
    }

    /**
     * The tasks themselves, with the actions as buttons under them — reading the list and acting
     * on it are the same step, so there is no separate View action to tap first.
     */
    private CommandReply actionMenu(long chatId) {
        TaskActionContext context = context(chatId);
        List<ReplyButton> buttons = actions.stream()
                .flatMap(action -> action.menuLabel().stream()
                        .map(menuLabel -> new ReplyButton(menuLabel, context.commandFor(action))))
                .toList();
        return CommandReply.withButtons(TaskListFormatter.format(context.tasks(), taskType), buttons);
    }

    private CommandReply addTasks(String arguments) {
        List<String> descriptions = splitDescriptions(arguments);
        if (descriptions.isEmpty()) {
            return CommandReply.text("Usage: %s <task>[, <task>, ...]".formatted(name()));
        }
        taskService.addTasks(descriptions, taskType);
        return CommandReply.text(formatConfirmation(descriptions));
    }

    private TaskActionContext context(long chatId) {
        return new TaskActionContext(taskService, taskType, name(), chatId);
    }

    private String typeLabel() {
        return taskType.label();
    }

    private static String firstWord(String arguments) {
        int space = arguments.indexOf(' ');
        return space < 0 ? arguments : arguments.substring(0, space);
    }

    private static String afterFirstWord(String arguments) {
        int space = arguments.indexOf(' ');
        return space < 0 ? "" : arguments.substring(space + 1).strip();
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
