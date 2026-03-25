package mejisue.sleact.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class StompHandler implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT == accessor.getCommand()) {
            Map<String, Object> attributes = accessor.getSessionAttributes();
            if (attributes == null || attributes.get("token") == null) {
                log.warn("STOMP CONNECT - 인증되지 않은 연결 시도");
                throw new AccessDeniedException("인증되지 않은 연결입니다.");
            }
            log.info("STOMP CONNECT - 인증 확인 완료");
        }
        return message;
    }
}
