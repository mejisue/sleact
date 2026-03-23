package mejisue.sleact.workspace.service;

import jakarta.persistence.EntityNotFoundException;
import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.channel.repository.ChannelRepository;
import mejisue.sleact.channelMember.domain.ChannelMember;
import mejisue.sleact.channelMember.repository.ChannelMemberRepository;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.repository.UserRepository;
import mejisue.sleact.workspace.domain.Workspace;
import mejisue.sleact.workspace.dto.InviteMemberDto;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

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

    @Test
    @DisplayName("워크스페이스 생성 성공")
    void createWorkspace_success() {
        // given
        User owner = User.builder().email("test@test.com").nickname("테스터").build();
        WorkspaceCreateDto dto = new WorkspaceCreateDto();
        dto.setWorkspace("테스트 워크스페이스");
        dto.setUrl("test-workspace");

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(owner));
        given(workspaceRepository.save(any(Workspace.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(channelsRepository.save(any(Channel.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        WorkspaceResDto result = workspaceService.createWorkspace("test@test.com", dto);

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

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(owner));
        given(workspaceRepository.save(any(Workspace.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(channelsRepository.save(any(Channel.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        workspaceService.createWorkspace("test@test.com", dto);

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

        given(userRepository.findByEmail("notfound@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceService.createWorkspace("notfound@test.com", dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verify(workspaceRepository, never()).save(any());
        verify(channelsRepository, never()).save(any());
    }

    @Test
    @DisplayName("워크스페이스 멤버 초대 성공")
    void inviteMembersToWorkspace_success() {
        // given
        User owner = User.builder().email("owner@test.com").build();
        User invitee = User.builder().email("invitee@test.com").nickname("초대받은유저").build();
        Workspace workspace = Workspace.builder().name("테스트 워크스페이스").url("test-ws").owner(owner).build();
        Channel defaultChannel = Channel.builder().name("일반").workspace(workspace).build();

        InviteMemberDto dto = new InviteMemberDto();
        dto.setEmail("invitee@test.com");

        given(workspaceRepository.findByName("테스트 워크스페이스")).willReturn(Optional.of(workspace));
        given(userRepository.findByEmail("invitee@test.com")).willReturn(Optional.of(invitee));
        given(workspaceMemberRepository.findByWorkspaceNameAndUserId(anyString(), any())).willReturn(Optional.empty());
        given(channelsRepository.findByWorkspaceAndName(workspace, "일반")).willReturn(Optional.of(defaultChannel));

        // when
        workspaceService.inviteMembersToWorkspace("테스트 워크스페이스", dto);

        // then
        verify(workspaceMemberRepository).save(any(WorkspaceMember.class));
        verify(channelMembersRepository).save(any(ChannelMember.class));
    }

    @Test
    @DisplayName("존재하지 않는 워크스페이스로 초대 시 EntityNotFoundException 발생")
    void inviteMembersToWorkspace_workspaceNotFound_throwsEntityNotFoundException() {
        // given
        InviteMemberDto dto = new InviteMemberDto();
        dto.setEmail("invitee@test.com");

        given(workspaceRepository.findByName("없는 워크스페이스")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceService.inviteMembersToWorkspace("없는 워크스페이스", dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("없는 워크스페이스");

        verify(workspaceMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 유저 초대 시 EntityNotFoundException 발생")
    void inviteMembersToWorkspace_userNotFound_throwsEntityNotFoundException() {
        // given
        User owner = User.builder().email("owner@test.com").build();
        Workspace workspace = Workspace.builder().name("테스트 워크스페이스").url("test-ws").owner(owner).build();

        InviteMemberDto dto = new InviteMemberDto();
        dto.setEmail("notfound@test.com");

        given(workspaceRepository.findByName("테스트 워크스페이스")).willReturn(Optional.of(workspace));
        given(userRepository.findByEmail("notfound@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceService.inviteMembersToWorkspace("테스트 워크스페이스", dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 이메일을 가진 회원이 존재하지 않습니다.");

        verify(workspaceMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 워크스페이스 멤버인 유저 초대 시 IllegalStateException 발생")
    void inviteMembersToWorkspace_alreadyMember_throwsIllegalStateException() {
        // given
        User owner = User.builder().email("owner@test.com").build();
        User invitee = User.builder().email("invitee@test.com").build();
        Workspace workspace = Workspace.builder().name("테스트 워크스페이스").url("test-ws").owner(owner).build();

        InviteMemberDto dto = new InviteMemberDto();
        dto.setEmail("invitee@test.com");

        given(workspaceRepository.findByName("테스트 워크스페이스")).willReturn(Optional.of(workspace));
        given(userRepository.findByEmail("invitee@test.com")).willReturn(Optional.of(invitee));
        given(workspaceMemberRepository.findByWorkspaceNameAndUserId(anyString(), any()))
                .willReturn(Optional.of(WorkspaceMember.builder().workspace(workspace).user(invitee).build()));

        // when & then
        assertThatThrownBy(() -> workspaceService.inviteMembersToWorkspace("테스트 워크스페이스", dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 회원이 워크스페이스에 소속되어 있습니다.");

        verify(workspaceMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("워크스페이스 멤버 삭제 성공")
    void deleteMemberInWorkspace_success() {
        // given
        User user = User.builder().email("member@test.com").build();
        Workspace workspace = Workspace.builder().name("테스트 워크스페이스").url("test-ws").owner(user).build();
        WorkspaceMember workspaceMember = WorkspaceMember.builder().workspace(workspace).user(user).build();

        given(workspaceMemberRepository.findByWorkspaceNameAndUserEmail("테스트 워크스페이스", "member@test.com"))
                .willReturn(Optional.of(workspaceMember));

        // when
        workspaceService.deleteMemberInWorkspace("테스트 워크스페이스", "member@test.com");

        // then
        verify(workspaceMemberRepository, times(1)).delete(workspaceMember);
    }

    @Test
    @DisplayName("존재하지 않는 워크스페이스 또는 유저로 멤버 삭제 시 EntityNotFoundException 발생")
    void deleteMemberInWorkspace_notFound_throwsEntityNotFoundException() {
        // given
        given(workspaceMemberRepository.findByWorkspaceNameAndUserEmail("없는 워크스페이스", "member@test.com"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceService.deleteMemberInWorkspace("없는 워크스페이스", "member@test.com"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당하는 워크스페이스 또는 유저 정보가 없습니다.");

        verify(workspaceMemberRepository, never()).delete(any());
    }
}
