ALTER TABLE shadowing_lessons
    ADD COLUMN IF NOT EXISTS cloudinary_public_id VARCHAR(500),
    ADD COLUMN IF NOT EXISTS video_url TEXT,
    ADD COLUMN IF NOT EXISTS thumbnail_url TEXT,
    ADD COLUMN IF NOT EXISTS storage_provider VARCHAR(50);

CREATE TABLE IF NOT EXISTS shadowing_lesson_subtitles (
    id UUID PRIMARY KEY,
    lesson_id UUID NOT NULL REFERENCES shadowing_lessons(id) ON DELETE CASCADE,
    start_time_ms INT NOT NULL,
    end_time_ms INT NOT NULL,
    english_text TEXT NOT NULL,
    vietnamese_text TEXT,
    order_index INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_shadowing_subtitles_lesson_order
    ON shadowing_lesson_subtitles(lesson_id, order_index, start_time_ms);
