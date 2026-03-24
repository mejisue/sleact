package mejisue.sleact.workspace.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import mejisue.sleact.common.auth.JwtTokenProvider;
import mejisue.sleact.common.config.SecurityConfig;
import mejisue.sleact.workspace.dto.InviteMemberDto;
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

import mejisue.sleact.workspaceMember.service.WorkspaceMemberService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @MockitoBean
    private WorkspaceMemberService workspaceMemberService;

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
        given(workspaceService.createWorkspace(anyString(), any(WorkspaceCreateDto.class))).willReturn(resDto);

        // when & then
        mockMvc.perform(post("/api/workspace")
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
                .given(workspaceService).createWorkspace(anyString(), any(WorkspaceCreateDto.class));

        // when & then
        mockMvc.perform(post("/api/workspace")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    @DisplayName("내 워크스페이스 목록 조회 성공 시 200과 목록 반환")
    @WithMockUser(username = "test@test.com")
    void getMyWorkspaces_success() throws Exception {
        // given
        List<WorkspaceResDto> resDtoList = List.of(
                WorkspaceResDto.builder().id(1L).name("워크스페이스1").url("ws1").ownerId(1L).build(),
                WorkspaceResDto.builder().id(2L).name("워크스페이스2").url("ws2").ownerId(1L).build()
        );
        given(workspaceMemberService.findWorkspacesByEmail(anyString())).willReturn(resDtoList);

        // when & then
        mockMvc.perform(get("/api/workspace"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("워크스페이스1"))
                .andExpect(jsonPath("$[1].name").value("워크스페이스2"));
    }

    @Test
    @DisplayName("존재하지 않는 유저로 목록 조회 시 404 반환")
    @WithMockUser(username = "notfound@test.com")
    void getMyWorkspaces_userNotFound_returns404() throws Exception {
        // given
        willThrow(new EntityNotFoundException("User not found"))
                .given(workspaceMemberService).findWorkspacesByEmail(anyString());

        // when & then
        mockMvc.perform(get("/api/workspace"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    @DisplayName("워크스페이스 멤버 초대 성공 시 200 반환")
    @WithMockUser(username = "test@test.com")
    void inviteMembersToWorkspace_success() throws Exception {
        // given
        InviteMemberDto dto = new InviteMemberDto();
        dto.setEmail("invitee@test.com");

        willDoNothing().given(workspaceService).inviteMembersToWorkspace(anyLong(), any(InviteMemberDto.class));

        // when & then
        mockMvc.perform(post("/api/workspace/1/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    @DisplayName("존재하지 않는 워크스페이스로 초대 시 404 반환")
    @WithMockUser(username = "test@test.com")
    void inviteMembersToWorkspace_workspaceNotFound_returns404() throws Exception {
        // given
        InviteMemberDto dto = new InviteMemberDto();
        dto.setEmail("invitee@test.com");

        willThrow(new EntityNotFoundException("workspace(id=999)가 존재하지 않습니다."))
                .given(workspaceService).inviteMembersToWorkspace(anyLong(), any(InviteMemberDto.class));

        // when & then
        mockMvc.perform(post("/api/workspace/999/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("workspace(id=999)가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 유저 초대 시 404 반환")
    @WithMockUser(username = "test@test.com")
    void inviteMembersToWorkspace_userNotFound_returns404() throws Exception {
        // given
        InviteMemberDto dto = new InviteMemberDto();
        dto.setEmail("notfound@test.com");

        willThrow(new EntityNotFoundException("해당 이메일을 가진 회원이 존재하지 않습니다."))
                .given(workspaceService).inviteMembersToWorkspace(anyLong(), any(InviteMemberDto.class));

        // when & then
        mockMvc.perform(post("/api/workspace/1/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("해당 이메일을 가진 회원이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("이미 멤버인 유저 초대 시 400 반환")
    @WithMockUser(username = "test@test.com")
    void inviteMembersToWorkspace_alreadyMember_returns400() throws Exception {
        // given
        InviteMemberDto dto = new InviteMemberDto();
        dto.setEmail("already@test.com");

        willThrow(new IllegalStateException("이미 회원이 워크스페이스에 소속되어 있습니다."))
                .given(workspaceService).inviteMembersToWorkspace(anyLong(), any(InviteMemberDto.class));

        // when & then
        mockMvc.perform(post("/api/workspace/1/member")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 회원이 워크스페이스에 소속되어 있습니다."));
    }

    @Test
    @DisplayName("워크스페이스 멤버 삭제 성공 시 200 반환")
    @WithMockUser(username = "member@test.com")
    void deleteMemberInWorkspace_success() throws Exception {
        // given
        willDoNothing().given(workspaceService).deleteMemberInWorkspace(anyLong(), anyString());

        // when & then
        mockMvc.perform(delete("/api/workspace/1/member/me"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    @DisplayName("존재하지 않는 워크스페이스 또는 유저로 삭제 시 404 반환")
    @WithMockUser(username = "member@test.com")
    void deleteMemberInWorkspace_notFound_returns404() throws Exception {
        // given
        willThrow(new EntityNotFoundException("해당하는 워크스페이스 또는 유저 정보가 없습니다."))
                .given(workspaceService).deleteMemberInWorkspace(anyLong(), anyString());

        // when & then
        mockMvc.perform(delete("/api/workspace/999/member/me"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("해당하는 워크스페이스 또는 유저 정보가 없습니다."));
    }

    @Test
    @DisplayName("인증 없이 멤버 삭제 요청 시 403 반환")
    void deleteMemberInWorkspace_unauthenticated_returns403() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/workspace/1/member/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("인증 없이 요청 시 403 반환")
    void createWorkspace_unauthenticated_returns403() throws Exception {
        // given
        WorkspaceCreateDto createDto = new WorkspaceCreateDto();
        createDto.setWorkspace("테스트 워크스페이스");
        createDto.setUrl("test-workspace");

        // when & then
        mockMvc.perform(post("/api/workspace")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isForbidden());
    }
}
