ALTER TABLE collection_review_settings
    ADD COLUMN fsrs_desired_retention NUMERIC(4,3) NOT NULL DEFAULT 0.900,
    ADD COLUMN fsrs_max_interval_days INT NOT NULL DEFAULT 3650;
