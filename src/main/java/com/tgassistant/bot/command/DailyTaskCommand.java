package com.tgassistant.bot.command;

import java.util.List;

import com.tgassistant.bot.command.action.TaskAction;
import com.tgassistant.domain.TaskType;
import com.tgassistant.service.TaskService;
import org.springframework.stereotype.Component;

@Component
public class DailyTaskCommand extends TaskTypeCommand {

    public DailyTaskCommand(TaskService taskService, List<TaskAction> actions) {
        super(taskService, TaskType.DAILY, actions);
    }

    @Override
    public String name() {
        return "/day";
    }
}
