package mejisue.sleact.channelChat.service;

import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.channel.dto.ChannelResDto;
import mejisue.sleact.channelChat.domain.ChannelChat;
import mejisue.sleact.channelChat.dto.ChannelChatDto;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.workspace.domain.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelChatMapperTest {

    private ChannelChatMapper channelChatMapper;

    @BeforeEach
    void setUp() {
        channelChatMapper = new ChannelChatMapper();
    }

    @Test
    @DisplayName("ChannelChat 엔티티를 ChannelChatDto로 정상 변환")
    void toChannelChatDto_success() {
        // given
        User user = User.builder().id(1L).email("user@test.com").nickname("유저").build();
        Workspace workspace = Workspace.builder().id(5L).name("테스트워크스페이스").url("test-ws").owner(user).build();
        Channel channel = Channel.builder().id(10L).name("일반").workspace(workspace).build();
        ChannelChat channelChat = ChannelChat.builder()
                .content("안녕하세요")
                .channel(channel)
                .user(user)
                .build();

        // when
        ChannelChatDto result = channelChatMapper.toChannelChatDto(channelChat);

        // then
        assertThat(result.getContent()).isEqualTo("안녕하세요");
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUser().getEmail()).isEqualTo("user@test.com");
        assertThat(result.getUser().getNickname()).isEqualTo("유저");
        assertThat(result.getChannelId()).isEqualTo(10L);
        assertThat(result.getChannel().getName()).isEqualTo("일반");
        assertThat(result.getChannel().getWorkspaceId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("채널 정보가 ChannelResDto로 정상 변환")
    void toChannelChatDto_channelMappedCorrectly() {
        // given
        User user = User.builder().id(1L).email("user@test.com").nickname("유저").build();
        Workspace workspace = Workspace.builder().id(5L).name("테스트워크스페이스").url("test-ws").owner(user).build();
        Channel channel = Channel.builder().id(10L).name("개발").workspace(workspace).build();
        ChannelChat channelChat = ChannelChat.builder()
                .content("내용")
                .channel(channel)
                .user(user)
                .build();

        // when
        ChannelChatDto result = channelChatMapper.toChannelChatDto(channelChat);
        ChannelResDto channelDto = result.getChannel();

        // then
        assertThat(channelDto.getId()).isEqualTo(10L);
        assertThat(channelDto.getName()).isEqualTo("개발");
        assertThat(channelDto.getWorkspaceId()).isEqualTo(5L);
    }
}
