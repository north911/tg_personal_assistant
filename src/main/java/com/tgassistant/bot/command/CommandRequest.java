package com.tgassistant.bot.command;

import java.util.Optional;

/**
 * A chat message parsed into the command itself and whatever the user typed after it.
 *
 * @param command   normalized command name, e.g. {@code /day}
 * @param arguments everything after the command, stripped; empty when nothing followed
 * @param chatId    chat the message came from, so commands can reply per chat later
 */
public record CommandRequest(String command, String arguments, long chatId) {

    /**
     * @return the parsed request, or empty when the message does not start with a command.
     */
    public static Optional<CommandRequest> parse(String messageText, long chatId) {
        String trimmed = messageText.strip();
        if (!trimmed.startsWith("/")) {
            return Optional.empty();
        }
        int separator = indexOfWhitespace(trimmed);
        String rawCommand = separator < 0 ? trimmed : trimmed.substring(0, separator);
        String arguments = separator < 0 ? "" : trimmed.substring(separator + 1).strip();
        return Optional.of(new CommandRequest(normalizeCommand(rawCommand), arguments, chatId));
    }

    public boolean hasArguments() {
        return !arguments.isEmpty();
    }

    private static int indexOfWhitespace(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Telegram addresses commands as {@code /day@MyBot} in group chats; strip that
     * suffix and lower-case so matching is case-insensitive.
     */
    private static String normalizeCommand(String rawCommand) {
        int at = rawCommand.indexOf('@');
        String command = at < 0 ? rawCommand : rawCommand.substring(0, at);
        return command.toLowerCase();
    }
}