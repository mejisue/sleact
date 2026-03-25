package mejisue.sleact.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisPubSubService implements MessageListener {

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public void publish(String channel, String message) {
        stringRedisTemplate.convertAndSend(channel, message);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody());
        String channel = stringRedisTemplate.getStringSerializer().deserialize(message.getChannel());

        if (channel == null || payload.isBlank()) {
            return;
        }

        // TODO: feat/channel-chat, feat/dm-chat 브랜치에서 채널별 DTO 역직렬화 및 분기 처리 구현
        messagingTemplate.convertAndSend(channel, payload);
    }
}
