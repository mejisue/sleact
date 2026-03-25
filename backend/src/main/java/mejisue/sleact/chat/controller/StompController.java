package mejisue.sleact.chat.controller;

import mejisue.sleact.chat.dto.ChatMessageReqDto;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class StompController {

    // 메시지 브로커 역할
    @MessageMapping("/chat/channel/{channelName}")
    public void sendChannelMessage(@DestinationVariable String channelName) {
    }
    //
    @MessageMapping("/chat/dm/{userId}")
    public void sendDmMessage(@DestinationVariable Long userId, ChatMessageReqDto dto) {
//
    }
}
