package mejisue.sleact.workspace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import mejisue.sleact.common.auth.JwtTokenProvider;
import mejisue.sleact.common.config.SecurityConfig;
import mejisue.sleact.workspace.dto.WorkspaceCreateDto;
import mejisue.sleact.workspace.dto.WorkspaceResDto;
import mejisue.sleact.workspace.service.WorkspaceService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkspaceController.class)
@Import(SecurityConfig.class)
@MockitoBean(types = JpaMetamodelMappingContext.class)
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WorkspaceService workspaceService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("워크스페이스 생성 성공 시 200과 워크스페이스 정보 반환")
    @WithMockUser(username = "test@test.com")
    void createWorkspace_success() throws Exception {
        // given
        WorkspaceCreateDto createDto = new WorkspaceCreateDto();
        createDto.setWorkspace("테스트 워크스페이스");
        createDto.setUrl("test-workspace");

        WorkspaceResDto resDto = WorkspaceResDto.builder()
                .id(1L)
                .name("테스트 워크스페이스")
                .url("test-workspace")
                .ownerId(1L)
                .build();
        given(workspaceService.createWorkspace(any(), any(WorkspaceCreateDto.class))).willReturn(resDto);

        // when & then
        mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트 워크스페이스"))
                .andExpect(jsonPath("$.url").value("test-workspace"))
                .andExpect(jsonPath("$.ownerId").value(1L));
    }

    @Test
    @DisplayName("존재하지 않는 유저로 생성 시 404 반환")
    @WithMockUser(username = "notfound@test.com")
    void createWorkspace_userNotFound_returns404() throws Exception {
        // given
        WorkspaceCreateDto createDto = new WorkspaceCreateDto();
        createDto.setWorkspace("테스트 워크스페이스");
        createDto.setUrl("test-workspace");

        willThrow(new EntityNotFoundException("User not found"))
                .given(workspaceService).createWorkspace(any(), any(WorkspaceCreateDto.class));

        // when & then
        mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    @DisplayName("인증 없이 요청 시 403 반환")
    void createWorkspace_unauthenticated_returns403() throws Exception {
        // given
        WorkspaceCreateDto createDto = new WorkspaceCreateDto();
        createDto.setWorkspace("테스트 워크스페이스");
        createDto.setUrl("test-workspace");

        // when & then
        mockMvc.perform(post("/api/workspaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }
}
