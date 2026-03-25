package mejisue.sleact.channelChat.dto;

import lombok.Data;
import mejisue.sleact.channel.dto.ChannelResDto;
import mejisue.sleact.user.dto.UserResDto;

import java.time.LocalDateTime;

@Data
public class ChannelChatDto {
    private Long id;
    private Long userId;
    private UserResDto user;
    private String content;
    private LocalDateTime createdAt;
    private Long channelId;
    private ChannelResDto channel;

}
