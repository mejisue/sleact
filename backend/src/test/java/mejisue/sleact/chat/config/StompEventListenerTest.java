package mejisue.sleact.chat.config;

import mejisue.sleact.chat.dto.PresenceResDto;
import mejisue.sleact.chat.service.PresenceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StompEventListenerTest {

    @InjectMocks
    private StompEventListener stompEventListener;

    @Mock
    private PresenceService presenceService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private Message<byte[]> createMessage(Map<String, Object> sessionAttributes) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setSessionAttributes(sessionAttributes);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    @DisplayName("CONNECT 시 Redis에 유저 추가 및 online 브로드캐스트")
    void handleConnect_addsToRedisAndBroadcastsOnline() {
        // given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "user@test.com");
        attributes.put("workspace", "1");
        SessionConnectEvent event = new SessionConnectEvent(this, createMessage(attributes));

        // when
        stompEventListener.handleConnect(event);

        // then
        verify(presenceService).add("1", "user@test.com");
        verify(messagingTemplate).convertAndSend(
                eq("/topic/workspace/1/presence"),
                argThat((PresenceResDto dto) ->
                        dto.getEmail().equals("user@test.com") && dto.getStatus().equals("online"))
        );
    }

    @Test
    @DisplayName("DISCONNECT 시 Redis에서 유저 제거 및 offline 브로드캐스트")
    void handleDisconnect_removesFromRedisAndBroadcastsOffline() {
        // given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "user@test.com");
        attributes.put("workspace", "1");
        SessionDisconnectEvent event = new SessionDisconnectEvent(this, createMessage(attributes), "session1", null);

        // when
        stompEventListener.handleDisconnect(event);

        // then
        verify(presenceService).remove("1", "user@test.com");
        verify(messagingTemplate).convertAndSend(
                eq("/topic/workspace/1/presence"),
                argThat((PresenceResDto dto) ->
                        dto.getEmail().equals("user@test.com") && dto.getStatus().equals("offline"))
        );
    }

    @Test
    @DisplayName("세션 attributes가 null이면 아무것도 하지 않음")
    void handleConnect_nullAttributes_doesNothing() {
        // given
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        // sessionAttributes 설정 안 함 → null
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        SessionConnectEvent event = new SessionConnectEvent(this, message);

        // when
        stompEventListener.handleConnect(event);

        // then
        verify(presenceService, never()).add(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(messagingTemplate, never()).convertAndSend(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.<Object>any()
        );
    }
}
