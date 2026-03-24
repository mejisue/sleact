package mejisue.sleact.user.dto;

import lombok.Data;
import mejisue.sleact.workspace.dto.WorkspaceResDto;

import java.util.List;

@Data
public class UserResDto {
    private Long id;
    private String nickname;
    private String email;
    private List<WorkspaceResDto> workspaces;
    /**
     * IWorkspaceDto:
     * id, name, url, ownerId
     */

}
