package mejisue.sleact.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mejisue.sleact.chat.dto.PresenceResDto;
import mejisue.sleact.chat.service.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class StompEventListener {

    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes == null) return;

        String email = (String) attributes.get("email");
        String workspaceId = (String) attributes.get("workspace");

        presenceService.add(workspaceId, email);
        messagingTemplate.convertAndSend(
                "/topic/workspace/" + workspaceId + "/presence",
                new PresenceResDto(email, "online")
        );
        log.info("STOMP 연결 | workspace: {}, email: {}", workspaceId, email);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> attributes = accessor.getSessionAttributes();
        if (attributes == null) return;

        String email = (String) attributes.get("email");
        String workspaceId = (String) attributes.get("workspace");

        presenceService.remove(workspaceId, email);
        messagingTemplate.convertAndSend(
                "/topic/workspace/" + workspaceId + "/presence",
                new PresenceResDto(email, "offline")
        );
        log.info("STOMP 해제 | workspace: {}, email: {}", workspaceId, email);
    }
}
