package mejisue.sleact.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mejisue.sleact.channelChat.dto.ChannelChatDto;
import mejisue.sleact.channelChat.service.ChannelChatService;
import mejisue.sleact.chat.dto.ChatMessageReqDto;
import mejisue.sleact.chat.service.RedisPubSubService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class StompController {

    private final ChannelChatService channelChatService;
    private final ObjectMapper objectMapper;
    private final RedisPubSubService pubSubService;

    @MessageMapping("/chat/channel/{channelName}")
    public void sendChannelMessage(
            @DestinationVariable String channelName,
            ChatMessageReqDto dto,
            SimpMessageHeaderAccessor headerAccessor) throws JsonProcessingException {

        String email = (String) headerAccessor.getSessionAttributes().get("email");
        log.info("채널 메시지 수신 | channel: {}, email: {}", channelName, email);

        ChannelChatDto chatDto = channelChatService.postChatFromWSAndChannel(
                dto.getWorkspaceId(), channelName, dto.getContent(), email);

        String redisChannel = "/topic/chat/channel/" + channelName;
        String messageJson = objectMapper.writeValueAsString(chatDto);
        pubSubService.publish(redisChannel, messageJson);
    }

    @MessageMapping("/chat/dm/{userId}")
    public void sendDmMessage(@DestinationVariable Long userId, ChatMessageReqDto dto) {
    }
}
