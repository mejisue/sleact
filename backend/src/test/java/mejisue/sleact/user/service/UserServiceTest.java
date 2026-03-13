package mejisue.sleact.user.service;

import mejisue.sleact.user.domain.User;
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
}
