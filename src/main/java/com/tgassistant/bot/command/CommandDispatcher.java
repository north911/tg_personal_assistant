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

    public CommandDispatcher(List<BotCommand> commands, ChatKeyboard chatKeyboard) {
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
        // stay in a stable order for /help and Telegram command registration.
        this.commandsByName = Collections.unmodifiableMap(byName);
        this.commandsByButtonLabel = chatKeyboard.buttons().stream()
                .collect(Collectors.toUnmodifiableMap(ReplyButton::label, ReplyButton::command));
    }

    /**
     * Routes a typed message, a tapped panel button — which arrives as its plain label and is
     * translated to the command it stands for first — or an inline button's callback data,
     * which is already a command. All three take one path.
     *
     * @return the reply to send back, or empty when the message is not a known command.
     */
    public Optional<CommandReply> dispatch(String messageText, long chatId) {
        String resolved = commandsByButtonLabel.getOrDefault(messageText.strip(), messageText);
        return CommandRequest.parse(resolved, chatId)
                .flatMap(request -> Optional.ofNullable(commandsByName.get(request.command()))
                        .map(command -> command.execute(request)));
    }

    /**
     * All registered commands, in registration order.
     *
     * <p>No production caller today: {@code /help} and the {@code /tasks} menu each build their
     * own list from an {@code ObjectProvider<BotCommand>}, because as commands themselves they
     * cannot depend on this dispatcher without a startup cycle.
     */
    public List<BotCommand> commands() {
        return List.copyOf(commandsByName.values());
    }
}