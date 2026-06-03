# BUSINESS_RULES.md

# Vocabulary

A vocabulary can belong to multiple collections.

Do NOT duplicate vocabulary records unnecessarily.

Use:

collection_vocabularies

for many-to-many relation.

---

# Collection

Collection types:

* PRIVATE
* PUBLIC
* SYSTEM

Rules:

* Owner can modify PRIVATE collection.
* Owner can publish collection.
* SYSTEM collection cannot be deleted by users.
* PUBLIC collection can be saved by other users.

---

# Vocabulary Import

User can import:

* TEXT
* CSV
* JSON

AI Normalize converts input into standard schema.

All AI output must pass validation.

---

# Flashcard

Each flashcard result updates learning progress.

Memory Levels:

* NEW
* HARD
* MEDIUM
* EASY
* MASTERED

---

# Quiz

Quiz result must be stored.

Accuracy contributes to dashboard statistics.

---

# Spaced Repetition

Intervals are configurable.

Default:

1
3
7
14
30

But user may change them.

Email reminder only sends on review date.

Browser notification only sends on review date.

No duplicate notifications.

---

# Shadowing

Admin uploads:

* MP4
* YouTube URL

System automatically:

* Extract audio
* Generate transcript
* Generate English subtitle
* Generate Vietnamese subtitle

Admin does not manually write subtitles.

---

# Roleplay

AI generates scenario automatically.

AI generates correction automatically.

AI generates report automatically.

Roleplay must support:

* Topic
* Difficulty
* Persona

---

# Dashboard

Must display:

* Total words
* Learned words
* Review due today
* Streak
* Accuracy

---

# Export PDF

PDF must contain:

Word

Meaning

Synonym

Example
