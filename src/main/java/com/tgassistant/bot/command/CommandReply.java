package com.tgassistant.bot.command;

import java.util.List;
import java.util.Optional;

/**
 * What a {@link BotCommand} sends back: some text, and at most one way of answering it —
 * inline buttons, or a request for typed input.
 *
 * <p>Deliberately free of Telegram classes — commands describe <em>what</em> to reply and
 * {@code MainTelegramBot} decides how to render it, which keeps commands unit-testable
 * without the Telegram API.
 *
 * @param buttons          inline buttons to attach, empty for a plain text reply
 * @param inputPlaceholder hint shown in the input box when this reply asks the user to type
 *                         something; empty when it does not. Telegram allows one markup per
 *                         message, so this and {@code buttons} are never both set.
 */
public record CommandReply(String text, List<ReplyButton> buttons, Optional<String> inputPlaceholder) {

    public CommandReply {
        buttons = List.copyOf(buttons);
    }

    public static CommandReply text(String text) {
        return new CommandReply(text, List.of(), Optional.empty());
    }

    public static CommandReply withButtons(String text, List<ReplyButton> buttons) {
        return new CommandReply(text, buttons, Optional.empty());
    }

    /**
     * Asks the user to type an answer, focusing their input box with {@code placeholder} in it.
     * Whoever sends this is responsible for registering the follow-up in
     * {@link PendingInputStore}, or the answer has nowhere to go.
     */
    public static CommandReply prompt(String text, String placeholder) {
        return new CommandReply(text, List.of(), Optional.of(placeholder));
    }

    public boolean hasButtons() {
        return !buttons.isEmpty();
    }

    public boolean awaitsInput() {
        return inputPlaceholder.isPresent();
    }
}
