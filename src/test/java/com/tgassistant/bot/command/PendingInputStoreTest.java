package com.tgassistant.bot.command;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PendingInputStoreTest {

    private static final Instant NOW = Instant.parse("2026-08-09T12:00:00Z");

    /**
     * Lets a test move time forward without sleeping.
     */
    private static class MovableClock extends Clock {

        private Instant instant = NOW;

        @Override
        public Instant instant() {
            return instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        void advance(Duration amount) {
            instant = instant.plus(amount);
        }
    }

    @Test
    void handsBackTheCommandThatAskedForInput() {
        PendingInputStore store = new PendingInputStore();

        store.await(42L, "/day");

        assertThat(store.consume(42L)).contains("/day");
    }

    @Test
    void answersOnlyOnce() {
        PendingInputStore store = new PendingInputStore();
        store.await(42L, "/day");

        assertThat(store.consume(42L)).contains("/day");
        assertThat(store.consume(42L)).isEmpty();
    }

    @Test
    void hasNothingForAChatThatWasNotAsked() {
        assertThat(new PendingInputStore().consume(42L)).isEmpty();
    }

    @Test
    void keepsChatsApart() {
        PendingInputStore store = new PendingInputStore();

        store.await(1L, "/day");
        store.await(2L, "/week");

        assertThat(store.consume(2L)).contains("/week");
        assertThat(store.consume(1L)).contains("/day");
    }

    @Test
    void aSecondPromptReplacesTheFirst() {
        PendingInputStore store = new PendingInputStore();

        store.await(42L, "/day");
        store.await(42L, "/week");

        assertThat(store.consume(42L)).contains("/week");
    }

    @Test
    void clearAbandonsThePrompt() {
        PendingInputStore store = new PendingInputStore();
        store.await(42L, "/day");

        store.clear(42L);

        assertThat(store.consume(42L)).isEmpty();
    }

    /**
     * An abandoned prompt must not swallow a message sent hours later.
     */
    @Test
    void forgetsAPromptThatWentUnanswered() {
        MovableClock clock = new MovableClock();
        PendingInputStore store = new PendingInputStore(clock);
        store.await(42L, "/day");

        clock.advance(Duration.ofMinutes(6));

        assertThat(store.consume(42L)).isEmpty();
    }

    @Test
    void stillAnswersJustInsideTheWindow() {
        MovableClock clock = new MovableClock();
        PendingInputStore store = new PendingInputStore(clock);
        store.await(42L, "/day");

        clock.advance(Duration.ofMinutes(4));

        assertThat(store.consume(42L)).contains("/day");
    }
}
