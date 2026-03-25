package mejisue.sleact.chat.config;

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

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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

        String token = getParameterFromQuery(request.getURI(), "token");
        String workspace = getParameterFromQuery(request.getURI(), "workspace");

        // 1차 토큰 검증 - WebSocket 연결 수립 전 거부
        if (token == null) {
            log.warn("토큰 없음, WebSocket 연결 거부");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        try {
            jwtTokenProvider.getClaims(token);
            log.info("핸드셰이크 토큰 검증 완료");
        } catch (Exception e) {
            log.warn("핸드셰이크 토큰 검증 실패: {}", e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put("token", token);
        if (workspace != null) {
            attributes.put("workspace", workspace);
        }

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpSession session = servletRequest.getServletRequest().getSession();
            attributes.put("sessionId", session.getId());
        }

        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    private String getParameterFromQuery(URI uri, String paramName) {
        String query = uri.getQuery();
        if (query == null) return null;

        for (String pair : query.split("&")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2 && paramName.equals(keyValue[0])) {
                return URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
