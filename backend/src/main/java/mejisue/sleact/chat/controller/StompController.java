package mejisue.sleact.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mejisue.sleact.channelChat.dto.ChannelChatDto;
import mejisue.sleact.channelChat.service.ChannelChatService;
import mejisue.sleact.chat.dto.ChatMessageReqDto;
import mejisue.sleact.chat.service.RedisPubSubService;
import mejisue.sleact.dm.dto.DmDto;
import mejisue.sleact.dm.service.DmService;
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
    private final DmService dmService;

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
    public void sendDmMessage(
            @DestinationVariable Long userId, DmDto dto, SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long workspaceId = Long.parseLong((String) headerAccessor.getSessionAttributes().get("workspace"));
            String email = (String) headerAccessor.getSessionAttributes().get("email");
            log.info("dm 메시지 수신 | receiverId: {}, workspaceId: {}, email: {}, content: {}",
                    userId, workspaceId, email, dto.getContent());

            DmDto dm = dmService.createDm(workspaceId, dto.getReceiverId(), dto.getContent(), email);
            log.info("dm 저장 완료 | id: {}, senderId: {}, receiverId: {}", dm.getId(), dm.getSenderId(), dm.getReceiverId());

            String messageJson = objectMapper.writeValueAsString(dm);
            pubSubService.publish("/queue/chat/dm/" + dm.getSenderId(), messageJson);
            pubSubService.publish("/queue/chat/dm/" + dm.getReceiverId(), messageJson);
            log.info("dm Redis 발행 완료");

        } catch (Exception e) {
            log.error("dm 처리 중 오류 발생", e);
        }
    }
}
