# VocabVerse Backend

VocabVerse is a Spring Boot backend for an AI-powered English vocabulary learning platform. It is structured as a modular monolith with JWT authentication, PostgreSQL, Flyway migrations, Redis, RabbitMQ, MapStruct, and Swagger/OpenAPI.

## Tech Stack

- Java 21
- Spring Boot 3.3
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Redis
- RabbitMQ
- MapStruct
- springdoc-openapi
- Docker Compose

## Main API Modules

- Auth and current user
- Collection CRUD
- Vocabulary CRUD and collection vocabulary links
- AI vocabulary normalize
- Learning progress
- Review scheduler
- Flashcard, quiz, and typing practice
- Notifications
- Dashboard
- Public collections
- PDF export
- AI roleplay

Shadowing is currently scaffold-only in this worktree, so admin-only shadowing APIs are not available yet.

## Environment Variables

Create a local `.env` from `.env.example` and set production-like values before deployment.

Required for normal runtime:

```text
DATABASE_URL
DATABASE_USERNAME
DATABASE_PASSWORD
JWT_SECRET
```

Common optional variables:

```text
SPRING_PROFILES_ACTIVE
REDIS_HOST
REDIS_PORT
REDIS_PASSWORD
RABBITMQ_HOST
RABBITMQ_PORT
RABBITMQ_USERNAME
RABBITMQ_PASSWORD
MAIL_HOST
MAIL_PORT
MAIL_USERNAME
MAIL_PASSWORD
GROQ_API_KEY
GEMINI_API_KEY
DICTIONARY_API_BASE_URL
STORAGE_ENDPOINT
STORAGE_ACCESS_KEY
STORAGE_SECRET_KEY
STORAGE_BUCKET
APP_CORS_ALLOWED_ORIGINS
NOTIFICATION_EMAIL_LISTENER_ENABLED
NOTIFICATION_REVIEW_LINK
```

## Run Locally

Prerequisites:

- Java 21 for normal development
- PostgreSQL, Redis, and RabbitMQ running locally
- A `.env` or shell environment with the variables above

PowerShell:

```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/vocabverse"
$env:DATABASE_USERNAME="vocabverse"
$env:DATABASE_PASSWORD="<your-db-password>"
$env:JWT_SECRET="<at-least-32-byte-secret>"
.\mvnw.cmd spring-boot:run
```

The API runs at:

```text
http://localhost:8080/api/v1
```

## Run Tests

```powershell
.\mvnw.cmd validate
.\mvnw.cmd test
```

This repository includes Gradle build files, but no Gradle wrapper. Use Maven wrapper unless a local Gradle installation is available.

## Run With Docker

Create `.env`:

```powershell
Copy-Item .env.example .env
```

Set at least:

```text
DATABASE_PASSWORD=<your-db-password>
RABBITMQ_USERNAME=<your-rabbitmq-username>
RABBITMQ_PASSWORD=<your-rabbitmq-password>
JWT_SECRET=<at-least-32-byte-secret>
```

Start the stack:

```powershell
docker compose up --build
```

Stop the stack:

```powershell
docker compose down
```

Remove volumes:

```powershell
docker compose down -v
```

## Swagger

Swagger UI:

```text
http://localhost:8080/api/v1/swagger-ui.html
```

OpenAPI JSON:

```text
http://localhost:8080/api/v1/v3/api-docs
```

Use the Swagger `Authorize` button with the raw access token. Swagger UI adds the `Bearer` prefix automatically.

```text
<access_token>
```

## Production Notes

- Flyway runs on app startup.
- `spring.jpa.hibernate.ddl-auto=validate` prevents schema drift.
- All secrets are read from environment variables.
- CORS origins are configured with `APP_CORS_ALLOWED_ORIGINS`.
- Health endpoint: `GET /api/v1/actuator/health`.
