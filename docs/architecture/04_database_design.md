# 04-database-design.md

# THIẾT KẾ CƠ SỞ DỮ LIỆU

## Dự án

VocabVerse - AI Vocabulary Learning Platform

---

# 1. THÔNG TIN TÀI LIỆU

| Thuộc tính      | Giá trị                     |
| --------------- | --------------------------- |
| Tên tài liệu    | Database Design             |
| Dự án           | VocabVerse                  |
| Phiên bản       | 1.0                         |
| Trạng thái      | Draft                       |
| Database        | PostgreSQL                  |
| Backend Mapping | Spring Boot JPA / Hibernate |

---

# 2. MỤC TIÊU THIẾT KẾ DATABASE

Database cần hỗ trợ các nghiệp vụ chính:

* Quản lý user
* Phân quyền user/admin
* Quản lý collection từ vựng
* Một từ có thể thuộc nhiều collection
* Flashcard progress
* Quiz result
* Typing practice result
* Spaced repetition
* Email/browser notification
* Shadowing video
* Subtitle song ngữ
* AI roleplay session
* Export PDF history
* Public/private collection

---

# 3. NGUYÊN TẮC THIẾT KẾ

## 3.1 UUID Primary Key

Tất cả bảng chính sử dụng UUID.

Ví dụ:

```sql
id UUID PRIMARY KEY DEFAULT gen_random_uuid()
```

Lý do:

* Khó đoán ID
* Phù hợp REST API
* Dễ scale
* Không lộ số lượng record

---

## 3.2 Audit Columns

Các bảng chính nên có:

```sql
created_at TIMESTAMP NOT NULL DEFAULT NOW(),
updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
deleted_at TIMESTAMP NULL
```

---

## 3.3 Soft Delete

Không xóa cứng dữ liệu quan trọng.

Áp dụng cho:

* users
* collections
* vocabularies
* shadowing_videos
* roleplay_sessions

---

## 3.4 Enum Strategy

Một số field dùng enum dạng VARCHAR.

Ví dụ:

```sql
role VARCHAR(20)
visibility VARCHAR(20)
status VARCHAR(30)
```

Không nên dùng PostgreSQL ENUM ở giai đoạn đầu vì khó migrate khi thay đổi.

---

# 4. ERD TỔNG QUAN

```text
users
  ├── collections
  │      └── collection_vocabularies
  │              └── vocabularies
  │
  ├── flashcard_progress
  ├── quiz_sessions
  ├── typing_sessions
  ├── review_schedules
  │      └── review_sessions
  │
  ├── roleplay_sessions
  │      └── roleplay_messages
  │
  └── notification_logs

admin/users
  └── shadowing_videos
          └── shadowing_lines
```

---

# 5. DANH SÁCH BẢNG

## 5.1 users

Lưu thông tin tài khoản.

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    avatar_url TEXT,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);
```

Role:

```text
USER
ADMIN
```

Status:

```text
ACTIVE
INACTIVE
BANNED
```

---

## 5.2 collections

Lưu bộ từ vựng.

```sql
CREATE TABLE collections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NULL REFERENCES users(id),
    title VARCHAR(150) NOT NULL,
    description TEXT,
    visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    type VARCHAR(30) NOT NULL DEFAULT 'USER_CREATED',
    thumbnail_url TEXT,
    total_words INT NOT NULL DEFAULT 0,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);
