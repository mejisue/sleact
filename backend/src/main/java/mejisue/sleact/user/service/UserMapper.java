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
        UserResDto iUserDto = new UserResDto();
        iUserDto.setId(user.getId());
        iUserDto.setNickname(user.getNickname());
        iUserDto.setEmail(user.getEmail());

        List<WorkspaceResDto> workspaceDtos = workspaceMemberRepository.findByUserId(user.getId())
                .stream()
                .map(wm -> WorkspaceResDto.from(wm.getWorkspace()))
                .collect(Collectors.toList());
        iUserDto.setWorkspaces(workspaceDtos);

        return iUserDto;
    }
}