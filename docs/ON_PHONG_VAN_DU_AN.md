# ÔN PHỎNG VẤN DỰ ÁN - VocabVerse

Tài liệu này được viết dựa trên source code hiện có trong backend `VocabVerse` và frontend `VocabVerse_FE`. Mục tiêu là giúp ứng viên Java Developer Intern/Fresher hiểu dự án để trả lời phỏng vấn, không phải học thuộc lòng.

Lưu ý quan trọng:
- Ưu tiên code hiện tại hơn README nếu hai bên lệch nhau. Ví dụ README backend nói Shadowing còn scaffold-only, nhưng source hiện tại đã có controller/service/entity cho Shadowing.
- Vai trò cá nhân của người làm project chưa xác định được từ source code.
- Các file tham chiếu dùng đường dẫn tương đối tính từ root backend hoặc frontend.

## PHẦN 1 - TỔNG QUAN DỰ ÁN

### 1. Tên dự án

`VocabVerse`.

Nguồn:
- Backend README: `README.md`
- Spring application name: `src/main/resources/application.yml`
- Entry point: `src/main/java/com/vocabverse/VocabVerseApplication.java`

### 2. Mục đích của dự án

VocabVerse là nền tảng học từ vựng tiếng Anh có backend Spring Boot và frontend React. Dự án hỗ trợ quản lý bộ sưu tập từ vựng, học qua flashcard/quiz/typing, spaced repetition, tra từ điển, AI normalize vocabulary, AI roleplay, shadowing subtitle, notification, admin management và export PDF.

### 3. Đối tượng sử dụng

- Người học tiếng Anh: tạo collection, thêm vocabulary, học và review.
- Admin: quản lý user, collection công khai, notification, shadowing lesson, system health.
- Chưa xác định được từ source code có phân quyền khác ngoài `USER` và `ADMIN`.

### 4. Các chức năng chính

1. Authentication bằng JWT và refresh token.
2. Quản lý user hiện tại.
3. CRUD collection.
4. CRUD vocabulary và gắn vocabulary vào collection.
5. Public collection và clone collection.
6. AI normalize vocabulary.
7. Learning progress.
8. Review scheduler gồm `FIXED_INTERVAL`, `SM2`, `FSRS`.
9. Flashcard practice.
10. Quiz practice.
11. Typing practice.
12. Notification review reminder.
13. Dashboard thống kê.
14. Export vocabulary/collection ra PDF.
15. AI roleplay.
16. Shadowing lesson và generate subtitle bằng AI.
17. Admin dashboard/user/collection/notification/shadowing/system.

### 5. Tech stack

Backend:
- Java 21.
- Spring Boot 3.3.5.
- Spring Web MVC.
- Spring Security + JWT.
- Spring Data JPA + Hibernate.
- PostgreSQL.
- Flyway migration.
- Redis config.
- RabbitMQ AMQP.
- Spring Mail.
- MapStruct.
- Lombok.
- springdoc-openapi.
- PDFBox.
- Cloudinary.
- Java `HttpClient` để gọi Groq API.

Nguồn:
- `build.gradle`
- `src/main/resources/application.yml`

Frontend:
- React 19.
- Vite 6.
- React Router DOM 7.
- TanStack Query 5.
- Axios.
- Zustand.
- React Hook Form.
- Zod.
- Tailwind CSS 4.
- Sonner toast.
- React Icons.

Nguồn:
- `VocabVerse_FE/package.json`

### 6. Kiến trúc tổng thể

Backend là modular monolith theo package nghiệp vụ:

```text
Client React
  -> REST API /api/v1
  -> Controller
  -> Service
  -> Repository
  -> JPA Entity
  -> PostgreSQL

Async/External:
  -> RabbitMQ notification
  -> Redis config
  -> Groq AI
  -> Cloudinary
  -> yt-dlp / ffmpeg
  -> SMTP mail
```

### 7. Database sử dụng

PostgreSQL.

Nguồn:
- `src/main/resources/application.yml`
- Flyway migrations: `src/main/resources/db/migration`
- JPA column dùng `jsonb` ở vocabulary, roleplay, quiz option, collection review settings.

### 8. Các module/package chính

- `auth`: register, login, refresh, logout.
- `user`: current user.
- `collection`: collection và review settings.
- `vocabulary`: vocabulary và relation collection-vocabulary.
- `learning.flashcard`: flashcard session/item.
- `learning.quiz`: quiz session/question.
- `learning.typing`: typing session/question.
- `learning.progress`: tiến độ học từng user-vocabulary.
- `review`: review history, scheduler, statistics.
- `ai`: normalize vocabulary bằng AI.
- `dictionary`: tra từ điển external API.
- `publiccollection`: public collection, clone.
- `notification`: review reminders, mail, RabbitMQ.
- `dashboard`: dashboard user.
- `export`: export PDF.
- `roleplay`: AI roleplay session/message/report.
- `shadowing`: lesson, subtitle, AI subtitle generation.
- `admin`: admin APIs.
- `common`: security, config, response, exception.

### 9. Vai trò của tôi trong project

Chưa xác định được từ source code.

Nếu đi phỏng vấn, nên nói theo hướng trung thực: “Trong project này em phụ trách/đã làm các phần ...” rồi chỉ nêu phần thật sự bạn đã làm.

### Giới thiệu project trong 1-2 phút

“Project của em là VocabVerse, một hệ thống học từ vựng tiếng Anh gồm backend Spring Boot và frontend React. Backend được tổ chức theo modular monolith, mỗi nghiệp vụ như auth, collection, vocabulary, review, flashcard, quiz, typing, roleplay, shadowing nằm trong package riêng. Người dùng có thể tạo bộ sưu tập từ vựng, thêm từ, học qua nhiều mode, và hệ thống sẽ cập nhật tiến độ học trong bảng `learning_progress`. Phần review hỗ trợ nhiều thuật toán như fixed interval, SM2 và FSRS, được chọn theo setting của từng collection. Dự án cũng có JWT security, refresh token, phân quyền admin bằng `@PreAuthorize`, Flyway migration cho PostgreSQL, notification qua RabbitMQ/mail, AI normalize vocabulary, AI roleplay và generate subtitle cho shadowing qua Groq. Frontend dùng React, Vite, React Router, TanStack Query, Axios và Zustand để gọi API và quản lý trạng thái đăng nhập.”

## PHẦN 2 - KIẾN TRÚC VÀ LUỒNG HOẠT ĐỘNG

### Layer trong backend

Client/UI:
- Frontend React nằm ở repo `VocabVerse_FE`.
- Router chính: `src/app/router/AppRouter.jsx`.
- API client: `src/services/apiClient.js`.

Controller:
- Nhận HTTP request, validate `@Valid`, lấy `@PathVariable`, `@RequestParam`, `@RequestBody`.
- Ví dụ: `AuthController`, `CollectionController`, `ReviewController`, `AdminShadowingController`.

Service:
- Chứa nghiệp vụ chính, kiểm tra ownership, transaction, gọi repository, gọi service khác.
- Ví dụ: `ReviewService.submitReview`, `FlashcardService.submitAnswer`, `RoleplayService.sendMessage`.

Repository/DAO:
- Interface extends `JpaRepository`.
- Dùng query method và một số JPQL `@Query`.
- Ví dụ: `LearningProgressRepository`, `CollectionVocabularyRepository`.

Entity/Model:
- JPA entity map vào table.
- Ví dụ: `UserEntity`, `VocabularyEntity`, `LearningProgressEntity`, `RoleplaySessionEntity`.

DTO:
- Request/Response record/class tách API contract khỏi entity.
- Ví dụ: `LoginRequest`, `ReviewSubmitResponse`, `VocabularyResponse`.

Database:
- PostgreSQL, schema quản lý bằng Flyway.

### Sơ đồ tổng quát

```text
React Page/Component
  -> feature service/hook
  -> Axios apiClient
  -> Spring Controller
  -> Service
  -> Repository
  -> PostgreSQL
  -> Entity
  -> Mapper/DTO
  -> ApiResponse
  -> React Query state/UI
```

### 10 luồng nghiệp vụ quan trọng

#### Luồng 1: Register

User action: nhập email, password, full name.
-> Controller: `AuthController.register`
-> Service: `AuthService.register`
-> Repository: `UserRepository.existsByEmail`, `UserRepository.save`
-> Database: `users`
-> Response: `RegisterResponse`

Điểm phỏng vấn:
- Password không lưu plaintext mà encode bằng `PasswordEncoder`.
- Role mặc định là `UserRole.USER`.
- Status mặc định là `UserStatus.ACTIVE`.

#### Luồng 2: Login

User action: nhập email/password.
-> Controller: `AuthController.login`
-> Service: `AuthService.login`
-> Repository: `UserRepository.findByEmail`, `RefreshTokenService.createRefreshToken`
-> Database: `users`, `refresh_tokens`
-> Response: access token + refresh token + user info.

Điểm phỏng vấn:
- Access token là JWT.
- Refresh token lưu DB để có thể revoke.
- User inactive bị chặn bằng `ACCESS_DENIED`.

#### Luồng 3: Tạo collection

User action: tạo collection.
-> Controller: `CollectionController.createCollection`
-> Service: `CollectionService.createCollection`
-> Repository: `CollectionRepository.save`
-> Database: `collections`
-> Response: `CollectionResponse`

Điểm phỏng vấn:
- Không cho user tạo collection visibility `SYSTEM`.
- `owner` lấy từ SecurityContext.

#### Luồng 4: Tạo vocabulary và gắn vào collection

User action: tạo từ mới, có thể chọn collection.
-> Controller: `VocabularyController.createVocabulary`
-> Service: `VocabularyService.createVocabulary`
-> Repository: `VocabularyRepository.save`, `CollectionRepository.findAllByIdInAndOwnerIdAndDeletedAtIsNull`, `CollectionVocabularyRepository.save`
-> Database: `vocabularies`, `collection_vocabularies`, `collections`
-> Response: `VocabularyResponse`

