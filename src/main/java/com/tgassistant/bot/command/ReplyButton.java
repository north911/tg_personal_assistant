package com.tgassistant.bot.command;

import java.nio.charset.StandardCharsets;

/**
 * A tappable button, used for both kinds of keyboard this bot shows.
 *
 * <p>On the persistent panel under the input, a tap sends the {@code label} back as an
 * ordinary message — that is all Telegram offers there, so {@link CommandDispatcher}
 * translates a known label into its {@code command} before routing. On an inline keyboard
 * the {@code command} rides along as callback data instead. Either way a tap ends up on the
 * same path as typing the command.
 *
 * @param label   the button face, and what a persistent-panel tap sends
 * @param command the command the tap stands for, slash included
 */
public record ReplyButton(String label, String command) {

    public ReplyButton {
        // Only inline buttons are subject to this, but a button that silently stops working
        // once it is used inline is worse than one that fails to build.
        if (command.getBytes(StandardCharsets.UTF_8).length > 64) {
            throw new IllegalArgumentException(
                    "Button command exceeds Telegram's 64-byte callback data limit: " + command);
        }
    }
}
