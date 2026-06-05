# VocabVerse API Documentation For Frontend

## Base URL

Backend dang dung context path `/api/v1`. FE chon mot trong hai cach sau:

Cach khuyen nghi:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Sau do goi API bang path khong co `/api/v1`:

```ts
api.post("/auth/register", body)
```

Cach khac:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Sau do goi API bang path co `/api/v1`:

```ts
api.post("/api/v1/auth/register", body)
```

Khong duoc de ca base URL va request path cung chua `/api/v1`, vi se thanh `/api/v1/api/v1/...` va de bi 404.

Full local base URL: `http://localhost:8080/api/v1`

Swagger/OpenAPI:

- `http://localhost:8080/api/v1/swagger-ui.html`
- `http://localhost:8080/api/v1/v3/api-docs`

## Authentication

Tru cac endpoint trong folder Auth, cac API con lai can header:

```http
Authorization: Bearer <accessToken>
Content-Type: application/json
```

Flow cho FE:

1. Goi `POST /auth/login`.
2. Luu `data.accessToken` va `data.refreshToken`.
3. Gan `Authorization: Bearer ${accessToken}` cho request can login.
4. Neu token het han, goi `POST /auth/refresh`.

Tai khoan test:

```text
email: codex-check@example.com
password: 12345678
```

## Common Response Format

Success:

```json
{
  "success": true,
  "message": "Success",
  "data": {}
}
```

Error:

```json
{
  "success": false,
  "message": "Invalid input",
  "errorCode": "INVALID_INPUT",
  "errors": []
}
```

## Pagination

Cac API list/page dung query params:

```text
?page=0&size=10
```

## Endpoint Summary

### Auth

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `POST` | `http://localhost:8080/api/v1/auth/register` | No | `-` | `-` | `RegisterRequest` | `RegisterResponse` |
| `POST` | `http://localhost:8080/api/v1/auth/login` | No | `-` | `-` | `LoginRequest` | `LoginResponse` |
| `POST` | `http://localhost:8080/api/v1/auth/refresh` | No | `-` | `-` | `RefreshTokenRequest` | `RefreshTokenResponse` |
| `POST` | `http://localhost:8080/api/v1/auth/logout` | No | `-` | `-` | `RefreshTokenRequest` | `object` |

### Users

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `GET` | `http://localhost:8080/api/v1/users/me` | Bearer token | `-` | `-` | `-` | `UserResponse` |

### Collections

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `GET` | `http://localhost:8080/api/v1/collections` | Bearer token | `-` | `pageable` | `-` | `CollectionPageResponse` |
| `POST` | `http://localhost:8080/api/v1/collections` | Bearer token | `-` | `-` | `CreateCollectionRequest` | `CollectionResponse` |
| `GET` | `http://localhost:8080/api/v1/collections/my` | Bearer token | `-` | `pageable` | `-` | `CollectionPageResponse` |
| `DELETE` | `http://localhost:8080/api/v1/collections/{collectionId}` | Bearer token | `collectionId` | `-` | `-` | `object` |
| `GET` | `http://localhost:8080/api/v1/collections/{collectionId}` | Bearer token | `collectionId` | `-` | `-` | `CollectionResponse` |
| `PUT` | `http://localhost:8080/api/v1/collections/{collectionId}` | Bearer token | `collectionId` | `-` | `UpdateCollectionRequest` | `CollectionResponse` |

### Collection Vocabularies

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `GET` | `http://localhost:8080/api/v1/collections/{collectionId}/vocabularies` | Bearer token | `collectionId` | `pageable` | `-` | `VocabularyPageResponse` |
| `DELETE` | `http://localhost:8080/api/v1/collections/{collectionId}/vocabularies/{vocabularyId}` | Bearer token | `collectionId, vocabularyId` | `-` | `-` | `object` |
| `POST` | `http://localhost:8080/api/v1/collections/{collectionId}/vocabularies/{vocabularyId}` | Bearer token | `collectionId, vocabularyId` | `-` | `-` | `VocabularyResponse` |

