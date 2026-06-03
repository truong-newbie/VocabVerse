# 06-api-design.md

# THIẾT KẾ API

## Dự án

VocabVerse - AI Vocabulary Learning Platform

---

# 1. THÔNG TIN TÀI LIỆU

| Thuộc tính   | Giá trị    |
| ------------ | ---------- |
| Tên tài liệu | API Design |
| Dự án        | VocabVerse |
| Phiên bản    | 1.0        |
| Trạng thái   | Draft      |
| API Style    | REST API   |
| Base URL     | `/api/v1`  |

---

# 2. API CONVENTION

## 2.1 Base URL

```http
/api/v1
```

---

## 2.2 Authentication Header

```http
Authorization: Bearer <access_token>
```

---

## 2.3 Response Format Thành Công

```json
{
  "success": true,
  "message": "Success",
  "data": {}
}
```

---

## 2.4 Response Format Lỗi

```json
{
  "success": false,
  "message": "Something went wrong",
  "errorCode": "ERROR_CODE",
  "errors": []
}
```

---

## 2.5 Pagination Format

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "items": [],
    "page": 1,
    "size": 10,
    "totalItems": 100,
    "totalPages": 10
  }
}
```

---

# 3. AUTH API

## 3.1 Register

```http
POST /api/v1/auth/register
```

### Request

```json
{
  "email": "user@gmail.com",
  "password": "12345678",
  "fullName": "Nguyen Van A"
}
```

### Response

```json
{
  "success": true,
  "message": "Register successfully",
  "data": {
    "id": "uuid",
    "email": "user@gmail.com",
    "fullName": "Nguyen Van A",
    "role": "USER"
  }
}
```

---

## 3.2 Login

```http
POST /api/v1/auth/login
```

### Request

```json
{
  "email": "user@gmail.com",
  "password": "12345678"
}
```

### Response

```json
{
  "success": true,
  "message": "Login successfully",
  "data": {
    "accessToken": "jwt_access_token",
    "refreshToken": "jwt_refresh_token",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

---

## 3.3 Refresh Token

```http
POST /api/v1/auth/refresh-token
```

### Request

```json
{
  "refreshToken": "jwt_refresh_token"
}
```

---

## 3.4 Logout

```http
POST /api/v1/auth/logout
```

---

# 4. USER API

## 4.1 Get Current User

```http
GET /api/v1/me
```

### Response

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "email": "user@gmail.com",
    "fullName": "Nguyen Van A",
    "avatarUrl": null,
    "role": "USER"
  }
}
```

---

## 4.2 Update Profile

```http
PUT /api/v1/me
```

### Request

```json
{
  "fullName": "Nguyen Van B",
  "avatarUrl": "https://example.com/avatar.png"
}
```

---

# 5. COLLECTION API

## 5.1 Create Collection

```http
POST /api/v1/collections
```

### Request

```json
{
  "title": "TOEIC 600 Words",
  "description": "Bộ từ vựng TOEIC cơ bản",
  "visibility": "PRIVATE"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "title": "TOEIC 600 Words",
    "description": "Bộ từ vựng TOEIC cơ bản",
    "visibility": "PRIVATE",
    "totalWords": 0
  }
}
```

---

## 5.2 Get My Collections

```http
GET /api/v1/collections/my?page=1&size=10
```

---

## 5.3 Get Collection Detail

```http
GET /api/v1/collections/{collectionId}
```

---

## 5.4 Update Collection

```http
PUT /api/v1/collections/{collectionId}
```

### Request

```json
{
  "title": "IELTS Academic Words",
  "description": "Bộ từ vựng IELTS",
  "visibility": "PUBLIC"
}
```

---

## 5.5 Delete Collection

```http
DELETE /api/v1/collections/{collectionId}
```

---

## 5.6 Publish Collection

```http
PATCH /api/v1/collections/{collectionId}/publish
```

---

## 5.7 Make Collection Private

```http
PATCH /api/v1/collections/{collectionId}/private
```

---

## 5.8 Get Public Collections

```http
GET /api/v1/collections/public?page=1&size=10&keyword=toeic
```

---

## 5.9 Save Public Collection

```http
POST /api/v1/collections/{collectionId}/save
```

---

# 6. VOCABULARY API

## 6.1 Search Word

```http
GET /api/v1/vocabularies/search?word=abandon
```

### Response

```json
{
  "success": true,
  "data": {
    "word": "abandon",
    "phonetic": "/əˈbændən/",
    "audioUrl": "https://example.com/audio.mp3",
    "partOfSpeech": "verb",
    "meaningVi": "từ bỏ",
    "meaningEn": "to leave someone or something permanently",
    "synonyms": ["leave", "quit"],
    "antonyms": ["continue", "keep"],
    "examples": [
      {
        "en": "He abandoned the project.",
        "vi": "Anh ấy đã từ bỏ dự án."
      }
    ],
    "source": "DICTIONARY_API"
  }
}
```

---

## 6.2 Add Vocabulary To Collection

```http
POST /api/v1/collections/{collectionId}/vocabularies
```

### Request

```json
{
  "word": "abandon",
  "phonetic": "/əˈbændən/",
  "partOfSpeech": "verb",
  "meaningVi": "từ bỏ",
  "meaningEn": "to leave someone or something permanently",
  "synonyms": ["leave", "quit"],
  "antonyms": ["continue"],
  "examples": [
    {
      "en": "He abandoned the project.",
      "vi": "Anh ấy đã từ bỏ dự án."
    }
  ]
}
```

---

## 6.3 Get Vocabularies In Collection

```http
GET /api/v1/collections/{collectionId}/vocabularies?page=1&size=20
```

---

## 6.4 Remove Vocabulary From Collection

```http
DELETE /api/v1/collections/{collectionId}/vocabularies/{vocabularyId}
```

---

## 6.5 Update Vocabulary

```http
PUT /api/v1/vocabularies/{vocabularyId}
```

---

# 7. AI IMPORT API

## 7.1 Create AI Import Job

```http
POST /api/v1/ai/import-vocabularies
```

### Request

```json
{
  "collectionId": "uuid",
  "inputType": "TEXT",
  "rawInput": "abandon: từ bỏ\nability: khả năng"
}
```

### Response

```json
{
  "success": true,
  "message": "Import job created",
  "data": {
    "jobId": "uuid",
    "status": "PENDING"
  }
}
```

---

## 7.2 Get AI Import Job Status

```http
GET /api/v1/ai/import-jobs/{jobId}
```

### Response

```json
{
  "success": true,
  "data": {
    "jobId": "uuid",
    "status": "COMPLETED",
    "totalImported": 20,
    "failedItems": []
  }
}
```

---

# 8. FLASHCARD API

## 8.1 Start Flashcard Session

```http
POST /api/v1/learning/flashcards/start
```

### Request

```json
{
  "collectionId": "uuid"
}
```

---

## 8.2 Submit Flashcard Result

```http
POST /api/v1/learning/flashcards/result
```

### Request

```json
{
  "collectionId": "uuid",
  "vocabularyId": "uuid",
  "memoryLevel": "EASY"
}
```

---

# 9. QUIZ API

## 9.1 Start Quiz

```http
POST /api/v1/learning/quizzes/start
```

### Request

```json
{
  "collectionId": "uuid",
  "quizType": "MULTIPLE_CHOICE",
  "totalQuestions": 10
}
```

### Response

```json
{
  "success": true,
  "data": {
    "quizSessionId": "uuid",
    "questions": [
      {
        "questionId": "uuid",
        "vocabularyId": "uuid",
        "questionText": "What does 'abandon' mean?",
        "options": ["từ bỏ", "tiếp tục", "xây dựng", "mua"],
        "type": "MULTIPLE_CHOICE"
      }
    ]
  }
}
```

---

## 9.2 Submit Quiz

```http
POST /api/v1/learning/quizzes/{quizSessionId}/submit
```

### Request

```json
{
  "answers": [
    {
      "vocabularyId": "uuid",
      "userAnswer": "từ bỏ"
    }
  ]
}
```

---

# 10. TYPING PRACTICE API

## 10.1 Start Typing Practice

```http
POST /api/v1/learning/typing/start
```

### Request

```json
{
  "collectionId": "uuid",
  "totalQuestions": 10
}
```

---

## 10.2 Submit Typing Practice

```http
POST /api/v1/learning/typing/{typingSessionId}/submit
```

---

# 11. SPACED REPETITION API

## 11.1 Enable Spaced Repetition

```http
POST /api/v1/reviews/schedules
```

### Request

```json
{
  "collectionId": "uuid",
  "intervals": [1, 3, 7, 14, 30]
}
```

---

## 11.2 Update Review Schedule

```http
PUT /api/v1/reviews/schedules/{scheduleId}
```

### Request

```json
{
  "enabled": true,
  "intervals": [1, 3, 5, 14, 30]
}
```

---

## 11.3 Get Today Reviews

```http
GET /api/v1/reviews/today
```

---

## 11.4 Complete Review Session

```http
POST /api/v1/reviews/sessions/{reviewSessionId}/complete
```

---

# 12. DASHBOARD API

## 12.1 Get Dashboard Summary

```http
GET /api/v1/dashboard/summary
```

### Response

```json
{
  "success": true,
  "data": {
    "totalWords": 320,
    "learnedWords": 180,
    "reviewDueToday": 25,
    "streak": 12,
    "quizAccuracy": 82.5,
    "shadowingMinutes": 45,
    "roleplaySessions": 8
  }
}
```

---

# 13. SHADOWING API

## 13.1 Admin Upload Shadowing Video

```http
POST /api/v1/admin/shadowing/videos
```

### Request

```json
{
  "title": "Ordering Food",
  "description": "Luyện gọi món trong nhà hàng",
  "sourceType": "YOUTUBE_URL",
  "videoUrl": "https://youtube.com/watch?v=..."
}
```

---

## 13.2 Get Shadowing Videos

```http
GET /api/v1/shadowing/videos?page=1&size=10&difficulty=EASY
```

---

## 13.3 Get Shadowing Video Detail

```http
GET /api/v1/shadowing/videos/{videoId}
```

### Response

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "title": "Ordering Food",
    "videoUrl": "https://youtube.com/watch?v=...",
    "status": "READY",
    "lines": [
      {
        "lineOrder": 1,
        "startTimeMs": 1000,
        "endTimeMs": 3500,
        "englishText": "What would you like to order?",
        "vietnameseText": "Bạn muốn gọi món gì?"
      }
    ]
  }
}
```

