package mejisue.sleact.channel.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import mejisue.sleact.channel.dto.ChannelResDto;
import mejisue.sleact.channelMember.repository.ChannelMemberRepository;
import mejisue.sleact.workspace.repository.WorkspaceRepository;
import mejisue.sleact.workspaceMember.repository.WorkspaceMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChannelService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ChannelMemberRepository channelMembersRepository;

    public List<ChannelResDto> getMyChannels(String email, Long workspaceId) {
        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("workspace(id=" + workspaceId + ")가 존재하지 않습니다."));

        workspaceMemberRepository.findByWorkspaceIdAndUserEmail(workspaceId, email)
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스의 멤버가 아닙니다."));

        return channelMembersRepository.findByWorkspaceIdAndUserEmail(workspaceId, email)
                .stream()
                .map(cm -> ChannelResDto.toDto(cm.getChannel()))
                .collect(Collectors.toList());
    }
}
