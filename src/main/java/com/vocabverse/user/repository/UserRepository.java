package com.vocabverse.user.repository;

import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.entity.UserRole;
import com.vocabverse.user.entity.UserStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByStatus(UserStatus status);

    long countByRole(UserRole role);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
            select u
            from UserEntity u
            where lower(u.email) like lower(concat('%', :keyword, '%'))
               or lower(coalesce(u.fullName, '')) like lower(concat('%', :keyword, '%'))
            """)
    org.springframework.data.domain.Page<UserEntity> searchUsers(
            @Param("keyword") String keyword,
            org.springframework.data.domain.Pageable pageable
    );
}
