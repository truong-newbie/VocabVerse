# 13-prompt-engineering-design.md

# THIẾT KẾ HỆ THỐNG PROMPT AI

## Dự án

VocabVerse - AI Vocabulary Learning Platform

---

# 1. MỤC TIÊU

Tài liệu này định nghĩa:

* Prompt Architecture
* Prompt Versioning
* Structured Output
* AI Fallback Strategy
* AI Cost Optimization

---

# 2. KIẾN TRÚC AI

Không được:

```text
Controller
↓
Groq API
```

Phải là:

```text
Controller
↓
Service
↓
Prompt Builder
↓
AI Client
↓
Response Parser
↓
Validator
```

---

# 3. AI MODULES

Hệ thống AI gồm:

```text
AI Normalize

AI Roleplay

AI Subtitle Translation

AI Grammar Correction

AI Session Report
```

---

# 4. AI NORMALIZE VOCABULARY

## Mục tiêu

Chuyển dữ liệu nhập tự do thành JSON chuẩn.

---

## Input

Ví dụ:

```text
abandon
ability
achieve
```

Hoặc:

```text
abandon: từ bỏ
ability: khả năng
```

---

## Output Schema

```json
[
  {
    "word": "",
    "phonetic": "",
    "partOfSpeech": "",
    "meaningVi": "",
    "meaningEn": "",
    "synonyms": [],
    "antonyms": [],
    "examples": []
  }
]
```

---

## System Prompt

```text
You are an English vocabulary normalization engine.

Your job is to convert raw vocabulary input into valid JSON.

Rules:

1. Return ONLY JSON.
2. Do not include markdown.
3. Do not include explanation.
4. Always follow provided schema.
5. Fill missing fields if possible.
6. Examples must contain English and Vietnamese.
7. Synonyms and antonyms must be arrays.
8. Output must be valid JSON.
```

---

## User Prompt

```text
Normalize the following vocabulary list:

{{raw_input}}
```

---

# 5. JSON VALIDATION

Sau khi AI trả về:

```text
Response
↓
JSON Parse
↓
Schema Validation
↓
Save Database
```

---

Nếu lỗi:

```text
Retry 1
↓
Retry 2
↓
Fail
```

---

# 6. SHADOWING TRANSLATION

## Mục tiêu

Dịch subtitle tiếng Anh sang tiếng Việt.

---

## Input

```json
{
  "englishText":
  "What would you like to order today?"
}
```

---

## Output

```json
{
  "vietnameseText":
  "Bạn muốn gọi món gì hôm nay?"
}
```

---

## System Prompt

```text
You are a professional subtitle translator.

Rules:

1. Keep meaning natural.
2. Do not translate word by word.
3. Use conversational Vietnamese.
4. Keep subtitle short.
5. Preserve tone.
6. Return JSON only.
```

---

## Bad Example

```text
What would you like to order today?

Bạn sẽ thích gọi món gì hôm nay?
```

---

## Good Example

```text
Bạn muốn gọi món gì hôm nay?
```

---

# 7. ROLEPLAY SYSTEM

Đây là phần quan trọng nhất.

---

# 8. ROLEPLAY ARCHITECTURE

Input:

```text
Topic

Difficulty

Persona

Conversation History
```

↓

AI

↓

Response

↓

Correction

---

# 9. ROLEPLAY PERSONA

Ví dụ:

```text
Friendly Waiter

Strict Interviewer

Airport Officer

Hotel Receptionist

Business Partner
```

---

# 10. SYSTEM PROMPT ROLEPLAY

```text
You are acting as a character in an English roleplay session.

Rules:

1. Stay in character.
2. Never break character.
3. Never explain grammar directly.
4. Respond naturally.
5. Use English only.
6. Adapt difficulty level.
7. Keep response concise.
```

---

# 11. DIFFICULTY LEVELS

## EASY

```text
Simple vocabulary

Short sentences

Slow conversation
```

---

## MEDIUM

```text
Intermediate vocabulary

Normal conversation
```

---

## HARD

```text
Advanced vocabulary

Idioms

Natural speed
```

---

# 12. CORRECTION ENGINE

Sau mỗi tin nhắn user.

AI trả thêm:

```json
{
  "grammarMistakes": [],
  "betterExpression": "",
  "explanation": ""
}
```

---

Ví dụ:

Input:

```text
I want eat pizza
```

Output:

```json
{
  "original": "I want eat pizza",
  "corrected": "I want to eat pizza",
  "betterExpression":
  "I'd like to order a pizza.",
  "explanation":
  "Sau want cần dùng to + verb."
}
```

---

# 13. SESSION REPORT

Sau khi kết thúc.

AI sinh:

```json
{
  "fluencyScore": 85,
  "grammarScore": 80,
  "vocabularyScore": 90,
  "mistakes": [],
  "newVocabulary": [],
  "recommendations": []
}
```

---

# 14. PROMPT VERSIONING

Không hardcode prompt.

Tạo:

```text
prompt_templates
```

---

Database:

```sql
CREATE TABLE prompt_templates (
    id UUID,
    code VARCHAR(100),
    version INT,
    prompt TEXT,
    active BOOLEAN
);
```

---

Ví dụ:

```text
VOCAB_NORMALIZE_V1

VOCAB_NORMALIZE_V2

ROLEPLAY_V1

ROLEPLAY_V2
```

---

# 15. COST OPTIMIZATION

## Cache

Redis:

```text
normalize_cache

translation_cache
```

---

## Không gọi AI lại nếu:

```text
Input giống hệt
```

---

Ví dụ:

```text
abandon
ability
achieve
```

đã xử lý rồi.

↓

Trả cache.

---

# 16. FALLBACK STRATEGY

Primary:

```text
Groq
```

Fallback:

```text
Gemini
```

---

Flow:

```text
Groq Fail
↓
Retry
↓
Retry
↓
Gemini
↓
Success
```

---

# 17. TOKEN LIMIT

Normalize:

```text
max 500 words/request
```

---

Roleplay:

```text
max 30 messages/context
```

---

Subtitle Translation:

```text
max 50 subtitle lines/request
```

---

# 18. SECURITY

Không gửi:

```text
password

token

email

api key
```

cho AI.

---

# 19. OBSERVABILITY

Log:

```json
{
  "promptType": "ROLEPLAY",
  "provider": "GROQ",
  "latencyMs": 1200,
  "tokens": 350,
  "success": true
}
```

---

# 20. KẾT LUẬN

AI Layer của VocabVerse gồm:

```text
Normalize Engine

Roleplay Engine

Translation Engine

Correction Engine

Report Engine
```

Tất cả phải:

```text
Structured Output

Schema Validation

Retry Logic

Fallback Provider

Prompt Versioning
```

để đảm bảo AI ổn định trong production.

END OF DOCUMENT
