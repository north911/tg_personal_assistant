package com.tgassistant.bot.command;

import java.util.List;

import com.tgassistant.bot.command.action.TaskAction;
import com.tgassistant.domain.TaskType;
import com.tgassistant.service.TaskService;
import org.springframework.stereotype.Component;

@Component
public class WeeklyTaskCommand extends TaskTypeCommand {

    public WeeklyTaskCommand(TaskService taskService, List<TaskAction> actions) {
        super(taskService, TaskType.WEEKLY, actions);
    }

    @Override
    public String name() {
        return "/week";
    }
}
