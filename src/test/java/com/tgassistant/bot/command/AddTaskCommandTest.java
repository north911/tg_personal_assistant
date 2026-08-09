package com.tgassistant.bot.command;

import java.util.List;

import com.tgassistant.domain.TaskType;
import com.tgassistant.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AddTaskCommandTest {

    @Mock
    private TaskService taskService;

    @Captor
    private ArgumentCaptor<List<String>> descriptionsCaptor;

    private static CommandRequest request(String arguments) {
        return new CommandRequest("/day", arguments, 42L);
    }

    @Test
    void storesCommaSeparatedDescriptionsAsDailyTasks() {
        AddTaskCommand command = new DailyTaskCommand(taskService);

        CommandReply reply = command.execute(request("buy milk, walk the dog ,  call mom"));

        verify(taskService).addTasks(descriptionsCaptor.capture(), eq(TaskType.DAILY));
        assertThat(descriptionsCaptor.getValue()).containsExactly("buy milk", "walk the dog", "call mom");
        assertThat(reply.text()).isEqualTo("Added 3 daily tasks:\n- buy milk\n- walk the dog\n- call mom");
    }

    @Test
    void storesSingleDescriptionAsWeeklyTask() {
        AddTaskCommand command = new WeeklyTaskCommand(taskService);

        CommandReply reply = command.execute(new CommandRequest("/week", "finish the report", 42L));

        verify(taskService).addTasks(List.of("finish the report"), TaskType.WEEKLY);
        assertThat(reply.text()).isEqualTo("Added 1 weekly task:\n- finish the report");
    }

    /**
     * The face of the button in the {@code /tasks} menu — the command name never shows there.
     */
    @Test
    void labelsItselfReadably() {
        assertThat(new DailyTaskCommand(taskService).label()).isEqualTo("Daily tasks");
        assertThat(new WeeklyTaskCommand(taskService).label()).isEqualTo("Weekly tasks");
    }

    @Test
    void repliesWithUsageWhenNoDescriptionsGiven() {
        AddTaskCommand command = new DailyTaskCommand(taskService);

        assertThat(command.execute(request("")).text()).isEqualTo("Usage: /day <task>[, <task>, ...]");
        assertThat(command.execute(request("  ,  , ")).text()).isEqualTo("Usage: /day <task>[, <task>, ...]");
        verifyNoInteractions(taskService);
    }
}
