package com.tgassistant.bot.command;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandDispatcherTest {

    /**
     * Minimal command that echoes back what it received, so the dispatcher's routing and
     * parsing can be asserted without touching the task layer.
     */
    private record StubCommand(String name) implements BotCommand {

        @Override
        public String label() {
            return "stub";
        }

        @Override
        public String description() {
            return "stub";
        }

        @Override
        public CommandReply execute(CommandRequest request) {
            return CommandReply.text(
                    "%s|%s|%d".formatted(request.command(), request.arguments(), request.chatId()));
        }
    }

    private static CommandDispatcher dispatcherWith(String... names) {
        return new CommandDispatcher(Arrays.stream(names)
                .map(name -> (BotCommand) new StubCommand(name))
                .toList(), new ChatKeyboard());
    }

    @Test
    void routesToTheCommandMatchingTheMessage() {
        CommandDispatcher dispatcher = dispatcherWith("/day", "/week");

        assertThat(dispatcher.dispatch("/week finish the report", 7L))
                .contains(CommandReply.text("/week|finish the report|7"));
    }

    /**
     * A panel tap arrives as the button's plain label, so the label has to resolve to the
     * command before routing — otherwise "TASKS" is just unknown text and gets echoed.
     */
    @Test
    void resolvesPanelButtonLabelsToTheirCommand() {
        CommandDispatcher dispatcher = dispatcherWith("/tasks");

        assertThat(dispatcher.dispatch("TASKS", 7L)).contains(CommandReply.text("/tasks||7"));
    }

    /**
     * An inline button sends its command as callback data, which needs no translation.
     */
    @Test
    void routesInlineButtonCallbackDataLikeATypedCommand() {
        CommandDispatcher dispatcher = dispatcherWith("/day");

        assertThat(dispatcher.dispatch("/day", 7L)).contains(CommandReply.text("/day||7"));
    }

    @Test
    void treatsTextThatIsNotAButtonLabelAsOrdinaryText() {
        CommandDispatcher dispatcher = dispatcherWith("/tasks");

        assertThat(dispatcher.dispatch("tasks", 7L)).isEmpty();
        assertThat(dispatcher.dispatch("TASKS please", 7L)).isEmpty();
    }

    @Test
    void stripsBotMentionAndIgnoresCase() {
        CommandDispatcher dispatcher = dispatcherWith("/day");

        assertThat(dispatcher.dispatch("/Day@MyAssistantBot buy milk", 7L))
                .contains(CommandReply.text("/day|buy milk|7"));
    }

    @Test
    void passesEmptyArgumentsWhenOnlyTheCommandIsTyped() {
        CommandDispatcher dispatcher = dispatcherWith("/day");

        assertThat(dispatcher.dispatch("  /day  ", 7L)).contains(CommandReply.text("/day||7"));
    }

    @Test
    void ignoresPlainTextAndUnknownCommands() {
        CommandDispatcher dispatcher = dispatcherWith("/day");

        assertThat(dispatcher.dispatch("hello there", 7L)).isEmpty();
        assertThat(dispatcher.dispatch("/start", 7L)).isEmpty();
    }

    @Test
    void exposesRegisteredCommands() {
        CommandDispatcher dispatcher = dispatcherWith("/day", "/week");

        assertThat(dispatcher.commands()).extracting(BotCommand::name).containsExactly("/day", "/week");
    }

    @Test
    void rejectsDuplicateCommandNames() {
        assertThatThrownBy(() -> dispatcherWith("/day", "/day"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate command /day");
    }

    @Test
    void rejectsMalformedCommandNames() {
        assertThatThrownBy(() -> dispatcherWith("day"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must expose a lower-case name starting with '/'");
        assertThatThrownBy(() -> dispatcherWith("/Day"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must expose a lower-case name starting with '/'");
    }

    @Test
    void unknownCommandsDoNotReachOtherCommands() {
        CommandDispatcher dispatcher = new CommandDispatcher(List.of(), new ChatKeyboard());

        assertThat(dispatcher.dispatch("/anything", 7L)).isEmpty();
    }
}