Điểm phỏng vấn:
- `normalizedWord` được lower-case để hỗ trợ tìm/duplicate logic.
- `collection_vocabularies` là bảng trung gian.
- `totalWords` của collection được tăng thủ công.

#### Luồng 5: Review trực tiếp

User action: chọn AGAIN/HARD/GOOD/EASY cho một vocabulary.
-> Controller: `ReviewController.submitReview`
-> Service: `ReviewService.submitReview`
-> Scheduler: `ReviewSchedulerResolver.resolve`, `FixedIntervalScheduler` hoặc `Sm2Scheduler` hoặc `FsrsScheduler`
-> Repository: `LearningProgressRepository`, `ReviewHistoryRepository`
-> Database: `learning_progress`, `review_history`
-> Response: `ReviewSubmitResponse`

Điểm phỏng vấn:
- Thuật toán review được chọn từ `collection_review_settings.scheduler_type`.
- Nếu không có setting thì fallback `FIXED_INTERVAL`.

#### Luồng 6: Flashcard submit

User action: trả lời flashcard bằng rating AGAIN/HARD/GOOD/EASY.
-> Controller: `FlashcardController.submitAnswer`
-> Service: `FlashcardService.submitAnswer`
-> Service phụ: `ReviewService.submitReview`
-> Repository: `FlashcardSessionItemRepository`, `FlashcardSessionRepository`
-> Database: `flashcard_session_items`, `flashcard_sessions`, `learning_progress`, `review_history`
-> Response: `FlashcardAnswerResponse`

Điểm phỏng vấn:
- Flashcard truyền trực tiếp `request.result()` vào review scheduler.

#### Luồng 7: Quiz submit

User action: chọn/nhập đáp án quiz.
-> Controller: `QuizController.submitAnswer`
-> Service: `QuizService.submitAnswer`
-> Service phụ: `ReviewService.submitReview`
-> Database: `quiz_questions`, `quiz_sessions`, `learning_progress`, `review_history`
-> Response: `QuizAnswerResponse`

Điểm phỏng vấn:
- Đúng map thành `ReviewResult.GOOD`.
- Sai map thành `ReviewResult.AGAIN`.

#### Luồng 8: Typing submit

User action: gõ từ tiếng Anh.
-> Controller: `TypingController.submitAnswer`
-> Service: `TypingService.submitAnswer`
-> Service phụ: `ReviewService.submitReview`
-> Database: `typing_questions`, `typing_sessions`, `learning_progress`, `review_history`
-> Response: `TypingAnswerResponse`

Điểm phỏng vấn:
- Đúng/sai vẫn cập nhật SRS như quiz.
- Có tính `similarityScore` bằng Levenshtein nhưng quyết định đúng/sai hiện là exact match sau normalize.

#### Luồng 9: AI Roleplay

User action: tạo roleplay session, chat, end session.
-> Controller: `RoleplayController`
-> Service: `RoleplayService`
-> AI: `RoleplayAiService`
-> Prompt: `RoleplayPromptBuilder`
-> Repository: `RoleplaySessionRepository`, `RoleplayMessageRepository`, `RoleplayReportRepository`
-> Database: `roleplay_sessions`, `roleplay_messages`, `roleplay_reports`
-> Response: session/message/report DTO.

Điểm phỏng vấn:
- Có validate English-only trong `RoleplayLanguageValidator`.
- Có daily quota trong `RoleplayQuotaService`.
- Có `TransactionTemplate` để tránh giữ DB transaction lúc gọi AI.

#### Luồng 10: Admin generate AI subtitles

User action: admin bấm generate AI subtitle.
-> Controller: `AdminShadowingController.generateAiSubtitles`
-> Service: `AdminShadowingService.generateAiSubtitles`
-> Async Service: `ShadowingSubtitleGenerationService.generateSubtitlesAsync`
-> AI: `ShadowingAiSubtitleService`
-> External: ffmpeg/yt-dlp/Cloudinary/Groq transcription/Groq chat translation
-> Database: `shadowing_lessons`, `shadowing_lesson_subtitles`
-> Response: status processing/completed/failed.

Điểm phỏng vấn:
- Luồng chạy async bằng `@Async("shadowingTaskExecutor")`.
- Khi thành công xóa subtitle cũ và save subtitle mới.
- Khi lỗi lưu `FAILED`, `progress=0`, `errorMessage`.

## PHẦN 3 - DATABASE

### Database và migration

Database là PostgreSQL. Schema quản lý bằng Flyway tại `src/main/resources/db/migration`. Hibernate chạy `ddl-auto: validate`, nghĩa là app kiểm tra entity khớp schema thay vì tự tạo/sửa schema.

### Danh sách bảng chính

| Bảng | Mục đích | Entity/Migration |
|---|---|---|
| `users` | Tài khoản, role, status | `UserEntity`, `V1__create_users_table.sql` |
| `refresh_tokens` | Refresh token có revoke/expired | `RefreshTokenEntity`, `V2__create_refresh_tokens_table.sql` |
| `collections` | Bộ sưu tập từ vựng | `CollectionEntity`, `V3__create_collections_table.sql` |
| `vocabularies` | Từ vựng | `VocabularyEntity`, `V4__create_vocabularies_table.sql` |
| `collection_vocabularies` | Bảng nối collection-vocabulary | `CollectionVocabularyEntity`, `V4__create_vocabularies_table.sql` |
| `learning_progress` | Tiến độ học theo user-vocabulary | `LearningProgressEntity`, `V5`, `V17`, `V18` |
| `review_history` | Lịch sử review | `ReviewHistoryEntity`, `V6` |
| `flashcard_sessions` | Phiên học flashcard | `FlashcardSessionEntity`, `V7` |
| `flashcard_session_items` | Item trong phiên flashcard | `FlashcardSessionItemEntity`, `V7` |
| `quiz_sessions` | Phiên quiz | `QuizSessionEntity`, `V8` |
| `quiz_questions` | Câu hỏi quiz | `QuizQuestionEntity`, `V8` |
| `typing_sessions` | Phiên typing | `TypingSessionEntity`, `V9` |
| `typing_questions` | Câu hỏi typing | `TypingQuestionEntity`, `V9` |
| `notifications` | Notification/reminder | `NotificationEntity`, `V10` |
| `roleplay_sessions` | Phiên roleplay | `RoleplaySessionEntity`, `V11` |
| `roleplay_messages` | Message user/AI | `RoleplayMessageEntity`, `V11` |
| `roleplay_reports` | Báo cáo roleplay | `RoleplayReportEntity`, `V11`, `V21` |
| `collection_review_settings` | Cấu hình review từng collection | `CollectionReviewSettingEntity`, `V12`, `V16`, `V19` |
| `shadowing_lessons` | Lesson shadowing | `ShadowingLessonEntity`, `V13`, `V14`, `V15`, `V20` |
| `shadowing_lesson_subtitles` | Subtitle theo lesson | `ShadowingLessonSubtitleEntity`, `V15` |
| `public_collection_moderations` | Lịch sử moderation public collection | `PublicCollectionModerationEntity`, `V13` |

### Column quan trọng

`learning_progress`:
- `user_id`, `vocabulary_id`: unique theo user-vocabulary.
- `status`: NEW/LEARNING/REVIEWING/MASTERED.
- SM2: `ease_factor`, `repetition_count`, `last_interval_days`, `lapse_count`, `review_count`.
- FSRS: `fsrs_difficulty`, `fsrs_stability`, `fsrs_retrievability`.
- Schedule: `next_review_at`, `last_reviewed_at`.

`collection_review_settings`:
- `scheduler_type`: chọn `FIXED_INTERVAL`, `SM2`, `FSRS`.
- `intervals_json`: fixed interval.
- `fsrs_desired_retention`, `fsrs_max_interval_days`: cấu hình FSRS.
- `enabled`, `email_enabled`, `reminder_time`, `timezone`.

`roleplay_reports`:
- `overall_score`.
- Rubric: `grammar_score`, `vocabulary_score`, `relevance_score`, `fluency_score`, `interaction_score`.
- JSONB: `strengths`, `weaknesses`, `suggested_vocabulary`.

### Quan hệ

```text
users 1--N collections
users 1--N vocabularies
collections N--N vocabularies qua collection_vocabularies
users 1--N learning_progress
vocabularies 1--N learning_progress
users 1--N review_history
vocabularies 1--N review_history
collections 1--N flashcard_sessions / quiz_sessions / typing_sessions
flashcard_sessions 1--N flashcard_session_items
quiz_sessions 1--N quiz_questions
typing_sessions 1--N typing_questions
users 1--N roleplay_sessions
roleplay_sessions 1--N roleplay_messages
roleplay_sessions 1--1 roleplay_reports
shadowing_lessons 1--N shadowing_lesson_subtitles
```

### Vì sao thiết kế như vậy?

- `learning_progress` tách riêng khỏi `vocabularies` vì cùng một vocabulary có thể có progress khác nhau cho từng user.
- `collection_vocabularies` là bảng nối vì một collection chứa nhiều vocabulary và một vocabulary có thể nằm trong nhiều collection.
- Session/question tách riêng trong flashcard/quiz/typing để lưu lịch sử một lần học và từng câu/item.
- `review_history` tách khỏi `learning_progress` vì progress là trạng thái hiện tại, history là audit trail.
- `roleplay_sessions`, `roleplay_messages`, `roleplay_reports` tách nhau vì session có nhiều message nhưng chỉ có một report cuối phiên.

### Câu hỏi database có thể gặp

