ALTER TABLE collection_review_settings
    ADD COLUMN scheduler_type VARCHAR(30) NOT NULL DEFAULT 'FIXED_INTERVAL';

ALTER TABLE collection_review_settings
    ADD CONSTRAINT chk_collection_review_scheduler_type
    CHECK (scheduler_type IN ('FIXED_INTERVAL', 'SM2', 'FSRS'));
