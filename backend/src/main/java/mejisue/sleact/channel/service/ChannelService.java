package mejisue.sleact.channel.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.channel.dto.ChannelResDto;
import mejisue.sleact.channel.dto.CreateChannelDto;
import mejisue.sleact.channel.repository.ChannelRepository;
import mejisue.sleact.channelMember.domain.ChannelMember;
import mejisue.sleact.channelMember.repository.ChannelMemberRepository;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.dto.UserResDto;
import mejisue.sleact.user.repository.UserRepository;
import mejisue.sleact.workspace.domain.Workspace;
import mejisue.sleact.workspace.dto.WorkspaceResDto;
import mejisue.sleact.workspace.repository.WorkspaceRepository;
import mejisue.sleact.workspaceMember.repository.WorkspaceMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChannelService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    public List<ChannelResDto> getMyChannels(String email, Long workspaceId) {
        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("workspace(id=" + workspaceId + ")가 존재하지 않습니다."));

        workspaceMemberRepository.findByWorkspaceIdAndUserEmail(workspaceId, email)
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스의 멤버가 아닙니다."));

        return channelMemberRepository.findByWorkspaceIdAndUserEmail(workspaceId, email)
                .stream()
                .map(cm -> ChannelResDto.toDto(cm.getChannel()))
                .collect(Collectors.toList());
    }

    @Transactional
    public ChannelResDto createChannel(String email, Long workspaceId, CreateChannelDto dto) {
        Workspace targetWorkspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("workspace(id=" + workspaceId + ")가 존재하지 않습니다."));

        workspaceMemberRepository.findByWorkspaceIdAndUserEmail(workspaceId, email)
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스의 멤버가 아닙니다."));

        channelRepository.findByWorkspaceAndName(targetWorkspace, dto.getName()).ifPresent(c -> {
            throw new IllegalStateException("이미 존재하는 채널 이름입니다.");
        });

        User targetUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("user(email=" + email + ")가 존재하지 않습니다."));

        Channel newChannel = Channel.builder().name(dto.getName()).workspace(targetWorkspace).build();
        channelRepository.save(newChannel);
        channelMemberRepository.save(ChannelMember.builder().channel(newChannel).user(targetUser).build());
        return ChannelResDto.toDto(newChannel);
    }

    public ChannelResDto getChannelInfo(String email, Long workspaceId, String channelName) {
        Workspace targetWorkspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("workspace(id=" + workspaceId + ")가 존재하지 않습니다."));

        workspaceMemberRepository.findByWorkspaceIdAndUserEmail(workspaceId, email)
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스의 멤버가 아닙니다."));

        Channel targetChannel = channelRepository.findByWorkspaceAndName(targetWorkspace, channelName)
                .orElseThrow(() -> new EntityNotFoundException("channel(channelName=" + channelName + ")가 존재하지 않습니다."));

        channelMemberRepository.findByChannelIdAndUserEmail(targetChannel.getId(), email)
                .orElseThrow(() -> new EntityNotFoundException("해당 채널의 멤버가 아닙니다."));

        return ChannelResDto.toDto(targetChannel);

    }

    public List<UserResDto> getMembersInfo(Long workspaceId, String channelName) {
        Workspace targetWorkspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("workspace(id=" + workspaceId + ")가 존재하지 않습니다."));

        Channel targetChannel = channelRepository.findByWorkspaceAndName(targetWorkspace, channelName)
                .orElseThrow(() -> new EntityNotFoundException("channel(channelName=" + channelName + ")가 존재하지 않습니다."));

        List<ChannelMember> channelMembers = channelMemberRepository.findByChannelIdWithUser(targetChannel.getId());

        List<Long> userIds = channelMembers.stream()
                .map(cm -> cm.getUser().getId())
                .collect(Collectors.toList());

        Map<Long, List<WorkspaceResDto>> workspacesByUserId = workspaceMemberRepository.findByUserIdIn(userIds)
                .stream()
                .collect(Collectors.groupingBy(
                        wm -> wm.getUser().getId(),
                        Collectors.mapping(wm -> WorkspaceResDto.from(wm.getWorkspace()), Collectors.toList())
                ));

        return channelMembers.stream()
                .map(cm -> {
                    User user = cm.getUser();
                    UserResDto dto = new UserResDto();
                    dto.setId(user.getId());
                    dto.setEmail(user.getEmail());
                    dto.setNickname(user.getNickname());
                    dto.setWorkspaces(workspacesByUserId.getOrDefault(user.getId(), List.of()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