Q: Bảng nào lưu tham số SM2/FSRS?
A: `learning_progress`. SM2 dùng `ease_factor`, `repetition_count`, `last_interval_days`, `lapse_count`, `review_count`. FSRS dùng `fsrs_difficulty`, `fsrs_stability`, `fsrs_retrievability`, và vẫn dùng một số field chung như `review_count`, `lapse_count`, `next_review_at`.

Q: Vì sao `collection_vocabularies` có unique `(collection_id, vocabulary_id)`?
A: Để một từ không bị add trùng trong cùng một collection.

Q: Vì sao dùng Flyway?
A: Vì production cần schema versioning rõ ràng. Project còn cấu hình `ddl-auto=validate`, nên Hibernate chỉ validate schema, Flyway chịu trách nhiệm thay đổi schema.

Q: Có soft delete không?
A: Có ở `collections.deleted_at`, `vocabularies.deleted_at`, `shadowing_lessons.deleted_at`. Các query thường lọc `DeletedAtIsNull`.

Q: Dữ liệu JSONB dùng ở đâu?
A: `vocabularies.synonyms/antonyms/examples`, `quiz_questions.options`, `collection_review_settings.intervals_json`, `roleplay_reports.strengths/weaknesses/suggested_vocabulary`, `roleplay_messages.correction`.

## PHẦN 4 - PHÂN TÍCH JAVA

### OOP và Encapsulation

Xuất hiện ở entity/service/controller. Entity dùng field private với Lombok `@Getter/@Setter/@Builder`. Service đóng gói nghiệp vụ, ví dụ `ReviewService` che giấu chi tiết initialize progress, chọn scheduler, lưu history.

Interviewer hỏi: “Encapsulation trong project em thể hiện ở đâu?”

Trả lời mẫu: “Em tách API layer, business layer và persistence layer. Ví dụ controller không tự tính SRS mà gọi `ReviewService.submitReview`. Bản thân `ReviewService` lại đóng gói các bước private như `initializeProgress`, `findEnabledReviewSetting`, `applyScheduleResult`.”

### Abstraction và Interface

Xuất hiện rõ ở:
- `ReviewScheduler` interface.
- Implementations: `FixedIntervalScheduler`, `Sm2Scheduler`, `FsrsScheduler`.
- `AiClient` interface với `GroqVocabularyClient`, `LocalFallbackAiClient`.
- `EmailSender` interface với `SmtpEmailSender`.

Trả lời mẫu: “Scheduler được abstraction bằng interface để service không phụ thuộc trực tiếp vào từng thuật toán. `ReviewSchedulerResolver` chọn implementation theo `ReviewSchedulerType`.”

### Polymorphism

`ReviewService` gọi `ReviewScheduler scheduler = reviewSchedulerResolver.resolve(...)`, sau đó gọi `scheduler.schedule(...)`. Runtime có thể là fixed, SM2 hoặc FSRS.

### Enum

Xuất hiện nhiều:
- `UserRole`, `UserStatus`
- `CollectionVisibility`
- `LearningStatus`
- `ReviewResult`
- `ReviewSchedulerType`
- `FlashcardSessionStatus`, `QuizSessionStatus`, `TypingSessionStatus`
- `RoleplaySessionStatus`, `RoleplayDifficulty`
- `ShadowingLessonStatus`, `ShadowingLessonSource`

Trả lời mẫu: “Enum giúp giới hạn giá trị hợp lệ ở domain, ví dụ review chỉ nhận AGAIN/HARD/GOOD/EASY thay vì string tùy ý.”

### Collection API

Project dùng `List`, `Set`, `Map`, `Optional`.

Ví dụ:
- `VocabularyService.attachToCollections` nhận `Set<UUID> collectionIds`.
- `AdminShadowingService.loadSubtitleCounts` dùng `Map<UUID, Integer>`.
- `RoleplayAiService.textArray` trả `List<String>`.

### Stream API và Lambda

Xuất hiện trong mapping entity sang DTO và build list:
- `vocabularies.stream().map(...).toList()`.
- `page.map(...)`.
- `Collectors.toMap(...)` trong `AdminShadowingService`.

Trả lời mẫu: “Em dùng Stream chủ yếu cho transform collection, ví dụ chuyển danh sách `VocabularyEntity` thành question/item hoặc DTO.”

### Optional

Dùng để xử lý kết quả có thể không tồn tại:
- `userRepository.findByEmail(...).orElseThrow(...)`.
- `roleplayReportRepository.findBySessionId(...).map(...).orElse(null)`.

### Exception Handling

Domain exception:
- `BusinessException`
- `BaseException`
- `ErrorCode`

Global handler:
- `GlobalExceptionHandler`

Trả lời mẫu: “Service throw `BusinessException(ErrorCode...)`, còn `GlobalExceptionHandler` chuyển thành response thống nhất `ApiResponse.error` với HTTP status từ `ErrorCode`.”

### Date/Time API

Dùng `LocalDateTime`, `LocalTime`, `Instant`, `Duration`, `ChronoUnit`.

Ví dụ:
- `ReviewService.submitReview`: `LocalDateTime.now()`.
- `FsrsScheduler.calculateRetrievability`: `ChronoUnit.DAYS.between`.
- `JwtTokenProvider`: `Instant`.
- AI HTTP timeout: `Duration.ofSeconds(...)`.

### Record

DTO request/response dùng Java record:
- `LoginRequest`
- `CreateRoleplaySessionRequest`
- `ReviewSubmitResponse`
- `RoleplayAiReport`

Trả lời mẫu: “Record phù hợp cho immutable DTO, giảm boilerplate getter/constructor.”

### Annotation

Spring/JPA/Validation/Lombok:
- `@RestController`, `@Service`, `@Repository`, `@Entity`
- `@Transactional`, `@PreAuthorize`
- `@NotBlank`, `@NotNull`, `@Email`, `@Size`
- `@RequiredArgsConstructor`, `@Builder`

### Design pattern

Có thể nói các pattern thực tế:
- Strategy pattern: `ReviewScheduler`.
- Resolver/Factory-like: `ReviewSchedulerResolver`.
- DTO pattern: request/response DTO.
- Repository pattern: Spring Data repositories.
- Dependency Injection: constructor injection qua Lombok `@RequiredArgsConstructor`.

## PHẦN 5 - SPRING / SPRING BOOT

### IoC, DI, Bean

File:
- `SecurityConfig`
- các class `@Service`, `@Component`, `@Repository`

Project dùng Spring IoC để quản lý bean. DI chủ yếu qua constructor injection với `@RequiredArgsConstructor`.

Q: Vì sao dùng constructor injection?
A: Dễ test, dependency rõ ràng, field có thể `final`, tránh object thiếu dependency.

### Controller / RestController

Các controller:
- `AuthController`
- `CollectionController`
- `VocabularyController`
- `ReviewController`
- `FlashcardController`
- `QuizController`
- `TypingController`
- `RoleplayController`
- `AdminShadowingController`

Nhiệm vụ: nhận request, validate, gọi service, wrap response bằng `ApiResponse`.

### Service

Service chứa business logic và transaction:
- `AuthService`
- `ReviewService`
- `FlashcardService`
- `RoleplayService`
- `AdminShadowingService`

### Repository

Repository extends `JpaRepository<Entity, UUID>`. Project dùng query method và JPQL `@Query`.

Ví dụ:
- `LearningProgressRepository`
- `CollectionVocabularyRepository`
- `RoleplayMessageRepository`
- `ShadowingLessonSubtitleRepository`

### Configuration

File:
- `application.yml`
- `SecurityConfig`
- `OpenApiConfig`
- `RabbitMqConfig`
- `AsyncConfig`
- `TransactionConfig`

Nội dung quan trọng:
- `server.servlet.context-path=/api/v1`
- datasource từ env
- `spring.jpa.hibernate.ddl-auto=validate`
- Flyway enabled
- JWT secret/expiration
- Groq key
- CORS allowed origins
- upload limit

### Spring MVC

REST API dùng annotation mapping:
- `@RequestMapping`
- `@GetMapping`
- `@PostMapping`
- `@PutMapping`
- `@PatchMapping`
- `@DeleteMapping`

### Transaction

Service dùng:
- `@Transactional` cho write flow.
- `@Transactional(readOnly = true)` cho read flow.
- `TransactionTemplate` trong roleplay/shadowing để không giữ transaction trong lúc gọi AI async/network.

Q: Vì sao không giữ transaction khi gọi AI?
A: Gọi network có thể chậm hoặc timeout. Giữ DB transaction lâu làm tăng lock/resource usage. Code hiện tách lưu DB trước/sau bằng `TransactionTemplate`.

### Validation

Request DTO dùng:
- `@NotBlank`
- `@NotNull`
- `@Email`
- `@Size`
- `@Min`
- `@Valid`

Lỗi validation được xử lý trong `GlobalExceptionHandler.handleValidationException`.

### Security

File:
- `SecurityConfig`
- `JwtAuthenticationFilter`
- `JwtTokenProvider`
- `CustomUserDetailsService`

Cơ chế:
- Stateless session.
- CSRF disabled.
- JWT filter chạy trước `UsernamePasswordAuthenticationFilter`.
- Public endpoints: register, login, refresh, logout, swagger, api-docs, actuator health.
- Còn lại phải authenticated.
- Admin APIs dùng `@PreAuthorize("hasRole('ADMIN')")`.

### Authentication

Login:
- `AuthService.login`.
- Check email/password bằng BCrypt `PasswordEncoder`.
- Generate JWT access token.
- Tạo refresh token trong DB.

### Authorization

- Role lưu trong `users.role`.
- JWT claim có `role`.
- `CustomUserDetailsService` load authorities.
- Admin controller dùng `@PreAuthorize`.

### PasswordEncoder

`SecurityConfig.passwordEncoder()` trả `BCryptPasswordEncoder`.

## PHẦN 6 - JPA / HIBERNATE

### Entity mapping