### Vocabularies

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `GET` | `http://localhost:8080/api/v1/vocabularies` | Bearer token | `-` | `pageable` | `-` | `VocabularyPageResponse` |
| `POST` | `http://localhost:8080/api/v1/vocabularies` | Bearer token | `-` | `-` | `CreateVocabularyRequest` | `VocabularyResponse` |
| `DELETE` | `http://localhost:8080/api/v1/vocabularies/{id}` | Bearer token | `id` | `-` | `-` | `object` |
| `GET` | `http://localhost:8080/api/v1/vocabularies/{id}` | Bearer token | `id` | `-` | `-` | `VocabularyResponse` |
| `PUT` | `http://localhost:8080/api/v1/vocabularies/{id}` | Bearer token | `id` | `-` | `UpdateVocabularyRequest` | `VocabularyResponse` |

### Public Collections

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `GET` | `http://localhost:8080/api/v1/public/collections` | Bearer token | `-` | `pageable` | `-` | `PublicCollectionPageResponse` |
| `GET` | `http://localhost:8080/api/v1/public/collections/{collectionId}` | Bearer token | `collectionId` | `-` | `-` | `PublicCollectionResponse` |
| `POST` | `http://localhost:8080/api/v1/public/collections/{collectionId}/clone` | Bearer token | `collectionId` | `-` | `-` | `CloneCollectionResponse` |
| `GET` | `http://localhost:8080/api/v1/public/collections/{collectionId}/vocabularies` | Bearer token | `collectionId` | `pageable` | `-` | `PublicVocabularyPageResponse` |

### Learning Progress

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `GET` | `http://localhost:8080/api/v1/learning/progress` | Bearer token | `-` | `pageable` | `-` | `LearningProgressPageResponse` |
| `GET` | `http://localhost:8080/api/v1/learning/progress/{vocabularyId}` | Bearer token | `vocabularyId` | `-` | `-` | `LearningProgressResponse` |
| `POST` | `http://localhost:8080/api/v1/learning/progress/{vocabularyId}/initialize` | Bearer token | `vocabularyId` | `-` | `-` | `LearningProgressResponse` |

### Flashcards

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `POST` | `http://localhost:8080/api/v1/flashcards/sessions` | Bearer token | `-` | `-` | `CreateFlashcardSessionRequest` | `FlashcardSessionResponse` |
| `GET` | `http://localhost:8080/api/v1/flashcards/sessions/{sessionId}` | Bearer token | `sessionId` | `-` | `-` | `FlashcardSessionResponse` |
| `GET` | `http://localhost:8080/api/v1/flashcards/sessions/{sessionId}/cards` | Bearer token | `sessionId` | `-` | `-` | `array` |
| `POST` | `http://localhost:8080/api/v1/flashcards/sessions/{sessionId}/cards/{vocabularyId}/answer` | Bearer token | `sessionId, vocabularyId` | `-` | `SubmitFlashcardAnswerRequest` | `FlashcardAnswerResponse` |

### Quizzes

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `POST` | `http://localhost:8080/api/v1/quizzes/sessions` | Bearer token | `-` | `-` | `CreateQuizSessionRequest` | `QuizSessionResponse` |
| `GET` | `http://localhost:8080/api/v1/quizzes/sessions/{sessionId}` | Bearer token | `sessionId` | `-` | `-` | `QuizSessionResponse` |
| `GET` | `http://localhost:8080/api/v1/quizzes/sessions/{sessionId}/questions` | Bearer token | `sessionId` | `-` | `-` | `array` |
| `POST` | `http://localhost:8080/api/v1/quizzes/sessions/{sessionId}/questions/{questionId}/answer` | Bearer token | `sessionId, questionId` | `-` | `SubmitQuizAnswerRequest` | `QuizAnswerResponse` |

