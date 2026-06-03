# 02-prd.md

# TÀI LIỆU YÊU CẦU SẢN PHẨM (PRD)

## Dự án

VocabVerse - Nền tảng học từ vựng tiếng Anh tích hợp AI

---

# 1. THÔNG TIN TÀI LIỆU

| Thuộc tính    | Giá trị                      |
| ------------- | ---------------------------- |
| Tên sản phẩm  | VocabVerse                   |
| Phiên bản     | 1.0                          |
| Trạng thái    | Draft                        |
| Loại tài liệu | Product Requirement Document |
| Ngày cập nhật | 2026-06-02                   |

---

# 2. MỤC TIÊU SẢN PHẨM

## 2.1 Mục tiêu kinh doanh

Xây dựng một nền tảng học tiếng Anh tập trung vào:

* Thu thập từ vựng
* Quản lý từ vựng
* Ghi nhớ từ vựng
* Luyện nghe nói
* Theo dõi tiến độ học tập

Thông qua:

* AI
* Flashcard
* Spaced Repetition
* Shadowing
* AI Roleplay

---

## 2.2 Mục tiêu người dùng

Người dùng có thể:

* Tự xây dựng kho từ vựng cá nhân
* Học từ vựng hiệu quả
* Không quên từ đã học
* Có môi trường thực hành tiếng Anh

---

# 3. PHẠM VI DỰ ÁN

## Bao gồm

### Vocabulary Learning

* Tra cứu từ vựng
* Quản lý collection
* AI chuẩn hóa dữ liệu

### Learning Activities

* Flashcard
* Quiz
* Typing Practice

### Review System

* Spaced Repetition
* Email Reminder
* Browser Notification

### Speaking

* Shadowing
* AI Roleplay

---

## Không bao gồm V1

* Mobile App
* Thanh toán
* Subscription
* Livestream
* Video Call

---

# 4. USER PERSONA

## Persona 1

### Sinh viên đại học

Mục tiêu:

* TOEIC
* IELTS
* Phỏng vấn

Khó khăn:

* Quên từ vựng
* Không có kế hoạch học

---

## Persona 2

### Người tự học

Mục tiêu:

* Giao tiếp

Khó khăn:

* Không biết nên học gì
* Thiếu động lực

---

# 5. USER ROLE

## USER

Quyền:

* Tạo collection
* Học từ vựng
* Export PDF
* Shadowing
* Roleplay

---

## ADMIN

Quyền:

* Quản lý người dùng
* Tạo collection mẫu
* Upload video shadowing
* Quản lý nội dung hệ thống

---

# 6. MODULE 01 - QUẢN LÝ TỪ VỰNG

---

## Mục tiêu

Cho phép người dùng xây dựng kho từ vựng cá nhân.

---

## UC-01

### Tra cứu từ vựng

Người dùng nhập:

love

Hệ thống trả về:

* Phát âm
* Loại từ
* Nghĩa
* Ví dụ
* Đồng nghĩa
* Trái nghĩa

---

### User Story

Là một người học

Tôi muốn tra cứu từ vựng

Để hiểu rõ nghĩa và cách sử dụng từ đó

---

### Acceptance Criteria

* Tìm thấy từ
* Hiển thị phát âm
* Hiển thị nghĩa
* Hiển thị ví dụ

---

## UC-02

### Lưu từ vào Collection

Người dùng:

Add To Collection

Chọn:

TOEIC Words

---

### Kết quả

Từ được thêm vào collection.

---

## UC-03

### Tạo Collection

Người dùng tạo:

IELTS Speaking

---

Validation:

* Tên không được rỗng
* Tối đa 100 ký tự

---

# 7. MODULE 02 - IMPORT TỪ VỰNG

---

## Mục tiêu

Cho phép nhập từ vựng từ nhiều nguồn khác nhau.

---

## UC-04

### Import bằng Text

Ví dụ:

abandon
ability
achieve

---

Kết quả:

AI chuẩn hóa thành dữ liệu hệ thống.

---

## UC-05

### Import bằng JSON

Ví dụ:

[
{
"word":"abandon"
}
]

---

## UC-06

### Import bằng CSV

Ví dụ:

word,meaning
abandon,từ bỏ

---

## UC-07

### AI Normalize

Input:

abandon: từ bỏ
ability: khả năng

---

Output:

Dữ liệu chuẩn hóa.

---

### User Story

Là người học

Tôi muốn nhập dữ liệu ở bất kỳ định dạng nào

Để không phải tự format thủ công

---

### Acceptance Criteria

* Chấp nhận Text
* Chấp nhận JSON
* Chấp nhận CSV
* Chấp nhận dữ liệu không cấu trúc

---

