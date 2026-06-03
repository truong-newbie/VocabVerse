# 09-backend-project-structure.md

# BACKEND PROJECT STRUCTURE

## Dự án

VocabVerse - AI Vocabulary Learning Platform

---

# 1. MỤC TIÊU

Tài liệu này định nghĩa:

* Cấu trúc source code
* Quy tắc đặt tên
* Quy tắc package
* Exception handling
* Response handling
* Security architecture
* Integration architecture

Mục tiêu:

```text
Code sạch
Dễ mở rộng
Dễ maintain
Dễ onboarding
```

---

# 2. KIẾN TRÚC BACKEND

Áp dụng:

```text
Clean Architecture
+
Modular Monolith
```

---

# 3. PACKAGE ROOT

```text
com.vocabverse
```

---

# 4. PROJECT STRUCTURE

```text
src/main/java

com.vocabverse

├── common
├── auth
├── user
├── collection
├── vocabulary
├── learning
├── review
├── notification
├── shadowing
├── roleplay
├── ai
├── export
└── storage
```

---

# 5. COMMON MODULE

Chỉ chứa thành phần dùng chung.

```text
common

├── config
├── security
├── exception
├── response
├── util
├── constant
├── validator
└── mapper
```

---

# 6. MODULE STRUCTURE

Ví dụ:

```text
collection

├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
├── exception
└── specification
```

---

# 7. CONTROLLER LAYER

Controller chỉ làm:

```text
Receive Request
Validate Input
Call Service
Return Response
```

Không được:

```text
Business Logic
External API Call
Database Query
```

---

Ví dụ:

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/collections")
public class CollectionController {

    private final CollectionService collectionService;

    @PostMapping
    public ApiResponse<CollectionResponse> create(
            @Valid @RequestBody CreateCollectionRequest request
    ) {
        return ApiResponse.success(
                collectionService.create(request)
        );
    }
}
```

---

# 8. SERVICE LAYER

Chứa toàn bộ business logic.

Ví dụ:

```java
@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository repository;

    public CollectionResponse create(
            CreateCollectionRequest request
    ) {

        validateTitle(request);

        CollectionEntity entity = mapper.toEntity(request);

        repository.save(entity);

        return mapper.toResponse(entity);
    }
}
```

---

# 9. REPOSITORY LAYER

Chỉ làm việc với database.

Ví dụ:

```java
public interface CollectionRepository
        extends JpaRepository<
        CollectionEntity,
        UUID> {

}
```

Không viết business logic ở đây.

---

# 10. DTO STRUCTURE

## Request

```java
CreateCollectionRequest
UpdateCollectionRequest
ImportVocabularyRequest
```

---

## Response

```java
CollectionResponse
VocabularyResponse
DashboardResponse
```

---

Không trả Entity trực tiếp.

Sai:

```java
return collectionEntity;
```

Đúng:

```java
return CollectionResponse;
```

---

# 11. ENTITY STRUCTURE

Ví dụ:

```java
@Entity
@Table(name = "collections")
public class CollectionEntity {

    @Id
    private UUID id;

    private String title;

    private String description;
}
```

---

Tên entity:

```text
UserEntity
CollectionEntity
VocabularyEntity
```

---

# 12. MAPPER STRATEGY

Dùng:

```text
MapStruct
```

Ví dụ:

```java
@Mapper
public interface CollectionMapper {

    CollectionEntity toEntity(
            CreateCollectionRequest request
    );

    CollectionResponse toResponse(
            CollectionEntity entity
    );
}
```

---

# 13. RESPONSE STANDARD

Tất cả API trả về:

```json
{
  "success": true,
  "message": "Success",
  "data": {}
}
```

---

Class:

```java
public class ApiResponse<T> {

    private boolean success;

    private String message;

    private T data;
}
```

---

# 14. EXCEPTION ARCHITECTURE

## Base Exception

```java
public abstract class BaseException
        extends RuntimeException {

    private final ErrorCode errorCode;
}
```

---

## Business Exception

Ví dụ:

```java
public class CollectionNotFoundException
        extends BaseException {
}
```

---

## Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
}
```

---

Không dùng:

```java
try {
}
catch(Exception e) {
}
```

