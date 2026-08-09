package com.tgassistant.bot.command;

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * The panel Telegram keeps under the text input: a single button that opens the command menu.
 *
 * <p>Single source of truth for that panel — {@code MainTelegramBot} renders these buttons and
 * {@link CommandDispatcher} resolves taps back to commands from the same list.
 */
@Component
public class ChatKeyboard {

    private final List<ReplyButton> buttons = List.of(new ReplyButton("TASKS", TasksCommand.NAME));

    public List<ReplyButton> buttons() {
        return buttons;
    }
}
