CREATE TABLE review_history (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    vocabulary_id UUID NOT NULL REFERENCES vocabularies(id),
    result VARCHAR(20) NOT NULL,
    reviewed_at TIMESTAMP NOT NULL,
    next_review_at TIMESTAMP NOT NULL,
    previous_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL
);

CREATE INDEX idx_review_history_user_id ON review_history(user_id);
CREATE INDEX idx_review_history_vocabulary_id ON review_history(vocabulary_id);
CREATE INDEX idx_review_history_reviewed_at ON review_history(reviewed_at);
CREATE INDEX idx_review_history_next_review_at ON review_history(next_review_at);
