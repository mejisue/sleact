package mejisue.sleact.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import mejisue.sleact.common.auth.JwtTokenProvider;
import mejisue.sleact.user.domain.Role;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.dto.UserLoginReqDto;
import mejisue.sleact.user.dto.UserSaveReqDto;
import mejisue.sleact.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import mejisue.sleact.common.config.SecurityConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    @DisplayName("로그인 성공 시 200과 JWT 토큰 반환")
    void login_success() throws Exception {
        // given
        UserLoginReqDto dto = new UserLoginReqDto("test@test.com", "password123");
        User user = User.builder()
                .email("test@test.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();
        given(userService.login(any(UserLoginReqDto.class))).willReturn(user);
        given(jwtTokenProvider.createToken(anyString(), anyString())).willReturn("mocked.jwt.token");

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"));
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
}
