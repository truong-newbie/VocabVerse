CREATE TABLE vocabularies (
    id UUID PRIMARY KEY,
    word VARCHAR(150) NOT NULL,
    normalized_word VARCHAR(150) NOT NULL,
    phonetic VARCHAR(100),
    audio_url TEXT,
    part_of_speech VARCHAR(50),
    meaning_vi TEXT,
    meaning_en TEXT,
    synonyms JSONB,
    antonyms JSONB,
    examples JSONB,
    source VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    created_by UUID NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);

CREATE TABLE collection_vocabularies (
    id UUID PRIMARY KEY,
    collection_id UUID NOT NULL REFERENCES collections(id),
    vocabulary_id UUID NOT NULL REFERENCES vocabularies(id),
    added_by UUID NULL REFERENCES users(id),
    position INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_collection_vocabulary UNIQUE (collection_id, vocabulary_id)
);

CREATE INDEX idx_vocabularies_word ON vocabularies(word);
CREATE INDEX idx_vocabularies_normalized_word ON vocabularies(normalized_word);
CREATE INDEX idx_vocabularies_created_by ON vocabularies(created_by);
CREATE INDEX idx_collection_vocabularies_collection_id ON collection_vocabularies(collection_id);
CREATE INDEX idx_collection_vocabularies_vocabulary_id ON collection_vocabularies(vocabulary_id);
