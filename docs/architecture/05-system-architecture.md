# 05-system-architecture.md

# THIẾT KẾ KIẾN TRÚC HỆ THỐNG

## Dự án

VocabVerse - AI Vocabulary Learning Platform

---

# 1. THÔNG TIN TÀI LIỆU

| Thuộc tính        | Giá trị                         |
| ----------------- | ------------------------------- |
| Tên tài liệu      | System Architecture Design      |
| Dự án             | VocabVerse                      |
| Phiên bản         | 1.0                             |
| Trạng thái        | Draft                           |
| Kiến trúc đề xuất | Modular Monolith + Async Worker |
| Backend chính     | Spring Boot                     |
| Frontend          | React / TypeScript              |
| Database          | PostgreSQL                      |

---

# 2. MỤC TIÊU KIẾN TRÚC

Kiến trúc hệ thống cần đảm bảo:

* Dễ phát triển ở giai đoạn MVP
* Dễ mở rộng khi có nhiều user
* Tách rõ module nghiệp vụ
* Hỗ trợ xử lý tác vụ nặng bằng async job
* Tích hợp AI an toàn, dễ thay provider
* Hỗ trợ notification đúng ngày spaced repetition
* Hỗ trợ shadowing video processing
* Phù hợp triển khai cloud hoặc VPS

---

# 3. KIẾN TRÚC TỔNG QUAN

```text
User Browser
    |
    v
React Frontend
    |
    v
Spring Boot REST API
    |
    +--------------------+
    |                    |
    v                    v
PostgreSQL              Redis
    |
    v
RabbitMQ
    |
    v
Async Worker
    |
    +-----------------------------+
    |              |              |
    v              v              v
AI Provider   Dictionary API   File Storage
```

---

# 4. PHONG CÁCH KIẾN TRÚC

## 4.1 Giai đoạn MVP

Nên dùng:

```text
Modular Monolith
```

Tức là backend vẫn là một Spring Boot application, nhưng code được chia module rõ ràng:

```text
auth
user
collection
vocabulary
learning
review
notification
shadowing
roleplay
ai
export
```

Lý do:

* Dễ code
* Dễ debug
* Dễ deploy
* Không phức tạp như microservices
* Phù hợp dự án cá nhân/sinh viên

---

## 4.2 Giai đoạn Scale

Sau này có thể tách thành service riêng:

```text
AI Service
Notification Service
Video Processing Service
Export Service
```

Nhưng ban đầu không nên tách quá sớm.

---

# 5. THÀNH PHẦN HỆ THỐNG

## 5.1 Frontend

Công nghệ đề xuất:

```text
React
TypeScript
TailwindCSS
React Query
Zustand hoặc Redux Toolkit
React Router
```

Trách nhiệm:

* Hiển thị UI
* Gọi API
* Quản lý state phía client
* Hiển thị dashboard
* Học flashcard
* Làm quiz
* Roleplay chat UI
* Shadowing video player

---

## 5.2 Backend API

Công nghệ đề xuất:

```text
Spring Boot
Spring Security
Spring Data JPA
Spring Validation
Spring Mail
RabbitMQ Client
Redis Client
```

Trách nhiệm:

* Authentication
* Authorization
* Business logic
* CRUD collection
* CRUD vocabulary
* Learning progress
* Spaced repetition
* Roleplay session
* Shadowing metadata
* Export PDF
* Tạo async job

---

## 5.3 PostgreSQL

Lưu dữ liệu chính:

* User
* Collection
* Vocabulary
* Learning progress
* Review schedule
* Shadowing transcript
* Roleplay messages
* Notification logs
* Export jobs

---

## 5.4 Redis

Dùng cho:

* Cache dictionary lookup
* Cache public collections
* Rate limit API
* Temporary session data
* AI response cache nếu cần

Ví dụ:

```text
dictionary:abandon
collection:public:page:1
rate_limit:user:{userId}
```

---

