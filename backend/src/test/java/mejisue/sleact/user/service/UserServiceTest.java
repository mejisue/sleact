package mejisue.sleact.user.service;

import jakarta.persistence.EntityNotFoundException;
import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.channel.repository.ChannelRepository;
import mejisue.sleact.channelMember.domain.ChannelMember;
import mejisue.sleact.channelMember.repository.ChannelMemberRepository;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.dto.UserLoginReqDto;
import mejisue.sleact.user.dto.UserResDto;
import mejisue.sleact.user.dto.UserSaveReqDto;
import mejisue.sleact.user.repository.UserRepository;
import mejisue.sleact.workspace.domain.Workspace;
import mejisue.sleact.workspace.repository.WorkspaceRepository;
import mejisue.sleact.workspaceMember.domain.WorkspaceMember;
import mejisue.sleact.workspaceMember.repository.WorkspaceMemberRepository;
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
    private WorkspaceRepository workspaceRepository;
    @Mock
    private ChannelRepository channelRepository;
    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;
    @Mock
    private ChannelMemberRepository channelMemberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    // =====================
    // signup
    // =====================

    @Test
    @DisplayName("회원가입 성공 시 기본 워크스페이스/채널 멤버로 등록")
    void signup_success() {
        // given
        UserSaveReqDto dto = new UserSaveReqDto("test@test.com", "password123", "테스터");
        User newUser = User.builder().email(dto.getEmail()).nickname(dto.getNickname()).build();
        User adminUser = User.builder().email("admin@sleact.com").build();
        Workspace defaultWS = Workspace.builder().id(1L).name("sleact").url("sleact").owner(adminUser).build();
        Channel defaultChannel = Channel.builder().id(1L).name("일반").workspace(defaultWS).build();

        given(userRepository.findByEmail(dto.getEmail())).willReturn(Optional.empty());
        given(passwordEncoder.encode(dto.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(newUser);
        given(workspaceRepository.findByName("sleact")).willReturn(Optional.of(defaultWS));
        given(channelRepository.findByWorkspaceAndName(defaultWS, "일반")).willReturn(Optional.of(defaultChannel));

        // when
        userService.signup(dto);

        // then
        verify(userRepository).save(any(User.class));
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
        verify(channelMemberRepository).save(any(ChannelMember.class));
    }

    @Test
    @DisplayName("기본 워크스페이스 없을 시 어드민 유저로 생성 후 가입")
    void signup_defaultWorkspaceNotFound_createsWorkspace() {
        // given
        UserSaveReqDto dto = new UserSaveReqDto("test@test.com", "password123", "테스터");
        User adminUser = User.builder().email("admin@sleact.com").build();
        Workspace createdWS = Workspace.builder().id(1L).name("sleact").url("sleact").owner(adminUser).build();
        Channel defaultChannel = Channel.builder().id(1L).name("일반").workspace(createdWS).build();

        given(userRepository.findByEmail(dto.getEmail())).willReturn(Optional.empty());
        given(passwordEncoder.encode(dto.getPassword())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(User.builder().build());
        given(workspaceRepository.findByName("sleact")).willReturn(Optional.empty());
        given(userRepository.findByEmail("admin@sleact.com")).willReturn(Optional.of(adminUser));
        given(workspaceRepository.save(any(Workspace.class))).willReturn(createdWS);
        given(channelRepository.findByWorkspaceAndName(createdWS, "일반")).willReturn(Optional.of(defaultChannel));

        // when
        userService.signup(dto);

        // then
        verify(workspaceRepository).save(any(Workspace.class));
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
        verify(channelMemberRepository).save(any(ChannelMember.class));
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

    // =====================
    // getUser
    // =====================

    @Test
    @DisplayName("이메일로 유저 정보 조회 성공")
    void getUser_success() {
        // given
        User user = User.builder().id(1L).email("test@test.com").nickname("테스터").build();
        UserResDto expectedDto = new UserResDto();
        expectedDto.setId(1L);
        expectedDto.setEmail("test@test.com");
        expectedDto.setNickname("테스터");

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
        given(userMapper.toUserDto(user)).willReturn(expectedDto);

        // when
        UserResDto result = userService.getUser("test@test.com");

        // then
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 유저 조회 시 EntityNotFoundException 발생")
    void getUser_notFound_throwsException() {
        // given
        given(userRepository.findByEmail("notfound@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUser("notfound@test.com"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("존재하지 않는 이메일입니다.");
    }

    // =====================
    // login
    // =====================

    @Test
    @DisplayName("로그인 성공 시 User 반환")
    void login_success() {
        // given
        UserLoginReqDto dto = new UserLoginReqDto("test@test.com", "password123");
        User user = User.builder().email("test@test.com").password("encodedPassword").build();
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
        User user = User.builder().email("test@test.com").password("encodedPassword").build();
        given(userRepository.findByEmail(dto.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(dto.getPassword(), user.getPassword())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.login(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }
}
