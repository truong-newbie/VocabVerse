package com.vocabverse.review.entity;

import com.vocabverse.learning.progress.entity.LearningStatus;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.vocabulary.entity.VocabularyEntity;
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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "review_history")
public class ReviewHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocabulary_id", nullable = false)
    private VocabularyEntity vocabulary;

    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 20)
    private ReviewResult result;

    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;

    @Column(name = "next_review_at", nullable = false)
    private LocalDateTime nextReviewAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false, length = 20)
    private LearningStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private LearningStatus newStatus;
}
