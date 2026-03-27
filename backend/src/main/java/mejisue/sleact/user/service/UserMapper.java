package mejisue.sleact.user.service;

import lombok.RequiredArgsConstructor;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.dto.UserResDto;
import mejisue.sleact.workspace.dto.WorkspaceResDto;
import mejisue.sleact.workspaceMember.repository.WorkspaceMemberRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserMapper {

    private final WorkspaceMemberRepository workspaceMemberRepository;

    public UserResDto toUserDto(User user) {
        UserResDto userResDto = new UserResDto();
        userResDto.setId(user.getId());
        userResDto.setNickname(user.getNickname());
        userResDto.setEmail(user.getEmail());
        userResDto.setRole(user.getRole().toString());

        List<WorkspaceResDto> workspaceDto = workspaceMemberRepository.findByUserId(user.getId())
                .stream()
                .map(wm -> WorkspaceResDto.from(wm.getWorkspace()))
                .collect(Collectors.toList());
        userResDto.setWorkspaces(workspaceDto);

        return userResDto;
    }
}