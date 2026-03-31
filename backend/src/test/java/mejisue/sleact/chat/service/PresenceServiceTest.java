package mejisue.sleact.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PresenceServiceTest {

    @InjectMocks
    private PresenceService presenceService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForSet()).willReturn(setOperations);
    }

    @Test
    @DisplayName("유저 온라인 등록 시 Redis SADD 호출")
    void add_callsRedisSAdd() {
        // when
        presenceService.add("1", "user@test.com");

        // then
        verify(setOperations).add("presence:1", "user@test.com");
    }

    @Test
    @DisplayName("유저 오프라인 처리 시 Redis SREM 호출")
    void remove_callsRedisSRem() {
        // when
        presenceService.remove("1", "user@test.com");

        // then
        verify(setOperations).remove("presence:1", "user@test.com");
    }

    @Test
    @DisplayName("온라인 멤버 조회 시 Redis SMEMBERS 결과 반환")
    void getOnlineEmails_returnsMembers() {
        // given
        given(setOperations.members("presence:1")).willReturn(Set.of("a@test.com", "b@test.com"));

        // when
        Set<String> result = presenceService.getOnlineEmails("1");

        // then
        assertThat(result).containsExactlyInAnyOrder("a@test.com", "b@test.com");
    }

    @Test
    @DisplayName("Redis가 null 반환 시 빈 Set 반환")
    void getOnlineEmails_nullFromRedis_returnsEmptySet() {
        // given
        given(setOperations.members("presence:1")).willReturn(null);

        // when
        Set<String> result = presenceService.getOnlineEmails("1");

        // then
        assertThat(result).isEmpty();
    }
}
