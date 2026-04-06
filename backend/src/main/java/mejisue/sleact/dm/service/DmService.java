package mejisue.sleact.dm.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import mejisue.sleact.dm.domain.Dm;
import mejisue.sleact.dm.dto.DmDto;
import mejisue.sleact.dm.repository.DmRepository;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.workspace.domain.Workspace;
import mejisue.sleact.workspace.repository.WorkspaceRepository;
import mejisue.sleact.workspaceMember.repository.WorkspaceMemberRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class DmService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final DmRepository dmRepository;
    private final DmMapper dmMapper;

    /**
     * 두 유저 간 DM 목록 조회 (최신순, 페이지네이션)
     */
    @Transactional(readOnly = true)
    public List<DmDto> getDms(Long workspaceId, Long otherUserId, String myEmail, int page, int size) {
        Workspace targetWS = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다: " + workspaceId));

        User me = workspaceMemberRepository.findByWorkspaceIdAndUserEmail(targetWS.getId(), myEmail)
                .orElseThrow(() -> new EntityNotFoundException("해당 워크스페이스의 멤버가 아닙니다."))
                .getUser();

        Pageable pageable = PageRequest.of(page - 1, size);
        return dmRepository.findDmsBetweenUsers(workspaceId, me.getId(), otherUserId, pageable)
                .stream()
                .map(dmMapper::toIDMDto)
                .toList();
    }

    /**
     * DM 생성하기
     */
    public DmDto createDm(Long workspaceId, long receiverId, String content, String senderEmail) {
        Workspace targetWS = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다: " + workspaceId));

        User sender = workspaceMemberRepository.findByWorkspaceIdAndUserEmail(targetWS.getId(), senderEmail)
                .orElseThrow(() -> new EntityNotFoundException("보내는 유저가 해당 워크스페이스의 멤버가 아닙니다."))
                .getUser();

        User receiver = workspaceMemberRepository.findByWorkspaceIdAndUserId(targetWS.getId(), receiverId)
                .orElseThrow(() -> new EntityNotFoundException("받는 유저가 해당 워크스페이스의 멤버가 아닙니다."))
                .getUser();

        Dm createdDm = Dm.builder()
                .content(content)
                .workspace(targetWS)
                .sender(sender)
                .receiver(receiver)
                .build();
        dmRepository.save(createdDm);

        return dmMapper.toIDMDto(createdDm);
    }
}
