package mejisue.sleact.channel.controller;

import jakarta.persistence.EntityNotFoundException;
import mejisue.sleact.channel.dto.ChannelResDto;
import mejisue.sleact.channel.dto.CreateChannelDto;
import mejisue.sleact.channel.service.ChannelService;
import mejisue.sleact.common.auth.JwtTokenProvider;
import mejisue.sleact.common.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    @DisplayName("채널 생성 성공 시 201과 생성된 채널 반환")
    @WithMockUser(username = "user@test.com")
    void createChannel_success() throws Exception {
        // given
        ChannelResDto newChannel = ChannelResDto.builder().id(10L).name("신규채널").workspaceId(1L).build();
        given(channelService.createChannel(anyString(), anyLong(), any(CreateChannelDto.class))).willReturn(newChannel);

        // when & then
        mockMvc.perform(post("/api/workspace/1/channels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"신규채널\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("신규채널"))
                .andExpect(jsonPath("$.workspaceId").value(1L));
    }

    @Test
    @DisplayName("채널 생성 시 워크스페이스가 존재하지 않으면 404 반환")
    @WithMockUser(username = "user@test.com")
    void createChannel_workspaceNotFound_returns404() throws Exception {
        // given
        willThrow(new EntityNotFoundException("workspace(id=999)가 존재하지 않습니다."))
                .given(channelService).createChannel(anyString(), anyLong(), any(CreateChannelDto.class));

        // when & then
        mockMvc.perform(post("/api/workspace/999/channels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"신규채널\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("workspace(id=999)가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("채널 생성 시 워크스페이스 멤버가 아니면 404 반환")
    @WithMockUser(username = "outsider@test.com")
    void createChannel_notWorkspaceMember_returns404() throws Exception {
        // given
        willThrow(new EntityNotFoundException("해당 워크스페이스의 멤버가 아닙니다."))
                .given(channelService).createChannel(anyString(), anyLong(), any(CreateChannelDto.class));

        // when & then
        mockMvc.perform(post("/api/workspace/1/channels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"신규채널\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("해당 워크스페이스의 멤버가 아닙니다."));
    }

    @Test
    @DisplayName("채널 생성 시 중복 채널명이면 400 반환")
    @WithMockUser(username = "user@test.com")
    void createChannel_duplicateChannelName_returns400() throws Exception {
        // given
        willThrow(new IllegalStateException("이미 존재하는 채널 이름입니다."))
                .given(channelService).createChannel(anyString(), anyLong(), any(CreateChannelDto.class));

        // when & then
        mockMvc.perform(post("/api/workspace/1/channels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"중복채널\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 존재하는 채널 이름입니다."));
    }

    @Test
    @DisplayName("인증 없이 채널 생성 시 403 반환")
    void createChannel_unauthenticated_returns403() throws Exception {
        // when & then
        mockMvc.perform(post("/api/workspace/1/channels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"신규채널\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("채널 정보 조회 성공 시 200과 채널 정보 반환")
    @WithMockUser(username = "user@test.com")
    void getChannelInfo_success() throws Exception {
        // given
        ChannelResDto channel = ChannelResDto.builder().id(10L).name("일반").workspaceId(1L).build();
        given(channelService.getChannelInfo(anyString(), anyLong(), anyString())).willReturn(channel);

        // when & then
        mockMvc.perform(get("/api/workspace/1/channels/일반"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("일반"))
                .andExpect(jsonPath("$.workspaceId").value(1L));
    }

    @Test
    @DisplayName("채널 정보 조회 시 워크스페이스가 존재하지 않으면 404 반환")
    @WithMockUser(username = "user@test.com")
    void getChannelInfo_workspaceNotFound_returns404() throws Exception {
        // given
        willThrow(new EntityNotFoundException("workspace(id=999)가 존재하지 않습니다."))
                .given(channelService).getChannelInfo(anyString(), anyLong(), anyString());

        // when & then
        mockMvc.perform(get("/api/workspace/999/channels/일반"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("workspace(id=999)가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("채널 정보 조회 시 워크스페이스 멤버가 아니면 404 반환")
    @WithMockUser(username = "outsider@test.com")
    void getChannelInfo_notWorkspaceMember_returns404() throws Exception {
        // given
        willThrow(new EntityNotFoundException("해당 워크스페이스의 멤버가 아닙니다."))
                .given(channelService).getChannelInfo(anyString(), anyLong(), anyString());

        // when & then
        mockMvc.perform(get("/api/workspace/1/channels/일반"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("해당 워크스페이스의 멤버가 아닙니다."));
    }

    @Test
    @DisplayName("채널 정보 조회 시 채널이 존재하지 않으면 404 반환")
    @WithMockUser(username = "user@test.com")
    void getChannelInfo_channelNotFound_returns404() throws Exception {
        // given
        willThrow(new EntityNotFoundException("channel(channelName=없는채널)가 존재하지 않습니다."))
                .given(channelService).getChannelInfo(anyString(), anyLong(), anyString());

        // when & then
        mockMvc.perform(get("/api/workspace/1/channels/없는채널"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("channel(channelName=없는채널)가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("채널 정보 조회 시 채널 멤버가 아니면 404 반환")
    @WithMockUser(username = "user@test.com")
    void getChannelInfo_notChannelMember_returns404() throws Exception {
        // given
        willThrow(new EntityNotFoundException("해당 채널의 멤버가 아닙니다."))
                .given(channelService).getChannelInfo(anyString(), anyLong(), anyString());

        // when & then
        mockMvc.perform(get("/api/workspace/1/channels/일반"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("해당 채널의 멤버가 아닙니다."));
    }

    @Test
    @DisplayName("인증 없이 채널 정보 조회 시 403 반환")
    void getChannelInfo_unauthenticated_returns403() throws Exception {
        // when & then
        mockMvc.perform(get("/api/workspace/1/channels/일반"))
                .andExpect(status().isForbidden());
    }
}
