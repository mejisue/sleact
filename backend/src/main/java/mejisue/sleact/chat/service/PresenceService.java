package mejisue.sleact.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String KEY_PREFIX = "presence:";

    public void add(String workspaceId, String email) {
        redisTemplate.opsForSet().add(KEY_PREFIX + workspaceId, email);
    }

    public void remove(String workspaceId, String email) {
        redisTemplate.opsForSet().remove(KEY_PREFIX + workspaceId, email);
    }

    public Set<String> getOnlineEmails(String workspaceId) {
        Set<String> members = redisTemplate.opsForSet().members(KEY_PREFIX + workspaceId);
        return members != null ? members : Collections.emptySet();
    }

    public void clearAll() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
