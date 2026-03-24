package mejisue.sleact.channel.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mejisue.sleact.channel.dto.ChannelResDto;
import mejisue.sleact.channel.dto.CreateChannelDto;
import mejisue.sleact.channel.service.ChannelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ChannelController {

    private final ChannelService channelService;

    /** 워크스페이스 내부의 내가 속해있는 채널 리스트 가져오기 **/
    @GetMapping("/workspace/{workspaceId}/channels")
    public ResponseEntity<List<ChannelResDto>> listChannels(Authentication authentication, @PathVariable Long workspaceId) {
        log.info("workspaceId: {}", workspaceId);
        String email = authentication.getName();
        List<ChannelResDto> channels = channelService.getMyChannels(email, workspaceId);
        return ResponseEntity.ok(channels);
    }

    /** 현재 워크스페이스 내부에 채널 생성하기 **/
    @PostMapping("/workspace/{workspaceId}/channels")
    public ResponseEntity<ChannelResDto> createChannel(
            Authentication authentication, @PathVariable Long workspaceId, @RequestBody CreateChannelDto dto) {
        String email = authentication.getName();
        ChannelResDto newChannel = channelService.createChannel(email, workspaceId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newChannel);
    }
}
