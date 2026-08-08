-- VocabVerse MySQL ERD schema
-- Purpose: import into MySQL-compatible ERD tools only.
-- Source schema: Spring Boot Flyway PostgreSQL migrations V1-V20.
-- Notes:
--   - PostgreSQL UUID is represented as CHAR(36).
--   - PostgreSQL JSONB is represented as JSON.
--   - PostgreSQL BOOLEAN is represented as TINYINT(1).
--   - This file is for schema visualization, not for running the production app.

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS shadowing_lesson_subtitles;
DROP TABLE IF EXISTS public_collection_moderations;
DROP TABLE IF EXISTS shadowing_lessons;
DROP TABLE IF EXISTS roleplay_reports;
DROP TABLE IF EXISTS roleplay_messages;
DROP TABLE IF EXISTS roleplay_sessions;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS collection_review_settings;
DROP TABLE IF EXISTS typing_questions;
DROP TABLE IF EXISTS typing_sessions;
DROP TABLE IF EXISTS quiz_questions;
DROP TABLE IF EXISTS quiz_sessions;
DROP TABLE IF EXISTS flashcard_session_items;
DROP TABLE IF EXISTS flashcard_sessions;
DROP TABLE IF EXISTS review_history;
DROP TABLE IF EXISTS learning_progress;
DROP TABLE IF EXISTS collection_vocabularies;
DROP TABLE IF EXISTS vocabularies;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS collections;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
    id CHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    avatar_url TEXT,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by CHAR(36) NULL,
    updated_by CHAR(36) NULL,

    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_revoked TINYINT(1) NOT NULL DEFAULT 0,

    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_refresh_tokens_token (token),
    INDEX idx_refresh_tokens_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE collections (
    id CHAR(36) PRIMARY KEY,
    owner_id CHAR(36) NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    thumbnail_url TEXT,
    total_words INT NOT NULL DEFAULT 0,
    is_featured TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    CONSTRAINT fk_collections_owner FOREIGN KEY (owner_id) REFERENCES users(id),
    INDEX idx_collections_owner_id (owner_id),
    INDEX idx_collections_visibility (visibility),
    INDEX idx_collections_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE vocabularies (
    id CHAR(36) PRIMARY KEY,
    word VARCHAR(150) NOT NULL,
    normalized_word VARCHAR(150) NOT NULL,
    phonetic VARCHAR(100),
    audio_url TEXT,
    part_of_speech VARCHAR(50),
    meaning_vi TEXT,
    meaning_en TEXT,
    synonyms JSON,
    antonyms JSON,
    examples JSON,
    source VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    created_by CHAR(36) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    CONSTRAINT fk_vocabularies_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_vocabularies_word (word),
    INDEX idx_vocabularies_normalized_word (normalized_word),
    INDEX idx_vocabularies_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE collection_vocabularies (
    id CHAR(36) PRIMARY KEY,
    collection_id CHAR(36) NOT NULL,
    vocabulary_id CHAR(36) NOT NULL,
    added_by CHAR(36) NULL,
    position INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_collection_vocabulary UNIQUE (collection_id, vocabulary_id),
    CONSTRAINT fk_collection_vocabularies_collection FOREIGN KEY (collection_id) REFERENCES collections(id),
    CONSTRAINT fk_collection_vocabularies_vocabulary FOREIGN KEY (vocabulary_id) REFERENCES vocabularies(id),
    CONSTRAINT fk_collection_vocabularies_added_by FOREIGN KEY (added_by) REFERENCES users(id),
    INDEX idx_collection_vocabularies_collection_id (collection_id),
    INDEX idx_collection_vocabularies_vocabulary_id (vocabulary_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE learning_progress (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    vocabulary_id CHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    repetition_count INT NOT NULL DEFAULT 0,
    ease_factor DECIMAL(4,2) NOT NULL DEFAULT 2.50,
    last_interval_days INT NOT NULL DEFAULT 0,
    lapse_count INT NOT NULL DEFAULT 0,
    review_count INT NOT NULL DEFAULT 0,
    fsrs_difficulty DECIMAL(6,3),
    fsrs_stability DECIMAL(8,3),
    fsrs_retrievability DECIMAL(6,4),
    next_review_at TIMESTAMP NULL,
    last_reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_learning_progress_user_vocabulary UNIQUE (user_id, vocabulary_id),
    CONSTRAINT fk_learning_progress_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_learning_progress_vocabulary FOREIGN KEY (vocabulary_id) REFERENCES vocabularies(id),
    INDEX idx_learning_progress_user_id (user_id),
    INDEX idx_learning_progress_vocabulary_id (vocabulary_id),
    INDEX idx_learning_progress_status (status),
    INDEX idx_learning_progress_next_review_at (next_review_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE review_history (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    vocabulary_id CHAR(36) NOT NULL,
    result VARCHAR(20) NOT NULL,
    reviewed_at TIMESTAMP NOT NULL,
    next_review_at TIMESTAMP NOT NULL,
    previous_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,

    CONSTRAINT fk_review_history_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_review_history_vocabulary FOREIGN KEY (vocabulary_id) REFERENCES vocabularies(id),
    INDEX idx_review_history_user_id (user_id),
    INDEX idx_review_history_vocabulary_id (vocabulary_id),
    INDEX idx_review_history_reviewed_at (reviewed_at),
    INDEX idx_review_history_next_review_at (next_review_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE flashcard_sessions (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    source VARCHAR(20) NOT NULL,
    collection_id CHAR(36) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    total_cards INT NOT NULL DEFAULT 0,
    completed_cards INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_flashcard_sessions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_flashcard_sessions_collection FOREIGN KEY (collection_id) REFERENCES collections(id),
    INDEX idx_flashcard_sessions_user_id (user_id),
    INDEX idx_flashcard_sessions_collection_id (collection_id),
    INDEX idx_flashcard_sessions_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE flashcard_session_items (
    id CHAR(36) PRIMARY KEY,
    session_id CHAR(36) NOT NULL,
    vocabulary_id CHAR(36) NOT NULL,
    result VARCHAR(20) NULL,
    answered_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_flashcard_session_vocabulary UNIQUE (session_id, vocabulary_id),
    CONSTRAINT fk_flashcard_session_items_session FOREIGN KEY (session_id) REFERENCES flashcard_sessions(id),
    CONSTRAINT fk_flashcard_session_items_vocabulary FOREIGN KEY (vocabulary_id) REFERENCES vocabularies(id),
    INDEX idx_flashcard_session_items_session_id (session_id),
    INDEX idx_flashcard_session_items_vocabulary_id (vocabulary_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quiz_sessions (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    source VARCHAR(20) NOT NULL,
    collection_id CHAR(36) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    total_questions INT NOT NULL DEFAULT 0,
    correct_answers INT NOT NULL DEFAULT 0,
    completed_questions INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_quiz_sessions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_quiz_sessions_collection FOREIGN KEY (collection_id) REFERENCES collections(id),
    INDEX idx_quiz_sessions_user_id (user_id),
    INDEX idx_quiz_sessions_collection_id (collection_id),
    INDEX idx_quiz_sessions_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE quiz_questions (
    id CHAR(36) PRIMARY KEY,
    session_id CHAR(36) NOT NULL,
    vocabulary_id CHAR(36) NOT NULL,
    question_type VARCHAR(30) NOT NULL,
    question_text TEXT NOT NULL,
    correct_answer TEXT NOT NULL,
    options JSON,
    user_answer TEXT NULL,
    is_correct TINYINT(1) NULL,
    answered_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_quiz_questions_session FOREIGN KEY (session_id) REFERENCES quiz_sessions(id),
    CONSTRAINT fk_quiz_questions_vocabulary FOREIGN KEY (vocabulary_id) REFERENCES vocabularies(id),
    INDEX idx_quiz_questions_session_id (session_id),
    INDEX idx_quiz_questions_vocabulary_id (vocabulary_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE typing_sessions (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    source VARCHAR(20) NOT NULL,
    collection_id CHAR(36) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    total_questions INT NOT NULL DEFAULT 0,
    correct_answers INT NOT NULL DEFAULT 0,
    completed_questions INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_typing_sessions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_typing_sessions_collection FOREIGN KEY (collection_id) REFERENCES collections(id),
    INDEX idx_typing_sessions_user_id (user_id),
    INDEX idx_typing_sessions_collection_id (collection_id),
    INDEX idx_typing_sessions_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE typing_questions (
    id CHAR(36) PRIMARY KEY,
    session_id CHAR(36) NOT NULL,
    vocabulary_id CHAR(36) NOT NULL,
    prompt_text TEXT NOT NULL,
    correct_answer TEXT NOT NULL,
    user_answer TEXT NULL,
    is_correct TINYINT(1) NULL,
    similarity_score DECIMAL(5,2) NULL,
    answered_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_typing_questions_session FOREIGN KEY (session_id) REFERENCES typing_sessions(id),
    CONSTRAINT fk_typing_questions_vocabulary FOREIGN KEY (vocabulary_id) REFERENCES vocabularies(id),
    INDEX idx_typing_questions_session_id (session_id),
    INDEX idx_typing_questions_vocabulary_id (vocabulary_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE collection_review_settings (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    collection_id CHAR(36) NOT NULL,
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    email_enabled TINYINT(1) NOT NULL DEFAULT 1,
    intervals_json JSON NOT NULL,
    reminder_time TIME NULL,
    timezone VARCHAR(100),
    scheduler_type VARCHAR(30) NOT NULL DEFAULT 'FIXED_INTERVAL',
    fsrs_desired_retention DECIMAL(4,3) NOT NULL DEFAULT 0.900,
    fsrs_max_interval_days INT NOT NULL DEFAULT 3650,
    last_reset_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_collection_review_settings_user_collection UNIQUE (user_id, collection_id),
    CONSTRAINT fk_collection_review_settings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_collection_review_settings_collection FOREIGN KEY (collection_id) REFERENCES collections(id),
    INDEX idx_collection_review_settings_user_id (user_id),
    INDEX idx_collection_review_settings_collection_id (collection_id),
    INDEX idx_collection_review_settings_enabled (enabled),
    INDEX idx_collection_review_settings_email_enabled (email_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_date DATE GENERATED ALWAYS AS (DATE(created_at)) STORED,

    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_notifications_user_type_created_date UNIQUE (user_id, type, created_date),
    INDEX idx_notifications_user_id (user_id),
    INDEX idx_notifications_type (type),
    INDEX idx_notifications_status (status),
    INDEX idx_notifications_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roleplay_sessions (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    persona VARCHAR(100) NOT NULL,
    scenario TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_roleplay_sessions_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_roleplay_sessions_user_id (user_id),
    INDEX idx_roleplay_sessions_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roleplay_messages (
    id CHAR(36) PRIMARY KEY,
    session_id CHAR(36) NOT NULL,
    sender VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    correction JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_roleplay_messages_session FOREIGN KEY (session_id) REFERENCES roleplay_sessions(id),
    INDEX idx_roleplay_messages_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE roleplay_reports (
    id CHAR(36) PRIMARY KEY,
    session_id CHAR(36) NOT NULL UNIQUE,
    summary TEXT NOT NULL,
    strengths JSON,
    weaknesses JSON,
    suggested_vocabulary JSON,
    grammar_feedback TEXT,
    overall_score INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_roleplay_reports_session FOREIGN KEY (session_id) REFERENCES roleplay_sessions(id),
    INDEX idx_roleplay_reports_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE shadowing_lessons (
    id CHAR(36) PRIMARY KEY,
    source VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    title VARCHAR(200),
    description TEXT,
    original_filename VARCHAR(255),
    youtube_url TEXT,
    cloudinary_public_id VARCHAR(500),
    video_url TEXT,
    thumbnail_url TEXT,
    storage_provider VARCHAR(50),
    content_type VARCHAR(100),
    file_size BIGINT,
    duration VARCHAR(20),
    progress INT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_by CHAR(36) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,

    CONSTRAINT fk_shadowing_lessons_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_shadowing_lessons_status (status),
    INDEX idx_shadowing_lessons_created_at (created_at),
    INDEX idx_shadowing_lessons_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE shadowing_lesson_subtitles (
    id CHAR(36) PRIMARY KEY,
    lesson_id CHAR(36) NOT NULL,
    start_time_ms INT NOT NULL,
    end_time_ms INT NOT NULL,
    english_text TEXT NOT NULL,
    vietnamese_text TEXT,
    order_index INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_shadowing_subtitles_lesson FOREIGN KEY (lesson_id) REFERENCES shadowing_lessons(id) ON DELETE CASCADE,
    INDEX idx_shadowing_subtitles_lesson_order (lesson_id, order_index, start_time_ms)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE public_collection_moderations (
    id CHAR(36) PRIMARY KEY,
    collection_id CHAR(36) NOT NULL,
    moderated_by CHAR(36) NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_public_collection_moderations_collection FOREIGN KEY (collection_id) REFERENCES collections(id),
    CONSTRAINT fk_public_collection_moderations_moderated_by FOREIGN KEY (moderated_by) REFERENCES users(id),
    INDEX idx_public_collection_moderations_collection_id (collection_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
