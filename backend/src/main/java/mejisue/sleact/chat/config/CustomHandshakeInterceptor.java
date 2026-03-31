package mejisue.sleact.chat.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mejisue.sleact.common.auth.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        log.info("Handshake interceptor called");

        String token = null;
        String workspace = null;
        if (request instanceof ServletServerHttpRequest servletRequest) {
            Cookie[] cookies = servletRequest.getServletRequest().getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
            workspace = servletRequest.getServletRequest().getParameter("workspace");
        }

        // 1차 토큰 검증 - WebSocket 연결 수립 전 거부
        if (token == null) {
            log.warn("토큰 없음, WebSocket 연결 거부");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        try {
            String email = jwtTokenProvider.getClaims(token).getSubject();
            attributes.put("token", token);
            attributes.put("email", email);
            log.info("핸드셰이크 토큰 검증 완료. email: {}", email);
        } catch (Exception e) {
            log.warn("핸드셰이크 토큰 검증 실패: {}", e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        if (workspace == null || workspace.isBlank()) {
            log.warn("workspace 파라미터 없음, WebSocket 연결 거부");
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return false;
        }
        attributes.put("workspace", workspace);

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpSession session = servletRequest.getServletRequest().getSession();
            attributes.put("sessionId", session.getId());
        }

        return super.beforeHandshake(request, response, wsHandler, attributes);
    }
}
