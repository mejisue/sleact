package mejisue.sleact.workspace.service;

import jakarta.persistence.EntityNotFoundException;
import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.channel.repository.ChannelRepository;
import mejisue.sleact.channelMember.domain.ChannelMember;
import mejisue.sleact.channelMember.repository.ChannelMemberRepository;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.repository.UserRepository;
import mejisue.sleact.workspace.domain.Workspace;
import mejisue.sleact.workspace.dto.WorkspaceCreateDto;
import mejisue.sleact.workspace.dto.WorkspaceResDto;
import mejisue.sleact.workspace.repository.WorkspaceRepository;
import mejisue.sleact.workspaceMember.domain.WorkspaceMember;
import mejisue.sleact.workspaceMember.repository.WorkspaceMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @InjectMocks
    private WorkspaceService workspaceService;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private ChannelRepository channelsRepository;

    @Mock
    private ChannelMemberRepository channelMembersRepository;

    @Mock
    private Authentication authentication;

    @Test
    @DisplayName("워크스페이스 생성 성공")
    void createWorkspace_success() {
        // given
        User owner = User.builder().email("test@test.com").nickname("테스터").build();
        WorkspaceCreateDto dto = new WorkspaceCreateDto();
        dto.setWorkspace("테스트 워크스페이스");
        dto.setUrl("test-workspace");

        given(authentication.getName()).willReturn("test@test.com");
        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(owner));
        given(workspaceRepository.save(any(Workspace.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(channelsRepository.save(any(Channel.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        WorkspaceResDto result = workspaceService.createWorkspace(authentication, dto);

        // then
        assertThat(result.getName()).isEqualTo("테스트 워크스페이스");
        assertThat(result.getUrl()).isEqualTo("test-workspace");
        verify(workspaceRepository).save(any(Workspace.class));
        verify(channelsRepository).save(any(Channel.class));
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
        verify(channelMembersRepository).save(any(ChannelMember.class));
    }

    @Test
    @DisplayName("워크스페이스 생성 시 기본 채널명은 '일반'")
    void createWorkspace_defaultChannelName() {
        // given
        User owner = User.builder().email("test@test.com").build();
        WorkspaceCreateDto dto = new WorkspaceCreateDto();
        dto.setWorkspace("테스트 워크스페이스");
        dto.setUrl("test-workspace");

        given(authentication.getName()).willReturn("test@test.com");
        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(owner));
        given(workspaceRepository.save(any(Workspace.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(channelsRepository.save(any(Channel.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        workspaceService.createWorkspace(authentication, dto);

        // then
        verify(channelsRepository).save(argThat(channel -> "일반".equals(channel.getName())));
    }

    @Test
    @DisplayName("존재하지 않는 유저로 워크스페이스 생성 시 EntityNotFoundException 발생")
    void createWorkspace_userNotFound_throwsEntityNotFoundException() {
        // given
        WorkspaceCreateDto dto = new WorkspaceCreateDto();
        dto.setWorkspace("테스트 워크스페이스");
        dto.setUrl("test-workspace");

        given(authentication.getName()).willReturn("notfound@test.com");
        given(userRepository.findByEmail("notfound@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceService.createWorkspace(authentication, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verify(workspaceRepository, never()).save(any());
        verify(channelsRepository, never()).save(any());
    }
}
