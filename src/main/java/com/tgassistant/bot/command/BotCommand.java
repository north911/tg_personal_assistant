package com.tgassistant.bot.command;

/**
 * A single chat command, e.g. {@code /day}. Every implementation is a Spring bean;
 * {@link CommandDispatcher} collects them all and routes incoming messages by
 * {@link #name()}, so adding a command means adding a {@code @Component} — no
 * central switch to touch.
 */
public interface BotCommand {

    /**
     * The command as typed by the user, slash included and lower-case, e.g. {@code /day}.
     * Must be unique across all commands.
     */
    String name();

    /**
     * Short human-readable name for the command's button in the {@code /tasks} menu, e.g.
     * {@code Daily tasks}. Kept separate from {@link #description()}, which is a full usage
     * line and far too long to fit on a button.
     */
    String label();

    /**
     * One-line explanation shown by {@code /help}.
     */
    String description();

    /**
     * Runs the command and returns the reply to send back.
     */
    CommandReply execute(CommandRequest request);
}
