package com.vocabverse.user.repository;

import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.entity.UserRole;
import com.vocabverse.user.entity.UserStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByStatus(UserStatus status);

    long countByRole(UserRole role);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
