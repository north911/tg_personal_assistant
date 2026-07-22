CREATE TABLE task (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    description  TEXT NOT NULL,
    type         TEXT NOT NULL CHECK (type IN ('DAILY', 'WEEKLY'))
);