---

## 13.4 Update Shadowing Progress

```http
POST /api/v1/shadowing/videos/{videoId}/progress
```

### Request

```json
{
  "lastLineOrder": 10,
  "completedLines": 10
}
```

---

# 14. ROLEPLAY API

## 14.1 Create Roleplay Session

```http
POST /api/v1/roleplay/sessions
```

### Request

```json
{
  "topic": "Restaurant",
  "difficulty": "EASY",
  "persona": "Friendly Waiter"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "sessionId": "uuid",
    "scenario": "You are ordering food in a small restaurant.",
    "firstMessage": "Hi! What would you like to order today?"
  }
}
```

---

## 14.2 Send Roleplay Message

```http
POST /api/v1/roleplay/sessions/{sessionId}/messages
```

### Request

```json
{
  "message": "I want eat pizza"
}
```

### Response

```json
{
  "success": true,
  "data": {
    "aiMessage": "Sure! What size pizza would you like?",
    "correction": {
      "original": "I want eat pizza",
      "corrected": "I want to eat pizza.",
      "betterExpression": "I'd like to order a pizza.",
      "explanation": "Sau 'want' cần dùng 'to + verb'."
    }
  }
}
```

---

## 14.3 End Roleplay Session

```http
POST /api/v1/roleplay/sessions/{sessionId}/end
```

