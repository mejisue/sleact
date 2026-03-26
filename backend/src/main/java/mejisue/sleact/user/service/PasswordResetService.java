package mejisue.sleact.user.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final String TOKEN_PREFIX = "password-reset:";
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Value("${mail.from}")
    private String mailFrom;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * 비밀번호 재설정 링크 요청
     * - 가입된 이메일인 경우에만 재설정 링크 발송
     * - 가입되지 않은 이메일이어도 동일하게 200 응답 (이메일 존재 여부 노출 방지)
     */
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(TOKEN_PREFIX + token, email, TOKEN_TTL);
            sendResetEmail(email, token);
        });
    }

    /**
     * 토큰 검증 후 비밀번호 변경
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        String email = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
        if (email == null) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 토큰입니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일입니다."));

        user.updatePassword(passwordEncoder.encode(newPassword));
        redisTemplate.delete(TOKEN_PREFIX + token);
    }

    private void sendResetEmail(String to, String token) {
        try {
            String resetLink = baseUrl + "/reset-password?token=" + token;
            MimeMessageHelper helper = new MimeMessageHelper(mailSender.createMimeMessage(), "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject("[Sleact] 비밀번호 재설정");
            helper.setText(
                    "<p>아래 링크를 클릭하여 비밀번호를 재설정하세요. 링크는 30분간 유효합니다.</p>" +
                    "<a href=\"" + resetLink + "\">" + resetLink + "</a>",
                    true
            );
            mailSender.send(helper.getMimeMessage());
        } catch (Exception e) {
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
}
