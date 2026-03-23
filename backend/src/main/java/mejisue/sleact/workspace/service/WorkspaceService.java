package mejisue.sleact.workspace.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.channel.repository.ChannelRepository;
import mejisue.sleact.channelMember.domain.ChannelMember;
import mejisue.sleact.channelMember.repository.ChannelMemberRepository;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.repository.UserRepository;
import mejisue.sleact.workspace.domain.Workspace;
import mejisue.sleact.workspace.dto.WorkspaceCreateDto;
import mejisue.sleact.workspace.dto.WorkspaceResDto;
import mejisue.sleact.workspace.repository.WorkspaceRepository;
import mejisue.sleact.workspaceMember.domain.WorkspaceMember;
import mejisue.sleact.workspaceMember.repository.WorkspaceMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelRepository channelsRepository;
    private final ChannelMemberRepository channelMembersRepository;

    /** 워크스페이스 만들기 **/
    public WorkspaceResDto createWorkspace(String email, WorkspaceCreateDto dto) {
        User targetUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Workspace newWorkspace = Workspace.builder()
                .owner(targetUser)
                .name(dto.getWorkspace())
                .url(dto.getUrl())
                .build();
        workspaceRepository.save(newWorkspace);

        // 워크스페이스 생성 시 기본(일반) 채널 만들기
        Channel channel = Channel.builder().name("일반").workspace(newWorkspace).build();
        channelsRepository.save(channel);

        workspaceMemberRepository.save(WorkspaceMember.builder().workspace(newWorkspace).user(targetUser).build());
        channelMembersRepository.save(ChannelMember.builder().channel(channel).user(targetUser).build());

        return WorkspaceResDto.from(newWorkspace);
    }
}
