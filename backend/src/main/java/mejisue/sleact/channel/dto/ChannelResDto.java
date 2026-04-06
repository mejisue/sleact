package mejisue.sleact.channel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mejisue.sleact.channel.domain.Channel;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChannelResDto {
    private Long id;
    private String name;
    private Long workspaceId;

    public static ChannelResDto toDto(Channel channel) {
        ChannelResDto channelDto = new ChannelResDto();
        channelDto.setId(channel.getId());
        channelDto.setName(channel.getName());
        channelDto.setWorkspaceId(channel.getWorkspace().getId());
        return channelDto;
    }
}
