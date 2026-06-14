# Plan triển khai FsrsScheduler cho VocabVerse

## 1. Mục tiêu

Phase hiện tại đã có kiến trúc scheduler:

- `ReviewSchedulerType`
  - `FIXED_INTERVAL`
  - `SM2`
  - `FSRS`
- `ReviewScheduler`
- `ReviewScheduleResult`
- `ReviewSchedulerResolver`
- `FixedIntervalScheduler`
- `Sm2Scheduler`

Mục tiêu phase FSRS:

- Implement `FsrsScheduler`.
- Cho user chọn `schedulerType = FSRS`.
- Lưu memory state theo FSRS cho từng vocabulary/user.
- Tính `nextReviewAt` dựa trên `difficulty`, `stability`, `retrievability`, `desiredRetention`.
- Không phá behavior hiện tại của `FIXED_INTERVAL` và `SM2`.

## 2. Tổng quan FSRS

FSRS là thuật toán spaced repetition hiện đại hơn SM-2.

Thay vì chỉ dựa vào:

```text
repetitionCount
easeFactor
lastIntervalDays
```

FSRS dùng memory state:

```text
difficulty
stability
retrievability
```

Ý nghĩa:

- `difficulty`: độ khó dài hạn của card với user. Cao hơn nghĩa là khó nhớ hơn.
- `stability`: độ bền trí nhớ. Cao hơn nghĩa là có thể ôn lại sau interval dài hơn.
- `retrievability`: xác suất user còn nhớ tại thời điểm review.
- `desiredRetention`: xác suất nhớ mục tiêu khi tới ngày ôn, thường là `0.90`.

Flow tổng quát:

```text
User bấm AGAIN/HARD/GOOD/EASY
-> map sang rating FSRS
-> tính retrievability tại thời điểm review
-> update difficulty + stability
-> tính interval theo desiredRetention
-> nextReviewAt = reviewedAt + interval
```

## 3. Phạm vi phase FSRS

Làm trong phase này:

- Thêm FSRS memory fields vào `learning_progress`.
- Thêm FSRS setting fields vào `collection_review_settings`.
- Implement `FsrsScheduler`.
- Cho phép update collection setting sang `FSRS`.
- Update reset schedule cho FSRS.
- Update response DTO để FE biết FSRS config hiện tại.
- Viết unit test cho `FsrsScheduler`.
- Viết service test cho update setting và submit review.

Không làm trong phase này:

- Không train/optimize FSRS weights theo user history.
- Không import/export Anki.
- Không hiển thị biểu đồ memory state.
- Không tự động migrate toàn bộ lịch cũ sang FSRS theo thuật toán phức tạp.

## 4. Thuật toán đề xuất

Có 2 hướng:

### Option A: Implement FSRS simplified

Tự implement công thức FSRS đơn giản trong Java.

Ưu điểm:

- Chủ động, ít dependency.
- Dễ test.
- Phù hợp phase đầu.

Nhược điểm:

- Không đảm bảo giống 100% FSRS chính thức.
- Sau này nếu muốn tối ưu weights cần nâng cấp tiếp.

### Option B: Dùng library FSRS Java nếu có

Chỉ nên dùng nếu tìm được library:

- Maintained tốt.
- Có license phù hợp.
- Có test/usage rõ ràng.

Khuyến nghị cho project hiện tại:

```text
Option A: implement FSRS simplified trước.
```

Lý do:

- App đang cần feature chọn mode và lịch ôn thông minh hơn.
- SM-2 đã có.
- FSRS simplified đủ để tạo foundation, sau này thay công thức/weights vẫn giữ được architecture.

## 5. Rating mapping

App hiện có:

```text
AGAIN
HARD
GOOD
EASY
```

FSRS rating:

```text
AGAIN -> 1
HARD  -> 2
GOOD  -> 3
EASY  -> 4
```

Không đổi request API submit review.

FE vẫn gửi:

```json
{
  "result": "GOOD"
}
```

## 6. DB changes

