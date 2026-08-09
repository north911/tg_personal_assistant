package com.tgassistant.bot.command.action;

import java.util.Optional;

import com.tgassistant.bot.command.CommandReply;

/**
 * One thing you can do with tasks of a given type — view them, delete one, and so on.
 *
 * <p>Every implementation is a Spring bean; {@code TaskTypeCommand} collects them all and
 * selects by {@link #token()}, so adding an action means adding a {@code @Component} — there
 * is no switch to extend. Declare an {@code @Order} to place it in the menu.
 *
 * <p>The same action serves every task type: nothing here is bound to daily or weekly, the
 * type arrives with the {@link TaskActionContext} at call time.
 */
public interface TaskAction {

    /**
     * Marker word that selects this action, e.g. {@code :view}. The leading {@code :} is what
     * keeps actions out of the space of things a user might type as a task description, so
     * every token must carry it.
     */
    String token();

    /**
     * Face of this action's button in the menu, or empty to stay out of the menu — for actions
     * only reachable from another action's reply, such as confirming a deletion.
     */
    default Optional<String> menuLabel() {
        return Optional.empty();
    }

    /**
     * @param arguments whatever followed the token, empty when nothing did; e.g. the task id
     *                  in {@code /day :delete 7}
     */
    CommandReply apply(TaskActionContext context, String arguments);
}
