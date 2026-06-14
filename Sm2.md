# Plan triển khai Sm2Scheduler cho VocabVerse

## 1. Mục tiêu

Hiện tại hệ thống review của VocabVerse đang dùng cơ chế interval cố định:

- Collection có `intervalsJson`, mặc định `[1, 3, 7, 14, 30]`.
- Khi user submit review, `nextReviewAt` được tính bằng cách lấy số ngày từ interval list hoặc từ `SimpleReviewIntervalStrategy`.
- User chưa chọn được thuật toán review.

Mục tiêu của phase này:

- Giữ nguyên mode hiện tại dưới tên `FIXED_INTERVAL`.
- Thêm mode mới `SM2`.
- Thiết kế kiến trúc để sau này thêm `FSRS` mà không phá API/service hiện tại.
- Cho user chọn scheduler mode ở cấp collection review setting.
- Khi collection dùng `SM2`, `nextReviewAt` phải được tính theo thuật toán SM-2 đơn giản.
- Không implement FSRS trong phase này, chỉ chuẩn bị enum/interface/database field để thêm sau.

## 2. Phạm vi phase SM-2

Trong phase này cần làm:

- Thêm enum `ReviewSchedulerType` gồm:
  - `FIXED_INTERVAL`
  - `SM2`
  - `FSRS`
- Thêm field `schedulerType` vào `CollectionReviewSettingEntity`.
- Thêm migration DB để thêm column `scheduler_type`.
- Update request/response DTO của collection review setting để FE có thể đọc và chỉnh mode.
- Refactor scheduler logic hiện tại thành strategy selector.
- Đổi `SimpleReviewIntervalStrategy` hiện tại thành `FixedIntervalScheduler` hoặc giữ class cũ nhưng đăng ký là mode `FIXED_INTERVAL`.
- Thêm `Sm2Scheduler`.
- Update `ReviewService` để chọn scheduler theo collection setting.
- Viết unit test cho SM-2.
- Viết integration/service test cho luồng submit review.

Không làm trong phase này:

- Không implement công thức FSRS thật.
- Không tối ưu tham số SM-2 theo user.
- Không làm dashboard thống kê thuật toán.
- Không thay đổi toàn bộ UI review, ngoài việc FE có thể chọn scheduler mode nếu cần.

## 3. Code hiện tại cần hiểu trước khi làm

Các file chính:

- `src/main/java/com/vocabverse/review/service/ReviewService.java`
  - Method `submitReview(...)`
  - Method `updateProgress(...)`
  - Method `calculateNextReviewDate(...)`
  - Method `resolveCollectionIntervalDays(...)`
- `src/main/java/com/vocabverse/review/strategy/ReviewIntervalStrategy.java`
- `src/main/java/com/vocabverse/review/strategy/SimpleReviewIntervalStrategy.java`
- `src/main/java/com/vocabverse/collection/entity/CollectionReviewSettingEntity.java`
- `src/main/java/com/vocabverse/collection/service/CollectionReviewSettingService.java`
- `src/main/java/com/vocabverse/collection/dto/request/UpdateCollectionReviewSettingRequest.java`
- `src/main/java/com/vocabverse/collection/dto/response/CollectionReviewSettingResponse.java`
- `src/main/java/com/vocabverse/learning/progress/entity/LearningProgressEntity.java`

Vấn đề hiện tại:

- `ReviewService` vừa update status/repetition, vừa tính interval.
- Interface `ReviewIntervalStrategy` chỉ trả `LocalDateTime`, nên hơi thiếu thông tin nếu sau này thêm FSRS.
- `easeFactor` đã tồn tại trong `LearningProgressEntity`, rất phù hợp để dùng cho SM-2.
- `LearningProgressEntity` chưa có `lastIntervalDays`, `lapseCount`, `reviewCount`. Với SM-2 phase đầu, có thể chưa bắt buộc thêm hết, nhưng nên thêm `lastIntervalDays` để tính interval tốt hơn và phục vụ debug.

