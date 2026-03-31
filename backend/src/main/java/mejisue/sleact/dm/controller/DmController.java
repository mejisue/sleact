package mejisue.sleact.dm.controller;

import lombok.RequiredArgsConstructor;
import mejisue.sleact.dm.dto.DmDto;
import mejisue.sleact.dm.service.DmService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DmController {

    private final DmService dmService;

    /**
     * 두 유저 간 DM 목록 조회
     * GET /api/workspaces/{workspaceId}/dms/{userId}?page=1&perPage=20
     */
    @GetMapping("/workspaces/{workspaceId}/dms/{userId}")
    public ResponseEntity<List<DmDto>> getDms(
            @PathVariable Long workspaceId,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int perPage,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<DmDto> dms = dmService.getDms(workspaceId, userId, userDetails.getUsername(), page, perPage);
        return ResponseEntity.ok(dms);
    }
}
