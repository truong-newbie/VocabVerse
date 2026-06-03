You are a Senior Java Backend Engineer.

Project: VocabVerse Backend.

Read:

docs/ai/AI_RULES.md
docs/ai/BUSINESS_RULES.md
docs/ai/ARCHITECTURE_LOCK.md
docs/engineering/09-backend-project-structure.md
docs/architecture/04_database_design.md
docs/architecture/12-erd-diagram.md

Task:
Implement User Module foundation.

Scope:
src/main/java/com/vocabverse/user

Requirements:

Create:

- UserEntity
- UserRepository
- UserRole enum
- UserStatus enum

Entity fields:

id UUID

email

password

fullName

avatarUrl

role

status

createdAt

updatedAt

createdBy

updatedBy

Constraints:

- email unique
- email required
- password required
- role required
- status required

UserRole:

- USER
- ADMIN

UserStatus:

- ACTIVE
- INACTIVE
- LOCKED

Repository:

UserRepository extends JpaRepository

Methods:

Optional<UserEntity> findByEmail(String email)

boolean existsByEmail(String email)

Rules:

- Use JPA annotations
- Use UUID
- Use Lombok
- No business logic
- No controller
- No service
- No DTO
- No JWT
- No security yet

Output:

1. Files created
2. Explanation
3. Self review
4. Potential improvements