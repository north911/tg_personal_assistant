package com.tgassistant.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Minimal Telegram bot that replies "hello" to every text message it receives.
 *
 * <p>Registered automatically by {@code telegrambots-spring-boot-starter}: any
 * Spring bean that is a {@code LongPollingBot} is picked up and connected to the
 * Telegram API on startup, so no manual {@code TelegramBotsApi.registerBot(...)}
 * call is needed.
 */
@Component
public class HelloBot extends TelegramLongPollingBot {

    private static final Logger log = LoggerFactory.getLogger(HelloBot.class);

    private final String botUsername;

    public HelloBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername) {
        super(botToken);
        this.botUsername = botUsername;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        long chatId = update.getMessage().getChatId();
        SendMessage reply = SendMessage.builder()
                .chatId(chatId)
                .text("hello")
                .build();

        try {
            execute(reply);
        } catch (TelegramApiException e) {
            log.error("Failed to send reply to chat {}", chatId, e);
        }
    }
}
