package mejisue.sleact.workspace.dto;

import lombok.Builder;
import lombok.Getter;
import mejisue.sleact.workspace.domain.Workspace;

@Getter
@Builder
public class WorkspaceResDto {
    private Long id;
    private String name;
    private String url;
    private Long ownerId;

    public static WorkspaceResDto from(Workspace workspace) {
        return WorkspaceResDto.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .url(workspace.getUrl())
                .ownerId(workspace.getOwner().getId())
                .build();
    }
}
