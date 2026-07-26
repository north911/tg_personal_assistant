package com.tgassistant.bot.command;

import com.tgassistant.domain.TaskType;
import com.tgassistant.service.TaskService;
import org.springframework.stereotype.Component;

@Component
public class AddDailyTaskCommand extends AddTaskCommand {

    public AddDailyTaskCommand(TaskService taskService) {
        super(taskService, TaskType.DAILY);
    }

    @Override
    public String name() {
        return "/day";
    }
}