package mejisue.sleact.workspaceMember.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.repository.UserRepository;
import mejisue.sleact.workspace.dto.WorkspaceResDto;
import mejisue.sleact.workspaceMember.repository.WorkspaceMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<WorkspaceResDto> findWorkspacesByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return workspaceMemberRepository.findByUserId(user.getId()).stream()
                .map(wm -> WorkspaceResDto.from(wm.getWorkspace()))
                .collect(Collectors.toList());
    }
}