```

visibility:

```text
PRIVATE
PUBLIC
SYSTEM
```

type:

```text
USER_CREATED
ADMIN_CREATED
AI_GENERATED
IMPORTED
```

Ghi chú:

* Collection do user tạo có `owner_id`.
* Collection hệ thống do admin tạo có thể để `visibility = SYSTEM`.
* Public collection là collection user cho phép người khác xem/lưu.

---

## 5.3 vocabularies

Lưu dữ liệu chuẩn của từ vựng.

```sql
CREATE TABLE vocabularies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
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
```

source:

```text
MANUAL
DICTIONARY_API
AI_NORMALIZED
ADMIN
IMPORT
```

Ví dụ `examples`:

```json
[
  {
    "en": "He abandoned the project.",
    "vi": "Anh ấy đã từ bỏ dự án."
  }
]
```

---

## 5.4 collection_vocabularies

Bảng trung gian many-to-many giữa collection và vocabulary.

```sql
CREATE TABLE collection_vocabularies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    collection_id UUID NOT NULL REFERENCES collections(id),
    vocabulary_id UUID NOT NULL REFERENCES vocabularies(id),
    added_by UUID NULL REFERENCES users(id),
    position INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_collection_vocabulary UNIQUE (collection_id, vocabulary_id)
);
```

Lý do cần bảng trung gian:

* Một collection có nhiều từ
* Một từ có thể nằm trong nhiều collection

---

# 6. LEARNING PROGRESS

## 6.1 flashcard_progress

Theo dõi tiến độ học flashcard của từng user.

```sql
CREATE TABLE flashcard_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    collection_id UUID NOT NULL REFERENCES collections(id),
    vocabulary_id UUID NOT NULL REFERENCES vocabularies(id),
    memory_level VARCHAR(20) NOT NULL DEFAULT 'NEW',
    correct_count INT NOT NULL DEFAULT 0,
    wrong_count INT NOT NULL DEFAULT 0,
    last_reviewed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_flashcard_progress UNIQUE (user_id, collection_id, vocabulary_id)
);
```

memory_level:

```text
NEW
HARD
MEDIUM
EASY
MASTERED
```

---

## 6.2 quiz_sessions

Lưu một lần làm quiz.

```sql
CREATE TABLE quiz_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    collection_id UUID NOT NULL REFERENCES collections(id),
    quiz_type VARCHAR(30) NOT NULL,
    total_questions INT NOT NULL,
    correct_answers INT NOT NULL DEFAULT 0,
    score NUMERIC(5,2) NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP NULL
);
```

quiz_type:

```text
MULTIPLE_CHOICE
MATCHING
FILL_BLANK
MIXED
```

---

## 6.3 quiz_answers

Lưu từng câu trả lời trong quiz.

```sql
CREATE TABLE quiz_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    quiz_session_id UUID NOT NULL REFERENCES quiz_sessions(id),
    vocabulary_id UUID NOT NULL REFERENCES vocabularies(id),
    question_text TEXT NOT NULL,
    user_answer TEXT,
    correct_answer TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## 6.4 typing_sessions

Lưu một phiên luyện gõ đáp án.

```sql
CREATE TABLE typing_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    collection_id UUID NOT NULL REFERENCES collections(id),
    total_questions INT NOT NULL,
    correct_answers INT NOT NULL DEFAULT 0,
    score NUMERIC(5,2) NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP NULL
);
```

---

## 6.5 typing_answers

```sql
CREATE TABLE typing_answers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    typing_session_id UUID NOT NULL REFERENCES typing_sessions(id),
    vocabulary_id UUID NOT NULL REFERENCES vocabularies(id),
    prompt_text TEXT NOT NULL,
    user_answer TEXT,
    correct_answer TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

# 7. SPACED REPETITION

## 7.1 review_schedules

Lưu cấu hình ôn tập của user cho collection.

```sql
CREATE TABLE review_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    collection_id UUID NOT NULL REFERENCES collections(id),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    intervals JSONB NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    next_review_date DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_review_schedule UNIQUE (user_id, collection_id)
);
```

Ví dụ `intervals`:

```json
[1, 3, 7, 14, 30]
```

---

## 7.2 review_sessions

Lưu từng ngày ôn cụ thể.

```sql
CREATE TABLE review_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    schedule_id UUID NOT NULL REFERENCES review_schedules(id),
    user_id UUID NOT NULL REFERENCES users(id),
    collection_id UUID NOT NULL REFERENCES collections(id),
    review_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_review_session UNIQUE (schedule_id, review_date)
);
```

status:

```text
PENDING
COMPLETED
MISSED
SKIPPED
```

---

# 8. NOTIFICATION

## 8.1 notification_preferences

Lưu cấu hình nhận thông báo.

```sql
CREATE TABLE notification_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    browser_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    reminder_time TIME NOT NULL DEFAULT '08:00:00',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

---

## 8.2 notification_logs

Lưu lịch sử gửi thông báo.