### 6.1. Add FSRS fields to learning_progress

Tạo migration tiếp theo, ví dụ:

```text
src/main/resources/db/migration/V18__add_fsrs_progress_fields.sql
```

SQL:

```sql
ALTER TABLE learning_progress
    ADD COLUMN fsrs_difficulty NUMERIC(6,3),
    ADD COLUMN fsrs_stability NUMERIC(8,3),
    ADD COLUMN fsrs_retrievability NUMERIC(6,4);
```

Gợi ý default:

- Để nullable để không ảnh hưởng dữ liệu cũ.
- `FsrsScheduler` sẽ tự initialize nếu field null.

### 6.2. Add FSRS settings to collection_review_settings

Tạo migration:

```text
src/main/resources/db/migration/V19__add_fsrs_review_settings.sql
```

SQL:

```sql
ALTER TABLE collection_review_settings
    ADD COLUMN fsrs_desired_retention NUMERIC(4,3) NOT NULL DEFAULT 0.900,
    ADD COLUMN fsrs_max_interval_days INT NOT NULL DEFAULT 3650;
```

Giải thích:

- `fsrs_desired_retention`: mục tiêu xác suất nhớ. Ví dụ `0.90`.
- `fsrs_max_interval_days`: giới hạn interval tối đa.

Validation:

```text
desiredRetention min: 0.70
desiredRetention max: 0.98
maxIntervalDays min: 1
maxIntervalDays max: 3650
```

## 7. Entity changes

### 7.1. LearningProgressEntity

File:

```text
src/main/java/com/vocabverse/learning/progress/entity/LearningProgressEntity.java
```

Thêm:

```java
@Column(name = "fsrs_difficulty", precision = 6, scale = 3)
private BigDecimal fsrsDifficulty;

@Column(name = "fsrs_stability", precision = 8, scale = 3)
private BigDecimal fsrsStability;

@Column(name = "fsrs_retrievability", precision = 6, scale = 4)
private BigDecimal fsrsRetrievability;
```

### 7.2. CollectionReviewSettingEntity

File:

```text
src/main/java/com/vocabverse/collection/entity/CollectionReviewSettingEntity.java
```

Thêm:

```java
@Column(name = "fsrs_desired_retention", nullable = false, precision = 4, scale = 3)
private BigDecimal fsrsDesiredRetention;

@Column(name = "fsrs_max_interval_days", nullable = false)
private int fsrsMaxIntervalDays;
```

Default khi tạo setting:

```java
.fsrsDesiredRetention(BigDecimal.valueOf(0.900))
.fsrsMaxIntervalDays(3650)
```

## 8. DTO/API changes

### 8.1. UpdateCollectionReviewSettingRequest

Thêm:

```java
BigDecimal fsrsDesiredRetention,
Integer fsrsMaxIntervalDays
```

Record:

```java
public record UpdateCollectionReviewSettingRequest(
        Boolean enabled,
        Boolean emailEnabled,
        ReviewSchedulerType schedulerType,
        List<Integer> intervals,
        LocalTime reminderTime,
        String timezone,
        BigDecimal fsrsDesiredRetention,
        Integer fsrsMaxIntervalDays
) {
}
```

### 8.2. CollectionReviewSettingResponse

Thêm:

```java
BigDecimal fsrsDesiredRetention,
int fsrsMaxIntervalDays
```

Response example:

```json
{
  "success": true,
  "data": {
    "schedulerType": "FSRS",
    "fsrsDesiredRetention": 0.9,
    "fsrsMaxIntervalDays": 3650
  }
}
```

### 8.3. Enable FSRS update

Hiện phase SM-2 đang reject:

```java
if (schedulerType == ReviewSchedulerType.FSRS) {
    throw new BusinessException(ErrorCode.SCHEDULER_NOT_SUPPORTED);
}
```

Phase FSRS cần xóa rule này.

Validation mới:

- `FIXED_INTERVAL`: hợp lệ.
- `SM2`: hợp lệ.
- `FSRS`: hợp lệ.

