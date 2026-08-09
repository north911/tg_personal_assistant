package com.tgassistant.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Keeps Telegram's menu button out of the chat, which the {@link com.tgassistant.bot.command.ChatKeyboard}
 * TASKS button already duplicates.
 *
 * <p>There is no API to hide that button: its three types (commands, web app, default) all
 * show something, and {@code default} falls back to the command list. It only disappears when
 * the bot has no registered commands, so clearing the list is what removes it.
 *
 * <p>Runs on every start rather than once because the list lives on Telegram's servers, not
 * here — anything that puts commands back (BotFather, an earlier build) would otherwise
 * bring the button back for good. Commands still work when typed; registration only ever
 * drove the menu UI.
 */
@Component
public class BotCommandCleaner {

    private static final Logger log = LoggerFactory.getLogger(BotCommandCleaner.class);

    private final TelegramClient telegramClient;

    public BotCommandCleaner(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void deleteCommands() {
        try {
            telegramClient.execute(DeleteMyCommands.builder().build());
            log.info("Cleared the bot's command list, so Telegram shows no menu button");
        } catch (TelegramApiException e) {
            // The bot answers commands either way, so don't fail startup over the menu.
            log.error("Failed to clear the bot's command list", e);
        }
    }
}
