CREATE TABLE shadowing_lessons (
    id UUID PRIMARY KEY,
    source VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    title VARCHAR(200),
    original_filename VARCHAR(255),
    youtube_url TEXT,
    content_type VARCHAR(100),
    file_size BIGINT,
    error_message TEXT,
    created_by UUID NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_shadowing_lessons_status ON shadowing_lessons(status);
CREATE INDEX idx_shadowing_lessons_created_at ON shadowing_lessons(created_at);

CREATE TABLE public_collection_moderations (
    id UUID PRIMARY KEY,
    collection_id UUID NOT NULL REFERENCES collections(id),
    moderated_by UUID NOT NULL REFERENCES users(id),
    reason TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_public_collection_moderations_collection_id ON public_collection_moderations(collection_id);
