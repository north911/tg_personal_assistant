package com.tgassistant.bot.command;

import java.util.List;

/**
 * What a {@link BotCommand} sends back: some text, optionally with inline buttons attached
 * to that message.
 *
 * <p>Deliberately free of Telegram classes — commands describe <em>what</em> to reply and
 * {@code MainTelegramBot} decides how to render it, which keeps commands unit-testable
 * without the Telegram API.
 *
 * @param buttons inline buttons to attach, empty for a plain text reply
 */
public record CommandReply(String text, List<ReplyButton> buttons) {

    public CommandReply {
        buttons = List.copyOf(buttons);
    }

    public static CommandReply text(String text) {
        return new CommandReply(text, List.of());
    }

    public static CommandReply withButtons(String text, List<ReplyButton> buttons) {
        return new CommandReply(text, buttons);
    }

    public boolean hasButtons() {
        return !buttons.isEmpty();
    }
}
