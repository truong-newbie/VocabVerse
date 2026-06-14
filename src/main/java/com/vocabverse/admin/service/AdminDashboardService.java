package com.vocabverse.admin.service;

import com.vocabverse.admin.dto.response.AdminDashboardResponse;
import com.vocabverse.collection.enums.CollectionVisibility;
import com.vocabverse.collection.repository.CollectionRepository;
import com.vocabverse.learning.progress.repository.LearningProgressRepository;
import com.vocabverse.shadowing.repository.ShadowingLessonRepository;
import com.vocabverse.user.entity.UserStatus;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.repository.VocabularyRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CollectionRepository collectionRepository;
    private final VocabularyRepository vocabularyRepository;
    private final ShadowingLessonRepository shadowingLessonRepository;
    private final LearningProgressRepository learningProgressRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        return new AdminDashboardResponse(
                userRepository.count(),
                userRepository.countByStatus(UserStatus.ACTIVE),
                collectionRepository.countByDeletedAtIsNull(),
                collectionRepository.countByVisibilityAndDeletedAtIsNull(CollectionVisibility.PUBLIC),
                vocabularyRepository.countByDeletedAtIsNull(),
                shadowingLessonRepository.count(),
                learningProgressRepository.countDueReviewsUntil(endOfDay),
                userRepository.countByCreatedAtBetween(startOfDay, endOfDay)
        );
    }
}
