package com.tgassistant.domain;

/**
 * What kind of list a {@link Task} belongs to — a recurrence for chores, a shopping list for
 * things to buy. Persisted as its name (see {@code @Enumerated(STRING)}), so the stored values
 * are {@code DAILY} / {@code WEEKLY} / {@code DAILY_SHOPPING} / {@code GLOBAL_SHOPPING} — kept
 * in sync with the CHECK constraint last widened by the
 * {@code V2__add_shopping_task_types.sql} Flyway migration.
 *
 * <p>Both labels are spelled out rather than derived from {@link #name()}: {@code
 * DAILY_SHOPPING} would otherwise read as "daily_shopping" in the middle of a sentence, and
 * "Daily shopping tasks" is the wrong noun for a shopping list.
 */
public enum TaskType {
    DAILY("daily", "Daily tasks"),
    WEEKLY("weekly", "Weekly tasks"),
    DAILY_SHOPPING("daily shopping", "Daily shopping list"),
    GLOBAL_SHOPPING("global shopping", "Global shopping list");

    private final String label;
    private final String buttonLabel;

    TaskType(String label, String buttonLabel) {
        this.label = label;
        this.buttonLabel = buttonLabel;
    }

    /**
     * Human-readable form used inside sentences, e.g. {@code daily} in "No daily tasks yet."
     * Also what {@code /tasks <type>} matches on, so it stays lower-case.
     */
    public String label() {
        return label;
    }

    /**
     * Face of this type's button in the {@code /tasks} menu, e.g. {@code Daily tasks}.
     */
    public String buttonLabel() {
        return buttonLabel;
    }
}
