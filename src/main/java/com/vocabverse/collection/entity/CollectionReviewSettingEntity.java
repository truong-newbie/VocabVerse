package com.vocabverse.collection.entity;

import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.review.strategy.ReviewSchedulerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "collection_review_settings",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_collection_review_settings_user_collection",
                columnNames = {"user_id", "collection_id"}
        )
)
public class CollectionReviewSettingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    private CollectionEntity collection;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "scheduler_type", nullable = false, length = 30)
    private ReviewSchedulerType schedulerType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "intervals_json", nullable = false, columnDefinition = "jsonb")
    private List<Integer> intervalsJson;

    @Column(name = "reminder_time")
    private LocalTime reminderTime;

    @Column(name = "timezone", length = 100)
    private String timezone;

    @Column(name = "fsrs_desired_retention", nullable = false, precision = 4, scale = 3)
    private BigDecimal fsrsDesiredRetention;

    @Column(name = "fsrs_max_interval_days", nullable = false)
    private int fsrsMaxIntervalDays;

    @Column(name = "last_reset_at")
    private LocalDateTime lastResetAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
