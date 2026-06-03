CREATE TABLE learning_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    vocabulary_id UUID NOT NULL REFERENCES vocabularies(id),
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    repetition_count INT NOT NULL DEFAULT 0,
    ease_factor NUMERIC(4,2) NOT NULL DEFAULT 2.50,
    next_review_at TIMESTAMP NULL,
    last_reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_learning_progress_user_vocabulary UNIQUE (user_id, vocabulary_id)
);

CREATE INDEX idx_learning_progress_user_id ON learning_progress(user_id);
CREATE INDEX idx_learning_progress_vocabulary_id ON learning_progress(vocabulary_id);
CREATE INDEX idx_learning_progress_status ON learning_progress(status);
CREATE INDEX idx_learning_progress_next_review_at ON learning_progress(next_review_at);
