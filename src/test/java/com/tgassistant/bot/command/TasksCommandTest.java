package com.tgassistant.bot.command;

import java.util.List;

import com.tgassistant.domain.Task;
import com.tgassistant.domain.TaskType;
import com.tgassistant.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TasksCommandTest {

    @Mock
    private TaskService taskService;

    private static CommandRequest request(String arguments) {
        return new CommandRequest("/tasks", arguments, 42L);
    }

    private TasksCommand commandWith(BotCommand... registered) {
        return new TasksCommand(taskService, providerOf(registered));
    }

    /**
     * This is what the persistent TASKS button reaches. Each inline button carries the command
     * itself, which is what comes back as callback data when it is tapped.
     */
    @Test
    void offersEveryRegisteredCommandAsAButton() {
        TasksCommand command = commandWith(
                new StubCommand("/week", "Weekly tasks"),
                new StubCommand("/day", "Daily tasks"));

        CommandReply reply = command.execute(request(""));

        assertThat(reply.text()).isEqualTo("Available commands:");
        // Human-readable face, command underneath: the label is what the user reads, the
        // command is what comes back as callback data.
        assertThat(reply.buttons()).containsExactly(
                new ReplyButton("Daily tasks", "/day"),
                new ReplyButton("Weekly tasks", "/week"));
        verifyNoInteractions(taskService);
    }

    @Test
    void listsTasksOfTheRequestedType() {
        when(taskService.tasksOfType(TaskType.DAILY))
                .thenReturn(List.of(new Task("buy milk", TaskType.DAILY), new Task("walk the dog", TaskType.DAILY)));

        CommandReply reply = commandWith().execute(request("daily"));

        assertThat(reply.text()).isEqualTo("Your daily tasks:\n- buy milk\n- walk the dog");
        assertThat(reply.hasButtons()).isFalse();
    }

    @Test
    void matchesTheTypeIgnoringCase() {
        when(taskService.tasksOfType(TaskType.WEEKLY))
                .thenReturn(List.of(new Task("finish the report", TaskType.WEEKLY)));

        assertThat(commandWith().execute(request("Weekly")).text())
                .isEqualTo("Your weekly tasks:\n- finish the report");
    }

    @Test
    void saysSoWhenNothingIsStoredForTheType() {
        when(taskService.tasksOfType(TaskType.WEEKLY)).thenReturn(List.of());

        assertThat(commandWith().execute(request("weekly")).text()).isEqualTo("No weekly tasks yet.");
    }

    @Test
    void repliesWithTheKnownTypesForAnUnknownOne() {
        assertThat(commandWith().execute(request("monthly")).text())
                .isEqualTo("Unknown task type 'monthly'. Expected one of: daily, weekly");
        verifyNoInteractions(taskService);
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<BotCommand> providerOf(BotCommand... commands) {
        List<BotCommand> registered = List.of(commands);
        ObjectProvider<BotCommand> provider = mock(ObjectProvider.class);
        // lenient: the task-listing tests never reach the command menu.
        lenient().when(provider.orderedStream()).thenAnswer(_ -> registered.stream());
        return provider;
    }

    private record StubCommand(String name, String label) implements BotCommand {

        @Override
        public String description() {
            return "stub";
        }

        @Override
        public CommandReply execute(CommandRequest request) {
            throw new UnsupportedOperationException();
        }
    }
}
