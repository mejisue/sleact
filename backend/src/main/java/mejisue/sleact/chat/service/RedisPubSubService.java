package mejisue.sleact.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mejisue.sleact.channelChat.dto.ChannelChatDto;
import mejisue.sleact.dm.dto.DmDto;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisPubSubService implements MessageListener {

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

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

        try {
            if (channel.startsWith("/topic/chat/channel/")) {
                ChannelChatDto dto = objectMapper.readValue(payload, ChannelChatDto.class);
                messagingTemplate.convertAndSend(channel, dto);
            } else if (channel.startsWith("/queue/chat/dm/")) {
                DmDto dto = objectMapper.readValue(payload, DmDto.class);
                messagingTemplate.convertAndSend(channel, dto);
            }
        } catch (IOException e) {

            log.error("Redis 메시지 역직렬화 실패 | channel: {}", channel, e);
        }
    }
}
