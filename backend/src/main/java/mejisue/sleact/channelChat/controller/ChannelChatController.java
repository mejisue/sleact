package mejisue.sleact.channelChat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mejisue.sleact.channelChat.dto.ChannelChatDto;
import mejisue.sleact.channelChat.service.ChannelChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ChannelChatController {

    private final ChannelChatService channelChatsService;

    /** 워크스페이스 내부의 채널의 채팅을 가져옴 **/
    @GetMapping("/workspace/{workspaceId}/channels/{channelName}/chats")
    public ResponseEntity<List<ChannelChatDto>> getChatsFromWSAndChannel
            (@PathVariable Long workspaceId, @PathVariable String channelName,
             @RequestParam("perPage") int perPage,@RequestParam("page") int page){
        List<ChannelChatDto> channelChatDto = channelChatsService.getChatsfromWSAndChannel(workspaceId, channelName, perPage, page);
        return ResponseEntity.ok(channelChatDto);
    }

}
