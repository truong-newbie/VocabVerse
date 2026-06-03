# 12-erd-diagram.md

# ENTITY RELATIONSHIP DIAGRAM

## Tổng quan

```text
USER
│
├── COLLECTION
│      │
│      └── COLLECTION_VOCABULARY
│                │
│                └── VOCABULARY
│
├── FLASHCARD_PROGRESS
│
├── QUIZ_SESSION
│      │
│      └── QUIZ_ANSWER
│
├── TYPING_SESSION
│      │
│      └── TYPING_ANSWER
│
├── REVIEW_SCHEDULE
│      │
│      └── REVIEW_SESSION
│
├── ROLEPLAY_SESSION
│      │
│      └── ROLEPLAY_MESSAGE
│
└── NOTIFICATION_LOG
```

---

# Vocabulary Knowledge Graph

Đề xuất nâng cấp V2:

```text
VOCABULARY

├── MEANINGS
│
├── EXAMPLES
│
├── SYNONYMS
│
├── ANTONYMS
│
├── COLLOCATIONS
│
├── PHRASAL_VERBS
│
├── COMMON_MISTAKES
│
└── MEMORY_TIPS
```

---

# Shadowing

```text
SHADOWING_VIDEO
│
└── SHADOWING_LINE
```

---

# Roleplay

```text
ROLEPLAY_SESSION
│
└── ROLEPLAY_MESSAGE
```

---

# AI

```text
AI_IMPORT_JOB

EXPORT_JOB

PROMPT_TEMPLATE
```

---

# Cardinality

## User → Collection

```text
1 : N
```

---

## Collection → Vocabulary

```text
N : N
```

---

## User → Review Schedule

```text
1 : N
```

---

## Review Schedule → Review Session

```text
1 : N
```

---

## User → Roleplay Session

```text
1 : N
```

---

## Roleplay Session → Message

```text
1 : N
```

---

END OF DOCUMENT