Entity có `@Entity`, `@Table`, `@Id`, `@GeneratedValue(strategy = GenerationType.UUID)`.

Ví dụ:
- `UserEntity` -> `users`
- `VocabularyEntity` -> `vocabularies`
- `LearningProgressEntity` -> `learning_progress`

### Relationship

`@ManyToOne(fetch = FetchType.LAZY)` xuất hiện nhiều:
- collection owner -> user.
- vocabulary owner -> user.
- progress -> user/vocabulary.
- session -> user/collection.
- question/item -> session/vocabulary.

`@OneToOne(fetch = FetchType.LAZY)`:
- `RoleplayReportEntity.session` với unique `session_id`.

`@ManyToMany` trực tiếp không thấy trong code. Project dùng entity trung gian `CollectionVocabularyEntity` thay vì `@ManyToMany`, đây là thiết kế tốt vì bảng nối có thêm metadata `addedBy`, `position`, `createdAt`.

### LAZY/EAGER

Phần lớn relation dùng `FetchType.LAZY`. Lý do:
- Tránh load toàn bộ object graph không cần thiết.
- Service/mapper chủ động query phần cần dùng.

### JpaRepository

Ví dụ:
- `UserRepository extends JpaRepository<UserEntity, UUID>`
- `LearningProgressRepository extends JpaRepository<LearningProgressEntity, UUID>`

### Query method

Ví dụ:
- `findByEmail`
- `existsByEmail`
- `findByIdAndOwnerIdAndDeletedAtIsNull`
- `findAllBySessionIdOrderByCreatedAtAsc`

### JPQL

Có ở:
- `LearningProgressRepository`
- `CollectionVocabularyRepository`
- `ReviewHistoryRepository`
- `RoleplayMessageRepository`
- `ShadowingLessonSubtitleRepository`

Ví dụ thực tế:
- count due reviews.
- tìm collection title chứa vocabulary.
- batch count subtitle theo lesson để tránh N+1.

### Entity lifecycle

Một số entity có `createdAt`, `updatedAt`; source có khả năng dùng `@PrePersist/@PreUpdate` ở entity, nhưng cần mở từng entity để xác định đầy đủ. Trong các service cũng có chỗ set thời gian thủ công như session started/completed.

### N+1 Query

Vấn đề có khả năng xảy ra:
- Mapper truy cập relation LAZY trong vòng lặp có thể gây N+1 nếu không fetch join/batch query.

Bằng chứng project đã tối ưu một case:
- `AdminShadowingService.listLessons` dùng `loadSubtitleCounts` và `ShadowingLessonSubtitleRepository.countByLessonIds` để tránh count subtitle từng lesson.

## PHẦN 7 - API / HTTP

Context path: `/api/v1`.

### Bảng endpoint chính

| Endpoint | Method | Mục đích | Request | Response | Class/Method |
|---|---|---|---|---|---|
| `/auth/register` | POST | Đăng ký | `RegisterRequest` | `RegisterResponse` | `AuthController.register` |
| `/auth/login` | POST | Đăng nhập | `LoginRequest` | `LoginResponse` | `AuthController.login` |
| `/auth/refresh` | POST | Refresh token | `RefreshTokenRequest` | `RefreshTokenResponse` | `AuthController.refresh` |
| `/auth/logout` | POST | Logout/revoke token | `RefreshTokenRequest` | void | `AuthController.logout` |
| `/users/me` | GET | User hiện tại | JWT | `UserResponse` | `UserController.getCurrentUser` |
| `/collections` | POST | Tạo collection | `CreateCollectionRequest` | `CollectionResponse` | `CollectionController.createCollection` |
| `/collections/my` | GET | Collection của tôi | pageable | `CollectionPageResponse` | `CollectionController.getMyCollections` |
| `/collections/{collectionId}` | GET | Chi tiết collection | path | `CollectionResponse` | `CollectionController.getCollectionDetail` |
| `/collections/{collectionId}` | PUT | Update collection | `UpdateCollectionRequest` | `CollectionResponse` | `CollectionController.updateCollection` |
| `/collections/{collectionId}` | DELETE | Soft delete collection | path | void | `CollectionController.deleteCollection` |
| `/vocabularies` | POST | Tạo vocabulary | `CreateVocabularyRequest` | `VocabularyResponse` | `VocabularyController.createVocabulary` |
| `/vocabularies` | GET | List vocabulary | pageable | `VocabularyPageResponse` | `VocabularyController.getMyVocabularies` |
| `/vocabularies/{id}` | GET | Chi tiết vocabulary | path | `VocabularyResponse` | `VocabularyController.getVocabularyDetail` |
| `/vocabularies/{id}` | PUT | Update vocabulary | `UpdateVocabularyRequest` | `VocabularyResponse` | `VocabularyController.updateVocabulary` |
| `/vocabularies/{id}` | DELETE | Soft delete vocabulary | path | void | `VocabularyController.deleteVocabulary` |
| `/collections/{collectionId}/vocabularies/{vocabularyId}` | POST | Add vocabulary vào collection | path | `VocabularyResponse` | `CollectionVocabularyController.add` |
| `/reviews/{vocabularyId}` | POST | Submit review | `SubmitReviewRequest` | `ReviewSubmitResponse` | `ReviewController.submitReview` |
| `/reviews/today` | GET | Review due | pageable | `ReviewDuePageResponse` | `ReviewController.getTodayReviews` |
| `/reviews/history` | GET | Review history | pageable | `ReviewHistoryPageResponse` | `ReviewController.getReviewHistory` |
| `/reviews/stats` | GET | Review stats | - | `ReviewStatisticsResponse` | `ReviewController.getStatistics` |
| `/reviews/due-count` | GET | Count due | - | `ReviewDueCountResponse` | `ReviewController.getDueCount` |
| `/flashcards/sessions` | POST | Tạo flashcard session | `CreateFlashcardSessionRequest` | `FlashcardSessionResponse` | `FlashcardController.createSession` |
| `/flashcards/sessions/{sessionId}/cards/{vocabularyId}/answer` | POST | Submit flashcard | `SubmitFlashcardAnswerRequest` | `FlashcardAnswerResponse` | `FlashcardController.submitAnswer` |
| `/quizzes/sessions` | POST | Tạo quiz session | `CreateQuizSessionRequest` | `QuizSessionResponse` | `QuizController.createSession` |
| `/quizzes/sessions/{sessionId}/questions/{questionId}/answer` | POST | Submit quiz | `SubmitQuizAnswerRequest` | `QuizAnswerResponse` | `QuizController.submitAnswer` |
| `/typing/sessions` | POST | Tạo typing session | `CreateTypingSessionRequest` | `TypingSessionResponse` | `TypingController.createSession` |
| `/typing/sessions/{sessionId}/questions/{questionId}/answer` | POST | Submit typing | `SubmitTypingAnswerRequest` | `TypingAnswerResponse` | `TypingController.submitAnswer` |
| `/ai/vocabulary/normalize` | POST | Normalize 1 từ | `NormalizeVocabularyRequest` | `NormalizeVocabularyResponse` | `AiVocabularyController.normalize` |
| `/ai/vocabulary/normalize-bulk` | POST | Normalize nhiều từ | `NormalizeBulkVocabularyRequest` | `NormalizeBulkVocabularyResponse` | `AiVocabularyController.normalizeBulk` |
| `/dictionary/search` | GET | Tra từ điển | query | dictionary response | `DictionaryController.search` |
| `/roleplay/sessions` | POST | Tạo roleplay | `CreateRoleplaySessionRequest` | `RoleplaySessionResponse` | `RoleplayController.createSession` |
| `/roleplay/sessions/{sessionId}/messages` | POST | Gửi message | `SendRoleplayMessageRequest` | `RoleplayMessageResponse` | `RoleplayController.sendMessage` |
| `/roleplay/sessions/{sessionId}/end` | POST | Kết thúc roleplay | path | `RoleplayReportResponse` | `RoleplayController.endSession` |
| `/shadowing/lessons` | GET | List lesson public | pageable | `ShadowingLessonPageResponse` | `ShadowingLessonController.listLessons` |
| `/shadowing/lessons/{lessonId}` | GET | Detail lesson | path | `ShadowingLessonDetailResponse` | `ShadowingLessonController.getLessonDetail` |
| `/admin/users` | GET | Admin list user | pageable | page | `AdminUserController` |
| `/admin/shadowing/lessons/upload` | POST | Upload MP4 lesson | multipart | admin lesson | `AdminShadowingController.uploadLesson` |
| `/admin/shadowing/lessons/youtube` | POST | Import YouTube audio | request body | admin lesson | `AdminShadowingController.importFromYouTube` |
| `/admin/shadowing/lessons/{lessonId}/subtitles/generate-ai` | POST | Generate subtitle AI | path | status | `AdminShadowingController.generateAiSubtitles` |

### API questions

Q: Vì sao response được wrap trong `ApiResponse`?
A: Để client nhận format thống nhất cho success/error.

Q: Pageable hoạt động thế nào?
A: Controller nhận `Pageable`, repository trả `Page<T>`, service map sang custom page response gồm content/page/size/totalElements/totalPages.

Q: File upload xử lý thế nào?
A: Admin shadowing upload dùng `MultipartFile`, validate MP4, upload Cloudinary, lưu metadata vào `shadowing_lessons`.

## PHẦN 8 - CÁC CHỨC NĂNG QUAN TRỌNG

### 1. Authentication JWT

Nghiệp vụ: đăng ký, đăng nhập, refresh, logout.

Code liên quan:
- `AuthController`
- `AuthService`
- `RefreshTokenService`
- `JwtTokenProvider`
- `JwtAuthenticationFilter`
- `SecurityConfig`

Kỹ thuật:
- Spring Security, JWT, BCrypt, JPA, validation.

