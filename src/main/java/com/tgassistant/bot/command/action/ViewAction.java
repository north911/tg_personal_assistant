package com.tgassistant.bot.command.action;

import java.util.Optional;

import com.tgassistant.bot.command.CommandReply;
import com.tgassistant.bot.command.TaskListFormatter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class ViewAction implements TaskAction {

    @Override
    public String token() {
        return ":view";
    }

    @Override
    public Optional<String> menuLabel() {
        return Optional.of("View");
    }

    @Override
    public CommandReply apply(TaskActionContext context, String arguments) {
        return CommandReply.text(TaskListFormatter.format(context.tasks(), context.taskType()));
    }
}