## 5.5 RabbitMQ

Dùng để xử lý async job:

* AI normalize vocabulary
* Generate PDF
* Send email
* Process shadowing video
* Generate subtitle
* Translate subtitle
* Generate roleplay report

Exchange đề xuất:

```text
vocabverse.exchange
```

Queue đề xuất:

```text
ai.normalize.queue
notification.email.queue
export.pdf.queue
shadowing.video.queue
roleplay.report.queue
```

---

## 5.6 Async Worker

Có thể là:

```text
Spring Boot Worker App
```

hoặc cùng backend app nhưng chạy consumer.

Khuyến nghị enterprise:

```text
Tách worker thành app riêng
```

Ví dụ:

```text
vocabverse-api
vocabverse-worker
```

Worker xử lý:

* Job nặng
* Gọi AI
* Gọi external API
* Tạo file PDF
* Xử lý video/audio

---

## 5.7 AI Provider

Provider đề xuất:

```text
Groq
Gemini
OpenAI
```

Không gọi trực tiếp provider trong business logic.

Nên tạo interface:

```java
public interface AiClient {
    VocabularyNormalizeResult normalizeVocabulary(String input);
    RoleplayResponse generateRoleplayResponse(...);
    SubtitleTranslationResult translateSubtitle(...);
}
```

Lợi ích:

* Dễ đổi Groq sang Gemini
* Dễ mock khi test
* Dễ fallback provider

---

## 5.8 Dictionary Provider

Dùng để tra từ.

Provider gợi ý:

```text
Free Dictionary API
```

Nên wrap qua service:

```java
DictionaryClient
```

Không để controller gọi thẳng external API.

---

## 5.9 File Storage

Dùng để lưu:

* Video upload
* Audio extract
* PDF export
* Thumbnail
* Avatar

Giai đoạn local:

```text
Local Storage
```

Giai đoạn production:

```text
S3 Compatible Storage
Cloudflare R2
AWS S3
MinIO
```

---

# 6. BACKEND MODULE STRUCTURE

Cấu trúc package đề xuất:

```text
com.vocabverse
├── auth
│   ├── controller
│   ├── service
│   ├── dto
│   ├── entity
│   └── repository
│
├── user
├── collection
├── vocabulary
├── learning
│   ├── flashcard
│   ├── quiz
│   └── typing
│
├── review
├── notification
├── shadowing
├── roleplay
├── ai
├── dictionary
├── export
├── storage
├── common
│   ├── exception
│   ├── response
│   ├── security
│   ├── config
│   └── util
```

---

# 7. FRONTEND STRUCTURE

```text
src
├── app
├── pages
│   ├── auth
│   ├── dashboard
│   ├── collections
│   ├── vocabulary
│   ├── flashcard
│   ├── quiz
│   ├── shadowing
│   ├── roleplay
│   └── admin
│
├── features
│   ├── auth
│   ├── collection
│   ├── vocabulary
│   ├── learning
│   ├── review
│   ├── shadowing
│   └── roleplay
│
├── components
├── hooks
├── services
├── types
├── utils
└── constants
```

---

# 8. LUỒNG XỬ LÝ CHÍNH

# 8.1 Luồng đăng nhập

```text
User nhập email/password
        ↓
Frontend gọi POST /auth/login
        ↓
Backend validate
        ↓
Kiểm tra password BCrypt
        ↓
Sinh access token + refresh token
        ↓
Trả token cho frontend
```

---

# 8.2 Luồng tra từ

```text
User search "abandon"
        ↓
Frontend gọi API search
        ↓
Backend kiểm tra Redis cache
        ↓
Nếu có cache: trả kết quả
        ↓
Nếu không có cache:
        gọi Dictionary API
        ↓
Normalize response
        ↓
Lưu cache Redis
        ↓
Trả kết quả
```

---

# 8.3 Luồng import từ vựng bằng AI