# 8. MODULE 03 - FLASHCARD

---

## Mục tiêu

Học từ vựng bằng Flashcard.

---

## UC-08

### Học Flashcard

Mặt trước:

Word

Mặt sau:

* Nghĩa
* Ví dụ
* Đồng nghĩa

---

### Hành động

* Flip
* Next
* Previous

---

## UC-09

### Đánh dấu mức độ nhớ

Người dùng chọn:

* Dễ
* Trung bình
* Khó

---

Dữ liệu được dùng cho Spaced Repetition.

---

# 9. MODULE 04 - QUIZ

---

## Mục tiêu

Kiểm tra khả năng ghi nhớ.

---

## Loại Quiz

### Multiple Choice

4 đáp án

---

### Matching

Ghép từ với nghĩa

---

### Fill In Blank

Điền từ còn thiếu

---

## UC-10

### Bắt đầu Quiz

Người dùng chọn collection.

Hệ thống sinh:

10 câu hỏi.

---

### Kết quả

Hiển thị:

* Điểm
* Số câu đúng
* Số câu sai

---

# 10. MODULE 05 - TYPING PRACTICE

---

## Mục tiêu

Tăng khả năng nhớ chính tả.

---

## UC-11

### Gõ đáp án

Hiển thị:

Từ bỏ

Người dùng gõ:

abandon

---

Kết quả:

Đúng hoặc sai.

---

# 11. MODULE 06 - SPACED REPETITION

---

## Mục tiêu

Giúp người dùng ôn tập đúng thời điểm.

---

## UC-12

### Kích hoạt Spaced Repetition

Người dùng chọn:

Enable Review Schedule

---

Mặc định:

1
3
7
14
30

---

Người dùng có thể sửa.

---

## UC-13

### Email Reminder

Đến ngày ôn tập.

Hệ thống gửi:

Bạn có 25 từ cần ôn hôm nay.

---

## UC-14

### Browser Notification

Hiển thị:

Review TOEIC Collection Today

---

### Acceptance Criteria

* Tạo lịch ôn
* Gửi email đúng ngày
* Gửi notification đúng ngày

---

# 12. MODULE 07 - SHADOWING

---

## Mục tiêu

Luyện nghe và phát âm.

---

## UC-15

### Upload Video

Admin upload:

* MP4
* Youtube URL

---

## Hệ thống

Tự động:

* Extract Audio
* Speech To Text
* Generate English Subtitle
* Generate Vietnamese Subtitle

---

## UC-16

### Shadowing Practice

Hiển thị:

Video

Eng Sub

Viet Sub

---

Người dùng:

* Nghe
* Nhại lại

---

# 13. MODULE 08 - AI ROLEPLAY

---

## Mục tiêu

Tạo môi trường giao tiếp thực tế.

---

## UC-17

### Chọn chủ đề

Ví dụ:

* Restaurant
* Airport
* Shopping
* Interview

---

## UC-18

### Chọn độ khó

* Easy
* Medium
* Hard

---

## UC-19

### Chọn Persona

* Friendly
* Strict
* Professional
* Angry

---

## UC-20

### Bắt đầu hội thoại

AI tạo tình huống mới.

---

Ví dụ:

You are ordering food in a restaurant.

---

## UC-21

### Sửa lỗi

Sau mỗi tin nhắn:

AI hiển thị:

* Grammar Correction
* Better Expression

---

## UC-22

### Session Report

Sau khi kết thúc.

Hiển thị:

* New Vocabulary
* Mistakes
* Fluency
* Suggested Improvements

---

# 14. MODULE 09 - DASHBOARD

---

## UC-23

### Learning Statistics

Hiển thị:

* Total Words
* Learned Words
* Review Due Today
* Streak

---

## UC-24

### Collection Statistics

Hiển thị:

* Collection Count
* Public Collection Count

---

# 15. MODULE 10 - EXPORT PDF

---

## UC-25

### Export Vocabulary Table

Cột:

Word

Meaning

Synonym

Example

---

## UC-26

### Export Flashcard PDF

Mỗi từ:

Front Side

Back Side

---

# 16. CHIA SẺ COLLECTION

---

## UC-27

### Public Collection

Người dùng:

Publish Collection

---

Collection xuất hiện trong Community Library.

---

## UC-28

### Private Collection

Chỉ chủ sở hữu nhìn thấy.

---

# 17. KPI SẢN PHẨM

## KPI Học Tập

* Quiz Accuracy > 70%
* Daily Review Completion > 50%

---

## KPI Người Dùng

* DAU
* WAU
* MAU

---

## KPI Retention

* D1 Retention
* D7 Retention
* D30 Retention

---

END OF DOCUMENT