---

## 14.4 Get Roleplay Report

```http
GET /api/v1/roleplay/sessions/{sessionId}/report
```

---

# 15. PDF EXPORT API

## 15.1 Create Export Job

```http
POST /api/v1/exports/pdf
```

### Request

```json
{
  "collectionId": "uuid",
  "exportType": "VOCABULARY_TABLE"
}
```

---

## 15.2 Get Export Job Status

```http
GET /api/v1/exports/{exportJobId}
```

---

# 16. ADMIN API

## 16.1 Get Users

```http
GET /api/v1/admin/users?page=1&size=20&keyword=gmail
```

---

## 16.2 Ban User

```http
PATCH /api/v1/admin/users/{userId}/ban
```

---

## 16.3 Unban User

```http
PATCH /api/v1/admin/users/{userId}/unban
```

---

## 16.4 Create System Collection

```http
POST /api/v1/admin/collections
```

### Request

```json
{
  "title": "600 Từ TOEIC Cơ Bản",
  "description": "Bộ từ vựng TOEIC miễn phí cho người mới bắt đầu",
  "visibility": "SYSTEM",
  "isFeatured": true
}
```

---

# 17. ERROR CODES

| Error Code              | HTTP Status | Ý nghĩa                        |
| ----------------------- | ----------: | ------------------------------ |
| AUTH_INVALID_CREDENTIAL |         401 | Sai email hoặc mật khẩu        |
| AUTH_TOKEN_EXPIRED      |         401 | Token hết hạn                  |
| ACCESS_DENIED           |         403 | Không có quyền                 |
| USER_NOT_FOUND          |         404 | Không tìm thấy user            |
| COLLECTION_NOT_FOUND    |         404 | Không tìm thấy collection      |
| VOCABULARY_NOT_FOUND    |         404 | Không tìm thấy từ vựng         |
| DUPLICATE_WORD          |         409 | Từ đã tồn tại trong collection |
| INVALID_INPUT_FORMAT    |         400 | Input không hợp lệ             |
| AI_PROCESSING_FAILED    |         500 | AI xử lý thất bại              |
| VIDEO_PROCESSING_FAILED |         500 | Xử lý video thất bại           |
| PDF_GENERATION_FAILED   |         500 | Tạo PDF thất bại               |

---

# 18. API PRIORITY CHO MVP

## Phase 1

```text
Auth API
User API
Collection API
Vocabulary API
AI Import API
Flashcard API
Quiz API
Typing Practice API
PDF Export API
```

## Phase 2

```text
Spaced Repetition API
Dashboard API
Notification API
Public Collection API
```

## Phase 3

```text
Shadowing API
Roleplay API
Admin API nâng cao
```

---

END OF DOCUMENT
