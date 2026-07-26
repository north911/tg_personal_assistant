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
        public String description() {
            return "stub";
        }

        @Override
        public String execute(CommandRequest request) {
            return "%s|%s|%d".formatted(request.command(), request.arguments(), request.chatId());
        }
    }

    private static CommandDispatcher dispatcherWith(String... names) {
        return new CommandDispatcher(Arrays.stream(names)
                .map(name -> (BotCommand) new StubCommand(name))
                .toList());
    }

    @Test
    void routesToTheCommandMatchingTheMessage() {
        CommandDispatcher dispatcher = dispatcherWith("/day", "/week");

        assertThat(dispatcher.dispatch("/week finish the report", 7L)).contains("/week|finish the report|7");
    }

    @Test
    void stripsBotMentionAndIgnoresCase() {
        CommandDispatcher dispatcher = dispatcherWith("/day");

        assertThat(dispatcher.dispatch("/Day@MyAssistantBot buy milk", 7L)).contains("/day|buy milk|7");
    }

    @Test
    void passesEmptyArgumentsWhenOnlyTheCommandIsTyped() {
        CommandDispatcher dispatcher = dispatcherWith("/day");

        assertThat(dispatcher.dispatch("  /day  ", 7L)).contains("/day||7");
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
        CommandDispatcher dispatcher = new CommandDispatcher(List.of());

        assertThat(dispatcher.dispatch("/anything", 7L)).isEmpty();
    }
}
