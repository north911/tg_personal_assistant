package com.tgassistant.bot.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatKeyboardTest {

    @Test
    void showsASingleTasksButton() {
        assertThat(new ChatKeyboard().buttons())
                .containsExactly(new ReplyButton("TASKS", "/tasks"));
    }
}