### Typing

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `POST` | `http://localhost:8080/api/v1/typing/sessions` | Bearer token | `-` | `-` | `CreateTypingSessionRequest` | `TypingSessionResponse` |
| `GET` | `http://localhost:8080/api/v1/typing/sessions/{sessionId}` | Bearer token | `sessionId` | `-` | `-` | `TypingSessionResponse` |
| `GET` | `http://localhost:8080/api/v1/typing/sessions/{sessionId}/questions` | Bearer token | `sessionId` | `-` | `-` | `array` |
| `POST` | `http://localhost:8080/api/v1/typing/sessions/{sessionId}/questions/{questionId}/answer` | Bearer token | `sessionId, questionId` | `-` | `SubmitTypingAnswerRequest` | `TypingAnswerResponse` |

### Reviews

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `GET` | `http://localhost:8080/api/v1/reviews/history` | Bearer token | `-` | `pageable` | `-` | `ReviewHistoryPageResponse` |
| `GET` | `http://localhost:8080/api/v1/reviews/today` | Bearer token | `-` | `pageable` | `-` | `ReviewDuePageResponse` |
| `POST` | `http://localhost:8080/api/v1/reviews/{vocabularyId}` | Bearer token | `vocabularyId` | `-` | `SubmitReviewRequest` | `ReviewSubmitResponse` |

### Dashboard

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `GET` | `http://localhost:8080/api/v1/dashboard/learning-status` | Bearer token | `-` | `-` | `-` | `LearningStatusStatsResponse` |
| `GET` | `http://localhost:8080/api/v1/dashboard/recent-activity` | Bearer token | `-` | `-` | `-` | `RecentActivityResponse` |
| `GET` | `http://localhost:8080/api/v1/dashboard/review-due` | Bearer token | `-` | `pageable` | `-` | `ReviewDueResponse` |
| `GET` | `http://localhost:8080/api/v1/dashboard/summary` | Bearer token | `-` | `-` | `-` | `DashboardSummaryResponse` |

### Notifications

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `GET` | `http://localhost:8080/api/v1/notifications` | Bearer token | `-` | `pageable` | `-` | `NotificationPageResponse` |
| `GET` | `http://localhost:8080/api/v1/notifications/history` | Bearer token | `-` | `pageable` | `-` | `NotificationPageResponse` |

### AI Vocabulary

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `POST` | `http://localhost:8080/api/v1/ai/vocabulary/normalize` | Bearer token | `-` | `-` | `NormalizeVocabularyRequest` | `NormalizeVocabularyResponse` |

### Roleplay

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `GET` | `http://localhost:8080/api/v1/roleplay/sessions` | Bearer token | `-` | `pageable` | `-` | `RoleplaySessionPageResponse` |
| `POST` | `http://localhost:8080/api/v1/roleplay/sessions` | Bearer token | `-` | `-` | `CreateRoleplaySessionRequest` | `RoleplaySessionResponse` |
| `GET` | `http://localhost:8080/api/v1/roleplay/sessions/{sessionId}` | Bearer token | `sessionId` | `-` | `-` | `RoleplaySessionResponse` |
| `POST` | `http://localhost:8080/api/v1/roleplay/sessions/{sessionId}/end` | Bearer token | `sessionId` | `-` | `-` | `RoleplayReportResponse` |
| `POST` | `http://localhost:8080/api/v1/roleplay/sessions/{sessionId}/messages` | Bearer token | `sessionId` | `-` | `SendRoleplayMessageRequest` | `RoleplayMessageResponse` |

### Export

| Method | Full URL | Auth | Path Params | Query Params | Request Body | Response Data |
|---|---|---|---|---|---|---|
| `GET` | `http://localhost:8080/api/v1/export/collections/{collectionId}/pdf` | Bearer token | `collectionId` | `-` | `-` | `PDF binary` |
| `GET` | `http://localhost:8080/api/v1/export/vocabularies/pdf` | Bearer token | `-` | `-` | `-` | `PDF binary` |

## Request Body Samples

Enum values:

- `visibility: PRIVATE | PUBLIC | SYSTEM`
- `source: ALL | COLLECTION | REVIEW_DUE`
- `result: AGAIN | HARD | GOOD | EASY`
- `difficulty: EASY | MEDIUM | HARD`

