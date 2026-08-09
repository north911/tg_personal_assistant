package com.tgassistant.bot.command;

import com.tgassistant.domain.TaskType;
import com.tgassistant.service.TaskService;
import org.springframework.stereotype.Component;

@Component
public class WeeklyTaskCommand extends AddTaskCommand {

    public WeeklyTaskCommand(TaskService taskService) {
        super(taskService, TaskType.WEEKLY);
    }

    @Override
    public String name() {
        return "/week";
    }
}