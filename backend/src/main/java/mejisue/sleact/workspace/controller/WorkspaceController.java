package mejisue.sleact.workspace.controller;

import lombok.RequiredArgsConstructor;
import mejisue.sleact.workspace.dto.InviteMemberDto;
import mejisue.sleact.workspace.dto.WorkspaceCreateDto;
import mejisue.sleact.workspace.dto.WorkspaceResDto;
import mejisue.sleact.workspace.service.WorkspaceService;
import mejisue.sleact.workspaceMember.service.WorkspaceMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspacesService;
    private final WorkspaceMemberService workspaceMemberService;

    /** 워크스페이스 생성하기 **/
    @PostMapping
    public WorkspaceResDto createWorkspace(Authentication authentication, @RequestBody WorkspaceCreateDto createDto) {
        return workspacesService.createWorkspace(authentication.getName(), createDto);
    }

    /** 내 워크스페이스 목록 가져오기 **/
    @GetMapping
    public ResponseEntity<List<WorkspaceResDto>> getMyWorkspaces(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(workspaceMemberService.findWorkspacesByEmail(email));
    }

    /** 워크스페이스로 멤버 초대하기(워크스페이스, 일반채널 -> 기본채널) **/
    @PostMapping("/{workspace}/members")
    public ResponseEntity<?> inviteMembersToWorkspace(@PathVariable String workspace, @RequestBody InviteMemberDto dto) {
        workspacesService.inviteMembersToWorkspace(workspace, dto);
        return ResponseEntity.ok("ok");
    }
}
