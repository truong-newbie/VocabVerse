package com.vocabverse.auth.service;

import com.vocabverse.auth.dto.LoginRequest;
import com.vocabverse.auth.dto.LoginResponse;
import com.vocabverse.auth.dto.RegisterRequest;
import com.vocabverse.auth.dto.RegisterResponse;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.common.security.JwtTokenProvider;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.entity.UserRole;
import com.vocabverse.user.entity.UserStatus;
import com.vocabverse.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
        }

        UserEntity user = UserEntity.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        UserEntity savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole()
        );
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIAL));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIAL);
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);

        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                new LoginResponse.UserInfo(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole()
                )
        );
    }
}
