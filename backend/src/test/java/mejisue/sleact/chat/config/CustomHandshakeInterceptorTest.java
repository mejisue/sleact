package mejisue.sleact.chat.config;

import io.jsonwebtoken.Claims;
import mejisue.sleact.common.auth.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.socket.WebSocketHandler;

import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CustomHandshakeInterceptorTest {

    @InjectMocks
    private CustomHandshakeInterceptor interceptor;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private ServerHttpRequest createRequest(String token, String workspace) {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        if (token != null) {
            servletRequest.setCookies(new Cookie("token", token));
        }
        if (workspace != null) {
            servletRequest.setParameter("workspace", workspace);
        }
        return new ServletServerHttpRequest(servletRequest);
    }

    private ServerHttpResponse createResponse() {
        return new ServletServerHttpResponse(new MockHttpServletResponse());
    }

    @Test
    @DisplayName("토큰과 workspace 파라미터가 모두 있으면 연결 허용")
    void beforeHandshake_validTokenAndWorkspace_returnsTrue() throws Exception {
        // given
        Claims claims = mock(Claims.class);
        given(claims.getSubject()).willReturn("user@test.com");
        given(jwtTokenProvider.getClaims("valid-token")).willReturn(claims);

        ServerHttpRequest request = createRequest("valid-token", "1");
        ServerHttpResponse response = createResponse();
        Map<String, Object> attributes = new HashMap<>();

        // when
        boolean result = interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), attributes);

        // then
        assertThat(result).isTrue();
        assertThat(attributes.get("email")).isEqualTo("user@test.com");
        assertThat(attributes.get("workspace")).isEqualTo("1");
    }

    @Test
    @DisplayName("토큰이 없으면 401 반환 및 연결 거부")
    void beforeHandshake_noToken_returns401() throws Exception {
        // given
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        ServerHttpRequest request = createRequest(null, "1");
        ServerHttpResponse response = new ServletServerHttpResponse(mockResponse);

        // when
        boolean result = interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), new HashMap<>());

        // then
        assertThat(result).isFalse();
        assertThat(mockResponse.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @DisplayName("workspace 파라미터가 없으면 400 반환 및 연결 거부")
    void beforeHandshake_noWorkspace_returns400() throws Exception {
        // given
        Claims claims = mock(Claims.class);
        given(claims.getSubject()).willReturn("user@test.com");
        given(jwtTokenProvider.getClaims("valid-token")).willReturn(claims);

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        ServerHttpRequest request = createRequest("valid-token", null);
        ServerHttpResponse response = new ServletServerHttpResponse(mockResponse);

        // when
        boolean result = interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), new HashMap<>());

        // then
        assertThat(result).isFalse();
        assertThat(mockResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("workspace 파라미터가 빈 문자열이면 400 반환 및 연결 거부")
    void beforeHandshake_blankWorkspace_returns400() throws Exception {
        // given
        Claims claims = mock(Claims.class);
        given(claims.getSubject()).willReturn("user@test.com");
        given(jwtTokenProvider.getClaims("valid-token")).willReturn(claims);

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        ServerHttpRequest request = createRequest("valid-token", "  ");
        ServerHttpResponse response = new ServletServerHttpResponse(mockResponse);

        // when
        boolean result = interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), new HashMap<>());

        // then
        assertThat(result).isFalse();
        assertThat(mockResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("토큰 검증 실패 시 401 반환 및 연결 거부")
    void beforeHandshake_invalidToken_returns401() throws Exception {
        // given
        given(jwtTokenProvider.getClaims("invalid-token")).willThrow(new RuntimeException("invalid token"));

        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        ServerHttpRequest request = createRequest("invalid-token", "1");
        ServerHttpResponse response = new ServletServerHttpResponse(mockResponse);

        // when
        boolean result = interceptor.beforeHandshake(request, response, mock(WebSocketHandler.class), new HashMap<>());

        // then
        assertThat(result).isFalse();
        assertThat(mockResponse.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
