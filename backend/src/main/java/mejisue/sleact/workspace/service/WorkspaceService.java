package mejisue.sleact.workspace.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.channel.repository.ChannelRepository;
import mejisue.sleact.channelMember.domain.ChannelMember;
import mejisue.sleact.channelMember.repository.ChannelMemberRepository;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.dto.UserResDto;
import mejisue.sleact.user.repository.UserRepository;
import mejisue.sleact.chat.service.PresenceService;
import mejisue.sleact.user.service.UserMapper;
import mejisue.sleact.workspace.domain.Workspace;
import mejisue.sleact.workspace.dto.InviteMemberDto;
import mejisue.sleact.workspace.dto.WorkspaceCreateDto;
import mejisue.sleact.workspace.dto.WorkspaceResDto;
import mejisue.sleact.workspace.repository.WorkspaceRepository;
import mejisue.sleact.workspaceMember.domain.WorkspaceMember;
import mejisue.sleact.workspaceMember.repository.WorkspaceMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private final UserMapper userMapper;
    private final PresenceService presenceService;

    /**
     * 워크스페이스 멤버 불러오기
     **/
    @Transactional(readOnly = true)
    public List<UserResDto> findMembersInWorkspace(Long workspaceId) {
        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("workspace(id=" + workspaceId + ")가 존재하지 않습니다."));

        return workspaceMemberRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(wm -> userMapper.toUserDto(wm.getUser()))
                .collect(Collectors.toList());
    }

    /**
     * 워크스페이스 온라인 멤버 조회
     **/
    @Transactional(readOnly = true)
    public Set<String> getOnlineMembers(Long workspaceId) {
        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("workspace(id=" + workspaceId + ")가 존재하지 않습니다."));
        return presenceService.getOnlineEmails(workspaceId.toString());
    }

    /**
     * 워크스페이스 만들기
     **/
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

    /**
     * 워크스페이스로 멤버 초대하기
     * - 워크스페이스 + 기본(일반) 채널에 자동 추가
     * - dto.channelNames 에 지정된 추가 채널에도 함께 추가 (선택사항)
     **/
    public void inviteMembersToWorkspace(Long workspaceId, InviteMemberDto dto) {
        Workspace targetWorkspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("workspace(id=" + workspaceId + ")가 존재하지 않습니다."));

        User targetUser = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("해당 이메일을 가진 회원이 존재하지 않습니다."));

        if (workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, targetUser.getId()).isPresent()) {
            throw new IllegalStateException("이미 회원이 워크스페이스에 소속되어 있습니다.");
        }

        workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(targetWorkspace)
                .user(targetUser)
                .loggedInAt(LocalDateTime.now())
                .build());

        // 기본(일반) 채널에 자동 추가
        Channel defaultChannel = channelsRepository.findByWorkspaceAndName(targetWorkspace, "일반")
                .orElseThrow(() -> new EntityNotFoundException("기본 채널을 찾을 수 없습니다."));
        channelMembersRepository.save(ChannelMember.builder().channel(defaultChannel).user(targetUser).build());

        // 선택된 추가 채널에도 추가 (존재하지 않는 채널은 경고 로그 후 건너뜀)
        List<String> extraChannels = dto.getChannelNames();
        if (extraChannels != null && !extraChannels.isEmpty()) {
            String inviteeEmail = targetUser.getEmail();
            for (String channelName : extraChannels) {
                if ("일반".equals(channelName)) continue;
                var channelOpt = channelsRepository.findByWorkspaceAndName(targetWorkspace, channelName);
                if (channelOpt.isEmpty()) {
                    log.warn("존재하지 않는 채널 이름이 초대 요청에 포함됨: '{}' (workspaceId={})", channelName, workspaceId);
                    continue;
                }
                Channel extraChannel = channelOpt.get();
                boolean alreadyMember = channelMembersRepository
                        .findByChannelIdAndUserEmail(extraChannel.getId(), inviteeEmail)
                        .isPresent();
                if (!alreadyMember) {
                    channelMembersRepository.save(
                            ChannelMember.builder().channel(extraChannel).user(targetUser).build());
                    log.info("채널 '{}' 에 멤버 추가: {}", channelName, inviteeEmail);
                }
            }
        }

        log.info("invite To Workspace! {} member: {}", targetWorkspace.getName(), targetUser.getNickname());
    }


    /**
     * 워크스페이스에서 나가기
     **/
    public void deleteMemberInWorkspace(Long workspaceId, String memberEmail) {
        WorkspaceMember workspaceMember = workspaceMemberRepository.findByWorkspaceIdAndUserEmail(workspaceId, memberEmail)
                .orElseThrow(() -> new EntityNotFoundException("해당하는 워크스페이스 또는 유저 정보가 없습니다."));

        workspaceMemberRepository.delete(workspaceMember);
    }
}