```text
User paste text
        ↓
Frontend gửi raw input
        ↓
Backend tạo ai_import_job
        ↓
Push message vào RabbitMQ
        ↓
Worker nhận job
        ↓
Gọi AI Provider
        ↓
Validate JSON schema
        ↓
Lưu vocabularies
        ↓
Gắn vào collection
        ↓
Cập nhật job COMPLETED
        ↓
Frontend polling hoặc websocket lấy kết quả
```

---

# 8.4 Luồng bật spaced repetition

```text
User bật spaced repetition cho collection
        ↓
Chọn intervals
        ↓
Backend tạo review_schedule
        ↓
Backend sinh review_sessions tương ứng
        ↓
Scheduler chạy hằng ngày
        ↓
Tìm review_sessions hôm nay
        ↓
Gửi email/browser notification
```

---

# 8.5 Luồng shadowing video

```text
Admin upload video hoặc YouTube URL
        ↓
Backend tạo shadowing_video status PROCESSING
        ↓
Push job vào shadowing.video.queue
        ↓
Worker xử lý:
    - Extract audio
    - Speech to text
    - Segment transcript
    - Translate to Vietnamese
        ↓
Lưu shadowing_lines
        ↓
Cập nhật video READY
```

---

# 8.6 Luồng AI roleplay

```text
User chọn topic/difficulty/persona
        ↓
Backend tạo roleplay_session
        ↓
AI sinh scenario mở đầu
        ↓
User gửi message
        ↓
Backend gửi context cho AI
        ↓
AI phản hồi + correction
        ↓
Lưu roleplay_messages
        ↓
Trả về frontend
```

---

# 8.7 Luồng export PDF

```text
User chọn collection
        ↓
Chọn export type
        ↓
Backend tạo export_job
        ↓
Push vào export.pdf.queue
        ↓
Worker tạo PDF
        ↓
Upload file lên storage
        ↓
Cập nhật file_url
        ↓
User tải file
```

---

# 9. API GATEWAY

Giai đoạn MVP không cần API Gateway riêng.

Frontend gọi trực tiếp:

```text
https://api.vocabverse.com/api/v1
```

Sau này có thể thêm:

```text
Nginx
Kong
Spring Cloud Gateway
```

---

# 10. AUTHENTICATION & AUTHORIZATION

## 10.1 Authentication

Dùng JWT:

```text
Access Token
Refresh Token
```

Access token ngắn hạn:

```text
15 phút - 60 phút
```

Refresh token dài hơn:

```text
7 ngày - 30 ngày
```

---

## 10.2 Authorization

Phân quyền theo role:

```text
USER
ADMIN
```

Ví dụ:

```text
/api/v1/admin/** chỉ ADMIN truy cập
/api/v1/me/** chỉ user đăng nhập truy cập
```

---

# 11. ASYNC PROCESSING DESIGN

## Khi nào dùng async?

Dùng async cho tác vụ:

* Gọi AI lâu
* Tạo PDF
* Gửi email
* Xử lý video
* Tạo subtitle
* Generate report

Không dùng async cho:

* Login
* Search collection
* CRUD đơn giản
* Dashboard cơ bản

---

## Message format

```json
{
  "jobId": "uuid",
  "jobType": "AI_NORMALIZE_VOCABULARY",
  "userId": "uuid",
  "payload": {}
}
```

---

# 12. SCHEDULER DESIGN

Dùng Spring Scheduler hoặc Quartz.

## Jobs

```text
DailyReviewNotificationJob
MissedReviewMarkJob
PublicCollectionStatsJob
ExpiredTokenCleanupJob
```

---

## DailyReviewNotificationJob

Chạy mỗi ngày lúc:

```text
08:00
```

Logic:

```text
Find review_sessions where review_date = today and status = PENDING
For each session:
    Check notification log
    Send email/browser notification
    Create notification log
```

---

# 13. CACHING STRATEGY

## Cache dictionary

```text
Key: dictionary:{word}
TTL: 7 days
```

