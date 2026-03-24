package mejisue.sleact.channel.controller;

import jakarta.persistence.EntityNotFoundException;
import mejisue.sleact.channel.dto.ChannelResDto;
import mejisue.sleact.channel.service.ChannelService;
import mejisue.sleact.common.auth.JwtTokenProvider;
import mejisue.sleact.common.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChannelController.class)
@Import(SecurityConfig.class)
@MockitoBean(types = JpaMetamodelMappingContext.class)
class ChannelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChannelService channelService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("채널 목록 조회 성공 시 200과 채널 목록 반환")
    @WithMockUser(username = "user@test.com")
    void listChannels_success() throws Exception {
        // given
        List<ChannelResDto> channels = List.of(
                ChannelResDto.builder().id(1L).name("일반").workspaceId(1L).build(),
                ChannelResDto.builder().id(2L).name("개발").workspaceId(1L).build()
        );
        given(channelService.getMyChannels(anyString(), anyLong())).willReturn(channels);

        // when & then
        mockMvc.perform(get("/api/workspace/1/channels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("일반"))
                .andExpect(jsonPath("$[1].name").value("개발"))
                .andExpect(jsonPath("$[0].workspaceId").value(1L));
    }

    @Test
    @DisplayName("존재하지 않는 워크스페이스 조회 시 404 반환")
    @WithMockUser(username = "user@test.com")
    void listChannels_workspaceNotFound_returns404() throws Exception {
        // given
        willThrow(new EntityNotFoundException("workspace(id=999)가 존재하지 않습니다."))
                .given(channelService).getMyChannels(anyString(), anyLong());

        // when & then
        mockMvc.perform(get("/api/workspace/999/channels"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("workspace(id=999)가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("워크스페이스 멤버가 아닌 유저 조회 시 404 반환")
    @WithMockUser(username = "outsider@test.com")
    void listChannels_notWorkspaceMember_returns404() throws Exception {
        // given
        willThrow(new EntityNotFoundException("해당 워크스페이스의 멤버가 아닙니다."))
                .given(channelService).getMyChannels(anyString(), anyLong());

        // when & then
        mockMvc.perform(get("/api/workspace/1/channels"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("해당 워크스페이스의 멤버가 아닙니다."));
    }

    @Test
    @DisplayName("인증 없이 요청 시 403 반환")
    void listChannels_unauthenticated_returns403() throws Exception {
        // when & then
        mockMvc.perform(get("/api/workspace/1/channels"))
                .andExpect(status().isForbidden());
    }
}