## 4. Thiết kế dữ liệu

### 4.1. Thêm enum scheduler type

Tạo file:

```text
src/main/java/com/vocabverse/review/strategy/ReviewSchedulerType.java
```

Nội dung:

```java
package com.vocabverse.review.strategy;

public enum ReviewSchedulerType {
    FIXED_INTERVAL,
    SM2,
    FSRS
}
```

Ghi chú:

- `FSRS` được thêm vào enum ngay từ phase này để API không phải đổi nhiều ở phase sau.
- Nếu chưa implement FSRS, service phải reject hoặc fallback rõ ràng khi user chọn `FSRS`.

Khuyến nghị phase này:

- Cho phép lưu `FSRS` trong enum nhưng chưa cho update sang `FSRS`, trả lỗi `INVALID_INPUT` hoặc `SCHEDULER_NOT_SUPPORTED`.
- Nếu muốn đơn giản hơn, vẫn có thể cho update nhưng khi review thì fallback về `SM2` hoặc `FIXED_INTERVAL`. Tuy nhiên cách này dễ gây hiểu nhầm, không khuyến nghị.

### 4.2. Update collection_review_settings

Thêm field vào `CollectionReviewSettingEntity`:

```java
@Enumerated(EnumType.STRING)
@Column(name = "scheduler_type", nullable = false, length = 30)
private ReviewSchedulerType schedulerType;
```

Default:

```java
ReviewSchedulerType.FIXED_INTERVAL
```

Trong `findOrDefault(...)` của `CollectionReviewSettingService`, set:

```java
.schedulerType(ReviewSchedulerType.FIXED_INTERVAL)
```

### 4.3. Migration DB

Tạo migration tiếp theo, ví dụ:

```text
src/main/resources/db/migration/V16__add_review_scheduler_type.sql
```

SQL:

```sql
alter table collection_review_settings
    add column scheduler_type varchar(30) not null default 'FIXED_INTERVAL';
```

Nếu muốn chuẩn hơn với PostgreSQL:

```sql
alter table collection_review_settings
    add constraint chk_collection_review_scheduler_type
    check (scheduler_type in ('FIXED_INTERVAL', 'SM2', 'FSRS'));
```

Lưu ý:

- Nếu thêm check constraint có `FSRS`, sau này không cần đổi DB khi implement FSRS.
- Existing records sẽ tự nhận `FIXED_INTERVAL`.

### 4.4. Có nên thêm field vào learning_progress không?

Hiện đã có:

```text
repetition_count
ease_factor
next_review_at
last_reviewed_at
```

SM-2 có thể chạy với các field này. Tuy nhiên nên thêm:

```text
last_interval_days integer not null default 0
lapse_count integer not null default 0
review_count integer not null default 0
```

Khuyến nghị:

- Phase SM-2 nên thêm `lastIntervalDays` tối thiểu.
- `reviewCount` và `lapseCount` rất hữu ích cho thống kê và FSRS sau này.

Migration đề xuất:

```text
src/main/resources/db/migration/V17__add_sm2_progress_fields.sql
```

SQL:

```sql
alter table learning_progress
    add column last_interval_days integer not null default 0,
    add column lapse_count integer not null default 0,
    add column review_count integer not null default 0;
```

Update `LearningProgressEntity`:

```java
@Column(name = "last_interval_days", nullable = false)
private int lastIntervalDays;

@Column(name = "lapse_count", nullable = false)
private int lapseCount;

@Column(name = "review_count", nullable = false)
private int reviewCount;
```

Builder khi tạo progress mới:

```java
.lastIntervalDays(0)
.lapseCount(0)
.reviewCount(0)
```

## 5. API contract

### 5.1. Update request DTO

File:

```text
src/main/java/com/vocabverse/collection/dto/request/UpdateCollectionReviewSettingRequest.java
```

Thêm field:

```java
ReviewSchedulerType schedulerType
```

Validation:

- `null`: không đổi scheduler.
- `FIXED_INTERVAL`: hợp lệ.
- `SM2`: hợp lệ.
- `FSRS`: phase này chưa support, trả lỗi.