```sql
CREATE TABLE notification_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    review_session_id UUID NULL REFERENCES review_sessions(id),
    channel VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP NULL,
    error_message TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

channel:

```text
EMAIL
BROWSER
```

status:

```text
PENDING
SENT
FAILED
```

---

# 9. SHADOWING

## 9.1 shadowing_videos

Lưu video shadowing do admin upload.

```sql
CREATE TABLE shadowing_videos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    source_type VARCHAR(30) NOT NULL,
    video_url TEXT NOT NULL,
    thumbnail_url TEXT,
    duration_seconds INT,
    difficulty VARCHAR(20) DEFAULT 'EASY',
    status VARCHAR(30) NOT NULL DEFAULT 'PROCESSING',
    uploaded_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP NULL
);
```

source_type:

```text
UPLOAD
YOUTUBE_URL
```

status:

```text
PROCESSING
READY
FAILED
```

difficulty:

```text
EASY
MEDIUM
HARD
```

---

## 9.2 shadowing_lines

Lưu từng dòng subtitle.

```sql
CREATE TABLE shadowing_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    video_id UUID NOT NULL REFERENCES shadowing_videos(id),
    line_order INT NOT NULL,
    start_time_ms INT NOT NULL,
    end_time_ms INT NOT NULL,
    english_text TEXT NOT NULL,
    vietnamese_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_shadowing_line_order UNIQUE (video_id, line_order)
);
```

---

## 9.3 shadowing_progress

Theo dõi tiến độ học shadowing của user.

```sql
CREATE TABLE shadowing_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    video_id UUID NOT NULL REFERENCES shadowing_videos(id),
    completed_lines INT NOT NULL DEFAULT 0,
    total_lines INT NOT NULL DEFAULT 0,
    last_line_order INT DEFAULT 0,
    completed_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_shadowing_progress UNIQUE (user_id, video_id)
);
```

---

# 10. AI ROLEPLAY

## 10.1 roleplay_sessions

```sql
CREATE TABLE roleplay_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    topic VARCHAR(100) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    persona VARCHAR(100) NOT NULL,
    scenario TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    score NUMERIC(5,2),
    report JSONB,
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    ended_at TIMESTAMP NULL,
    deleted_at TIMESTAMP NULL
);
```

difficulty:

```text
EASY
MEDIUM
HARD
```

status:

```text
ACTIVE
COMPLETED
CANCELLED
```

---

## 10.2 roleplay_messages

```sql
CREATE TABLE roleplay_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES roleplay_sessions(id),
    sender VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    correction JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

sender:

```text
USER
AI
SYSTEM
```

Ví dụ correction:

```json
{
  "grammarMistakes": [
    {
      "original": "I want eat pizza",
      "corrected": "I want to eat pizza",
      "explanation": "Sau want cần dùng to + verb."
    }
  ],
  "betterExpression": "I'd like to order a pizza."
}
```

---

# 11. AI IMPORT / NORMALIZATION

## 11.1 ai_import_jobs

Lưu job xử lý AI khi user import từ vựng.

```sql
CREATE TABLE ai_import_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    collection_id UUID NULL REFERENCES collections(id),
    input_type VARCHAR(30) NOT NULL,
    raw_input TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    result JSONB,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP NULL
);
```

input_type:

```text
TEXT
JSON
CSV
```

status:

```text
PENDING
PROCESSING
COMPLETED
FAILED
```

---

# 12. PDF EXPORT

## 12.1 export_jobs

```sql
CREATE TABLE export_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    collection_id UUID NOT NULL REFERENCES collections(id),
    export_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    file_url TEXT,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP NULL
);
```

export_type:

```text
VOCABULARY_TABLE
PRINTABLE_FLASHCARD
```

status:

```text
PENDING
PROCESSING
COMPLETED
FAILED
```

---

# 13. INDEX STRATEGY

## 13.1 users

```sql
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
```

---

## 13.2 collections

```sql
CREATE INDEX idx_collections_owner_id ON collections(owner_id);
CREATE INDEX idx_collections_visibility ON collections(visibility);
CREATE INDEX idx_collections_type ON collections(type);
CREATE INDEX idx_collections_title ON collections(title);
```

---

## 13.3 vocabularies

```sql
CREATE INDEX idx_vocabularies_word ON vocabularies(word);
CREATE INDEX idx_vocabularies_normalized_word ON vocabularies(normalized_word);
CREATE INDEX idx_vocabularies_created_by ON vocabularies(created_by);
```

---

## 13.4 collection_vocabularies

```sql
CREATE INDEX idx_collection_vocabularies_collection_id ON collection_vocabularies(collection_id);
CREATE INDEX idx_collection_vocabularies_vocabulary_id ON collection_vocabularies(vocabulary_id);
```

---

## 13.5 review

```sql
CREATE INDEX idx_review_sessions_user_date ON review_sessions(user_id, review_date);
CREATE INDEX idx_review_sessions_status ON review_sessions(status);
```

---

## 13.6 shadowing

