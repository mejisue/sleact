package mejisue.sleact.channelChat.service;

import mejisue.sleact.channel.dto.ChannelResDto;
import mejisue.sleact.channelChat.domain.ChannelChat;
import mejisue.sleact.channelChat.dto.ChannelChatDto;
import mejisue.sleact.user.dto.UserResDto;
import org.springframework.stereotype.Component;

@Component
public class ChannelChatMapper {

    public ChannelChatDto toChannelChatDto(ChannelChat channelChat) {
        UserResDto userResDto = new UserResDto();
        userResDto.setId(channelChat.getUser().getId());
        userResDto.setNickname(channelChat.getUser().getNickname());
        userResDto.setEmail(channelChat.getUser().getEmail());

        ChannelChatDto dto = new ChannelChatDto();
        dto.setId(channelChat.getId());
        dto.setUserId(channelChat.getUser().getId());
        dto.setUser(userResDto);
        dto.setContent(channelChat.getContent());
        dto.setCreatedAt(channelChat.getCreatedAt());
        dto.setChannelId(channelChat.getChannel().getId());
        dto.setChannel(ChannelResDto.toDto(channelChat.getChannel()));
        return dto;
    }
}
