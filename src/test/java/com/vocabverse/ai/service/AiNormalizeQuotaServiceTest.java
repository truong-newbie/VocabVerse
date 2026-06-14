package com.vocabverse.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vocabverse.ai.dto.response.AiNormalizeQuotaResponse;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

class AiNormalizeQuotaServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String EMAIL = "learner@example.com";

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void consumeTrialQuotaAllowsUsageWithinDailyLimit() {
        StringRedisTemplate redisTemplate = mockRedisTemplate(null);
        UserRepository userRepository = mockUserRepository();
        AiNormalizeQuotaService service = newService(redisTemplate, userRepository);
        when(redisTemplate.opsForValue().increment(anyString())).thenReturn(1L);

        service.consumeTrialQuota();

        verify(redisTemplate.opsForValue()).increment(anyString());
        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    void consumeTrialQuotaRejectsUsageOverDailyLimit() {
        StringRedisTemplate redisTemplate = mockRedisTemplate(null);
        UserRepository userRepository = mockUserRepository();
        AiNormalizeQuotaService service = newService(redisTemplate, userRepository);
        when(redisTemplate.opsForValue().increment(anyString())).thenReturn(4L);

        assertThatThrownBy(service::consumeTrialQuota)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AI_DAILY_LIMIT_REACHED);
    }

    @Test
    void getCurrentQuotaReturnsRemainingUsage() {
        StringRedisTemplate redisTemplate = mockRedisTemplate("2");
        UserRepository userRepository = mockUserRepository();
        AiNormalizeQuotaService service = newService(redisTemplate, userRepository);

        AiNormalizeQuotaResponse response = service.getCurrentQuota();

        assertThat(response.dailyLimit()).isEqualTo(3);
        assertThat(response.used()).isEqualTo(2);
        assertThat(response.remaining()).isEqualTo(1);
        assertThat(response.resetAt()).isNotNull();
    }

    @Test
    void shouldApplySystemTrialQuotaOnlyWhenUserApiKeyIsBlank() {
        AiNormalizeQuotaService service = newService(mockRedisTemplate(null), mockUserRepository());

        assertThat(service.shouldApplySystemTrialQuota(null)).isTrue();
        assertThat(service.shouldApplySystemTrialQuota("   ")).isTrue();
        assertThat(service.shouldApplySystemTrialQuota("gsk_user_key")).isFalse();
    }

    private AiNormalizeQuotaService newService(StringRedisTemplate redisTemplate, UserRepository userRepository) {
        setAuthentication();
        AiNormalizeQuotaService service = new AiNormalizeQuotaService(redisTemplate, userRepository);
        ReflectionTestUtils.setField(service, "dailyLimit", 3);
        return service;
    }

    private UserRepository mockUserRepository() {
        UserRepository userRepository = mock(UserRepository.class);
        UserEntity user = UserEntity.builder()
                .id(USER_ID)
                .email(EMAIL)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        return userRepository;
    }

    @SuppressWarnings("unchecked")
    private StringRedisTemplate mockRedisTemplate(String value) {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(value);
        when(redisTemplate.expire(anyString(), any(Duration.class))).thenReturn(true);
        return redisTemplate;
    }

    private void setAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, null, java.util.List.of())
        );
    }
}
