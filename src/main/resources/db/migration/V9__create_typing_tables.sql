CREATE TABLE typing_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    source VARCHAR(20) NOT NULL,
    collection_id UUID NULL REFERENCES collections(id),
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    total_questions INT NOT NULL DEFAULT 0,
    correct_answers INT NOT NULL DEFAULT 0,
    completed_questions INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE typing_questions (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES typing_sessions(id),
    vocabulary_id UUID NOT NULL REFERENCES vocabularies(id),
    prompt_text TEXT NOT NULL,
    correct_answer TEXT NOT NULL,
    user_answer TEXT NULL,
    is_correct BOOLEAN NULL,
    similarity_score NUMERIC(5,2) NULL,
    answered_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_typing_sessions_user_id ON typing_sessions(user_id);
CREATE INDEX idx_typing_sessions_collection_id ON typing_sessions(collection_id);
CREATE INDEX idx_typing_sessions_status ON typing_sessions(status);
CREATE INDEX idx_typing_questions_session_id ON typing_questions(session_id);
CREATE INDEX idx_typing_questions_vocabulary_id ON typing_questions(vocabulary_id);
