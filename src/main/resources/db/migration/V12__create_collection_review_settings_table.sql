CREATE TABLE collection_review_settings (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    collection_id UUID NOT NULL REFERENCES collections(id),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    intervals_json JSONB NOT NULL DEFAULT '[1,3,7,14,30]'::jsonb,
    reminder_time TIME NULL,
    timezone VARCHAR(100),
    last_reset_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_collection_review_settings_user_collection UNIQUE (user_id, collection_id)
);

CREATE INDEX idx_collection_review_settings_user_id ON collection_review_settings(user_id);
CREATE INDEX idx_collection_review_settings_collection_id ON collection_review_settings(collection_id);
CREATE INDEX idx_collection_review_settings_enabled ON collection_review_settings(enabled);
CREATE INDEX idx_collection_review_settings_email_enabled ON collection_review_settings(email_enabled);
