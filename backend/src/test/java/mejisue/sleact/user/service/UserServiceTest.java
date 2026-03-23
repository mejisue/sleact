package mejisue.sleact.user.service;

import jakarta.persistence.EntityNotFoundException;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.dto.UserLoginReqDto;
import mejisue.sleact.user.dto.UserSaveReqDto;
import mejisue.sleact.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        // given
        UserSaveReqDto dto = new UserSaveReqDto("test@test.com", "password123", "테스터");
        given(userRepository.findByEmail(dto.getEmail())).willReturn(Optional.empty());
        given(passwordEncoder.encode(dto.getPassword())).willReturn("encodedPassword");

        // when
        userService.signup(dto);

        // then
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 예외 발생")
    void signup_duplicateEmail_throwsException() {
        // given
        UserSaveReqDto dto = new UserSaveReqDto("duplicate@test.com", "password123", "테스터");
        given(userRepository.findByEmail(dto.getEmail()))
                .willReturn(Optional.of(User.builder().email(dto.getEmail()).build()));

        // when & then
        assertThatThrownBy(() -> userService.signup(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 존재하는 이메일입니다.");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("로그인 성공 시 User 반환")
    void login_success() {
        // given
        UserLoginReqDto dto = new UserLoginReqDto("test@test.com", "password123");
        User user = User.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .build();
        given(userRepository.findByEmail(dto.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(dto.getPassword(), user.getPassword())).willReturn(true);

        // when
        User result = userService.login(dto);

        // then
        assertThat(result.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 EntityNotFoundException 발생")
    void login_emailNotFound_throwsEntityNotFoundException() {
        // given
        UserLoginReqDto dto = new UserLoginReqDto("notfound@test.com", "password123");
        given(userRepository.findByEmail(dto.getEmail())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.login(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 이메일입니다.");
    }

    @Test
    @DisplayName("비밀번호 불일치 시 IllegalStateException 발생")
    void login_wrongPassword_throwsIllegalStateException() {
        // given
        UserLoginReqDto dto = new UserLoginReqDto("test@test.com", "wrongPassword");
        User user = User.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .build();
        given(userRepository.findByEmail(dto.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(dto.getPassword(), user.getPassword())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }
}