Logic trong service:

```java
if (request.schedulerType() != null) {
    if (request.schedulerType() == ReviewSchedulerType.FSRS) {
        throw new BusinessException(ErrorCode.SCHEDULER_NOT_SUPPORTED);
    }
    setting.setSchedulerType(request.schedulerType());
}
```

Nếu chưa muốn thêm `ErrorCode.SCHEDULER_NOT_SUPPORTED`, có thể dùng `INVALID_INPUT`, nhưng nên thêm error code riêng để FE dễ xử lý.

### 5.2. Update response DTO

File:

```text
src/main/java/com/vocabverse/collection/dto/response/CollectionReviewSettingResponse.java
```

Thêm field:

```java
ReviewSchedulerType schedulerType
```

Response example:

```json
{
  "id": "uuid",
  "collectionId": "uuid",
  "enabled": true,
  "emailEnabled": true,
  "schedulerType": "SM2",
  "intervals": [1, 3, 7, 14, 30],
  "reminderTime": "08:00:00",
  "timezone": "Asia/Ho_Chi_Minh",
  "lastResetAt": null,
  "createdAt": "2026-06-14T10:00:00",
  "updatedAt": "2026-06-14T10:00:00"
}
```

### 5.3. FE behavior đề xuất

FE có thể hiển thị select:

```text
Review algorithm:
- Fixed interval
- SM-2
- FSRS (coming soon / disabled)
```

Khi chọn `FIXED_INTERVAL`:

- Hiển thị interval list `[1, 3, 7, 14, 30]`.

Khi chọn `SM2`:

- Có thể ẩn interval list hoặc giữ nhưng ghi rõ không dùng cho SM-2.
- Phase đầu chưa cần thêm option nâng cao.

Khi chọn `FSRS`:

- Disable hoặc hiển thị coming soon.

## 6. Refactor scheduler architecture

### 6.1. Tạo output object cho scheduler

Tạo file:

```text
src/main/java/com/vocabverse/review/strategy/ReviewScheduleResult.java
```

Nội dung đề xuất:

```java
package com.vocabverse.review.strategy;

import com.vocabverse.learning.progress.entity.LearningStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReviewScheduleResult(
        LearningStatus status,
        int repetitionCount,
        BigDecimal easeFactor,
        int lastIntervalDays,
        int lapseCount,
        int reviewCount,
        LocalDateTime nextReviewAt
) {
}
```

Lý do:

- Scheduler không chỉ tính `nextReviewAt`.
- SM-2 cần update `easeFactor`, `repetitionCount`, `lastIntervalDays`.
- FSRS sau này sẽ cần update thêm `difficulty`, `stability`, `retrievability`.

Nếu muốn giảm scope phase này:

- Có thể giữ `ReviewIntervalStrategy` trả `LocalDateTime`, và update `easeFactor` trong `ReviewService`.
- Nhưng không khuyến nghị vì FSRS sau này sẽ phải refactor lại.

### 6.2. Tạo interface mới

Tạo file:

```text
src/main/java/com/vocabverse/review/strategy/ReviewScheduler.java
```

Nội dung:

```java
package com.vocabverse.review.strategy;

import com.vocabverse.collection.entity.CollectionReviewSettingEntity;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.review.entity.ReviewResult;
import java.time.LocalDateTime;

public interface ReviewScheduler {

    ReviewSchedulerType type();

    ReviewScheduleResult schedule(
            LearningProgressEntity progress,
            ReviewResult result,
            LocalDateTime reviewedAt,
            CollectionReviewSettingEntity setting
    );
}
```

`setting` có thể null nếu vocabulary không thuộc collection nào có setting enabled.

### 6.3. Scheduler resolver

Tạo file:

```text
src/main/java/com/vocabverse/review/strategy/ReviewSchedulerResolver.java
```

Nội dung:

```java
@Component
public class ReviewSchedulerResolver {

    private final Map<ReviewSchedulerType, ReviewScheduler> schedulers;

    public ReviewSchedulerResolver(List<ReviewScheduler> schedulerList) {
        this.schedulers = schedulerList.stream()
                .collect(Collectors.toMap(ReviewScheduler::type, Function.identity()));
    }

    public ReviewScheduler resolve(ReviewSchedulerType type) {
        ReviewScheduler scheduler = schedulers.get(type);
        if (scheduler == null) {
            throw new BusinessException(ErrorCode.SCHEDULER_NOT_SUPPORTED);
        }
        return scheduler;
    }
}
```

Nếu chưa thêm `SCHEDULER_NOT_SUPPORTED`, dùng `INVALID_INPUT`.

## 7. FixedIntervalScheduler

### 7.1. Mục tiêu

Giữ behavior hiện tại càng sát càng tốt.

Hiện tại:

- Nếu có collection setting enabled:
  - `AGAIN`: interval đầu tiên.
  - Khác `AGAIN`: dùng `repetitionCount - 1` để chọn interval.
- Nếu không có setting:
  - Dùng rule trong `SimpleReviewIntervalStrategy`.

### 7.2. Implementation đề xuất

Tạo/đổi class:

```text
src/main/java/com/vocabverse/review/strategy/FixedIntervalScheduler.java
```

`type()` trả:

```java
ReviewSchedulerType.FIXED_INTERVAL
```

Logic:

1. Tính `newRepetitionCount`.
2. Tính `newStatus`.
3. Resolve interval:
   - Nếu setting có `intervalsJson`, dùng interval list.
   - Nếu không, dùng default strategy cũ.
4. Return `ReviewScheduleResult`.

Lưu ý quan trọng:

- Đừng để `ReviewService` update repetition/status trước rồi scheduler lại update lần nữa.
- Sau refactor, chỉ nên có một nơi quyết định state mới. Khuyến nghị để scheduler quyết định state mới.

## 8. Sm2Scheduler

### 8.1. Thuật toán SM-2 đơn giản

SM-2 gốc dùng quality từ 0 đến 5. App hiện có 4 nút:

```text
AGAIN
HARD
GOOD
EASY
```

Mapping đề xuất:

```text
AGAIN -> quality = 2
HARD  -> quality = 3
GOOD  -> quality = 4
EASY  -> quality = 5
```

Quy tắc SM-2:

- Nếu `quality < 3`:
  - repetition về `0`
  - interval = `1`
  - status = `LEARNING`
  - lapseCount tăng `1`
- Nếu `quality >= 3`:
  - repetition tăng `1`
  - Nếu repetition mới là `1`: interval = `1`
  - Nếu repetition mới là `2`: interval = `6`
  - Nếu repetition mới >= `3`: interval = round(previousInterval * easeFactor)

Công thức update ease factor:

```text
newEF = oldEF + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
```

Giới hạn:

```text
newEF >= 1.30
```

Default:

```text
easeFactor = 2.50
```

### 8.2. Điều chỉnh thực dụng cho app

SM-2 gốc có thể hơi mạnh với interval 6 ngày ở lần nhớ thứ hai. Có 2 lựa chọn:

Option A - đúng SM-2 hơn:

```text
rep 1 -> 1 ngày
rep 2 -> 6 ngày
rep >= 3 -> previousInterval * easeFactor
```

Option B - mềm hơn cho app học từ vựng:

```text
rep 1 -> 1 ngày
rep 2 -> 3 ngày
rep >= 3 -> previousInterval * easeFactor
```

Khuyến nghị:

- Phase đầu dùng Option A vì đây là SM-2 standard hơn.
- Nếu user thấy interval nhảy xa quá, sau này thêm setting `sm2SecondIntervalDays`.

### 8.3. Hard/Easy adjustment

Nếu chỉ dùng quality, `HARD/GOOD/EASY` đã ảnh hưởng qua ease factor:

```text
HARD quality 3 -> ease factor giảm
GOOD quality 4 -> ease factor giảm nhẹ hoặc gần như giữ
EASY quality 5 -> ease factor tăng
```

