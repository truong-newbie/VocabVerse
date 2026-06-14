ALTER TABLE learning_progress
    ADD COLUMN fsrs_difficulty NUMERIC(6,3),
    ADD COLUMN fsrs_stability NUMERIC(8,3),
    ADD COLUMN fsrs_retrievability NUMERIC(6,4);
