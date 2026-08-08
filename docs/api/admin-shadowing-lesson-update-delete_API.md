# Admin Shadowing Lesson: Update & Soft Delete

Tai lieu nay mo ta 2 chuc nang moi cho man hinh Admin Shadowing Lesson:

- Chinh sua thong tin lesson shadowing.
- Xoa mem lesson shadowing trong database, giu nguyen asset tren Cloudinary.

## Thong tin chung

Base URL:

```text
/api/v1/admin/shadowing/lessons
```

Yeu cau:

- Can dang nhap.
- User phai co role `ADMIN`.
- Header:

```http
Authorization: Bearer <access_token>
Content-Type: application/json
```

Response wrapper chung:

```json
{
  "success": true,
  "message": "Success",
  "data": {}
}
```

## 1. Chinh sua thong tin lesson shadowing

### Endpoint

```http
PATCH /api/v1/admin/shadowing/lessons/{lessonId}
```

### Muc dich

Cho phep admin chinh sua metadata cua lesson shadowing da upload/import.

Hien tai chi cho sua:

- `title`
- `description`

Khong cho sua cac field ky thuat:

- `videoUrl`
- `cloudinaryPublicId`
- `thumbnailUrl`
- `source`
- `status`
- `duration`
- `createdBy`

### Request body

```json
{
  "title": "Daily English Conversation",
  "description": "Short conversation practice for beginners"
}
```

### Field rules

| Field | Type | Required | Rule |
| --- | --- | --- | --- |
| `title` | string | No | Toi da 200 ky tu. Neu gui len thi khong duoc blank |
| `description` | string | No | Toi da 5000 ky tu. Gui `null` hoac blank de xoa mo ta |

### Vi du chi sua title

```json
{
  "title": "Daily English Conversation"
}
```

### Vi du xoa description

```json
{
  "description": ""
}
```

### Response success

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": "b3b9f5e0-6b1d-4d15-a99e-1a9d2c123456",
    "source": "UPLOAD",
    "status": "COMPLETED",
    "title": "Daily English Conversation",
    "description": null,
    "originalFilename": "conversation.mp4",
    "videoUrl": "https://res.cloudinary.com/...",
    "thumbnailUrl": "https://res.cloudinary.com/...",
    "cloudinaryPublicId": "vocabverse/shadowing/xxx",
    "storageProvider": "CLOUDINARY",
    "contentType": "video/mp4",
    "fileSize": 12345678,
    "duration": "03:20",
    "progress": 100,
    "subtitleCount": 12,
    "errorMessage": null,
    "createdAt": "2026-08-08T10:00:00",
    "updatedAt": "2026-08-08T10:30:00"
  }
}
```

### FE behavior de xuat

- Sau khi update thanh cong, FE co the update lai row/card hien tai bang object trong `data`.
- Nen trim input o phia FE truoc khi submit.
- Neu `title` rong sau khi trim, nen chan submit va hien validation message.
- Neu lesson da bi xoa mem, API se tra not found.

## 2. Xoa mem lesson shadowing

### Endpoint

```http
DELETE /api/v1/admin/shadowing/lessons/{lessonId}
```

### Muc dich

Admin xoa mem lesson shadowing khoi he thong.

Backend chi set:

```text
shadowing_lessons.deleted_at = current timestamp
```

Backend khong xoa:

- File/video/audio tren Cloudinary.
- `cloudinaryPublicId`.
- `videoUrl`.
- `thumbnailUrl`.
- Subtitle trong database.

Lesson sau khi xoa mem se khong con xuat hien trong:

- Admin lesson list.
- Public lesson list.
- Public lesson detail.
- Admin status/subtitle/update/generate subtitle APIs.
- Admin dashboard shadowing lesson count.

### Response success

```json
{
  "success": true,
  "message": "Delete shadowing lesson successfully",
  "data": null
}
```

### FE behavior de xuat

- Hien confirm modal truoc khi goi API.
- Sau khi xoa thanh cong, remove lesson khoi danh sach hien tai hoac refetch list.
- Khong can goi API nao de xoa Cloudinary.

Goi y confirm text:

```text
Ban co chac muon xoa lesson nay khoi danh sach? Video tren Cloudinary se duoc giu nguyen.
```

## Error cases can xu ly

### Lesson khong ton tai hoac da bi xoa mem

```json
{
  "success": false,
  "message": "Shadowing lesson not found",
  "errorCode": "SHADOWING_LESSON_NOT_FOUND",
  "errors": []
}
```

### Khong co quyen admin

```json
{
  "success": false,
  "message": "Access denied",
  "errorCode": "ACCESS_DENIED",
  "errors": []
}
```

### Title blank khi update

```json
{
  "success": false,
  "message": "Lesson title must not be blank",
  "errorCode": "INVALID_INPUT",
  "errors": []
}
```

### Validation loi do dai field

```json
{
  "success": false,
  "message": "Validation error",
  "errorCode": "VALIDATION_ERROR",
  "errors": [
    {
      "field": "title",
      "message": "size must be between 0 and 200"
    }
  ]
}
```

## Summary cho FE

Can them 2 action o man Admin Shadowing Lesson:

```text
Edit lesson:
PATCH /api/v1/admin/shadowing/lessons/{lessonId}

Delete lesson:
DELETE /api/v1/admin/shadowing/lessons/{lessonId}
```

Payload edit:

```json
{
  "title": "string",
  "description": "string"
}
```

Delete la xoa mem. FE chi can remove item khoi UI sau khi success.