Câu hỏi:
1. Password lưu thế nào?
2. JWT chứa gì?
3. Refresh token khác access token thế nào?
4. Logout xử lý ra sao?
5. Vì sao dùng stateless session?

Trả lời ngắn:
- Password encode bằng BCrypt.
- JWT có subject email, claim `userId`, `role`, issuedAt, expiration.
- Refresh token lưu DB để revoke; access token không lưu DB.
- Logout revoke refresh token.
- Stateless phù hợp REST API, server không giữ session.

Đào sâu:
- Nếu JWT bị lộ thì xử lý thế nào?
- Vì sao cần token expiration?
- Role trong JWT có rủi ro stale role không?

### 2. Authorization Admin

Nghiệp vụ: chỉ admin gọi được API admin.

Code:
- `SecurityConfig` bật `@EnableMethodSecurity`.
- Admin controller có `@PreAuthorize("hasRole('ADMIN')")`.

Câu hỏi:
1. `hasRole('ADMIN')` kiểm tra cái gì?
2. Role lấy từ đâu?
3. Nếu user đổi role thì JWT cũ còn hiệu lực không?
4. Vì sao admin endpoint vẫn cần JWT?
5. Nếu thiếu token thì response gì?

Trả lời:
- `hasRole('ADMIN')` kiểm tra authority của authenticated user.
- Role đến từ `CustomUserDetailsService`/user entity, JWT cũng có claim role.
- JWT cũ có thể stale đến khi hết hạn nếu hệ thống chỉ tin JWT; trong project filter load `UserDetails` theo email nên quyền phụ thuộc implementation `CustomUserDetailsService`.

### 3. Collection Management

Nghiệp vụ: tạo/sửa/xóa/list collection của user.

Code:
- `CollectionController`
- `CollectionService`
- `CollectionRepository`
- `CollectionEntity`

Câu hỏi:
1. Làm sao đảm bảo user chỉ sửa collection của mình?
2. Vì sao dùng soft delete?
3. `SYSTEM` visibility là gì?
4. `totalWords` cập nhật ở đâu?
5. Có rủi ro race condition với `totalWords` không?

Trả lời:
- Query luôn dùng owner id từ SecurityContext.
- Soft delete giữ dữ liệu lịch sử.
- User thường không được tạo/sửa collection `SYSTEM`.
- `VocabularyService` tăng/giảm `totalWords` khi add/remove/delete vocabulary.
- Nếu nhiều request đồng thời, `totalWords` thủ công có thể lệch nếu không có locking/transaction isolation phù hợp.

### 4. Vocabulary Management

Nghiệp vụ: CRUD từ vựng và metadata.

Code:
- `VocabularyController`
- `VocabularyService`
- `VocabularyEntity`
- `VocabularyMapper`

Câu hỏi:
1. `normalizedWord` dùng làm gì?
2. JSONB trong vocabulary lưu gì?
3. Khi delete vocabulary thì relation xử lý sao?
4. Vì sao không xóa cứng vocabulary?
5. Từ thuộc nhiều collection được không?

Trả lời:
- Normalize để lower-case/search/duplicate.
- JSONB lưu synonyms, antonyms, examples.
- Delete vocabulary xóa relation collection-vocabulary rồi set `deletedAt`.
- Có, qua `collection_vocabularies`.

### 5. AI Normalize Vocabulary

Nghiệp vụ: AI chuẩn hóa thông tin từ vựng.

Code:
- `AiVocabularyController`
- `AiVocabularyService`
- `AiNormalizeQuotaService`
- `GroqVocabularyClient`
- `LocalFallbackAiClient`
- prompt/parser classes trong package `ai`.

Câu hỏi:
1. Khi không có AI key thì sao?
2. Vì sao cần parser JSON?
3. Quota trial nằm đâu?
4. Bulk normalize khác single normalize thế nào?
5. Có rủi ro AI trả sai schema không?

Trả lời:
- Có fallback client local.
- AI trả text nên cần parse/validate JSON.
- Config `ai.normalize.trial.daily-limit`.
- Bulk dùng prompt/parser riêng.
- Có, nên parser/test và error handling là cần thiết.

### 6. Review Scheduler

Nghiệp vụ: tính lần ôn tiếp theo sau mỗi đánh giá.

Code:
- `ReviewService`
- `ReviewScheduler`
- `ReviewSchedulerResolver`
- `FixedIntervalScheduler`
- `Sm2Scheduler`
- `FsrsScheduler`

Câu hỏi:
1. Scheduler được chọn như thế nào?
2. Nếu không có setting thì dùng gì?
3. SM2 cập nhật field nào?
4. FSRS cập nhật field nào?
5. Vì sao dùng Strategy pattern?

Trả lời:
- Lấy setting theo collection chứa vocabulary, nếu enabled thì dùng `scheduler_type`.
- Không có setting dùng `FIXED_INTERVAL`.
- SM2 cập nhật ease factor, repetition, interval, lapse/review count.
- FSRS cập nhật difficulty, stability, retrievability, interval.
- Strategy giúp thêm thuật toán mà ít sửa `ReviewService`.

### 7. Flashcard

Nghiệp vụ: tạo phiên flashcard từ ALL/COLLECTION/REVIEW_DUE, submit từng card.

Code:
- `FlashcardService`
- `FlashcardSessionEntity`
- `FlashcardSessionItemEntity`

Câu hỏi:
1. Source của session gồm gì?
2. Submit flashcard có cập nhật SRS không?
3. Làm sao chặn trả lời lại cùng card?
4. Khi nào session completed?
5. Nếu session rỗng thì sao?

Trả lời:
- Source gồm ALL, COLLECTION, REVIEW_DUE.
- Có, gọi `ReviewService.submitReview`.
- Kiểm tra `answeredAt != null`.
- Khi `completedCards >= totalCards`.
- Tạo xong set completed.

### 8. Quiz

Nghiệp vụ: tạo câu hỏi trắc nghiệm/quiz và submit đáp án.

Code:
- `QuizService`
- `QuizQuestionEntity`

Câu hỏi:
1. Câu hỏi được tạo kiểu gì?
2. Options được build thế nào?
3. Đúng/sai map sang review result nào?
4. Normalize answer thế nào?
5. Có random thật không?

Trả lời:
- Type dựa trên hash vocabulary id: term-to-meaning hoặc meaning-to-term.
- Options lấy correct answer + các candidate khác theo sorted word, tối đa 4.
- Đúng -> GOOD, sai -> AGAIN.
- Trim + lower-case.
- Không thấy random shuffle trong code hiện tại.

### 9. Typing

Nghiệp vụ: user gõ từ đúng theo meaning prompt.

Code:
- `TypingService`
- `TypingQuestionEntity`

Câu hỏi:
1. Prompt text lấy từ đâu?
2. Đúng/sai tính thế nào?
3. Similarity score dùng làm gì?
4. Có cập nhật SRS không?
5. Levenshtein implementation ở đâu?

Trả lời:
- Ưu tiên `meaningVi`, fallback `meaningEn`, cuối cùng text mặc định.
- Exact match sau trim/lower-case.
- Response hiển thị độ gần đúng, nhưng đúng/sai vẫn exact.
- Có, đúng GOOD, sai AGAIN.
- `TypingService.levenshteinDistance`.

### 10. Learning Progress

Nghiệp vụ: lưu tiến độ học từng từ của từng user.

Code:
- `LearningProgressEntity`
- `LearningProgressService`
- `LearningProgressRepository`
- `ReviewService.initializeProgress`

Câu hỏi:
1. Vì sao unique user-vocabulary?
2. `status` có ý nghĩa gì?
3. Field SM2 và FSRS nằm chung có sao không?
4. Khi nào progress được tạo?
5. `nextReviewAt` dùng ở đâu?

Trả lời:
- Một user có một progress hiện tại cho một từ.
- Status biểu diễn NEW/LEARNING/REVIEWING/MASTERED.
- Cùng bảng tiện query review due, nhưng field FSRS nullable.
- Khi submit review nếu chưa có.
- Query review due/today/dashboard/notification.

### 11. Notification

Nghiệp vụ: nhắc user review.

Code:
- `ReviewDueScheduler`
- `NotificationService`
- `NotificationProducer`
- `NotificationConsumer`
- `RabbitMqConfig`
- `EmailService`

Câu hỏi:
1. Vì sao dùng RabbitMQ?
2. Notification lưu DB không?
3. Email config ở đâu?
4. Có chống duplicate không?
5. Scheduler chạy theo logic nào?

Trả lời:
- RabbitMQ tách scheduling/event với xử lý gửi email.
- Có bảng `notifications`.
- `spring.mail` trong `application.yml`.
- Migration có unique index theo user/type/created date.
- Chi tiết cron cần đọc `ReviewDueScheduler`.

### 12. Dashboard

Nghiệp vụ: tổng hợp thông tin học tập.

Code:
- `DashboardController`
- `DashboardService`
- DTO trong `dashboard/dto/response`.

Câu hỏi:
1. Dashboard lấy dữ liệu từ bảng nào?
2. Vì sao nên dùng read-only transaction?
3. Recent activity tính từ đâu?
4. Review due count dựa vào field nào?
5. Có cache không?

Trả lời:
- Dựa vào vocabularies, collections, learning_progress, review_history.
- Read-only giảm overhead và thể hiện ý định không ghi.
- Cần đọc `DashboardService` để trả lời chi tiết từng metric.
- Dựa vào `next_review_at <= now`.
- Chưa xác định được cache từ source code dashboard.

### 13. Public Collection

Nghiệp vụ: xem collection công khai và clone.

Code:
- `PublicCollectionController`
- `PublicCollectionService`
- `AdminCollectionService`

Câu hỏi:
1. Public collection khác my collection thế nào?
2. Clone tạo dữ liệu gì?
3. Có kiểm tra visibility không?
4. Admin moderate public collection ra sao?
5. Có copy vocabulary hay chỉ link?

