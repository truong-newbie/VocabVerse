ALTER TABLE shadowing_lessons
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_shadowing_lessons_deleted_at
    ON shadowing_lessons(deleted_at);
