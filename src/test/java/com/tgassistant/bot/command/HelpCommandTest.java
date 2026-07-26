package com.tgassistant.bot.command;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HelpCommandTest {

    @Test
    void listsEveryRegisteredCommandAlphabetically() {
        HelpCommand help = new HelpCommand(providerOf(
                new StubCommand("/week", "Add weekly tasks"),
                new StubCommand("/day", "Add daily tasks")));

        String reply = help.execute(new CommandRequest("/help", "", 7L));

        assertThat(reply).isEqualTo("""
                Available commands:
                /day - Add daily tasks
                /week - Add weekly tasks""");
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<BotCommand> providerOf(BotCommand... commands) {
        List<BotCommand> registered = List.of(commands);
        ObjectProvider<BotCommand> provider = mock(ObjectProvider.class);
        when(provider.orderedStream()).thenAnswer(invocation -> registered.stream());
        return provider;
    }

    private record StubCommand(String name, String description) implements BotCommand {

        @Override
        public String execute(CommandRequest request) {
            throw new UnsupportedOperationException();
        }
    }
}
