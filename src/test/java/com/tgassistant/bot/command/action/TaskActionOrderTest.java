package com.tgassistant.bot.command.action;

import java.util.ArrayList;
import java.util.List;

import com.tgassistant.bot.command.PendingInputStore;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The action menu is ordered by each action's {@code @Order}, applied by Spring when it injects
 * {@code List<TaskAction>}. Sorting a deliberately shuffled list the same way Spring does keeps
 * a forgotten or duplicated annotation from silently reshuffling the buttons.
 */
class TaskActionOrderTest {

    @Test
    void ordersActionsAsTheMenuPresentsThem() {
        List<TaskAction> actions = new ArrayList<>(List.of(
                new ClearAction(new ClearConfirmedAction()),
                new ClearConfirmedAction(),
                new DeleteAction(),
                new AddAction(new PendingInputStore())));

        AnnotationAwareOrderComparator.sort(actions);

        assertThat(actions).extracting(action -> action.getClass().getSimpleName()).containsExactly(
                "AddAction", "DeleteAction", "ClearAction", "ClearConfirmedAction");
    }

    @Test
    void everyActionTokenIsMarkedAndUnique() {
        List<TaskAction> actions = List.of(new AddAction(new PendingInputStore()), new DeleteAction(),
                new ClearAction(new ClearConfirmedAction()), new ClearConfirmedAction());

        assertThat(actions).allSatisfy(action -> assertThat(action.token()).startsWith(":"));
        assertThat(actions).extracting(TaskAction::token).doesNotHaveDuplicates();
    }
}