Trả lời:
- Cần trả lời chi tiết dựa vào `PublicCollectionService`; từ package/migration xác định có public listing, vocabularies, clone và moderation.

### 14. AI Roleplay

Nghiệp vụ: luyện hội thoại tiếng Anh với AI, nhận correction và report.

Code:
- `RoleplayController`
- `RoleplayService`
- `RoleplayAiService`
- `RoleplayPromptBuilder`
- `RoleplayLanguageValidator`
- `RoleplayQuotaService`

Câu hỏi:
1. AI roleplay gọi provider nào?
2. Vì sao có fallback?
3. Vì sao validate English-only?
4. Chấm điểm theo tiêu chí nào?
5. Vì sao dùng `TransactionTemplate`?

Trả lời:
- Gọi Groq chat completions qua Java `HttpClient`.
- Fallback để app vẫn có response khi thiếu key/provider lỗi.
- Vì feature luyện nói/viết tiếng Anh, tránh user chat tiếng Việt.
- Rubric: grammar 25, vocabulary 20, relevance 20, fluency 20, interaction 15; overall là tổng.
- Tránh giữ transaction trong lúc gọi AI.

### 15. Shadowing AI Subtitle

Nghiệp vụ: admin upload/import lesson, tạo subtitle AI.

Code:
- `AdminShadowingController`
- `AdminShadowingService`
- `ShadowingSubtitleGenerationService`
- `ShadowingAiSubtitleService`
- `AudioExtractorService`
- `CloudinaryVideoStorageService`
- `YouTubeDownloadService`

Câu hỏi:
1. Generate subtitle chạy sync hay async?
2. AI xử lý mấy bước?
3. Dữ liệu subtitle lưu ở đâu?
4. Vì sao cần batch translation?
5. Khi lỗi xử lý thế nào?

Trả lời:
- Async bằng `@Async("shadowingTaskExecutor")`.
- Download/extract audio -> Groq transcription -> translate segment -> save subtitle.
- Lưu ở `shadowing_lesson_subtitles`.
- Để tránh prompt quá dài và giảm lỗi provider.
- Set lesson `FAILED`, progress 0, lưu error message.

## PHẦN 9 - BUG VÀ PROBLEM SOLVING

### Vấn đề có bằng chứng trong project

#### 1. README backend có thông tin lỗi thời về Shadowing

Bằng chứng:
- README ghi “Shadowing is currently scaffold-only”.
- Code có `AdminShadowingController`, `ShadowingLessonController`, service/entity/migration đầy đủ.

Nguyên nhân:
- README chưa được cập nhật sau khi implement.

Cách xử lý:
- Update README để phản ánh trạng thái hiện tại.

Interviewer hỏi:
- “Em xử lý documentation drift thế nào?”

Trả lời:
- “Em ưu tiên code/migration/test làm source of truth, sau đó cập nhật README/API docs trong cùng PR để tránh lệch.”

#### 2. Quiz options không thấy shuffle/random

Bằng chứng:
- `QuizService.buildOptions` dùng `LinkedHashSet`, option pool sorted by word.

Vấn đề:
- Đáp án đúng luôn được add đầu tiên trước khi add distractor, nếu frontend không shuffle thì có thể lộ pattern.

Cách xử lý:
- Shuffle options trước khi lưu hoặc trước khi response.

#### 3. Typing similarity score không ảnh hưởng đúng/sai

Bằng chứng:
- `TypingService.submitAnswer` dùng exact match để set `correct`.
- `calculateSimilarityScore` chỉ trả về response.

Vấn đề:
- User gõ gần đúng vẫn bị AGAIN.

Cách xử lý:
- Nếu business muốn chấm mềm, có thể dùng threshold similarity. Nếu muốn strict typing, giữ như hiện tại nhưng UI cần giải thích.

#### 4. totalWords cập nhật thủ công có thể lệch

Bằng chứng:
- `VocabularyService.incrementTotalWords/decrementTotalWords`.

Vấn đề:
- Khi concurrent add/remove/delete có thể lệch nếu không có locking.

Cách xử lý:
- Query count động khi cần chính xác hoặc dùng DB-level atomic update/locking.

#### 5. External AI/ffmpeg/yt-dlp phụ thuộc môi trường

Bằng chứng:
- `application.yml` có `YT_DLP_PATH`, `FFMPEG_PATH`, Groq key.

Vấn đề:
- Deploy thiếu binary/env sẽ làm shadowing AI fail.

Cách xử lý:
- Health check/cấu hình rõ trong README/Docker image.

### Vấn đề có khả năng xảy ra nhưng chưa đủ bằng chứng là bug thực tế

1. N+1 query do LAZY relation khi map list.
2. JWT stale nếu role/status thay đổi trong khi token còn hạn.
3. AI JSON response invalid dù đã retry.
4. Upload file lớn gây timeout provider.
5. Soft delete nhưng relation/history còn tham chiếu dữ liệu cũ.

## PHẦN 10 - SECURITY

### Password storage

Password lưu ở `users.password_hash` và entity field `passwordHash/password`. Service dùng `passwordEncoder.encode`.

### BCrypt

Bean:
- `SecurityConfig.passwordEncoder()`

### Authentication

- Login bằng email/password.
- JWT access token.
- Refresh token lưu DB.

### Authorization

- Authenticated mọi endpoint trừ whitelist.
- Admin endpoint dùng `@PreAuthorize("hasRole('ADMIN')")`.

### JWT

File:
- `JwtTokenProvider`
- `JwtAuthenticationFilter`

JWT có:
- subject = email.
- claim `userId`.
- claim `role`.
- issuedAt, expiration.
- sign bằng secret.

### Session

`SessionCreationPolicy.STATELESS`. Không dùng HTTP session server-side.

### SQL Injection

Project dùng Spring Data JPA/query method/JPQL parameter binding, không thấy nối chuỗi SQL trực tiếp trong code đã đọc. Rủi ro SQL injection thấp nếu tiếp tục dùng parameter binding.

### XSS

Backend trả JSON, không thấy xử lý XSS cụ thể. Frontend cần escape/render text an toàn theo React mặc định. Chưa xác định được có chỗ dùng `dangerouslySetInnerHTML`.

### CSRF

CSRF disabled trong `SecurityConfig`. Với stateless JWT qua Authorization header, cách này phổ biến. Nếu dùng cookie auth thì cần xem lại.

### Input validation

DTO dùng Bean Validation. GlobalExceptionHandler trả lỗi validation thống nhất.

### Access control

Service thường kiểm tra ownership bằng query theo `ownerId/userId`. Đây là điểm mạnh vì không chỉ dựa vào frontend.

## PHẦN 11 - CÂU HỎI PHỎNG VẤN

### LEVEL 1 - Cơ bản

1. Project của em làm gì?
   - A: Nền tảng học từ vựng tiếng Anh với collection, vocabulary, review, flashcard, quiz, typing, AI roleplay, shadowing.
   - File: `README.md`, package structure.
   - Hỏi tiếp: Người dùng chính là ai?

2. Backend dùng framework gì?
   - A: Spring Boot 3.3.5, Java 21.
   - File: `build.gradle`.
   - Hỏi tiếp: Spring Boot giúp gì?

3. Database là gì?
   - A: PostgreSQL, migration bằng Flyway.
   - File: `application.yml`, `db/migration`.
   - Hỏi tiếp: Vì sao không dùng ddl-auto update?

4. Auth dùng cơ chế gì?
   - A: JWT access token + refresh token DB.
   - File: `AuthService`, `JwtTokenProvider`.
   - Hỏi tiếp: Refresh token dùng làm gì?

5. Password lưu thế nào?
   - A: BCrypt hash.
   - File: `SecurityConfig`, `AuthService`.
   - Hỏi tiếp: Vì sao không lưu plaintext?

6. Controller làm gì?
   - A: Nhận request, validate, gọi service, trả response.
   - File: các controller.
   - Hỏi tiếp: Service khác controller thế nào?

7. Repository làm gì?
   - A: Tương tác DB qua Spring Data JPA.
   - File: repository package.
   - Hỏi tiếp: JpaRepository cung cấp gì?

8. DTO dùng để làm gì?
   - A: Tách API contract khỏi entity.
   - File: dto request/response.
   - Hỏi tiếp: Có nên trả entity trực tiếp không?

9. `@Transactional` dùng để làm gì?
   - A: Đảm bảo các thao tác DB trong một transaction.
   - File: service classes.
   - Hỏi tiếp: readOnly có tác dụng gì?

10. Validation nằm ở đâu?
    - A: Request DTO dùng Bean Validation, controller dùng `@Valid`.
    - File: dto request.
    - Hỏi tiếp: Lỗi validation trả về thế nào?

11. Role trong project gồm gì?
    - A: `USER`, `ADMIN`.
    - File: `UserRole`.
    - Hỏi tiếp: Admin API bảo vệ thế nào?

12. Soft delete là gì?
    - A: Không xóa record ngay, set `deleted_at`.
    - File: `CollectionService`, `VocabularyService`.
    - Hỏi tiếp: Ưu/nhược điểm?

13. Review result gồm gì?
    - A: AGAIN, HARD, GOOD, EASY.
    - File: `ReviewResult`.
    - Hỏi tiếp: Nó ảnh hưởng scheduler ra sao?

14. Frontend gọi API bằng gì?
    - A: Axios `apiClient`.
    - File: `VocabVerse_FE/src/services/apiClient.js`.
    - Hỏi tiếp: Token gắn vào request thế nào?

15. Frontend route chính ở đâu?
    - A: `AppRouter.jsx`.
    - File: `src/app/router/AppRouter.jsx`.
    - Hỏi tiếp: ProtectedRoute dùng để làm gì?

