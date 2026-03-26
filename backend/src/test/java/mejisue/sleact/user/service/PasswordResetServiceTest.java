package mejisue.sleact.user.service;

import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private StringRedisTemplate redisTemplate;
    // StringRedisTemplate에서 문자열 값을 읽고 쓰는 실제 작업은 opsForValue()가 반환하는 ValueOperations가 담당합니다.
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "mailFrom", "noreply@sleact.com");
        ReflectionTestUtils.setField(passwordResetService, "baseUrl", "http://localhost:3000");
    }

    // =====================
    // requestPasswordReset
    // =====================

    @Test
    @DisplayName("가입된 이메일로 요청 시 Redis에 토큰 저장 후 이메일 발송")
    void requestPasswordReset_success() {
        // given
        User user = User.builder().email("test@test.com").build();
        MimeMessage mimeMessage = org.mockito.Mockito.mock(MimeMessage.class);

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);

        // when
        passwordResetService.requestPasswordReset("test@test.com");

        // then
        // 캡처기 생성
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        // capture()를 인자 자리에 넣으면 실제 전달된 값을 가로채서 저장
        verify(valueOperations).set(keyCaptor.capture(), eq("test@test.com"), any());
        assertThat(keyCaptor.getValue()).startsWith("password-reset:");

        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("가입되지 않은 이메일로 요청 시 아무 처리 없이 정상 반환 (이메일 존재 여부 노출 방지)")
    void requestPasswordReset_emailNotFound_doesNothing() {
        // given
        given(userRepository.findByEmail("ghost@test.com")).willReturn(Optional.empty());

        // when
        passwordResetService.requestPasswordReset("ghost@test.com");

        // then
        verify(redisTemplate, never()).opsForValue();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    // =====================
    // resetPassword
    // =====================

    @Test
    @DisplayName("유효한 토큰으로 비밀번호 재설정 성공 후 토큰 삭제")
    void resetPassword_success() {
        // given
        String token = "valid-token";
        String email = "test@test.com";
        User user = User.builder().email(email).password("oldEncodedPassword").build();

        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("password-reset:" + token)).willReturn(email);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        given(passwordEncoder.encode("newPassword123")).willReturn("newEncodedPassword");

        // when
        passwordResetService.resetPassword(token, "newPassword123");

        // then
        assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
        verify(redisTemplate).delete("password-reset:" + token);
    }

    @Test
    @DisplayName("만료되거나 존재하지 않는 토큰으로 재설정 시 IllegalArgumentException 발생")
    void resetPassword_invalidToken_throwsException() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("password-reset:invalid-token")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> passwordResetService.resetPassword("invalid-token", "newPassword123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않거나 만료된 토큰입니다.");

        verify(userRepository, never()).findByEmail(anyString());
    }
}
