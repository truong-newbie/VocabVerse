ALTER TABLE learning_progress
    ADD COLUMN last_interval_days INT NOT NULL DEFAULT 0,
    ADD COLUMN lapse_count INT NOT NULL DEFAULT 0,
    ADD COLUMN review_count INT NOT NULL DEFAULT 0;