### LEVEL 2 - Trung bình

1. Vì sao dùng Strategy pattern cho scheduler?
   - A: Để `ReviewService` không phụ thuộc từng thuật toán, dễ thêm SM2/FSRS/fixed.
   - File: `ReviewScheduler`, `ReviewSchedulerResolver`.
   - Hỏi tiếp: Nếu thêm thuật toán mới cần sửa đâu?

2. Flashcard khác quiz/typing trong review mapping thế nào?
   - A: Flashcard nhận trực tiếp rating; quiz/typing map đúng GOOD, sai AGAIN.
   - File: `FlashcardService`, `QuizService`, `TypingService`.
   - Hỏi tiếp: Có nên map quiz đúng thành EASY không?

3. Vì sao dùng bảng trung gian `collection_vocabularies`?
   - A: Quan hệ many-to-many và cần metadata.
   - File: `CollectionVocabularyEntity`.
   - Hỏi tiếp: Vì sao không dùng `@ManyToMany`?

4. `learning_progress` khác `review_history` thế nào?
   - A: Progress là trạng thái hiện tại; history là log từng lần review.
   - File: `LearningProgressEntity`, `ReviewHistoryEntity`.
   - Hỏi tiếp: Nếu cần audit thì dùng bảng nào?

5. Vì sao `open-in-view=false`?
   - A: Tránh lazy loading ngoài transaction/view layer, buộc service load dữ liệu cần thiết.
   - File: `application.yml`.
   - Hỏi tiếp: Lỗi LazyInitializationException là gì?

6. Roleplay dùng `TransactionTemplate` vì sao?
   - A: Tách transaction DB khỏi gọi AI network.
   - File: `RoleplayService`.
   - Hỏi tiếp: Nếu AI timeout thì DB có bị lock lâu không?

7. Shadowing generate subtitle xử lý lỗi thế nào?
   - A: Catch exception, set lesson FAILED, progress 0, error message.
   - File: `ShadowingSubtitleGenerationService`.
   - Hỏi tiếp: Vì sao cần async?

8. API refresh token chống nhiều request 401 cùng lúc ở FE thế nào?
   - A: `refreshPromise` dùng chung trong axios interceptor.
   - File: `apiClient.js`.
   - Hỏi tiếp: Nếu refresh fail thì sao?

9. Flyway migration có vai trò gì?
   - A: Version schema database.
   - File: `db/migration`.
   - Hỏi tiếp: Nếu migration lỗi production thì sao?

10. CORS config ở đâu?
    - A: `SecurityConfig.corsConfigurationSource`, origin từ `app.cors.allowed-origins`.
    - File: `SecurityConfig`, `application.yml`.
    - Hỏi tiếp: Vì sao không allow all?

### LEVEL 3 - Đào sâu vào project

1. Giải thích luồng `ReviewService.submitReview`.
   - A: Lấy user, init progress, tìm setting collection, resolve scheduler, schedule, apply result, save progress, save history.
   - File: `ReviewService`.
   - Hỏi tiếp: Nếu vocabulary nằm nhiều collection có setting khác nhau thì sao?

2. SM2 trong project tính ease factor thế nào?
   - A: Convert result sang quality 2-5, dùng công thức SM2, min ease factor 1.30.
   - File: `Sm2Scheduler`.
   - Hỏi tiếp: AGAIN ảnh hưởng repetition ra sao?

3. FSRS trong project có phải bản FSRS chuẩn không?
   - A: Code implement một mô hình đơn giản có difficulty/stability/retrievability, không thấy dùng full FSRS parameter vector chuẩn trong file hiện tại.
   - File: `FsrsScheduler`.
   - Hỏi tiếp: Nếu muốn full FSRS thì cần gì?

4. Vì sao quiz option hiện có thể thiếu randomness?
   - A: `buildOptions` add correct trước và sort candidate theo word.
   - File: `QuizService`.
   - Hỏi tiếp: Cách sửa?

5. Typing dùng Levenshtein để làm gì?
   - A: Tính similarity score trong response.
   - File: `TypingService.calculateSimilarityScore`.
   - Hỏi tiếp: Có dùng để quyết định correct không?

6. AI roleplay chấm điểm theo tiêu chí nào?
   - A: Grammar 25, vocabulary 20, relevance 20, fluency 20, interaction 15; overall sum.
   - File: `RoleplayPromptBuilder`, `RoleplayAiReport`.
   - Hỏi tiếp: Làm sao đảm bảo AI trả overall đúng sum?

7. English-only roleplay check thế nào?
   - A: `RoleplayLanguageValidator` reject Vietnamese diacritics và message không có English words.
   - File: `RoleplayLanguageValidator`.
   - Hỏi tiếp: Có false positive/negative không?

8. Shadowing AI transcription request gồm gì?
   - A: Multipart file audio, model, language en, response_format verbose_json, timestamp segment.
   - File: `ShadowingAiSubtitleService.buildMultipartBodyPublisher`.
   - Hỏi tiếp: Vì sao cần verbose_json?

9. Admin list shadowing tối ưu N+1 thế nào?
   - A: Batch count subtitle theo lesson ids.
   - File: `AdminShadowingService.loadSubtitleCounts`, `ShadowingLessonSubtitleRepository.countByLessonIds`.
   - Hỏi tiếp: Nếu list 1000 lesson thì sao?

10. JWT filter hoạt động thế nào?
    - A: Resolve Bearer token, validate, extract email, load user details, set SecurityContext.
    - File: `JwtAuthenticationFilter`.
    - Hỏi tiếp: Nếu token invalid thì sao?

### LEVEL 4 - Tình huống / Problem Solving

1. User báo quiz luôn đúng đáp án đầu tiên, debug sao?
   - A: Kiểm tra `QuizService.buildOptions`, FE render/shuffle; add shuffle server hoặc client.
   - File: `QuizService`.

2. Log Hibernate count subtitle lặp nhiều, xử lý sao?
   - A: Batch count bằng `countByLessonIds`.
   - File: `AdminShadowingService`.

3. AI subtitle generate lâu làm request timeout, xử lý sao?
   - A: Chuyển async, trả status processing, FE polling.
   - File: `ShadowingSubtitleGenerationService`.

4. Người dùng nhập tiếng Việt trong roleplay, xử lý sao?
   - A: Validate trước khi save user message.
   - File: `RoleplayLanguageValidator`, `RoleplayService.sendMessage`.

5. DB schema mismatch khi startup, xử lý sao?
   - A: Vì `ddl-auto=validate`, kiểm tra migration/entity, thêm migration mới.
   - File: `application.yml`, migration.

6. Token hết hạn frontend xử lý sao?
   - A: Axios interceptor gọi `/auth/refresh`, cập nhật token, retry request.
   - File: `apiClient.js`.

7. Review due count sai, kiểm tra đâu?
   - A: `learning_progress.next_review_at`, timezone/current time, repository query.
   - File: `LearningProgressRepository`, `ReviewService`.

8. Upload shadowing fail production, kiểm tra đâu?
   - A: Cloudinary env, max file size, multipart, content type, network.
   - File: `application.yml`, `AdminShadowingService`, `CloudinaryVideoStorageService`.

9. AI trả JSON lỗi, xử lý sao?
   - A: Strip code fence, retry once, validate field required, fallback/error.
   - File: `RoleplayAiService`, `ShadowingAiSubtitleService`.

10. Collection totalWords lệch, xử lý sao?
    - A: Recompute count từ `collection_vocabularies`, cân nhắc atomic update.
    - File: `VocabularyService`.

### LEVEL 5 - Nâng cao có thể phát sinh

1. Nếu 1 triệu `learning_progress`, query due tối ưu sao?
   - A: Index `next_review_at`, thêm composite `(user_id, next_review_at)`, pagination.
   - File: migration V5 hiện có index riêng.

2. Nếu AI provider rate limit?
   - A: Map 429 thành `AI_RATE_LIMITED`, retry/backoff/job queue nếu cần.
   - File: `RoleplayAiService`, `ShadowingAiSubtitleService`.

3. Nếu muốn realtime roleplay voice?
   - A: Cần thêm audio capture FE, streaming/realtime protocol, speech-to-text/text-to-speech, khác scope hiện tại.

4. Nếu muốn refresh token rotation?
   - A: Mỗi refresh revoke old token và cấp new token, detect reuse.
   - File: `RefreshTokenService` cần xem chi tiết.

5. Nếu muốn multi-tenant?
   - A: Cần tenant id trong core table và filter ownership theo tenant.

## PHẦN 12 - INTERVIEWER ĐÀO SÂU

### Chuỗi 1: JPA

Interviewer: Em dùng Spring Data JPA đúng không?

Candidate: Dạ đúng. Repository của em extends `JpaRepository`, ví dụ `UserRepository`, `LearningProgressRepository`.

Interviewer: Vì sao dùng JPA?

Candidate: Vì project CRUD nhiều entity, JPA giúp map object-table, hỗ trợ query method, transaction và pagination.

Interviewer: Khi nào JPA gây vấn đề?

Candidate: Có thể gặp N+1 query với relation LAZY, hoặc query không tối ưu khi dữ liệu lớn.

Interviewer: Project em có xử lý case nào chưa?

Candidate: Có case shadowing list lessons, thay vì count subtitle từng lesson, service dùng batch query `countByLessonIds`.

### Chuỗi 2: Security

Interviewer: Auth của em hoạt động thế nào?

Candidate: User login bằng email/password, backend check BCrypt, trả JWT access token và refresh token.

Interviewer: Request sau đó authenticate thế nào?

Candidate: FE gắn `Authorization: Bearer token`. `JwtAuthenticationFilter` validate token, extract email, load user details, set SecurityContext.

Interviewer: Admin API bảo vệ thế nào?

Candidate: Admin controller dùng `@PreAuthorize("hasRole('ADMIN')")`, và method security được bật trong `SecurityConfig`.

