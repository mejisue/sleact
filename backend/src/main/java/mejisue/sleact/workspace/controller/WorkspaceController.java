package mejisue.sleact.workspace.controller;

import lombok.RequiredArgsConstructor;
import mejisue.sleact.user.dto.UserResDto;
import mejisue.sleact.workspace.dto.InviteMemberDto;
import mejisue.sleact.workspace.dto.WorkspaceCreateDto;
import mejisue.sleact.workspace.dto.WorkspaceResDto;
import mejisue.sleact.workspace.service.WorkspaceService;
import mejisue.sleact.workspaceMember.service.WorkspaceMemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/workspace")
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
    @PostMapping("/{workspaceId}/member")
    public ResponseEntity<?> inviteMembersToWorkspace(@PathVariable Long workspaceId, @RequestBody InviteMemberDto dto) {
        workspacesService.inviteMembersToWorkspace(workspaceId, dto);
        return ResponseEntity.ok("ok");
    }

    /** 워크스페이스 내부의 멤버 목록 가져오기 **/
    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<List<UserResDto>> getMyWorkspaceMembers(@PathVariable Long workspaceId) {
        List<UserResDto> membersInWorkspace = workspacesService.findMembersInWorkspace(workspaceId);
        return ResponseEntity.ok(membersInWorkspace);
    }

    /** 워크스페이스 온라인 멤버 조회 **/
    @GetMapping("/{workspaceId}/members/online")
    public ResponseEntity<Set<String>> getOnlineMembers(@PathVariable Long workspaceId) {
        return ResponseEntity.ok(workspacesService.getOnlineMembers(workspaceId));
    }

    /** 워크스페이스에서 나가기 **/
    @DeleteMapping("/{workspaceId}/member/me")
    public ResponseEntity<?> deleteMemberInWorkspace(@PathVariable Long workspaceId, Authentication authentication) {
        workspacesService.deleteMemberInWorkspace(workspaceId, authentication.getName());
        return ResponseEntity.ok("ok");
    }
}
