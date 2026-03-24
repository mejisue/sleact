package mejisue.sleact.channel.service;

import jakarta.persistence.EntityNotFoundException;
import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.channel.dto.ChannelResDto;
import mejisue.sleact.channel.dto.CreateChannelDto;
import mejisue.sleact.channel.repository.ChannelRepository;
import mejisue.sleact.channelMember.domain.ChannelMember;
import mejisue.sleact.channelMember.repository.ChannelMemberRepository;
import mejisue.sleact.user.domain.User;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private UserRepository userRepository;

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

    @Test
    @DisplayName("채널 생성 성공")
    void createChannel_success() {
        // given
        User user = User.builder().email("user@test.com").build();
        Workspace workspace = Workspace.builder().id(1L).name("테스트 워크스페이스").url("test-ws").owner(user).build();
        CreateChannelDto dto = new CreateChannelDto();
        dto.setName("신규채널");

        given(workspaceRepository.findById(1L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(1L, "user@test.com"))
                .willReturn(Optional.of(WorkspaceMember.builder().workspace(workspace).user(user).build()));
        given(channelRepository.findByWorkspaceAndName(workspace, "신규채널")).willReturn(Optional.empty());
        given(userRepository.findByEmail("user@test.com")).willReturn(Optional.of(user));
        given(channelRepository.save(any(Channel.class)))
                .willAnswer(inv -> {
                    Channel c = inv.getArgument(0);
                    return Channel.builder().id(10L).name(c.getName()).workspace(c.getWorkspace()).build();
                });

        // when
        ChannelResDto result = channelService.createChannel("user@test.com", 1L, dto);

        // then
        assertThat(result.getName()).isEqualTo("신규채널");
        assertThat(result.getWorkspaceId()).isEqualTo(1L);
        then(channelMemberRepository).should().save(any(ChannelMember.class));
    }

    @Test
    @DisplayName("채널 생성 시 워크스페이스가 존재하지 않으면 EntityNotFoundException 발생")
    void createChannel_workspaceNotFound_throwsEntityNotFoundException() {
        // given
        CreateChannelDto dto = new CreateChannelDto();
        dto.setName("신규채널");
        given(workspaceRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelService.createChannel("user@test.com", 999L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("채널 생성 시 워크스페이스 멤버가 아니면 EntityNotFoundException 발생")
    void createChannel_notWorkspaceMember_throwsEntityNotFoundException() {
        // given
        User owner = User.builder().email("owner@test.com").build();
        Workspace workspace = Workspace.builder().id(1L).name("테스트 워크스페이스").url("test-ws").owner(owner).build();
        CreateChannelDto dto = new CreateChannelDto();
        dto.setName("신규채널");

        given(workspaceRepository.findById(1L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(1L, "outsider@test.com"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelService.createChannel("outsider@test.com", 1L, dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 워크스페이스의 멤버가 아닙니다.");
    }

    @Test
    @DisplayName("채널 생성 시 이미 존재하는 채널명이면 IllegalStateException 발생")
    void createChannel_duplicateChannelName_throwsIllegalStateException() {
        // given
        User user = User.builder().email("user@test.com").build();
        Workspace workspace = Workspace.builder().id(1L).name("테스트 워크스페이스").url("test-ws").owner(user).build();
        Channel existing = Channel.builder().id(1L).name("중복채널").workspace(workspace).build();
        CreateChannelDto dto = new CreateChannelDto();
        dto.setName("중복채널");

        given(workspaceRepository.findById(1L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(1L, "user@test.com"))
                .willReturn(Optional.of(WorkspaceMember.builder().workspace(workspace).user(user).build()));
        given(channelRepository.findByWorkspaceAndName(workspace, "중복채널")).willReturn(Optional.of(existing));

        // when & then
        assertThatThrownBy(() -> channelService.createChannel("user@test.com", 1L, dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 존재하는 채널 이름입니다.");
    }

    @Test
    @DisplayName("채널 정보 조회 성공")
    void getChannelInfo_success() {
        // given
        User user = User.builder().email("user@test.com").build();
        Workspace workspace = Workspace.builder().id(1L).name("테스트 워크스페이스").url("test-ws").owner(user).build();
        Channel channel = Channel.builder().id(10L).name("일반").workspace(workspace).build();

        given(workspaceRepository.findById(1L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(1L, "user@test.com"))
                .willReturn(Optional.of(WorkspaceMember.builder().workspace(workspace).user(user).build()));
        given(channelRepository.findByWorkspaceAndName(workspace, "일반")).willReturn(Optional.of(channel));
        given(channelMemberRepository.findByChannelIdAndUserEmail(10L, "user@test.com"))
                .willReturn(Optional.of(ChannelMember.builder().channel(channel).user(user).build()));

        // when
        ChannelResDto result = channelService.getChannelInfo("user@test.com", 1L, "일반");

        // then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("일반");
        assertThat(result.getWorkspaceId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("채널 정보 조회 시 워크스페이스가 존재하지 않으면 EntityNotFoundException 발생")
    void getChannelInfo_workspaceNotFound_throwsEntityNotFoundException() {
        // given
        given(workspaceRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelService.getChannelInfo("user@test.com", 999L, "일반"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("채널 정보 조회 시 워크스페이스 멤버가 아니면 EntityNotFoundException 발생")
    void getChannelInfo_notWorkspaceMember_throwsEntityNotFoundException() {
        // given
        User owner = User.builder().email("owner@test.com").build();
        Workspace workspace = Workspace.builder().id(1L).name("테스트 워크스페이스").url("test-ws").owner(owner).build();

        given(workspaceRepository.findById(1L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(1L, "outsider@test.com"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelService.getChannelInfo("outsider@test.com", 1L, "일반"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 워크스페이스의 멤버가 아닙니다.");
    }

    @Test
    @DisplayName("채널 정보 조회 시 채널이 존재하지 않으면 EntityNotFoundException 발생")
    void getChannelInfo_channelNotFound_throwsEntityNotFoundException() {
        // given
        User user = User.builder().email("user@test.com").build();
        Workspace workspace = Workspace.builder().id(1L).name("테스트 워크스페이스").url("test-ws").owner(user).build();

        given(workspaceRepository.findById(1L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(1L, "user@test.com"))
                .willReturn(Optional.of(WorkspaceMember.builder().workspace(workspace).user(user).build()));
        given(channelRepository.findByWorkspaceAndName(workspace, "없는채널")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelService.getChannelInfo("user@test.com", 1L, "없는채널"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("없는채널");
    }

    @Test
    @DisplayName("채널 정보 조회 시 채널 멤버가 아니면 EntityNotFoundException 발생")
    void getChannelInfo_notChannelMember_throwsEntityNotFoundException() {
        // given
        User user = User.builder().email("user@test.com").build();
        Workspace workspace = Workspace.builder().id(1L).name("테스트 워크스페이스").url("test-ws").owner(user).build();
        Channel channel = Channel.builder().id(10L).name("일반").workspace(workspace).build();

        given(workspaceRepository.findById(1L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(1L, "user@test.com"))
                .willReturn(Optional.of(WorkspaceMember.builder().workspace(workspace).user(user).build()));
        given(channelRepository.findByWorkspaceAndName(workspace, "일반")).willReturn(Optional.of(channel));
        given(channelMemberRepository.findByChannelIdAndUserEmail(10L, "user@test.com"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelService.getChannelInfo("user@test.com", 1L, "일반"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 채널의 멤버가 아닙니다.");
    }
}
