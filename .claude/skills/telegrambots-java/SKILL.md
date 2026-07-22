---
name: telegrambots-java
description: How to use the rubenlagus telegrambots Java library (v6.7.0 in this repo) — long-polling bots, sending messages, keyboards, commands, and the AbilityBot framework used by this project. Use when writing or editing bot code, handling Telegram updates, sending replies, building keyboards, or defining abilities/commands.
---

# telegrambots (Java) — usage guide

Reference for the `org.telegram:telegrambots*` library. **This repo pins v6.7.0**
(`telegrambots-spring-boot-starter` + `telegrambots-abilities`) — the API notes below
target 6.7.x. Upstream docs:
https://rubenlagus.github.io/TelegramBotsDocumentation/telegram-bots.html and the
GitHub wiki at https://github.com/rubenlagus/TelegramBots/wiki

> Version caveat: telegrambots changed its API repeatedly across 6.x → 7.x → 8.x.
> The **bot token moved into the constructor** in 6.5.0 (`getBotToken()` was removed),
> and 7.x renamed several Spring interfaces. If a snippet from the web doesn't compile,
> check it against 6.7.0 before assuming your code is wrong.

## Two ways to build a bot

- **`TelegramLongPollingBot`** — the bot polls Telegram for updates. Simplest; no public
  URL needed. Preferred for this project.
- **`WebhookBot`** — Telegram pushes updates to your HTTPS endpoint. Only worth it at
  scale / behind a real domain.
- **`AbilityBot`** (from `telegrambots-abilities`) — a higher-level framework layered on
  long polling that turns `/commands` into declarative "abilities." **This repo already
  depends on it** (`ResponseHandler` uses `SilentSender` + `DBContext`), so prefer it for
  command-style features.

## Plain long-polling bot (6.7.0)

In 6.5.0+ the token is passed to the constructor; you only override `getBotUsername()`:

### Registration with the Spring Boot starter
With `telegrambots-spring-boot-starter` on the classpath, **any Spring bean that is a
`LongPollingBot` is auto-registered** on startup — no manual
`new TelegramBotsApi(...).registerBot(bot)`. Just annotate the bot `@Component`.
(Outside Spring you would register manually via `TelegramBotsApi`.)

## Sending things

- Build request objects and pass them to `execute(...)`:
  - `SendMessage` — text. `.parseMode("MarkdownV2")` or `"HTML"` for formatting
    (escape user input for MarkdownV2).
  - `SendPhoto`, `SendDocument` — files via `new InputFile(file)` or an `InputStream`.
  - `EditMessageText`, `DeleteMessage`, `AnswerCallbackQuery` — for inline interactions.
- All `execute(...)` calls throw **`TelegramApiException`** — handle or propagate it,
  never swallow.
- Reply *to* a message: `SendMessage.builder()....replyToMessageId(id)`.

## Keyboards

Reply keyboard (custom buttons under the input box):

```java
ReplyKeyboardMarkup kb = ReplyKeyboardMarkup.builder()
        .keyboardRow(new KeyboardRow(List.of(new KeyboardButton("Yes"),
                                             new KeyboardButton("No"))))
        .resizeKeyboard(true)
        .oneTimeKeyboard(true)
        .build();
sendMessage.setReplyMarkup(kb);
```

Inline keyboard (buttons attached to a message, produce callback queries):

```java
InlineKeyboardButton btn = InlineKeyboardButton.builder()
        .text("Click me").callbackData("action:1").build();
InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
        .keyboardRow(List.of(btn)).build();
```

Handle the press in `onUpdateReceived` via `update.hasCallbackQuery()` →
`update.getCallbackQuery().getData()`, then answer with `AnswerCallbackQuery` so the
client stops showing a loading spinner.

## AbilityBot (preferred for commands in this repo)

`AbilityBot` maps `/commands` to `Ability` objects and gives you a built-in per-chat
key-value store (`DBContext`) and a `SilentSender` (`silent`) that swallows send errors.

```java
@Component
public class AssistantBot extends AbilityBot {

    private static final long CREATOR_ID = 123456789L;

    public AssistantBot(@Value("${telegram.bot.token}") String token) {
        super(token, "my_assistant_bot");   // (token, username)
    }

    @Override
    public long creatorId() {               // required abstract method
        return CREATOR_ID;
    }

    public Ability sayHello() {
        return Ability.builder()
                .name("hello")               // → the /hello command
                .info("says hello")          // shown in /commands help
                .locality(Locality.USER)     // USER | GROUP | ALL
                .privacy(Privacy.PUBLIC)     // PUBLIC | ADMIN | CREATOR
                .action(ctx -> silent.send("Hello!", ctx.chatId()))
                .build();
    }
}
```

Key pieces:
- **`Ability.builder()`** fields: `.name` (command, no slash), `.info`, `.locality`,
  `.privacy`, `.input(n)` (required arg count), `.action(ctx -> ...)`, optional
  `.reply(...)` and `.flag(...)` for multi-step flows.
- **`MessageContext ctx`** gives `ctx.chatId()`, `ctx.user()`, `ctx.arguments()`,
  `ctx.firstArg()`.
- **`silent`** (a `SilentSender`) — send without try/catch; use `sender`/`execute`
  when you need the response or error.
- **`db`** (a `DBContext`) — `db.getMap("name")` for persistent per-chat state (this is
  exactly what `ResponseHandler` does). Note: this is AbilityBot's own MapDB store,
  **separate from the project's JPA/SQLite database** — don't conflate the two; for
  domain data use the JPA repositories, reserve `db` for transient conversation state.
- Multi-step conversations: use `.flag(...)` + `.reply(...)` or a state map keyed by
  chat id (the pattern `ResponseHandler.chatStates` is starting).

## Getting updates: what to check

`Update` is a union — always guard before reading:
- `update.hasMessage()` → `getMessage().hasText()/hasPhoto()/hasDocument()`
- `update.hasCallbackQuery()` → inline button press
- `update.hasInlineQuery()` → inline mode
- `getMessage().getChatId()` and `getFrom().getId()` are your chat/user identifiers.

## Conventions for this project

- Keep the **token in config/env** (`@Value("${telegram.bot.token}")` backed by an env
  var) — never hard-code it. See the `spring-boot-dev` skill.
- Keep the bot/handler thin: route the update, delegate work to a `@Service`, format the
  reply. No business logic or JPA calls inside `onUpdateReceived`/ability actions.
- Always handle `TelegramApiException` (or use `silent`/AbilityBot which handles it).
- Persist **domain data** in JPA/SQLite; use AbilityBot's `db` only for conversation state.