Validate FSRS config nếu request có:

```java
private BigDecimal validateDesiredRetention(BigDecimal desiredRetention) {
    if (desiredRetention == null) {
        return DEFAULT_FSRS_DESIRED_RETENTION;
    }
    if (desiredRetention.compareTo(BigDecimal.valueOf(0.70)) < 0
            || desiredRetention.compareTo(BigDecimal.valueOf(0.98)) > 0) {
        throw new BusinessException(ErrorCode.INVALID_INPUT);
    }
    return desiredRetention.setScale(3, RoundingMode.HALF_UP);
}

private int validateMaxIntervalDays(Integer maxIntervalDays) {
    if (maxIntervalDays == null) {
        return DEFAULT_FSRS_MAX_INTERVAL_DAYS;
    }
    if (maxIntervalDays < 1 || maxIntervalDays > 3650) {
        throw new BusinessException(ErrorCode.INVALID_INPUT);
    }
    return maxIntervalDays;
}
```

## 9. Scheduler result changes

Hiện `ReviewScheduleResult` có:

```java
LearningStatus status,
int repetitionCount,
BigDecimal easeFactor,
int lastIntervalDays,
int lapseCount,
int reviewCount,
LocalDateTime nextReviewAt
```

FSRS cần trả thêm:

```java
BigDecimal fsrsDifficulty,
BigDecimal fsrsStability,
BigDecimal fsrsRetrievability
```

Update record:

```java
public record ReviewScheduleResult(
        LearningStatus status,
        int repetitionCount,
        BigDecimal easeFactor,
        int lastIntervalDays,
        int lapseCount,
        int reviewCount,
        BigDecimal fsrsDifficulty,
        BigDecimal fsrsStability,
        BigDecimal fsrsRetrievability,
        LocalDateTime nextReviewAt
) {
}
```

Update `FixedIntervalScheduler` và `Sm2Scheduler`:

- Preserve existing FSRS fields from `progress`.
- Hoặc trả null nếu không dùng.

Khuyến nghị:

```java
progress.getFsrsDifficulty(),
progress.getFsrsStability(),
progress.getFsrsRetrievability()
```

Trong `ReviewService.applyScheduleResult(...)`, set thêm:

```java
progress.setFsrsDifficulty(schedule.fsrsDifficulty());
progress.setFsrsStability(schedule.fsrsStability());
progress.setFsrsRetrievability(schedule.fsrsRetrievability());
```

## 10. FsrsScheduler design

Tạo file:

```text
src/main/java/com/vocabverse/review/strategy/FsrsScheduler.java
```

Skeleton:

```java
@Component
public class FsrsScheduler implements ReviewScheduler {

    private static final BigDecimal DEFAULT_EASE_FACTOR = BigDecimal.valueOf(2.50);
    private static final BigDecimal DEFAULT_DESIRED_RETENTION = BigDecimal.valueOf(0.900);
    private static final int DEFAULT_MAX_INTERVAL_DAYS = 3650;

    @Override
    public ReviewSchedulerType type() {
        return ReviewSchedulerType.FSRS;
    }

    @Override
    public ReviewScheduleResult schedule(
            LearningProgressEntity progress,
            ReviewResult result,
            LocalDateTime reviewedAt,
            CollectionReviewSettingEntity setting
    ) {
        int rating = toRating(result);
        BigDecimal desiredRetention = resolveDesiredRetention(setting);
        int maxIntervalDays = resolveMaxIntervalDays(setting);

        FsrsMemoryState current = initializeOrLoad(progress, result);
        BigDecimal retrievability = calculateRetrievability(progress, reviewedAt, current.stability());
        FsrsMemoryState next = updateMemoryState(current, rating, retrievability);
        int intervalDays = calculateIntervalDays(next.stability(), desiredRetention, maxIntervalDays);

        int repetitionCount = calculateRepetitionCount(progress, result);
        int lapseCount = progress.getLapseCount() + (result == ReviewResult.AGAIN ? 1 : 0);
        int reviewCount = progress.getReviewCount() + 1;

        return new ReviewScheduleResult(
                resolveStatus(result, repetitionCount),
                repetitionCount,
                resolveEaseFactor(progress),
                intervalDays,
                lapseCount,
                reviewCount,
                next.difficulty(),
                next.stability(),
                retrievability,
                reviewedAt.plusDays(intervalDays)
        );
    }
}
```

