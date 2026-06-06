package com.vocabverse.admin.service;

import com.vocabverse.admin.dto.request.AdminUpdateUserRoleRequest;
import com.vocabverse.admin.dto.request.AdminUpdateUserStatusRequest;
import com.vocabverse.admin.dto.response.AdminPageResponse;
import com.vocabverse.admin.dto.response.AdminUserResponse;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.entity.UserRole;
import com.vocabverse.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public AdminPageResponse<AdminUserResponse> listUsers(String keyword, Pageable pageable) {
        Page<UserEntity> users = keyword == null || keyword.isBlank()
                ? userRepository.findAll(pageable)
                : userRepository.searchUsers(keyword.trim(), pageable);
        Page<AdminUserResponse> page = users.map(this::toResponse);
        return toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getUser(UUID userId) {
        return toResponse(findUser(userId));
    }

    @Transactional
    public AdminUserResponse updateStatus(UUID userId, AdminUpdateUserStatusRequest request) {
        UserEntity user = findUser(userId);
        user.setStatus(request.status());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public AdminUserResponse updateRole(UUID userId, AdminUpdateUserRoleRequest request) {
        UserEntity user = findUser(userId);
        if (user.getRole() == UserRole.ADMIN
                && request.role() != UserRole.ADMIN
                && userRepository.countByRole(UserRole.ADMIN) <= 1) {
            throw new BusinessException(ErrorCode.ADMIN_LAST_ADMIN_REMOVAL_NOT_ALLOWED);
        }

        user.setRole(request.role());
        return toResponse(userRepository.save(user));
    }

    private UserEntity findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private AdminUserResponse toResponse(UserEntity user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }

    private AdminPageResponse<AdminUserResponse> toPageResponse(Page<AdminUserResponse> page) {
        return new AdminPageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
