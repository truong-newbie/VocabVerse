ALTER TABLE roleplay_reports
    ADD COLUMN grammar_score INT,
    ADD COLUMN vocabulary_score INT,
    ADD COLUMN relevance_score INT,
    ADD COLUMN fluency_score INT,
    ADD COLUMN interaction_score INT;
