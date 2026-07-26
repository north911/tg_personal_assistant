-- Columns use snake_case (SQL convention); Hibernate maps Java camelCase fields onto them
-- automatically, e.g. a future `createdAt` field would map to a `created_at` column.
CREATE TABLE task (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    description TEXT NOT NULL,
    type        TEXT NOT NULL CHECK (type IN ('DAILY', 'WEEKLY'))
);
