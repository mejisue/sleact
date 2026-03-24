package mejisue.sleact.channel.service;

import jakarta.persistence.EntityNotFoundException;
import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.channel.dto.ChannelResDto;
import mejisue.sleact.channelMember.domain.ChannelMember;
import mejisue.sleact.channelMember.repository.ChannelMemberRepository;
import mejisue.sleact.user.domain.User;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

    @InjectMocks
    private ChannelService channelService;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private ChannelMemberRepository channelMemberRepository;

    @Test
    @DisplayName("채널 목록 조회 성공")
    void getMyChannels_success() {
        // given
        User user = User.builder().email("user@test.com").build();
        Workspace workspace = Workspace.builder().id(1L).name("테스트 워크스페이스").url("test-ws").owner(user).build();
        Channel channel1 = Channel.builder().id(1L).name("일반").workspace(workspace).build();
        Channel channel2 = Channel.builder().id(2L).name("개발").workspace(workspace).build();

        given(workspaceRepository.findById(1L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(1L, "user@test.com"))
                .willReturn(Optional.of(WorkspaceMember.builder().workspace(workspace).user(user).build()));
        given(channelMemberRepository.findByWorkspaceIdAndUserEmail(1L, "user@test.com"))
                .willReturn(List.of(
                        ChannelMember.builder().channel(channel1).user(user).build(),
                        ChannelMember.builder().channel(channel2).user(user).build()
                ));

        // when
        List<ChannelResDto> result = channelService.getMyChannels("user@test.com", 1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("일반");
        assertThat(result.get(1).getName()).isEqualTo("개발");
        assertThat(result.get(0).getWorkspaceId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("속한 채널이 없으면 빈 리스트 반환")
    void getMyChannels_noChannels_returnsEmptyList() {
        // given
        User user = User.builder().email("user@test.com").build();
        Workspace workspace = Workspace.builder().id(1L).name("테스트 워크스페이스").url("test-ws").owner(user).build();

        given(workspaceRepository.findById(1L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(1L, "user@test.com"))
                .willReturn(Optional.of(WorkspaceMember.builder().workspace(workspace).user(user).build()));
        given(channelMemberRepository.findByWorkspaceIdAndUserEmail(1L, "user@test.com"))
                .willReturn(List.of());

        // when
        List<ChannelResDto> result = channelService.getMyChannels("user@test.com", 1L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 워크스페이스 조회 시 EntityNotFoundException 발생")
    void getMyChannels_workspaceNotFound_throwsEntityNotFoundException() {
        // given
        given(workspaceRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelService.getMyChannels("user@test.com", 999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("워크스페이스 멤버가 아닌 유저 조회 시 EntityNotFoundException 발생")
    void getMyChannels_notWorkspaceMember_throwsEntityNotFoundException() {
        // given
        User owner = User.builder().email("owner@test.com").build();
        Workspace workspace = Workspace.builder().id(1L).name("테스트 워크스페이스").url("test-ws").owner(owner).build();

        given(workspaceRepository.findById(1L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(1L, "outsider@test.com"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelService.getMyChannels("outsider@test.com", 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 워크스페이스의 멤버가 아닙니다.");
    }
}
