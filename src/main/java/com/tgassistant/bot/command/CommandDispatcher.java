package com.tgassistant.bot.command;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

/**
 * Routes an incoming message to the {@link BotCommand} that owns it.
 *
 * <p>Spring injects every {@code BotCommand} bean here, so the routing table builds
 * itself: a new command is registered simply by existing as a bean.
 */
@Component
public class CommandDispatcher {

    private final Map<String, BotCommand> commandsByName;
    private final Map<String, String> commandsByButtonLabel;
    private final PendingInputStore pendingInput;

    public CommandDispatcher(List<BotCommand> commands, ChatKeyboard chatKeyboard,
                             PendingInputStore pendingInput) {
        this.pendingInput = pendingInput;
        Map<String, BotCommand> byName = new LinkedHashMap<>();
        for (BotCommand command : commands) {
            String name = command.name();
            if (name == null || !name.startsWith("/") || !name.equals(name.toLowerCase())) {
                throw new IllegalStateException(
                        "%s must expose a lower-case name starting with '/', got: %s"
                                .formatted(command.getClass().getName(), name));
            }
            BotCommand previous = byName.put(name, command);
            if (previous != null) {
                throw new IllegalStateException("Duplicate command %s declared by %s and %s"
                        .formatted(name, previous.getClass().getName(), command.getClass().getName()));
            }
        }
        // Not Map.copyOf: that leaves iteration order unspecified, and commands() should
        // stay in a stable order.
        this.commandsByName = Collections.unmodifiableMap(byName);
        this.commandsByButtonLabel = chatKeyboard.buttons().stream()
                .collect(Collectors.toUnmodifiableMap(ReplyButton::label, ReplyButton::command));
    }

    /**
     * Routes a typed message, a tapped panel button — which arrives as its plain label and is
     * translated to the command it stands for first — an inline button's callback data, which
     * is already a command, or an answer to a command that asked for input. All take one path.
     *
     * @return the reply to send back, or empty when the message is not a known command.
     */
    public Optional<CommandReply> dispatch(String messageText, long chatId) {
        String resolved = resolve(messageText, chatId);
        return CommandRequest.parse(resolved, chatId)
                .flatMap(request -> Optional.ofNullable(commandsByName.get(request.command()))
                        .map(command -> command.execute(request)));
    }

    /**
     * Turns whatever arrived into the command it stands for.
     *
     * <p>The order is the whole point. A panel button sends its plain label, so labels are
     * matched before anything else — otherwise tapping TASKS while a prompt is open would file
     * "TASKS" as a task. Commands come next and abandon the prompt, so changing your mind
     * mid-question works. Only what is left over answers the prompt.
     */
    private String resolve(String messageText, long chatId) {
        String text = messageText.strip();
        String buttonCommand = commandsByButtonLabel.get(text);
        if (buttonCommand != null) {
            pendingInput.clear(chatId);
            return buttonCommand;
        }
        if (text.startsWith("/")) {
            pendingInput.clear(chatId);
            return messageText;
        }
        return pendingInput.consume(chatId)
                .map(command -> "%s %s".formatted(command, text))
                .orElse(messageText);
    }

    /**
     * All registered commands, in registration order.
     *
     * <p>No production caller today: the {@code /tasks} menu builds its own list from an
     * {@code ObjectProvider<BotCommand>}, because as a command itself it cannot depend on
     * this dispatcher without a startup cycle.
     */
    public List<BotCommand> commands() {
        return List.copyOf(commandsByName.values());
    }
}