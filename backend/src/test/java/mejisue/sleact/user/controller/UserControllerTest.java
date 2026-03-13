package mejisue.sleact.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
}
