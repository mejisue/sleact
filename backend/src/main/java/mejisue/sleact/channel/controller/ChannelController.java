package mejisue.sleact.channel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mejisue.sleact.channel.dto.ChannelResDto;
import mejisue.sleact.channel.service.ChannelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ChannelController {

    private final ChannelService channelsService;

    /** 워크스페이스 내부의 내가 속해있는 채널 리스트 가져오기 **/
    @GetMapping("/workspace/{workspaceId}/channels")
    public ResponseEntity<List<ChannelResDto>> listChannels(Authentication authentication, @PathVariable Long workspaceId) {
        log.info("workspaceId: {}", workspaceId);
        String email = authentication.getName();
        List<ChannelResDto> channels = channelsService.getMyChannels(email, workspaceId);
        return ResponseEntity.ok(channels);
    }
}