Tính thử:

```text
oldEF = 2.50
AGAIN quality 2 -> EF giảm mạnh nhưng repetition reset
HARD  quality 3 -> EF giảm 0.14 => 2.36
GOOD  quality 4 -> EF giữ 2.50
EASY  quality 5 -> EF tăng 0.10 => 2.60
```

### 8.4. Status mapping

Đề xuất:

```text
AGAIN -> LEARNING
repetitionCount = 0 -> LEARNING
repetitionCount = 1 -> LEARNING
repetitionCount >= 2 -> REVIEWING
EASY và repetitionCount >= 5 -> MASTERED
```

Giữ tương thích với logic hiện tại:

- Hiện tại `MASTERED` chỉ khi `EASY` và `repetitionCount >= 5`.
- Giữ rule này để không làm thay đổi UX quá mạnh.

### 8.5. Max interval

Phase đầu nên có max interval để tránh nhảy quá xa:

```text
maxIntervalDays = 365
```

Có thể hardcode trong `Sm2Scheduler`:

```java
private static final int MAX_INTERVAL_DAYS = 365;
```

Sau này có thể đưa vào setting.

### 8.6. Rounding

Khi tính:

```text
previousInterval * easeFactor
```

Dùng:

```java
Math.round(...)
```

Sau đó clamp:

```java
interval = Math.max(1, Math.min(interval, MAX_INTERVAL_DAYS));
```

### 8.7. Implementation file

Tạo:

```text
src/main/java/com/vocabverse/review/strategy/Sm2Scheduler.java
```

Pseudo-code:

```java
@Component
public class Sm2Scheduler implements ReviewScheduler {

    private static final BigDecimal DEFAULT_EASE_FACTOR = BigDecimal.valueOf(2.50);
    private static final BigDecimal MIN_EASE_FACTOR = BigDecimal.valueOf(1.30);
    private static final int MAX_INTERVAL_DAYS = 365;

    @Override
    public ReviewSchedulerType type() {
        return ReviewSchedulerType.SM2;
    }

    @Override
    public ReviewScheduleResult schedule(
            LearningProgressEntity progress,
            ReviewResult result,
            LocalDateTime reviewedAt,
            CollectionReviewSettingEntity setting
    ) {
        int quality = toQuality(result);
        BigDecimal oldEaseFactor = progress.getEaseFactor() == null
                ? DEFAULT_EASE_FACTOR
                : progress.getEaseFactor();

        BigDecimal newEaseFactor = calculateEaseFactor(oldEaseFactor, quality);
        int oldRepetition = progress.getRepetitionCount();
        int oldInterval = Math.max(0, progress.getLastIntervalDays());
        int reviewCount = progress.getReviewCount() + 1;
        int lapseCount = progress.getLapseCount();

        int newRepetition;
        int intervalDays;

        if (quality < 3) {
            newRepetition = 0;
            intervalDays = 1;
            lapseCount++;
        } else {
            newRepetition = oldRepetition + 1;
            intervalDays = calculateInterval(newRepetition, oldInterval, newEaseFactor);
        }

        LearningStatus status = resolveStatus(result, newRepetition);

        return new ReviewScheduleResult(
                status,
                newRepetition,
                newEaseFactor,
                intervalDays,
                lapseCount,
                reviewCount,
                reviewedAt.plusDays(intervalDays)
        );
    }
}
```

## 9. Update ReviewService

### 9.1. Current flow

Hiện tại:

```text
submitReview
-> initializeProgress
-> updateProgress
-> calculateNextReviewDate
-> save progress
-> create history
```

### 9.2. Flow mới

Đề xuất:

```text
submitReview
-> get current user
-> initializeProgress
-> previousStatus = progress.status
-> reviewedAt = now
-> resolve enabled collection setting for vocabulary
-> schedulerType = setting.schedulerType or FIXED_INTERVAL
-> scheduler = resolver.resolve(schedulerType)
-> scheduleResult = scheduler.schedule(progress, result, reviewedAt, setting)
-> applyScheduleResult(progress, scheduleResult, reviewedAt)
-> save progress
-> create review history
```

