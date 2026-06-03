# AI_RULES.md

# VocabVerse AI Development Rules

## Project Overview

VocabVerse is an AI-powered English vocabulary learning platform.

Core features:

* Vocabulary Management
* Collection Management
* Flashcards
* Quiz
* Typing Practice
* Spaced Repetition
* Shadowing
* AI Roleplay
* Dashboard
* PDF Export

---

# Golden Rule

Before writing any code:

1. Read relevant documentation.
2. Follow architecture documents.
3. Do not make assumptions.
4. Do not modify unrelated files.
5. Keep changes minimal.
6. Prefer consistency over creativity.

---

# Mandatory Documents

Always follow:

docs/architecture/05-system-architecture.md

docs/engineering/09-backend-project-structure.md

docs/api/06-api-design.md

docs/database/04-database-design.md

---

# Forbidden Actions

Do NOT:

* create random folders
* create random utility classes
* change database schema without migration
* return entity directly
* hardcode secrets
* hardcode URLs
* introduce new frameworks
* change architecture without approval

---

# Backend Standards

Language:

Java 21

Framework:

Spring Boot 3

Database:

PostgreSQL

Migration:

Flyway

Authentication:

JWT

Caching:

Redis

Async:

RabbitMQ

---

# Entity Rules

Every entity must:

* use UUID
* use audit fields
* support soft delete when required

Example:

created_at

updated_at

deleted_at

---

# Controller Rules

Controller responsibilities:

* validate input
* call service
* return response

Controller must NOT:

* access repository
* call external APIs
* contain business logic

---

# Service Rules

Business logic belongs here.

Service may:

* validate business rules
* coordinate repositories
* call external clients

---

# Repository Rules

Repository only accesses database.

Repository must NOT:

* contain business logic
* call external services

---

# API Rules

All responses must use:

ApiResponse<T>

Success:

{
"success": true,
"message": "...",
"data": {}
}

Error:

{
"success": false,
"errorCode": "...",
"message": "..."
}

---

# Vocabulary Rules

A vocabulary may belong to multiple collections.

Never duplicate vocabulary records unnecessarily.

Use collection_vocabularies table.

---

# Collection Rules

Collection visibility:

PRIVATE

PUBLIC

SYSTEM

Only owner can modify collection.

---

# AI Normalize Rules

Output must follow schema.

Must validate JSON before saving.

Must retry failed parsing.

Maximum:

500 words per request.

---

# Roleplay Rules

Roleplay responses:

* stay in character
* respect difficulty
* include correction payload

Never break character.

---

# Spaced Repetition Rules

Intervals are configurable.

Do not hardcode intervals.

Email reminder only on review date.

---

# Security Rules

Never expose:

* password
* token
* api key

Use BCrypt.

Use JWT.

Validate ownership before modification.

---

# Testing Rules

Every service:

must have unit tests.

Every API:

must have integration tests.

---

# Logging Rules

Log:

* requestId
* userId
* latency

Never log:

* password
* token
* secret

---

# Before Creating New Code

Ask:

1. Does this already exist?
2. Can existing code be reused?
3. Does this follow architecture?
4. Does this violate any rule?

If unsure, stop and ask.
