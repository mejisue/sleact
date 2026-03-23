package mejisue.sleact.workspace.controller;

import lombok.RequiredArgsConstructor;
import mejisue.sleact.workspace.dto.WorkspaceCreateDto;
import mejisue.sleact.workspace.dto.WorkspaceResDto;
import mejisue.sleact.workspace.service.WorkspaceService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspacesService;

    /** 워크스페이스 생성하기 **/
    @PostMapping
    public WorkspaceResDto createWorkspace(Authentication authentication, @RequestBody WorkspaceCreateDto createDto) {
        return workspacesService.createWorkspace(authentication, createDto);
    }
}
