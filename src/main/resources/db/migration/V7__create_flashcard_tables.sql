CREATE TABLE flashcard_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    source VARCHAR(20) NOT NULL,
    collection_id UUID NULL REFERENCES collections(id),
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    total_cards INT NOT NULL DEFAULT 0,
    completed_cards INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE flashcard_session_items (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES flashcard_sessions(id),
    vocabulary_id UUID NOT NULL REFERENCES vocabularies(id),
    result VARCHAR(20) NULL,
    answered_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_flashcard_session_vocabulary UNIQUE (session_id, vocabulary_id)
);

CREATE INDEX idx_flashcard_sessions_user_id ON flashcard_sessions(user_id);
CREATE INDEX idx_flashcard_sessions_collection_id ON flashcard_sessions(collection_id);
CREATE INDEX idx_flashcard_sessions_status ON flashcard_sessions(status);
CREATE INDEX idx_flashcard_session_items_session_id ON flashcard_session_items(session_id);
CREATE INDEX idx_flashcard_session_items_vocabulary_id ON flashcard_session_items(vocabulary_id);
