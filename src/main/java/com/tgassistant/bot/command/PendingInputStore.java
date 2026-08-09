package com.tgassistant.bot.command;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Remembers that a chat was asked for something, so the next plain message can be handed to
 * the command that asked.
 *
 * <p>Held in memory on purpose: a forgotten prompt costs one extra tap, which is cheaper than
 * a table and a migration. A restart clears every prompt, which is the behaviour you want
 * anyway — nobody expects to answer yesterday's question.
 */
@Component
public class PendingInputStore {

    /**
     * Long enough to type a task, short enough that an abandoned prompt cannot swallow an
     * unrelated message later on.
     */
    private static final Duration TTL = Duration.ofMinutes(5);

    private final Map<Long, Pending> byChatId = new ConcurrentHashMap<>();
    private final Clock clock;

    public PendingInputStore() {
        this(Clock.systemUTC());
    }

    PendingInputStore(Clock clock) {
        this.clock = clock;
    }

    /**
     * Records that the next plain message in this chat belongs to {@code command}, replacing
     * any prompt already open there.
     */
    public void await(long chatId, String command) {
        byChatId.put(chatId, new Pending(command, clock.instant()));
    }

    /**
     * Takes the open prompt, if there is one that has not expired. Removes it either way — a
     * prompt is answered once.
     */
    public Optional<String> consume(long chatId) {
        Pending pending = byChatId.remove(chatId);
        if (pending == null || pending.hasExpired(clock.instant())) {
            return Optional.empty();
        }
        return Optional.of(pending.command());
    }

    /**
     * Abandons the open prompt, for when the user does something else instead of answering.
     */
    public void clear(long chatId) {
        byChatId.remove(chatId);
    }

    private record Pending(String command, Instant askedAt) {

        boolean hasExpired(Instant now) {
            return Duration.between(askedAt, now).compareTo(TTL) > 0;
        }
    }
}
