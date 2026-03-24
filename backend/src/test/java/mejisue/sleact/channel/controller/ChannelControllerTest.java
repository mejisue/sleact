package mejisue.sleact.channel.controller;

import jakarta.persistence.EntityNotFoundException;
import mejisue.sleact.channel.dto.ChannelResDto;
import mejisue.sleact.channel.dto.CreateChannelDto;
import mejisue.sleact.channel.service.ChannelService;
import mejisue.sleact.user.dto.UserResDto;
import org.mockito.Mockito;
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

    @Test
    @DisplayName("채널 멤버 목록 조회 성공 시 200과 멤버 목록 반환")
    @WithMockUser
    void getMembersInfo_success() throws Exception {
        // given
        List<UserResDto> users = new java.util.ArrayList<>();
        UserResDto user1 = new UserResDto();
        user1.setId(1L);
        user1.setEmail("owner@test.com");
        user1.setNickname("오너");
        UserResDto user2 = new UserResDto();
        user2.setId(2L);
        user2.setEmail("user@test.com");
        user2.setNickname("유저");
        users.add(user1);
        users.add(user2);

        given(channelService.getMembersInfo(anyLong(), anyString())).willReturn(users);

        // when & then
        mockMvc.perform(get("/api/workspace/1/channels/일반/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("owner@test.com"))
                .andExpect(jsonPath("$[1].email").value("user@test.com"));
    }

    @Test
    @DisplayName("채널 멤버 목록 조회 시 워크스페이스가 존재하지 않으면 404 반환")
    @WithMockUser
    void getMembersInfo_workspaceNotFound_returns404() throws Exception {
        // given
        willThrow(new EntityNotFoundException("workspace(id=999)가 존재하지 않습니다."))
                .given(channelService).getMembersInfo(anyLong(), anyString());

        // when & then
        mockMvc.perform(get("/api/workspace/999/channels/일반/members"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("workspace(id=999)가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("채널 멤버 목록 조회 시 채널이 존재하지 않으면 404 반환")
    @WithMockUser
    void getMembersInfo_channelNotFound_returns404() throws Exception {
        // given
        willThrow(new EntityNotFoundException("channel(channelName=없는채널)가 존재하지 않습니다."))
                .given(channelService).getMembersInfo(anyLong(), anyString());

        // when & then
        mockMvc.perform(get("/api/workspace/1/channels/없는채널/members"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("channel(channelName=없는채널)가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("인증 없이 채널 멤버 목록 조회 시 403 반환")
    void getMembersInfo_unauthenticated_returns403() throws Exception {
        // when & then
        mockMvc.perform(get("/api/workspace/1/channels/일반/members"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("채널 멤버 초대 성공 시 200 반환")
    @WithMockUser(username = "inviter@test.com")
    void inviteMemberToChannel_success() throws Exception {
        // given
        Mockito.doNothing().when(channelService)
                .inviteMemberToChannel(anyString(), anyLong(), anyString(), any());

        // when & then
        mockMvc.perform(post("/api/workspace/1/channels/일반/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invitee@test.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("채널 멤버 초대 시 초대하는 사람이 워크스페이스 멤버가 아니면 404 반환")
    @WithMockUser(username = "outsider@test.com")
    void inviteMemberToChannel_inviterNotWorkspaceMember_returns404() throws Exception {
        // given
        willThrow(new EntityNotFoundException("해당 워크스페이스의 멤버가 아닙니다."))
                .given(channelService).inviteMemberToChannel(anyString(), anyLong(), anyString(), any());

        // when & then
        mockMvc.perform(post("/api/workspace/1/channels/일반/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invitee@test.com\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("해당 워크스페이스의 멤버가 아닙니다."));
    }

    @Test
    @DisplayName("채널 멤버 초대 시 초대받는 사람이 워크스페이스 멤버가 아니면 404 반환")
    @WithMockUser(username = "inviter@test.com")
    void inviteMemberToChannel_inviteeNotWorkspaceMember_returns404() throws Exception {
        // given
        willThrow(new EntityNotFoundException("초대할 멤버가 워크스페이스에 소속되어 있지 않습니다."))
                .given(channelService).inviteMemberToChannel(anyString(), anyLong(), anyString(), any());

        // when & then
        mockMvc.perform(post("/api/workspace/1/channels/일반/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invitee@test.com\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("초대할 멤버가 워크스페이스에 소속되어 있지 않습니다."));
    }

    @Test
    @DisplayName("채널 멤버 초대 시 이미 채널 멤버면 400 반환")
    @WithMockUser(username = "inviter@test.com")
    void inviteMemberToChannel_alreadyChannelMember_returns400() throws Exception {
        // given
        willThrow(new IllegalStateException("이미 채널에 소속된 멤버입니다."))
                .given(channelService).inviteMemberToChannel(anyString(), anyLong(), anyString(), any());

        // when & then
        mockMvc.perform(post("/api/workspace/1/channels/일반/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invitee@test.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("이미 채널에 소속된 멤버입니다."));
    }

    @Test
    @DisplayName("인증 없이 채널 멤버 초대 시 403 반환")
    void inviteMemberToChannel_unauthenticated_returns403() throws Exception {
        // when & then
        mockMvc.perform(post("/api/workspace/1/channels/일반/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invitee@test.com\"}"))
                .andExpect(status().isForbidden());
    }
}