tràn lan.

---

# 15. ERROR CODE

Tạo enum:

```java
public enum ErrorCode {

    USER_NOT_FOUND,

    COLLECTION_NOT_FOUND,

    VOCABULARY_NOT_FOUND,

    DUPLICATE_WORD,

    INVALID_INPUT,

    INTERNAL_SERVER_ERROR
}
```

---

# 16. SECURITY STRUCTURE

```text
common/security

├── JwtAuthenticationFilter
├── JwtTokenProvider
├── CustomUserDetailsService
├── SecurityConfig
└── SecurityConstants
```

---

# 17. JWT FLOW

```text
Login
↓
Generate Access Token
↓
Generate Refresh Token
↓
Return To Client
```

---

Request:

```text
Authorization: Bearer xxx
```

↓

```text
JwtAuthenticationFilter
```

↓

```text
SecurityContext
```

↓

Controller

---

# 18. CONFIG PACKAGE

```text
config

├── DatabaseConfig
├── RedisConfig
├── RabbitMqConfig
├── MailConfig
├── OpenApiConfig
├── CorsConfig
└── StorageConfig
```

---

# 19. VALIDATION

Ví dụ:

```java
@NotBlank
@Size(max = 100)
private String title;
```

---

Không validate bằng:

```java
if(title == null)
```

ở service.

---

# 20. ASYNC MODULE

```text
async

├── producer
├── consumer
├── event
└── job
```

---

Ví dụ:

```java
AiNormalizeProducer

AiNormalizeConsumer
```

---

# 21. AI MODULE

```text
ai

├── client
├── dto
├── service
├── prompt
└── parser
```

---

## Client

```java
GroqClient

GeminiClient
```

---

## Interface

```java
AiClient
```

---

Lợi ích:

```text
Thay Groq bằng Gemini
không ảnh hưởng business logic
```

---

# 22. STORAGE MODULE

```text
storage

├── StorageService
├── LocalStorageService
├── S3StorageService
└── R2StorageService
```

---

Không gọi S3 trực tiếp trong business logic.

---

# 23. NOTIFICATION MODULE

```text
notification

├── email
├── browser
├── service
└── scheduler
```

---

Ví dụ:

```java
EmailNotificationService

BrowserNotificationService
```

---

# 24. TEST STRUCTURE

```text
src/test/java

auth
user
collection
vocabulary
review
```

---

## Unit Test

```java
CollectionServiceTest
```

---

## Integration Test

```java
CollectionControllerIT
```

---

# 25. LOGGING

Dùng:

```java
@Slf4j
```

Ví dụ:

```java
log.info(
    "Create collection success. userId={}",
    userId
);
```

---

Không log:

```text
password
token
api key
```

---

# 26. FOLDER CẤM XUẤT HIỆN

Không tạo:

```text
utils
helpers
common-utils
service-impl
```

khi chưa thật sự cần.

---

# 27. DEPENDENCY ĐỀ XUẤT

```gradle
spring-boot-starter-web

spring-boot-starter-security

spring-boot-starter-validation

spring-boot-starter-data-jpa

spring-boot-starter-mail

springdoc-openapi

postgresql

flyway

redis

rabbitmq

mapstruct

jjwt
```

---

# 28. OPENAPI

Bắt buộc sinh:

```text
Swagger UI
```

URL:

```text
/swagger-ui.html
```

---

# 29. NAMING CONVENTION

## Controller

```text
CollectionController
VocabularyController
```

---

## Service

```text
CollectionService
VocabularyService
```

---

## Repository

```text
CollectionRepository
VocabularyRepository
```

---

## DTO

```text
CreateCollectionRequest

UpdateCollectionRequest

CollectionResponse
```

---

# 30. KẾT LUẬN

Backend VocabVerse sử dụng:

```text
Clean Architecture

Modular Monolith

Spring Boot

PostgreSQL

Redis

RabbitMQ
```

với nguyên tắc:

```text
Controller mỏng

Service chứa business logic

Repository chỉ truy cập dữ liệu

Không trả Entity ra API

Tất cả exception xử lý tập trung

Có thể scale sang microservices sau này
```

END OF DOCUMENT