## 11. FSRS simplified formula

### 11.1. Memory state record

Tạo private record trong `FsrsScheduler`:

```java
private record FsrsMemoryState(
        BigDecimal difficulty,
        BigDecimal stability
) {
}
```

### 11.2. Initialize state

Nếu progress chưa có FSRS fields:

```text
AGAIN -> difficulty 7.0, stability 0.5
HARD  -> difficulty 6.0, stability 1.0
GOOD  -> difficulty 5.0, stability 2.5
EASY  -> difficulty 4.0, stability 4.0
```

Implementation:

```java
private FsrsMemoryState initializeOrLoad(LearningProgressEntity progress, ReviewResult result) {
    if (progress.getFsrsDifficulty() != null && progress.getFsrsStability() != null) {
        return new FsrsMemoryState(progress.getFsrsDifficulty(), progress.getFsrsStability());
    }
    return switch (result) {
        case AGAIN -> state("7.000", "0.500");
        case HARD -> state("6.000", "1.000");
        case GOOD -> state("5.000", "2.500");
        case EASY -> state("4.000", "4.000");
    };
}
```

### 11.3. Calculate retrievability

If never reviewed:

```text
retrievability = 1.0
```

If reviewed before:

```text
elapsedDays = days between lastReviewedAt and reviewedAt
retrievability = pow(1 + elapsedDays / (9 * stability), -1)
```

Clamp:

```text
0.0 <= retrievability <= 1.0
```

Implementation:

```java
private BigDecimal calculateRetrievability(
        LearningProgressEntity progress,
        LocalDateTime reviewedAt,
        BigDecimal stability
) {
    if (progress.getLastReviewedAt() == null || stability == null || stability.signum() <= 0) {
        return BigDecimal.ONE.setScale(4, RoundingMode.HALF_UP);
    }
    long elapsedDays = Math.max(0, ChronoUnit.DAYS.between(progress.getLastReviewedAt(), reviewedAt));
    double value = Math.pow(1.0 + elapsedDays / (9.0 * stability.doubleValue()), -1.0);
    return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
}
```

### 11.4. Update difficulty

Simplified:

```text
ratingModifier:
AGAIN: +1.2
HARD:  +0.6
GOOD:  -0.2
EASY:  -0.8
```

Formula:

```text
newDifficulty = oldDifficulty + ratingModifier
```

Clamp:

```text
1.0 <= difficulty <= 10.0
```

### 11.5. Update stability

Simplified:

If `AGAIN`:

```text
newStability = max(0.5, oldStability * 0.5)
```

If `HARD`:

```text
newStability = oldStability * (1.2 - difficulty * 0.02)
```

If `GOOD`:

```text
newStability = oldStability * (1.0 + (1.0 - retrievability) * 1.5 + (10.0 - difficulty) * 0.05)
```

If `EASY`:

```text
newStability = oldStability * (1.0 + (1.0 - retrievability) * 2.0 + (10.0 - difficulty) * 0.08 + 0.3)
```

Clamp:

```text
0.1 <= stability <= 3650.0
```

Round:

```text
scale 3
```

Ghi chú:

- Đây là FSRS-inspired simplified formula, không phải full FSRS official.
- Nếu sau này muốn full FSRS, thay logic trong `FsrsScheduler` nhưng giữ entity/API.

### 11.6. Calculate interval

Use desired retention:

```text
interval = stability * 9 * (1 / desiredRetention - 1)
```

Because retrievability formula:

```text
R(t, S) = (1 + t / (9S))^-1
```

Solve for target retention:

```text
t = 9S * (1/R - 1)
```