### 9.3. Tách resolve setting

Tạo method:

```java
private Optional<CollectionReviewSettingEntity> findEnabledReviewSetting(UUID userId, UUID vocabularyId)
```

Logic lấy từ `calculateNextReviewDate(...)` hiện tại:

```java
List<UUID> collectionIds = collectionVocabularyRepository.findOwnedCollectionIdsByVocabularyId(userId, vocabularyId);
if (collectionIds.isEmpty()) {
    return Optional.empty();
}
return collectionReviewSettingRepository
        .findFirstByUserIdAndCollectionIdInAndEnabledTrue(userId, collectionIds);
```

### 9.4. Apply schedule result

Tạo method:

```java
private void applyScheduleResult(
        LearningProgressEntity progress,
        ReviewScheduleResult schedule,
        LocalDateTime reviewedAt
) {
    progress.setStatus(schedule.status());
    progress.setRepetitionCount(schedule.repetitionCount());
    progress.setEaseFactor(schedule.easeFactor());
    progress.setLastIntervalDays(schedule.lastIntervalDays());
    progress.setLapseCount(schedule.lapseCount());
    progress.setReviewCount(schedule.reviewCount());
    progress.setLastReviewedAt(reviewedAt);
    progress.setNextReviewAt(schedule.nextReviewAt());
}
```

### 9.5. Backward compatibility

Nếu setting null:

```java
schedulerType = ReviewSchedulerType.FIXED_INTERVAL
```

Nếu setting có scheduler null do dữ liệu cũ:

```java
schedulerType = ReviewSchedulerType.FIXED_INTERVAL
```

Nếu setting scheduler là `FSRS` trong phase chưa support:

- `resolver.resolve(FSRS)` sẽ throw `SCHEDULER_NOT_SUPPORTED`.
- Hoặc service có thể fallback `SM2`, nhưng không khuyến nghị.

## 10. Update reset schedule

File:

```text
src/main/java/com/vocabverse/collection/service/CollectionReviewSettingService.java
```

Hiện `resetSchedule(...)` đang set:

```java
nextReviewAt = now + firstInterval(setting)
status = NEW
repetitionCount = 0
easeFactor = 2.50
lastReviewedAt = null
```

Phase SM-2:

- Giữ behavior reset giống cũ.
- Reset thêm:

```java
lastIntervalDays = 0
lapseCount = 0
reviewCount = 0
```

Nếu scheduler là `SM2`, `firstInterval(setting)` vẫn có thể dùng để set lịch đầu tiên, hoặc dùng `1`.

Khuyến nghị:

- Với `FIXED_INTERVAL`: dùng `firstInterval(setting)`.
- Với `SM2`: dùng `1`.
- Với `FSRS`: chưa support, reject reset hoặc dùng `1`.

## 11. ErrorCode cần thêm

File:

```text
src/main/java/com/vocabverse/common/constant/ErrorCode.java
```

Thêm:

```java
SCHEDULER_NOT_SUPPORTED("Scheduler type is not supported yet")
```

Nếu enum hiện tại yêu cầu HTTP status/message, follow style hiện có.

Các case dùng:

- User update setting sang `FSRS` khi chưa implement.
- Resolver không tìm thấy scheduler tương ứng.

## 12. Unit tests

### 12.1. Sm2SchedulerTest

Tạo:

```text
src/test/java/com/vocabverse/review/strategy/Sm2SchedulerTest.java
```

Test cases bắt buộc:

1. New card + AGAIN

Input:

```text
repetitionCount = 0
easeFactor = 2.50
lastIntervalDays = 0
result = AGAIN
```

Expected:

```text
repetitionCount = 0
interval = 1
status = LEARNING
easeFactor >= 1.30
lapseCount + 1
reviewCount + 1
nextReviewAt = reviewedAt + 1 day
```

2. New card + GOOD

