package mejisue.sleact.channelChat.service;

import jakarta.persistence.EntityNotFoundException;
import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.channel.repository.ChannelRepository;
import mejisue.sleact.channelChat.domain.ChannelChat;
import mejisue.sleact.channelChat.dto.ChannelChatDto;
import mejisue.sleact.channelChat.repository.ChannelChatRepository;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.repository.UserRepository;
import mejisue.sleact.workspace.domain.Workspace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ChannelChatServiceTest {

    @InjectMocks
    private ChannelChatService channelChatService;

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private ChannelChatRepository channelChatRepository;

    @Mock
    private ChannelChatMapper channelChatMapper;

    @Mock
    private UserRepository userRepository;

    // =====================
    // getChatsfromWSAndChannel
    // =====================

    @Test
    @DisplayName("채팅 목록 조회 성공 - 메시지 있는 경우")
    void getChatsfromWSAndChannel_success() {
        // given
        User user = User.builder().id(1L).email("user@test.com").nickname("유저").build();
        Workspace workspace = Workspace.builder().id(1L).name("테스트워크스페이스").url("test-ws").owner(user).build();
        Channel channel = Channel.builder().id(10L).name("일반").workspace(workspace).build();

        ChannelChat chat1 = ChannelChat.builder().content("첫 번째 메시지").channel(channel).user(user).build();
        ChannelChat chat2 = ChannelChat.builder().content("두 번째 메시지").channel(channel).user(user).build();
        Page<ChannelChat> page = new PageImpl<>(List.of(chat1, chat2));

        ChannelChatDto dto1 = new ChannelChatDto();
        dto1.setContent("첫 번째 메시지");
        ChannelChatDto dto2 = new ChannelChatDto();
        dto2.setContent("두 번째 메시지");

        given(channelRepository.findChannelWithWorkspaceById(1L, "일반"))
                .willReturn(Optional.of(channel));
        given(channelChatRepository.findByChannelId(eq(10L), any(Pageable.class)))
                .willReturn(page);
        given(channelChatMapper.toChannelChatDto(chat1)).willReturn(dto1);
        given(channelChatMapper.toChannelChatDto(chat2)).willReturn(dto2);

        // when
        List<ChannelChatDto> result = channelChatService.getChatsfromWSAndChannel(
                1L, "일반", 20, 1);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("첫 번째 메시지");
        assertThat(result.get(1).getContent()).isEqualTo("두 번째 메시지");
    }

    @Test
    @DisplayName("채팅 목록 조회 성공 - 메시지 없는 경우 빈 리스트 반환")
    void getChatsfromWSAndChannel_noMessages_returnsEmptyList() {
        // given
        User user = User.builder().id(1L).email("user@test.com").build();
        Workspace workspace = Workspace.builder().id(1L).name("테스트워크스페이스").url("test-ws").owner(user).build();
        Channel channel = Channel.builder().id(10L).name("일반").workspace(workspace).build();

        given(channelRepository.findChannelWithWorkspaceById(1L, "일반"))
                .willReturn(Optional.of(channel));
        given(channelChatRepository.findByChannelId(eq(10L), any(Pageable.class)))
                .willReturn(Page.empty());

        // when
        List<ChannelChatDto> result = channelChatService.getChatsfromWSAndChannel(
                1L, "일반", 20, 1);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("채팅 목록 조회 시 존재하지 않는 채널이면 EntityNotFoundException 발생")
    void getChatsfromWSAndChannel_channelNotFound_throwsEntityNotFoundException() {
        // given
        given(channelRepository.findChannelWithWorkspaceById(1L, "없는채널"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelChatService.getChatsfromWSAndChannel(
                1L, "없는채널", 20, 1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("없는채널");
    }

    // =====================
    // postChatFromWSAndChannel
    // =====================

    @Test
    @DisplayName("채널 메시지 저장 성공")
    void postChatFromWSAndChannel_success() {
        // given
        User user = User.builder().id(1L).email("user@test.com").nickname("유저").build();
        Workspace workspace = Workspace.builder().id(1L).name("테스트워크스페이스").url("test-ws").owner(user).build();
        Channel channel = Channel.builder().id(10L).name("일반").workspace(workspace).build();

        ChannelChatDto expectedDto = new ChannelChatDto();
        expectedDto.setId(100L);
        expectedDto.setContent("안녕하세요");
        expectedDto.setUserId(1L);
        expectedDto.setChannelId(10L);

        given(channelRepository.findChannelWithWorkspaceById(1L, "일반"))
                .willReturn(Optional.of(channel));
        given(userRepository.findByEmail("user@test.com"))
                .willReturn(Optional.of(user));
        given(channelChatMapper.toChannelChatDto(any(ChannelChat.class)))
                .willReturn(expectedDto);

        // when
        ChannelChatDto result = channelChatService.postChatFromWSAndChannel(
                1L, "일반", "안녕하세요", "user@test.com");

        // then
        assertThat(result.getContent()).isEqualTo("안녕하세요");
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getChannelId()).isEqualTo(10L);
        then(channelChatRepository).should().save(any(ChannelChat.class));
    }

    @Test
    @DisplayName("존재하지 않는 채널로 메시지 전송 시 EntityNotFoundException 발생")
    void postChatFromWSAndChannel_channelNotFound_throwsEntityNotFoundException() {
        // given
        given(channelRepository.findChannelWithWorkspaceById(1L, "없는채널"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelChatService.postChatFromWSAndChannel(
                1L, "없는채널", "안녕하세요", "user@test.com"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("없는채널");
    }

    @Test
    @DisplayName("존재하지 않는 유저가 메시지 전송 시 EntityNotFoundException 발생")
    void postChatFromWSAndChannel_userNotFound_throwsEntityNotFoundException() {
        // given
        User owner = User.builder().id(1L).email("owner@test.com").build();
        Workspace workspace = Workspace.builder().id(1L).name("테스트워크스페이스").url("test-ws").owner(owner).build();
        Channel channel = Channel.builder().id(10L).name("일반").workspace(workspace).build();

        given(channelRepository.findChannelWithWorkspaceById(1L, "일반"))
                .willReturn(Optional.of(channel));
        given(userRepository.findByEmail("ghost@test.com"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> channelChatService.postChatFromWSAndChannel(
                1L, "일반", "안녕하세요", "ghost@test.com"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("ghost@test.com");
    }
}