---

## Cache public collection

```text
Key: public_collections:page:{page}:size:{size}
TTL: 10 minutes
```

---

## Cache dashboard

```text
Key: dashboard:{userId}
TTL: 5 minutes
```

---

# 14. RATE LIMITING

Cần rate limit các API tốn chi phí.

## AI Normalize

```text
Free user: 10 requests/day
```

## Roleplay

```text
Free user: 20 messages/day
```

## Dictionary Search

```text
100 requests/day
```

Có thể dùng Redis counter.

---

# 15. OBSERVABILITY

## Logging

Log cần có:

```text
request_id
user_id
endpoint
latency
status_code
error_code
```

---

## Monitoring

Theo dõi:

```text
CPU
RAM
Database connections
RabbitMQ queue depth
Error rate
API latency
AI failure rate
```

---

## Error Tracking

Có thể dùng:

```text
Sentry
```

---

# 16. SECURITY DESIGN

## Password

Sử dụng:

```text
BCrypt
```

---

## API Security

* JWT authentication
* Role-based authorization
* Input validation
* CORS config
* Rate limit
* Không expose stack trace

---

## File Upload Security

Kiểm tra:

* File type
* File size
* Virus scan nếu production lớn
* Không dùng filename gốc trực tiếp
* Upload vào storage riêng

---

## AI Security

Không gửi dữ liệu nhạy cảm không cần thiết cho AI provider.

Cần sanitize input:

* Remove script tag
* Limit input length
* Validate JSON output

---

# 17. DEPLOYMENT ARCHITECTURE

## MVP Deployment

```text
React Frontend: Vercel / Netlify
Spring Boot API: VPS / Render / Railway
PostgreSQL: Managed DB hoặc Docker
Redis: Docker hoặc Managed Redis
RabbitMQ: Docker
File Storage: Cloudflare R2 / S3 / MinIO
```

---

## Docker Compose Local

```text
frontend
backend-api
backend-worker
postgres
redis
rabbitmq
minio
```

---

# 18. ENVIRONMENT

```text
local
dev
staging
production
```

---

# 19. CONFIGURATION

Các biến môi trường cần có:

```env
DATABASE_URL=
REDIS_URL=
RABBITMQ_URL=
JWT_SECRET=
GROQ_API_KEY=
GEMINI_API_KEY=
DICTIONARY_API_BASE_URL=
MAIL_HOST=
MAIL_USERNAME=
MAIL_PASSWORD=
STORAGE_ACCESS_KEY=
STORAGE_SECRET_KEY=
STORAGE_BUCKET=
```

---

# 20. SCALING PLAN

## Giai đoạn 1

Một backend API + một worker.

---

## Giai đoạn 2

Scale worker:

```text
api x1
worker x2
```

---

## Giai đoạn 3

Tách service:

```text
vocabulary-service
learning-service
ai-service
notification-service
video-service
```

---

# 21. RỦI RO KIẾN TRÚC

## Rủi ro 1

AI trả sai format.

Giải pháp:

* JSON Schema validation
* Retry
* Fallback model

---

## Rủi ro 2

Video processing lâu.

Giải pháp:

* Async worker
* Queue
* Status tracking

---

## Rủi ro 3

Email bị gửi trùng.

Giải pháp:

* notification_logs
* unique review_session + channel

---

## Rủi ro 4

Dictionary API bị lỗi.

Giải pháp:

* Redis cache
* Fallback AI explain
* Retry

---

# 22. KẾT LUẬN

Kiến trúc đề xuất cho VocabVerse là:

```text
Modular Monolith + Async Worker
```

Đây là hướng hợp lý nhất cho giai đoạn đầu vì:

* Không quá phức tạp
* Vẫn đủ chuẩn enterprise
* Dễ chia module
* Dễ scale worker
* Dễ tích hợp AI
* Dễ triển khai với Spring Boot

END OF DOCUMENT
