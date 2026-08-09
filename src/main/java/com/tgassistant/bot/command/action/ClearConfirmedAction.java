package com.tgassistant.bot.command.action;

import com.tgassistant.bot.command.CommandReply;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * The other half of {@link ClearAction}. Deliberately has no menu label: it is reachable only
 * from that confirmation, never straight off the action menu.
 */
@Component
@Order(50)
public class ClearConfirmedAction implements TaskAction {

    @Override
    public String token() {
        return ":clear-confirmed";
    }

    @Override
    public CommandReply apply(TaskActionContext context, String arguments) {
        long deleted = context.taskService().deleteAllOfType(context.taskType());
        if (deleted == 0) {
            return CommandReply.text("No %s tasks yet.".formatted(context.typeLabel()));
        }
        String plural = deleted == 1 ? "task" : "tasks";
        return CommandReply.text(
                "Deleted all %d %s %s.".formatted(deleted, context.typeLabel(), plural));
    }
}
