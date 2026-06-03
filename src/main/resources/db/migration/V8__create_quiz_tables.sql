CREATE TABLE quiz_sessions (
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

CREATE TABLE quiz_questions (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES quiz_sessions(id),
    vocabulary_id UUID NOT NULL REFERENCES vocabularies(id),
    question_type VARCHAR(30) NOT NULL,
    question_text TEXT NOT NULL,
    correct_answer TEXT NOT NULL,
    options JSONB,
    user_answer TEXT NULL,
    is_correct BOOLEAN NULL,
    answered_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_quiz_sessions_user_id ON quiz_sessions(user_id);
CREATE INDEX idx_quiz_sessions_collection_id ON quiz_sessions(collection_id);
CREATE INDEX idx_quiz_sessions_status ON quiz_sessions(status);
CREATE INDEX idx_quiz_questions_session_id ON quiz_questions(session_id);
CREATE INDEX idx_quiz_questions_vocabulary_id ON quiz_questions(vocabulary_id);