### Chuỗi 3: Review Scheduler

Interviewer: Em có SM2/FSRS đúng không?

Candidate: Dạ có. `ReviewScheduler` là interface, có `Sm2Scheduler`, `FsrsScheduler`, `FixedIntervalScheduler`.

Interviewer: Scheduler chọn bằng cách nào?

Candidate: `ReviewService` tìm enabled review setting của collection chứa vocabulary, lấy `scheduler_type`, rồi `ReviewSchedulerResolver` chọn implementation.

Interviewer: Nếu không có setting?

Candidate: Fallback về `FIXED_INTERVAL`.

Interviewer: Quiz/typing có update SRS không?

Candidate: Có. Quiz/typing submit gọi `ReviewService.submitReview`, đúng map GOOD, sai map AGAIN.

### Chuỗi 4: Database Design

Interviewer: Vì sao có cả `learning_progress` và `review_history`?

Candidate: `learning_progress` lưu trạng thái mới nhất cho user-vocabulary, còn `review_history` lưu từng lần review để xem lịch sử/audit.

Interviewer: Unique constraint nào quan trọng?

Candidate: `learning_progress` unique `(user_id, vocabulary_id)`, `collection_vocabularies` unique `(collection_id, vocabulary_id)`.

Interviewer: Nếu user xóa vocabulary thì history sao?

Candidate: Code hiện soft delete vocabulary và xóa relation collection, history/progress vẫn có thể tham chiếu vocabulary record còn tồn tại.

### Chuỗi 5: Roleplay AI

Interviewer: Roleplay AI của em gọi AI thật không?

Candidate: Có, `RoleplayAiService` gọi Groq chat completions qua Java `HttpClient` khi có `GROQ_API_KEY`.

Interviewer: Nếu AI lỗi?

Candidate: Service fallback scenario/reply/report để app vẫn usable.

Interviewer: Vì sao cần validate tiếng Anh?

Candidate: Vì tính năng là luyện roleplay tiếng Anh, nên `RoleplayLanguageValidator` chặn tiếng Việt/không có English words trước khi lưu message.

Interviewer: Chấm điểm thế nào?

Candidate: Rubric gồm grammar 25, vocabulary 20, relevance 20, fluency 20, interaction 15.

### Chuỗi 6: Shadowing

Interviewer: Generate subtitle AI chạy thế nào?

Candidate: Admin bấm generate, lesson set PROCESSING, async service extract audio, gọi Groq transcription, translate sang tiếng Việt, lưu subtitle.

Interviewer: Vì sao async?

Candidate: Transcription/translation có thể lâu, nếu sync request dễ timeout.

Interviewer: Nếu đang PROCESSING mà bấm lại?

Candidate: `AdminShadowingService.generateAiSubtitles` trả status hiện tại thay vì start job mới.

### Chuỗi 7: Frontend API

Interviewer: FE quản lý token thế nào?

Candidate: Axios interceptor lấy access token từ auth store/storage và gắn Bearer token.

Interviewer: Token hết hạn?

Candidate: Response interceptor nếu 401 sẽ gọi `/auth/refresh`, dùng `refreshPromise` để tránh nhiều refresh song song, sau đó retry request.

Interviewer: Nếu refresh fail?

Candidate: FE logout user và reject normalized error.

### Chuỗi 8: Transaction

Interviewer: Khi nào em dùng `@Transactional`?

Candidate: Các write flow như create vocabulary, submit review, submit quiz/typing/flashcard đều dùng transaction.

Interviewer: Có chỗ nào không dùng annotation mà dùng template?

Candidate: Có `RoleplayService` và shadowing async dùng `TransactionTemplate` để kiểm soát transaction quanh DB operations, không bao quanh network AI call.

### Chuỗi 9: Validation/Error Handling

Interviewer: Input invalid xử lý ở đâu?

Candidate: DTO dùng Bean Validation, lỗi do `GlobalExceptionHandler.handleValidationException` trả `ApiResponse.error` với field errors.

Interviewer: Business error thì sao?

Candidate: Service throw `BusinessException(ErrorCode...)`, global handler trả HTTP status theo `ErrorCode`.

### Chuỗi 10: Scalability

Interviewer: Nếu nhiều user cùng review thì sao?

Candidate: Mỗi progress unique theo user-vocabulary, request submit update row tương ứng và insert history. Cần chú ý concurrent submit cùng vocabulary có thể cần idempotency/locking nếu phát sinh double submit.

Interviewer: Query due nhiều record thì tối ưu sao?

Candidate: Dùng index `next_review_at`; tốt hơn có composite index `(user_id, next_review_at)` vì query lọc theo user và due time.

## PHẦN 13 - NHỮNG THỨ TÔI PHẢI THUỘC

### 🔴 BẮT BUỘC PHẢI BIẾT

- [ ] Project overview: VocabVerse là app học từ vựng tiếng Anh.
- [ ] Backend architecture: Controller -> Service -> Repository -> Entity -> DB.
- [ ] Auth flow: register/login/JWT/refresh/logout.
- [ ] Security: `SecurityConfig`, JWT filter, BCrypt, `@PreAuthorize`.
- [ ] Database core: users, collections, vocabularies, collection_vocabularies, learning_progress, review_history.
- [ ] SRS flow: `ReviewService.submitReview`.
- [ ] SM2 fields và FSRS fields trong `learning_progress`.
- [ ] Flashcard/quiz/typing đều cập nhật learning progress.
- [ ] Roleplay AI flow và rubric.
- [ ] Shadowing AI subtitle async flow.
- [ ] Exception handling bằng `BusinessException`, `ErrorCode`, `GlobalExceptionHandler`.
- [ ] Flyway migration + `ddl-auto=validate`.

### 🟡 NÊN BIẾT

- [ ] Public collection clone/moderation.
- [ ] Notification + RabbitMQ + email.
- [ ] Dashboard metrics.
- [ ] Export PDF bằng PDFBox.
- [ ] Frontend route/auth guard/apiClient.
- [ ] AI normalize vocabulary.
- [ ] DTO/MapStruct usage.
- [ ] Query method vs JPQL.
- [ ] Soft delete implications.

### 🟢 BIẾT THÊM

- [ ] Tối ưu N+1 query.
- [ ] Composite index cho due review.
- [ ] Refresh token rotation.
- [ ] AI retry/backoff/job queue.
- [ ] Realtime voice roleplay nếu mở rộng.

## PHẦN 14 - ĐÁNH GIÁ PROJECT

Đóng vai Senior Java Interviewer, đánh giá dựa trên source hiện tại:

| Tiêu chí | Điểm |
|---|---:|
| Java | 8/10 |
| Spring Boot | 8/10 |
| JPA/Hibernate | 7.5/10 |
| SQL/Database | 7.5/10 |
| REST API | 8/10 |
| Security | 7.5/10 |
| Architecture | 8/10 |
| Code quality | 7.5/10 |
| Interview readiness | 8/10 nếu bạn nắm được luồng review/auth/AI |

### Điểm mạnh

1. Package theo module nghiệp vụ rõ ràng.
2. Có security thật: JWT, BCrypt, refresh token, admin authorization.
3. Có Flyway migration và validate schema.
4. Có SRS nhiều strategy, phù hợp domain học từ vựng.
5. Flashcard/quiz/typing tích hợp với learning progress.
6. Có AI features thực tế: normalize, roleplay, shadowing subtitle.
7. Có async processing cho tác vụ dài.
8. Có frontend đầy đủ route, protected/admin route, API interceptor.

### Điểm yếu

1. README backend có thông tin lỗi thời về Shadowing.
2. Quiz options có khả năng thiếu shuffle.
3. Typing similarity score chưa ảnh hưởng đúng/sai.
4. Một số logic count/counter thủ công như `totalWords` có thể lệch khi concurrent.
5. Có khả năng N+1 ở các màn list nếu mapper truy cập LAZY relation trong loop.
6. FSRS hiện là implementation đơn giản, không chứng minh là full FSRS chuẩn với parameter vector.
7. Chưa thấy test coverage cho nhiều module lớn như auth/admin/shadowing end-to-end.

### Nên học thêm

1. Spring Security filter chain, JWT, refresh token rotation.
2. JPA performance: N+1, fetch join, EntityGraph, pagination.
3. Transaction isolation, optimistic/pessimistic locking.
4. Database indexing theo query thực tế.
5. Queue/async job design.
6. AI integration reliability: timeout, retry, fallback, JSON schema validation.
7. Testing: unit, integration, Testcontainers.

### Không nên ghi vào CV nếu source code không chứng minh được

1. “Microservices architecture” - source hiện là modular monolith, không phải microservices.
2. “Realtime voice conversation” - roleplay hiện là text chat, chưa thấy realtime voice.
3. “Full official FSRS algorithm” - code có FSRS-like scheduler nhưng chưa chứng minh full FSRS chuẩn.
4. “OAuth2 social login” - chưa thấy trong source.
5. “Kubernetes deployment” - chưa thấy manifest Kubernetes.

### 10 câu có khả năng cao interviewer sẽ hỏi

1. Em giải thích luồng login và JWT trong project.
2. Vì sao dùng refresh token và refresh token lưu ở đâu?
3. `ReviewService.submitReview` hoạt động thế nào?
4. SM2 và FSRS lưu tham số ở bảng nào?
5. Flashcard, quiz, typing có cập nhật progress không?
6. Vì sao dùng Strategy pattern cho scheduler?
7. Vì sao dùng bảng `collection_vocabularies` thay vì `@ManyToMany` trực tiếp?
8. AI roleplay gọi AI thế nào và fallback ra sao?
9. Generate subtitle shadowing vì sao phải async?
10. Nếu query review due chậm khi dữ liệu lớn thì em tối ưu thế nào?