Implementation:

```java
private int calculateIntervalDays(
        BigDecimal stability,
        BigDecimal desiredRetention,
        int maxIntervalDays
) {
    double interval = 9.0 * stability.doubleValue() * (1.0 / desiredRetention.doubleValue() - 1.0);
    int days = Math.max(1, (int) Math.round(interval));
    return Math.min(days, maxIntervalDays);
}
```

Important:

- With desired retention `0.90`, interval roughly equals `stability`.
- Always min 1 day.

## 12. Status mapping

Use same behavior as SM-2:

```text
AGAIN -> LEARNING
repetitionCount = 0 -> LEARNING
repetitionCount = 1 -> LEARNING
repetitionCount >= 2 -> REVIEWING
EASY and repetitionCount >= 5 -> MASTERED
```

## 13. Reset schedule behavior

File:

```text
CollectionReviewSettingService.java
```

Current SM-2 plan:

- `FIXED_INTERVAL`: reset to first interval.
- `SM2`: reset to 1 day.

Add:

- `FSRS`: reset to 1 day.

Also reset FSRS fields:

```java
progress.setFsrsDifficulty(null);
progress.setFsrsStability(null);
progress.setFsrsRetrievability(null);
```

Reason:

- Next review initializes FSRS memory state based on first rating.

## 14. ReviewService changes

Update `applyScheduleResult`:

```java
progress.setFsrsDifficulty(schedule.fsrsDifficulty());
progress.setFsrsStability(schedule.fsrsStability());
progress.setFsrsRetrievability(schedule.fsrsRetrievability());
```

No other flow changes needed because:

```text
ReviewService -> resolver.resolve(schedulerType) -> FsrsScheduler
```

## 15. Learning progress response

Optional but useful:

File:

```text
LearningProgressResponse.java
```

Add:

```java
BigDecimal fsrsDifficulty,
BigDecimal fsrsStability,
BigDecimal fsrsRetrievability
```

If FE does not need these values, this can be skipped.

Recommendation:

- Add them for debug/admin visibility.
- FE can ignore.

## 16. Tests

### 16.1. FsrsSchedulerTest

Create:

```text
src/test/java/com/vocabverse/review/strategy/FsrsSchedulerTest.java
```

Test cases:

1. New card AGAIN initializes hard memory state

Input:

```text
fsrs fields null
result AGAIN
```

Expected:

```text
difficulty around 8.2 after modifier or clamped <= 10
stability >= 0.1
interval >= 1
status LEARNING
lapseCount + 1
reviewCount + 1
```

2. New card GOOD initializes normal memory state

Expected:

```text
difficulty lower than AGAIN
stability higher than AGAIN
interval >= 1
status LEARNING
```

3. Existing card EASY increases stability

Input:

```text
difficulty 5.000
stability 10.000
retrievability calculated from lastReviewedAt
result EASY
```

Expected:

```text
newStability > oldStability
newDifficulty < oldDifficulty
```

4. Existing card AGAIN decreases stability

Expected:

```text
newStability < oldStability
newDifficulty > oldDifficulty
lapseCount increments
```

5. Desired retention affects interval

Compare:

```text
desiredRetention 0.80 -> longer interval
desiredRetention 0.95 -> shorter interval
```

6. Max interval clamp

Expected:

```text
interval <= fsrsMaxIntervalDays
```

7. Difficulty clamp

Expected:

```text
1.0 <= difficulty <= 10.0
```

8. Stability clamp

Expected:

```text
0.1 <= stability <= 3650.0
```

### 16.2. ReviewSchedulerResolverTest

Update:

- Resolver can resolve `FSRS` when `FsrsScheduler` exists.

### 16.3. CollectionReviewSettingServiceTest

Test:

- Update scheduler to `FSRS` succeeds.
- Update `fsrsDesiredRetention = 0.90` succeeds.
- Update `fsrsDesiredRetention = 0.50` fails.
- Update `fsrsDesiredRetention = 0.99` fails.
- Update `fsrsMaxIntervalDays = 3650` succeeds.
- Update `fsrsMaxIntervalDays = 0` fails.

