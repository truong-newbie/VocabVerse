package com.vocabverse.roleplay.repository;

import com.vocabverse.roleplay.entity.RoleplayMessageEntity;
import com.vocabverse.roleplay.enums.RoleplayMessageSender;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleplayMessageRepository extends JpaRepository<RoleplayMessageEntity, UUID> {

    List<RoleplayMessageEntity> findAllBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    List<RoleplayMessageEntity> findBySessionIdOrderByCreatedAtDesc(UUID sessionId, Pageable pageable);

    @Query("""
            select count(message.id)
            from RoleplayMessageEntity message
            where message.session.user.id = :userId
              and message.sender = :sender
              and message.createdAt >= :from
            """)
    long countByUserIdAndSenderSince(
            @Param("userId") UUID userId,
            @Param("sender") RoleplayMessageSender sender,
            @Param("from") LocalDateTime from
    );
}
