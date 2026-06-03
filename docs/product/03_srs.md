# 03-srs.md

# SOFTWARE REQUIREMENT SPECIFICATION

## Dự án

VocabVerse - AI Vocabulary Learning Platform

---

# 1. THÔNG TIN TÀI LIỆU

| Thuộc tính    | Giá trị                            |
| ------------- | ---------------------------------- |
| Tên tài liệu  | Software Requirement Specification |
| Phiên bản     | 1.0                                |
| Dự án         | VocabVerse                         |
| Trạng thái    | Draft                              |
| Ngày cập nhật | 2026-06-02                         |

---

# 2. MỤC ĐÍCH

Tài liệu này mô tả toàn bộ yêu cầu kỹ thuật của hệ thống VocabVerse.

Mục tiêu:

* Hỗ trợ phân tích hệ thống
* Thiết kế database
* Thiết kế API
* Lập trình Backend
* Lập trình Frontend
* Kiểm thử phần mềm

---

# 3. PHẠM VI HỆ THỐNG

Hệ thống cho phép:

* Quản lý từ vựng
* Quản lý collection
* Học flashcard
* Làm quiz
* Typing practice
* Spaced repetition
* Shadowing
* AI roleplay
* Dashboard học tập

---

# 4. ĐỊNH NGHĨA THUẬT NGỮ

| Thuật ngữ       | Ý nghĩa                                   |
| --------------- | ----------------------------------------- |
| Collection      | Bộ từ vựng                                |
| Vocabulary      | Từ vựng                                   |
| Flashcard       | Thẻ học                                   |
| Quiz            | Bài kiểm tra                              |
| Review Schedule | Lịch ôn tập                               |
| Shadowing       | Phương pháp luyện nghe và nhại lại        |
| Roleplay        | Hội thoại mô phỏng                        |
| AI Normalize    | Chuyển dữ liệu tự do thành cấu trúc chuẩn |

---

# 5. KIẾN TRÚC TỔNG QUAN

```text
Frontend (React)
        ↓
REST API
        ↓
Spring Boot
        ↓
PostgreSQL

Redis
RabbitMQ

AI Service

Dictionary Service

Notification Service
```

---

# 6. PHÂN QUYỀN

## USER

Có quyền:

* Quản lý collection
* Quản lý từ vựng
* Học tập
* Shadowing
* Roleplay

---

## ADMIN

Có quyền:

* Quản lý user
* Quản lý collection hệ thống
* Upload video
* Quản lý nội dung

---

# 7. FUNCTIONAL REQUIREMENTS

---

# MODULE AUTHENTICATION

## FR-AUTH-001

Người dùng phải có thể đăng ký tài khoản.

Input:

```json
{
  "email": "user@gmail.com",
  "password": "12345678"
}
```

Validation:

* Email hợp lệ
* Email chưa tồn tại
* Password >= 8 ký tự

---

## FR-AUTH-002

Người dùng phải có thể đăng nhập.

Input:

```json
{
  "email": "user@gmail.com",
  "password": "12345678"
}
```

Output:

```json
{
  "accessToken": "...",
  "refreshToken": "..."
}
```

---

## FR-AUTH-003

Hệ thống phải hỗ trợ Refresh Token.

---

## FR-AUTH-004

Hệ thống phải hỗ trợ Logout.

---

# MODULE VOCABULARY

## FR-VOCAB-001

Người dùng có thể tìm kiếm từ vựng.

Input:

```text
abandon
```

Output:

```json
{
  "word": "abandon",
  "meaningVi": "...",
  "phonetic": "...",
  "examples": []
}
```

---

## FR-VOCAB-002

Người dùng có thể lưu từ vào collection.

---

## FR-VOCAB-003

Một từ có thể thuộc nhiều collection.

Ví dụ:

```text
abandon
```

thuộc:

```text
TOEIC
IELTS
My Collection
```

---

## FR-VOCAB-004

Hệ thống phải ngăn duplicate word trong cùng collection.

---

## FR-VOCAB-005

Người dùng có thể chỉnh sửa từ vựng đã tạo thủ công.

---

# MODULE COLLECTION

## FR-COL-001

Người dùng có thể tạo collection.

---

## FR-COL-002

Người dùng có thể sửa collection.

---

## FR-COL-003

Người dùng có thể xóa collection.

---

## FR-COL-004

Collection có trạng thái:

```text
PRIVATE
PUBLIC
SYSTEM
```

---

## FR-COL-005

Collection public phải xuất hiện trong Community Library.

---

# MODULE AI NORMALIZATION

## FR-AI-001

Người dùng có thể nhập text tự do.

Ví dụ:

```text
abandon
ability
achieve
```

---

## FR-AI-002

Hệ thống phải chuyển đổi dữ liệu thành JSON chuẩn.

Schema:

```json
{
  "word": "",
  "phonetic": "",
  "meaningVi": "",
  "meaningEn": "",
  "synonyms": [],
  "antonyms": [],
  "examples": []
}
```

---

## FR-AI-003

AI phải xử lý tối thiểu:

* Text
* CSV
* JSON

---

## FR-AI-004

Nếu AI không parse được dữ liệu.

Hệ thống phải trả về:

```json
{
  "error": "INVALID_INPUT_FORMAT"
}
```