```sql
CREATE INDEX idx_shadowing_videos_status ON shadowing_videos(status);
CREATE INDEX idx_shadowing_lines_video_id ON shadowing_lines(video_id);
```

---

## 13.7 roleplay

```sql
CREATE INDEX idx_roleplay_sessions_user_id ON roleplay_sessions(user_id);
CREATE INDEX idx_roleplay_messages_session_id ON roleplay_messages(session_id);
```

---

# 14. UNIQUE CONSTRAINTS

| Bảng                    | Constraint                              | Mục đích                             |
| ----------------------- | --------------------------------------- | ------------------------------------ |
| users                   | email                                   | Không trùng email                    |
| collection_vocabularies | collection_id + vocabulary_id           | Không trùng từ trong cùng collection |
| flashcard_progress      | user_id + collection_id + vocabulary_id | Một progress duy nhất                |
| review_schedules        | user_id + collection_id                 | Một lịch ôn cho một collection       |
| review_sessions         | schedule_id + review_date               | Không tạo trùng ngày ôn              |
| shadowing_lines         | video_id + line_order                   | Không trùng thứ tự subtitle          |

---

# 15. QUY TẮC XÓA DỮ LIỆU

## 15.1 User xóa collection

Không xóa vocabulary gốc.

Chỉ soft delete collection.

```text
collections.deleted_at = NOW()
```

---

## 15.2 User xóa một từ khỏi collection

Xóa record trong:

```text
collection_vocabularies
```

Không xóa record trong:

```text
vocabularies
```

---

## 15.3 Admin xóa shadowing video

Soft delete video.

Không hiển thị cho user.

---

# 16. BUSINESS RULES LIÊN QUAN DATABASE

## DBR-001

Một vocabulary có thể tồn tại độc lập với collection.

---

## DBR-002

Một collection bắt buộc có title.

---

## DBR-003

Collection SYSTEM không có owner_id hoặc owner_id là admin.

---

## DBR-004

User không được sửa collection của user khác.

---

## DBR-005

User chỉ xem được:

* Collection của chính mình
* Collection PUBLIC
* Collection SYSTEM

---

## DBR-006

Spaced repetition chỉ áp dụng cho collection có ít nhất một vocabulary.

---

## DBR-007

Review session không được tạo trùng ngày.

---

# 17. GỢI Ý ENTITY MAPPING CHO SPRING BOOT

## UserEntity

```java
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    private UUID id;

    private String email;

    private String passwordHash;

    private String fullName;

    private String role;

    private String status;
}
```

---

## CollectionEntity

```java
@Entity
@Table(name = "collections")
public class CollectionEntity {
    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    private String title;

    private String visibility;

    private String type;
}
```

---

## VocabularyEntity

```java
@Entity
@Table(name = "vocabularies")
public class VocabularyEntity {
    @Id
    private UUID id;

    private String word;

    private String normalizedWord;

    private String phonetic;

    private String partOfSpeech;

    private String meaningVi;

    private String meaningEn;
}
```

---

# 18. MIGRATION STRATEGY

Nên dùng:

```text
Flyway
```

Cấu trúc:

```text
db/migration
├── V1__create_users_table.sql
├── V2__create_collections_table.sql
├── V3__create_vocabularies_table.sql
├── V4__create_learning_tables.sql
├── V5__create_spaced_repetition_tables.sql
├── V6__create_shadowing_tables.sql
├── V7__create_roleplay_tables.sql
```

---

# 19. DỮ LIỆU MẪU

## Admin Collection

```sql
INSERT INTO collections (
    id,
    owner_id,
    title,
    description,
    visibility,
    type,
    is_featured
)
VALUES (
    gen_random_uuid(),
    NULL,
    '600 Từ Vựng TOEIC Cơ Bản',
    'Bộ từ vựng TOEIC dành cho người mới bắt đầu.',
    'SYSTEM',
    'ADMIN_CREATED',
    TRUE
);
```

---

# 20. KẾT LUẬN

Thiết kế database này đáp ứng các yêu cầu chính của VocabVerse:

* Dễ mở rộng
* Hỗ trợ nhiều collection
* Hỗ trợ từ vựng dùng chung
* Hỗ trợ học tập cá nhân hóa
* Hỗ trợ spaced repetition
* Hỗ trợ shadowing
* Hỗ trợ AI roleplay
* Phù hợp triển khai với Spring Boot và PostgreSQL

END OF DOCUMENT