Expected:

```text
repetitionCount = 1
interval = 1
status = LEARNING
easeFactor = 2.50
```

3. First successful review + GOOD again

Input:

```text
repetitionCount = 1
lastIntervalDays = 1
result = GOOD
```

Expected:

```text
repetitionCount = 2
interval = 6
status = REVIEWING
```

4. Mature review + EASY

Input:

```text
repetitionCount = 4
lastIntervalDays = 14
easeFactor = 2.50
result = EASY
```

Expected:

```text
repetitionCount = 5
easeFactor = 2.60
interval = round(14 * 2.60) = 36
status = MASTERED
```

5. HARD reduces ease factor

Input:

```text
easeFactor = 2.50
result = HARD
```

Expected:

```text
newEaseFactor = 2.36
```

6. Ease factor never below 1.30

Input:

```text
easeFactor = 1.31
result = AGAIN
```

Expected:

```text
newEaseFactor = 1.30
```

7. Max interval clamp

Input:

```text
lastIntervalDays = 300
easeFactor = 2.50
result = EASY
```

Expected:

```text
interval <= 365
```

### 12.2. FixedIntervalSchedulerTest

Test để đảm bảo behavior cũ không bị phá:

1. `AGAIN` dùng interval đầu tiên.
2. `GOOD` lần 2 dùng interval thứ hai.
3. Không có setting thì dùng default rule cũ.
4. `EASY` và repetition >= 5 thành `MASTERED`.

### 12.3. ReviewSchedulerResolverTest

Test:

- Resolve `FIXED_INTERVAL` trả FixedIntervalScheduler.
- Resolve `SM2` trả Sm2Scheduler.
- Resolve `FSRS` khi chưa có implementation thì throw `SCHEDULER_NOT_SUPPORTED`.

## 13. Service/integration tests

### 13.1. CollectionReviewSettingServiceTest

Test cases:

1. Default setting có `schedulerType = FIXED_INTERVAL`.
2. Update setting sang `SM2` thành công.
3. Update setting sang `FSRS` bị reject trong phase này.
4. Response trả đúng `schedulerType`.

### 13.2. ReviewServiceTest

Test cases:

1. Collection setting `FIXED_INTERVAL`:
   - Submit `GOOD`.
   - `nextReviewAt` theo interval list.

2. Collection setting `SM2`:
   - Submit `GOOD` lần đầu.
   - `nextReviewAt = reviewedAt + 1 day`.
   - `easeFactor = 2.50`.
   - `repetitionCount = 1`.

3. Collection setting `SM2`, review lần 2:
   - Existing progress `repetitionCount = 1`, `lastIntervalDays = 1`.
   - Submit `GOOD`.
   - `nextReviewAt = reviewedAt + 6 days`.

4. Vocabulary không thuộc collection có setting:
   - Fallback `FIXED_INTERVAL`.

5. Vocabulary thuộc nhiều collection:
   - Hiện repository dùng `findFirstByUserIdAndCollectionIdInAndEnabledTrue`.
   - Test chỉ cần đảm bảo không crash.
   - Sau này có thể cần rule ưu tiên rõ ràng.

## 14. API test thủ công

### 14.1. Lấy setting

```http
GET /api/v1/collections/{collectionId}/review-setting
Authorization: Bearer <token>
```

Expected:

```json
{
  "success": true,
  "data": {
    "schedulerType": "FIXED_INTERVAL"
  }
}
```

### 14.2. Update sang SM2

```http
PATCH /api/v1/collections/{collectionId}/review-setting
Authorization: Bearer <token>
Content-Type: application/json

{
  "schedulerType": "SM2"
}
```

Expected:

```json
{
  "success": true,
  "data": {
    "schedulerType": "SM2"
  }
}
```

### 14.3. Update sang FSRS khi chưa support

```http
PATCH /api/v1/collections/{collectionId}/review-setting
Authorization: Bearer <token>
Content-Type: application/json

{
  "schedulerType": "FSRS"
}
```

Expected:

```json
{
  "success": false,
  "error": {
    "code": "SCHEDULER_NOT_SUPPORTED"
  }
}
```

### 14.4. Submit review bằng SM2

```http
POST /api/v1/reviews/{vocabularyId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "result": "GOOD"
}
```

Expected lần đầu:

```json
{
  "success": true,
  "data": {
    "status": "LEARNING",
    "repetitionCount": 1,
    "nextReviewAt": "<reviewedAt + 1 day>"
  }
}
```

Expected lần 2 với `GOOD`:

```json
{
  "success": true,
  "data": {
    "status": "REVIEWING",
    "repetitionCount": 2,
    "nextReviewAt": "<reviewedAt + 6 days>"
  }
}
```

## 15. Acceptance criteria

Tính năng SM-2 được coi là xong khi:

- User có thể chọn `schedulerType = SM2` cho collection.
- Default collection setting vẫn là `FIXED_INTERVAL`.
- Existing review behavior của `FIXED_INTERVAL` không đổi.
- `SM2` dùng `easeFactor`, `repetitionCount`, `lastIntervalDays` để tính `nextReviewAt`.
- `AGAIN`, `HARD`, `GOOD`, `EASY` tạo ra interval khác nhau hợp lý.
- `ReviewHistoryEntity` vẫn lưu đúng `previousStatus`, `newStatus`, `nextReviewAt`.
- `GET /reviews/today` vẫn lấy vocabulary có `nextReviewAt <= now`.
- Test scheduler và service pass.
- `FSRS` chưa được implement nhưng kiến trúc đã có chỗ để thêm.

## 16. Checklist triển khai cho AI/dev

Làm theo thứ tự sau:

1. Thêm enum `ReviewSchedulerType`.
2. Thêm migration `scheduler_type` vào `collection_review_settings`.
3. Thêm field `schedulerType` vào `CollectionReviewSettingEntity`.
4. Update `UpdateCollectionReviewSettingRequest`.
5. Update `CollectionReviewSettingResponse`.
6. Update `CollectionReviewSettingService`:
   - default `FIXED_INTERVAL`
   - update `schedulerType`
   - reject `FSRS`
7. Thêm migration SM-2 fields cho `learning_progress`.
8. Update `LearningProgressEntity` với `lastIntervalDays`, `lapseCount`, `reviewCount`.
9. Update các chỗ tạo/reset progress để set default field mới.
10. Tạo `ReviewScheduleResult`.
11. Tạo `ReviewScheduler`.
12. Tạo `ReviewSchedulerResolver`.
13. Tạo `FixedIntervalScheduler` từ logic cũ.
14. Tạo `Sm2Scheduler`.
15. Refactor `ReviewService` để dùng resolver.
16. Update response nếu muốn trả thêm `easeFactor` hoặc `lastIntervalDays`. Nếu không cần FE hiển thị, chưa cần thêm vào API.
17. Viết unit tests cho `Sm2Scheduler`.
18. Viết tests cho `FixedIntervalScheduler`.
19. Viết tests cho `CollectionReviewSettingService`.
20. Viết tests cho `ReviewService`.
21. Chạy test:

```powershell
./gradlew test
```

hoặc:

```powershell
./mvnw test
```

22. Test thủ công API bằng Postman/curl.
23. Cập nhật docs API nếu response/request thay đổi.

## 17. Ghi chú cho phase FSRS sau này

Khi làm FSRS, không nên nhét công thức FSRS vào `ReviewService`.

Nên thêm:

```text
difficulty decimal
stability decimal
retrievability decimal
desired_retention decimal
```

Và implement:

```text
FsrsScheduler implements ReviewScheduler
```

`ReviewScheduleResult` lúc đó có thể cần mở rộng hoặc tạo result riêng có optional FSRS fields.

Thiết kế ở phase SM-2 phải giữ nguyên nguyên tắc:

- `ReviewService` điều phối flow.
- Scheduler quyết định memory state và next review date.
- Collection setting quyết định scheduler mode.