### 16.4. ReviewServiceTest

Test:

- Collection setting `FSRS`.
- Submit `GOOD`.
- Progress gets:
  - `fsrsDifficulty != null`
  - `fsrsStability != null`
  - `fsrsRetrievability != null`
  - `nextReviewAt != null`

## 17. API examples

### 17.1. Enable FSRS

```http
PATCH /api/v1/collections/{collectionId}/review-setting
Authorization: Bearer <token>
Content-Type: application/json

{
  "schedulerType": "FSRS",
  "fsrsDesiredRetention": 0.9,
  "fsrsMaxIntervalDays": 3650
}
```

Expected:

```json
{
  "success": true,
  "data": {
    "schedulerType": "FSRS",
    "fsrsDesiredRetention": 0.9,
    "fsrsMaxIntervalDays": 3650
  }
}
```

### 17.2. Submit review

No API change:

```http
POST /api/v1/reviews/{vocabularyId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "result": "GOOD"
}
```

Expected:

```json
{
  "success": true,
  "data": {
    "status": "LEARNING",
    "nextReviewAt": "2026-06-15T09:00:00",
    "repetitionCount": 1
  }
}
```

## 18. FE impact

FE docs for SM-2 currently say:

```text
FSRS disabled / coming soon
```

After this phase:

- Enable FSRS option.
- Show advanced FSRS settings:
  - Desired retention
  - Max interval days

Suggested UI:

```text
Review algorithm
[ Fixed interval ] [ SM-2 ] [ FSRS ]

When FSRS selected:
Desired retention: slider/input 0.70 - 0.98, default 0.90
Max interval: number input, default 3650
```

FE should still not calculate `nextReviewAt`.

## 19. Acceptance criteria

FSRS phase is complete when:

- User can update collection setting to `FSRS`.
- `ReviewSchedulerResolver` resolves `FsrsScheduler`.
- Submit review for FSRS collection updates:
  - `fsrsDifficulty`
  - `fsrsStability`
  - `fsrsRetrievability`
  - `nextReviewAt`
- `GET /reviews/today` still works through `nextReviewAt <= now`.
- `FIXED_INTERVAL` behavior unchanged.
- `SM2` behavior unchanged.
- Reset schedule works for FSRS.
- Tests pass.

## 20. Implementation checklist

1. Add migration `V18__add_fsrs_progress_fields.sql`.
2. Add migration `V19__add_fsrs_review_settings.sql`.
3. Add FSRS fields to `LearningProgressEntity`.
4. Add FSRS fields to `CollectionReviewSettingEntity`.
5. Update `UpdateCollectionReviewSettingRequest`.
6. Update `CollectionReviewSettingResponse`.
7. Remove FSRS rejection from `CollectionReviewSettingService`.
8. Add validation for `fsrsDesiredRetention`.
9. Add validation for `fsrsMaxIntervalDays`.
10. Update default setting creation with FSRS defaults.
11. Extend `ReviewScheduleResult` with FSRS fields.
12. Update `FixedIntervalScheduler` to preserve FSRS fields.
13. Update `Sm2Scheduler` to preserve FSRS fields.
14. Implement `FsrsScheduler`.
15. Update `ReviewService.applyScheduleResult`.
16. Update reset schedule to clear FSRS fields.
17. Add `FsrsSchedulerTest`.
18. Update `ReviewSchedulerResolverTest`.
19. Add/Update service tests.
20. Run:

```powershell
.\gradlew.bat --no-daemon test --console=plain
```

21. Update FE docs after backend is done.

## 21. Notes for future improvement

After simplified FSRS is stable, consider:

- Full FSRS official weights.
- Per-user weight optimization.
- Import review logs to optimize parameters.
- Per-deck/collection desired retention.
- Admin/debug endpoint to inspect memory state.
- Analytics comparing `FIXED_INTERVAL`, `SM2`, and `FSRS`.