### CreateCollectionRequest

```json
{
  "title": "My English Collection",
  "description": "Words for daily practice",
  "visibility": "PRIVATE",
  "thumbnailUrl": "https://example.com/thumbnail.png"
}
```

### CreateFlashcardSessionRequest

```json
{
  "source": "ALL",
  "collectionId": null
}
```

### CreateQuizSessionRequest

```json
{
  "source": "ALL",
  "collectionId": null
}
```

### CreateRoleplaySessionRequest

```json
{
  "topic": "Ordering coffee",
  "difficulty": "EASY",
  "persona": "barista"
}
```

### CreateTypingSessionRequest

```json
{
  "source": "ALL",
  "collectionId": null
}
```

### CreateVocabularyRequest

```json
{
  "word": "serendipity",
  "phonetic": "/serendipity/",
  "audioUrl": "https://example.com/audio.mp3",
  "partOfSpeech": "noun",
  "meaningVi": "su tinh co may man",
  "meaningEn": "pleasant discovery by chance",
  "synonyms": [
    "chance",
    "fortune"
  ],
  "antonyms": [],
  "examples": [
    {
      "sentence": "Finding that book was pure serendipity.",
      "translation": "Tim thay cuon sach do la mot su tinh co may man."
    }
  ],
  "collectionIds": [
    "{{collectionId}}"
  ]
}
```

### LoginRequest

```json
{
  "email": "codex-check@example.com",
  "password": "12345678"
}
```

### NormalizeVocabularyRequest

```json
{
  "rawText": "serendipity"
}
```

### RefreshTokenRequest

```json
{
  "refreshToken": "{{refreshToken}}"
}
```

### RegisterRequest

```json
{
  "email": "test@example.com",
  "password": "12345678",
  "fullName": "Test User"
}
```

### SendRoleplayMessageRequest

```json
{
  "message": "Hello, I would like a cup of coffee."
}
```

### SubmitFlashcardAnswerRequest

```json
{
  "result": "GOOD"
}
```

### SubmitQuizAnswerRequest

```json
{
  "answer": "serendipity"
}
```

### SubmitReviewRequest

```json
{
  "result": "GOOD"
}
```

### SubmitTypingAnswerRequest

```json
{
  "answer": "serendipity"
}
```

### UpdateCollectionRequest

```json
{
  "title": "Updated Collection",
  "description": "Updated description",
  "visibility": "PUBLIC",
  "thumbnailUrl": "https://example.com/updated.png"
}
```

### UpdateVocabularyRequest

```json
{
  "word": "serendipity",
  "phonetic": "/serendipity/",
  "audioUrl": "https://example.com/audio.mp3",
  "partOfSpeech": "noun",
  "meaningVi": "su tinh co may man",
  "meaningEn": "pleasant discovery by chance",
  "synonyms": [
    "fortune"
  ],
  "antonyms": [],
  "examples": []
}
```

## Key Response Samples

### Login Success

```json
{
  "success": true,
  "message": "Login successfully",
  "data": {
    "accessToken": "jwt-access-token",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "refreshToken": "refresh-token",
    "user": {
      "id": "uuid",
      "email": "codex-check@example.com",
      "fullName": "Codex Check",
      "role": "USER"
    }
  }
}
```

### Register Success

```json
{
  "success": true,
  "message": "Register successfully",
  "data": {
    "id": "uuid",
    "email": "test@example.com",
    "fullName": "Test User",
    "role": "USER"
  }
}
```

## Axios Example

```ts
import axios from "axios";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1",
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

## Common 404 Causes

- Backend khong chay dung port `8080`.
- FE goi thieu `/api/v1`, vi du goi `/auth/register` trong khi base URL la `http://localhost:8080`.
- FE bi lap `/api/v1/api/v1`, do base URL va request path deu chua `/api/v1`.
- Sau khi sua `.env.local`, Vite dev server chua restart.
- DevTools Network cho thay request dang di toi frontend dev server, vi du `http://localhost:5173/api/...`, khong phai backend.