---

# MODULE FLASHCARD

## FR-FLASH-001

Người dùng có thể học theo flashcard.

---

## FR-FLASH-002

Flashcard phải hỗ trợ:

* Next
* Previous
* Flip

---

## FR-FLASH-003

Người dùng đánh dấu:

```text
EASY
MEDIUM
HARD
```

---

## FR-FLASH-004

Kết quả flashcard phải được lưu.

---

# MODULE QUIZ

## FR-QUIZ-001

Người dùng có thể tạo quiz từ collection.

---

## FR-QUIZ-002

Hệ thống hỗ trợ:

```text
MULTIPLE_CHOICE
MATCHING
FILL_BLANK
```

---

## FR-QUIZ-003

Số lượng câu hỏi mặc định:

```text
10
```

---

## FR-QUIZ-004

Quiz result phải được lưu.

---

# MODULE TYPING PRACTICE

## FR-TYPE-001

Người dùng nhập đáp án bằng bàn phím.

---

## FR-TYPE-002

Hệ thống kiểm tra:

* Chính xác
* Không phân biệt hoa thường

---

## FR-TYPE-003

Kết quả phải được ghi nhận.

---

# MODULE SPACED REPETITION

## FR-SR-001

Người dùng có thể bật spaced repetition.

---

## FR-SR-002

Người dùng có thể tùy chỉnh khoảng cách ngày.

Ví dụ:

```json
{
  "intervals": [1,3,7,14,30]
}
```

---

## FR-SR-003

Hệ thống phải sinh lịch review.

---

## FR-SR-004

Đến ngày review.

Hệ thống phải tạo review session.

---

## FR-SR-005

Hệ thống phải gửi email reminder.

---

## FR-SR-006

Hệ thống phải gửi browser notification.

---

## FR-SR-007

Không gửi trùng notification.

---

# MODULE SHADOWING

## FR-SHADOW-001

Admin có thể upload video.

Hỗ trợ:

```text
MP4
YOUTUBE_URL
```

---

## FR-SHADOW-002

Hệ thống phải tự động:

* Extract audio
* Generate transcript

---

## FR-SHADOW-003

Hệ thống phải tự động sinh English Subtitle.

---

## FR-SHADOW-004

Hệ thống phải tự động sinh Vietnamese Subtitle.

---

## FR-SHADOW-005

Transcript phải được chia theo từng câu.

---

## FR-SHADOW-006

Người dùng có thể replay từng câu.

---

## FR-SHADOW-007

Người dùng có thể ghi âm lại.

(V2)

---

# MODULE ROLEPLAY

## FR-ROLE-001

Người dùng có thể tạo roleplay session.

---

## FR-ROLE-002

Roleplay phải hỗ trợ:

```text
Topic
Difficulty
Persona
```

---

## FR-ROLE-003

AI phải tự sinh tình huống.

---

## FR-ROLE-004

AI phải duy trì ngữ cảnh hội thoại.

---

## FR-ROLE-005

AI phải sửa lỗi ngữ pháp.

---

## FR-ROLE-006

AI phải đề xuất câu tốt hơn.

---

## FR-ROLE-007

Kết thúc session phải sinh report.

---

# MODULE DASHBOARD

## FR-DASH-001

Hiển thị:

* Total Words
* Learned Words
* Review Due Today

---

## FR-DASH-002

Hiển thị streak.

---

## FR-DASH-003

Hiển thị accuracy.

---

# MODULE PDF EXPORT

## FR-PDF-001

Người dùng có thể export PDF.

---

## FR-PDF-002

PDF phải hiển thị:

```text
Word
Meaning
Synonym
Example
```

---

# 8. NON FUNCTIONAL REQUIREMENTS

## NFR-001 Performance

95% API phải phản hồi dưới:

```text
500ms
```

---

## NFR-002 Security

Password phải được mã hóa bằng:

```text
BCrypt
```

---

## NFR-003 Authentication

Sử dụng:

```text
JWT
```

---

## NFR-004 Availability

Mục tiêu:

```text
99.9%
```

---

## NFR-005 Scalability

Hệ thống phải hỗ trợ scale ngang.

---

## NFR-006 Logging

Tất cả API phải được log.

---

## NFR-007 Monitoring

Theo dõi:

* CPU
* RAM
* Error Rate

---

# 9. BUSINESS RULES

BR-001

Collection phải có chủ sở hữu.

---

BR-002

Vocabulary có thể thuộc nhiều collection.

---

BR-003

Collection SYSTEM không được xóa bởi User.

---

BR-004

Review Schedule phải thuộc một Collection.

---

BR-005

Email reminder chỉ gửi đúng ngày review.

---

BR-006

Collection PRIVATE không được hiển thị công khai.

---

# 10. ERROR CODES

AUTH_INVALID_CREDENTIAL

AUTH_TOKEN_EXPIRED

VOCAB_NOT_FOUND

COLLECTION_NOT_FOUND

DUPLICATE_WORD

INVALID_INPUT_FORMAT

AI_PROCESSING_FAILED

ROLEPLAY_SESSION_NOT_FOUND

VIDEO_PROCESSING_FAILED

PDF_GENERATION_FAILED

---

END OF DOCUMENT
