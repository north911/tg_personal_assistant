package com.tgassistant.bot;

import java.util.List;

import com.tgassistant.bot.command.ChatKeyboard;
import com.tgassistant.bot.command.CommandDispatcher;
import com.tgassistant.bot.command.CommandReply;
import com.tgassistant.bot.command.ReplyButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class MainTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(MainTelegramBot.class);

    private final String botToken;
    private final TelegramClient telegramClient;
    private final CommandDispatcher commandDispatcher;
    private final ReplyKeyboardMarkup persistentKeyboard;

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    public MainTelegramBot(@Value("${telegram.bot.token}") String botToken,
                           TelegramClient telegramClient,
                           CommandDispatcher commandDispatcher,
                           ChatKeyboard chatKeyboard) {
        this.botToken = botToken;
        this.telegramClient = telegramClient;
        this.commandDispatcher = commandDispatcher;
        this.persistentKeyboard = toPersistentKeyboard(chatKeyboard.buttons());
    }

    @Override
    public void consume(Update update) {
        if (update.hasCallbackQuery()) {
            consumeButtonPress(update.getCallbackQuery());
            return;
        }
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // A known command — or a tapped panel button, which the dispatcher resolves to
            // one — handles the message; anything else is ignored.
            commandDispatcher.dispatch(messageText, chatId)
                    .ifPresent(reply -> sendReply(chatId, reply));
        }
    }

    /**
     * An inline button carries the command it stands for as its callback data, so a tap goes
     * through the same dispatcher as a typed message.
     */
    private void consumeButtonPress(CallbackQuery callbackQuery) {
        long chatId = callbackQuery.getMessage().getChatId();
        // Telegram spins a loading indicator on the button until the query is answered.
        answerCallbackQuery(callbackQuery.getId());
        commandDispatcher.dispatch(callbackQuery.getData(), chatId)
                .ifPresent(reply -> sendReply(chatId, reply));
    }

    private void sendReply(long chatId, CommandReply reply) {
        SendMessage message = SendMessage // Create a message object
                .builder()
                .chatId(chatId)
                .text(reply.text())
                .replyMarkup(replyMarkupFor(reply))
                .build();
        try {
            telegramClient.execute(message); // Sending our message object to user
        } catch (TelegramApiException e) {
            log.error("Failed to send message to chat {}", chatId, e);
        }
    }

    /**
     * A message carries exactly one markup: inline buttons, a request to type something, or
     * the persistent panel. Telegram remembers the panel from the previous message, so it
     * stays on screen while either of the others is showing.
     */
    private ReplyKeyboard replyMarkupFor(CommandReply reply) {
        if (reply.hasButtons()) {
            return toInlineKeyboard(reply.buttons());
        }
        return reply.inputPlaceholder()
                .<ReplyKeyboard>map(MainTelegramBot::toForceReply)
                .orElse(persistentKeyboard);
    }

    /**
     * Opens the user's input box focused on this message, which is the closest Telegram gets
     * to a form field for an ordinary bot.
     */
    private static ForceReplyKeyboard toForceReply(String placeholder) {
        return ForceReplyKeyboard.builder()
                .forceReply(true)
                .inputFieldPlaceholder(placeholder)
                .build();
    }

    private void answerCallbackQuery(String callbackQueryId) {
        try {
            telegramClient.execute(AnswerCallbackQuery.builder().callbackQueryId(callbackQueryId).build());
        } catch (TelegramApiException e) {
            log.error("Failed to answer callback query {}", callbackQueryId, e);
        }
    }

    /**
     * One button per row, so long labels stay readable on narrow screens.
     */
    private static InlineKeyboardMarkup toInlineKeyboard(List<ReplyButton> buttons) {
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder<?, ?> keyboard = InlineKeyboardMarkup.builder();
        for (ReplyButton button : buttons) {
            keyboard.keyboardRow(new InlineKeyboardRow(InlineKeyboardButton.builder()
                    .text(button.label())
                    .callbackData(button.command())
                    .build()));
        }
        return keyboard.build();
    }

    /**
     * The panel under the text input. Telegram only shows it once a message carries it, which
     * is why every buttonless reply does.
     */
    private static ReplyKeyboardMarkup toPersistentKeyboard(List<ReplyButton> buttons) {
        KeyboardRow row = new KeyboardRow(buttons.stream()
                .map(button -> new KeyboardButton(button.label()))
                .toList());
        return ReplyKeyboardMarkup.builder()
                .keyboardRow(row)
                .resizeKeyboard(true)   // otherwise the panel takes up half the screen
                .isPersistent(true)     // stays put instead of collapsing behind the emoji key
                .build();
    }
}
