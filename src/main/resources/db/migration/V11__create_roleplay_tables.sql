CREATE TABLE roleplay_sessions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    topic VARCHAR(100) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    persona VARCHAR(100) NOT NULL,
    scenario TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE roleplay_messages (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES roleplay_sessions(id),
    sender VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    correction JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE roleplay_reports (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL UNIQUE REFERENCES roleplay_sessions(id),
    summary TEXT NOT NULL,
    strengths JSONB,
    weaknesses JSONB,
    suggested_vocabulary JSONB,
    grammar_feedback TEXT,
    overall_score INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_roleplay_sessions_user_id ON roleplay_sessions(user_id);
CREATE INDEX idx_roleplay_sessions_status ON roleplay_sessions(status);
CREATE INDEX idx_roleplay_messages_session_id ON roleplay_messages(session_id);
CREATE INDEX idx_roleplay_reports_session_id ON roleplay_reports(session_id);
