# 08-non-functional-requirements.md

# YÊU CẦU PHI CHỨC NĂNG

## Dự án

VocabVerse - AI Vocabulary Learning Platform

---

# 1. Mục tiêu

Tài liệu này mô tả các yêu cầu phi chức năng của hệ thống, bao gồm:

* Hiệu năng
* Bảo mật
* Khả năng mở rộng
* Độ ổn định
* Logging
* Monitoring
* Backup
* Rate limiting
* Maintainability

---

# 2. Performance Requirements

## NFR-PERF-001

95% API thông thường phải phản hồi dưới:

```text
500ms
```

Áp dụng cho:

```text
Login
Get collections
Get vocabularies
Dashboard summary
```

---

## NFR-PERF-002

Các tác vụ nặng phải xử lý async.

Bao gồm:

```text
AI normalize vocabulary
PDF export
Shadowing video processing
Email notification
Roleplay report generation
```

---

## NFR-PERF-003

Dictionary lookup phải cache bằng Redis.

```text
TTL: 7 ngày
```

---

# 3. Security Requirements

## NFR-SEC-001

Password phải được mã hóa bằng BCrypt.

---

## NFR-SEC-002

Authentication sử dụng JWT.

```text
Access Token
Refresh Token
```

---

## NFR-SEC-003

API phải phân quyền theo role:

```text
USER
ADMIN
```

---

## NFR-SEC-004

User không được truy cập tài nguyên của user khác.

Ví dụ:

```text
User A không được sửa collection của User B
```

---

## NFR-SEC-005

Không được trả stack trace ra client.

Sai:

```json
{
  "error": "NullPointerException..."
}
```

Đúng:

```json
{
  "success": false,
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "Unexpected server error"
}
```

---

# 4. Rate Limiting

## NFR-RATE-001

Các API tốn chi phí phải giới hạn request.

| API               |     Limit đề xuất |
| ----------------- | ----------------: |
| AI Normalize      |  10 lần/ngày/user |
| Roleplay Message  |  30 tin/ngày/user |
| Dictionary Search | 100 lần/ngày/user |
| PDF Export        |   5 lần/ngày/user |

---

## NFR-RATE-002

Rate limit nên lưu bằng Redis.

Key ví dụ:

```text
rate_limit:ai_normalize:{userId}:{date}
```

---

# 5. Availability Requirements

## NFR-AVA-001

Mục tiêu availability:

```text
99.5% cho MVP
99.9% cho production ổn định
```

---

## NFR-AVA-002

Nếu AI Provider lỗi, hệ thống không được crash.

Phải trả lỗi kiểm soát:

```json
{
  "success": false,
  "errorCode": "AI_PROVIDER_UNAVAILABLE",
  "message": "AI service is temporarily unavailable"
}
```

---

# 6. Scalability Requirements

## NFR-SCALE-001

Backend API phải có thể scale ngang.

```text
api-instance-1
api-instance-2
api-instance-3
```

---

## NFR-SCALE-002

Worker phải scale độc lập với API.

```text
worker-ai x2
worker-notification x1
worker-export x1
```

---

# 7. Logging Requirements

## NFR-LOG-001

Mỗi request phải có request_id.

Log format:

```json
{
  "requestId": "uuid",
  "userId": "uuid",
  "method": "POST",
  "path": "/api/v1/collections",
  "status": 200,
  "latencyMs": 123
}
```

---

## NFR-LOG-002

Không log dữ liệu nhạy cảm.

Không log:

```text
password
accessToken
refreshToken
API key
```

---

# 8. Monitoring Requirements

Theo dõi:

```text
CPU
RAM
Disk
Database connections
API latency
Error rate
RabbitMQ queue depth
Redis memory
AI failure rate
Email failure rate
```

Công cụ đề xuất:

```text
Prometheus
Grafana
Sentry
```

---

# 9. Backup Requirements

## NFR-BACKUP-001

Database production phải backup hằng ngày.

```text
Retention: 7-30 ngày
```

---

## NFR-BACKUP-002

File storage cần backup hoặc dùng object storage có độ bền cao.

Áp dụng cho:

```text
PDF export
Video upload
Avatar
Thumbnail
```

---

# 10. Maintainability Requirements

## NFR-MAIN-001

Code backend phải chia module rõ ràng.

```text
auth
user
collection
vocabulary
learning
review
notification
shadowing
roleplay
ai
export
```

---

## NFR-MAIN-002

Không gọi external API trực tiếp trong controller.

Sai:

```text
Controller → Groq API
```

Đúng:

```text
Controller → Service → AiClient → Groq API
```

---

## NFR-MAIN-003

Tất cả business exception phải dùng error code chuẩn.

Ví dụ:

```text
COLLECTION_NOT_FOUND
DUPLICATE_WORD
AI_PROCESSING_FAILED
```

---

# 11. Data Integrity Requirements

## NFR-DATA-001

Không được có duplicate vocabulary trong cùng collection.

---

## NFR-DATA-002

Xóa collection không được xóa vocabulary gốc.

Chỉ xóa quan hệ hoặc soft delete collection.

---

## NFR-DATA-003

Review notification không được gửi trùng.

Cần kiểm tra `notification_logs`.

---

# 12. AI Safety Requirements

## NFR-AI-001

AI output phải validate bằng JSON Schema.

---

## NFR-AI-002

Nếu AI trả sai format, hệ thống phải retry tối đa:

```text
2 lần
```

---

## NFR-AI-003

Input gửi cho AI phải giới hạn độ dài.

Đề xuất:

```text
10.000 ký tự/lần import
```

---

# 13. File Upload Requirements

## NFR-FILE-001

Giới hạn file upload.

| Loại file | Max size |
| --------- | -------: |
| Avatar    |      2MB |
| PDF       |     10MB |
| Video     |    200MB |
| CSV       |      5MB |

---

## NFR-FILE-002

Không dùng tên file gốc để lưu.

Đúng:

```text
uuid-generated-file-name.mp4
```

---

# 14. Browser Compatibility

Hỗ trợ:

```text
Chrome latest
Edge latest
Firefox latest
Safari latest
```

---

# 15. Kết luận

Các yêu cầu phi chức năng này giúp VocabVerse không chỉ chạy được, mà còn:

* An toàn hơn
* Dễ mở rộng hơn
* Dễ maintain hơn
* Dễ debug hơn
* Có phong cách giống dự án doanh nghiệp

END OF DOCUMENT
