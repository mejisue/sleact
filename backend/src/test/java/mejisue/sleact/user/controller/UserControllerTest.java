package mejisue.sleact.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mejisue.sleact.common.auth.JwtTokenProvider;
import mejisue.sleact.common.config.SecurityConfig;
import mejisue.sleact.user.domain.Role;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.dto.UserLoginReqDto;
import mejisue.sleact.user.dto.UserResDto;
import mejisue.sleact.user.dto.UserSaveReqDto;
import mejisue.sleact.user.service.PasswordResetService;
import mejisue.sleact.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@MockitoBean(types = JpaMetamodelMappingContext.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private PasswordResetService passwordResetService;

    // =====================
    // signup
    // =====================

    @Test
    @DisplayName("회원가입 성공 시 200 반환")
    void signup_success() throws Exception {
        // given
        UserSaveReqDto dto = new UserSaveReqDto("test@test.com", "password123", "테스터");
        willDoNothing().given(userService).signup(any(UserSaveReqDto.class));

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 400 반환")
    void signup_duplicateEmail_returns400() throws Exception {
        // given
        UserSaveReqDto dto = new UserSaveReqDto("duplicate@test.com", "password123", "테스터");
        willThrow(new IllegalStateException("이미 존재하는 이메일입니다."))
                .given(userService).signup(any(UserSaveReqDto.class));

        // when & then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 존재하는 이메일입니다."));
    }

    // =====================
    // login
    // =====================

    @Test
    @DisplayName("로그인 성공 시 200과 JWT 토큰 및 유저 정보 반환")
    void login_success() throws Exception {
        // given
        UserLoginReqDto dto = new UserLoginReqDto("test@test.com", "password123");
        User user = User.builder().email("test@test.com").password("encodedPassword").role(Role.USER).build();
        UserResDto userResDto = new UserResDto();
        userResDto.setId(1L);
        userResDto.setEmail("test@test.com");
        userResDto.setNickname("테스터");
        userResDto.setWorkspaces(List.of());

        given(userService.login(any(UserLoginReqDto.class))).willReturn(user);
        given(jwtTokenProvider.createToken(anyString(), anyString())).willReturn("mocked.jwt.token");
        given(userService.getUser(anyString())).willReturn(userResDto);

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.user.email").value("test@test.com"))
                .andExpect(jsonPath("$.user.nickname").value("테스터"));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 404 반환")
    void login_emailNotFound_returns404() throws Exception {
        // given
        UserLoginReqDto dto = new UserLoginReqDto("notfound@test.com", "password123");
        given(userService.login(any(UserLoginReqDto.class)))
                .willThrow(new EntityNotFoundException("존재하지 않는 이메일입니다."));

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("존재하지 않는 이메일입니다."));
    }

    @Test
    @DisplayName("비밀번호 불일치 시 400 반환")
    void login_wrongPassword_returns400() throws Exception {
        // given
        UserLoginReqDto dto = new UserLoginReqDto("test@test.com", "wrongPassword");
        given(userService.login(any(UserLoginReqDto.class)))
                .willThrow(new IllegalStateException("비밀번호가 일치하지 않습니다."));

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("비밀번호가 일치하지 않습니다."));
    }

    // =====================
    // GET /api/users
    // =====================

    @Test
    @DisplayName("로그인 유저 정보 조회 성공 시 200과 유저 정보 반환")
    @WithMockUser(username = "test@test.com")
    void getUser_success() throws Exception {
        // given
        UserResDto userResDto = new UserResDto();
        userResDto.setId(1L);
        userResDto.setEmail("test@test.com");
        userResDto.setNickname("테스터");
        userResDto.setWorkspaces(List.of());

        given(userService.getUser("test@test.com")).willReturn(userResDto);

        // when & then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("테스터"));
    }

    @Test
    @DisplayName("인증 없이 유저 정보 조회 시 403 반환")
    void getUser_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    // =====================
    // password reset
    // =====================

    @Test
    @DisplayName("비밀번호 재설정 링크 요청 성공 시 200 반환")
    void requestPasswordReset_success() throws Exception {
        // given
        willDoNothing().given(passwordResetService).requestPasswordReset(anyString());

        // when & then
        mockMvc.perform(post("/api/users/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", "test@test.com"))))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    @DisplayName("비밀번호 재설정 확인 성공 시 200 반환")
    void resetPassword_success() throws Exception {
        // given
        willDoNothing().given(passwordResetService).resetPassword(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/api/users/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("token", "valid-token", "password", "newPassword123"))))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 비밀번호 재설정 시 400 반환")
    void resetPassword_invalidToken_returns400() throws Exception {
        // given
        willThrow(new IllegalArgumentException("유효하지 않거나 만료된 토큰입니다."))
                .given(passwordResetService).resetPassword(anyString(), anyString());

        // when & then
        mockMvc.perform(post("/api/users/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("token", "invalid-token", "password", "newPassword123"))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("유효하지 않거나 만료된 토큰입니다."));
    }
}
