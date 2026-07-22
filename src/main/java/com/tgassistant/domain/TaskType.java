package com.tgassistant.domain;

/**
 * How often a {@link Task} recurs. Persisted as its name (see {@code @Enumerated(STRING)}),
 * so the stored values are {@code DAILY} / {@code WEEKLY} — kept in sync with the
 * CHECK constraint in the {@code V1__create_task_table.sql} Flyway migration.
 */
public enum TaskType {
    DAILY,
    WEEKLY
}
