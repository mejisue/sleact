package mejisue.sleact.channelChat.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.channel.repository.ChannelRepository;
import mejisue.sleact.channelChat.domain.ChannelChat;
import mejisue.sleact.channelChat.dto.ChannelChatDto;
import mejisue.sleact.channelChat.repository.ChannelChatRepository;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChannelChatService {

    private final ChannelRepository channelRepository;
    private final ChannelChatRepository channelChatRepository;
    private final ChannelChatMapper channelChatMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ChannelChatDto> getChatsfromWSAndChannel(Long workspaceId, String channelName, int perPage, int page) {
        log.info("채팅 목록 조회 | workspaceId: {}, channel: {}, page: {}", workspaceId, channelName, page);

        Channel channel = channelRepository.findChannelWithWorkspaceById(workspaceId, channelName)
                .orElseThrow(() -> new EntityNotFoundException("채널을 찾을 수 없습니다: " + channelName));

        Pageable pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending());

        return channelChatRepository.findByChannelId(channel.getId(), pageable)
                .getContent()
                .stream()
                .map(channelChatMapper::toChannelChatDto)
                .collect(Collectors.toList());
    }

    public ChannelChatDto postChatFromWSAndChannel(Long workspaceId, String channelName, String content, String email) {
        Channel channel = channelRepository.findChannelWithWorkspaceById(workspaceId, channelName)
                .orElseThrow(() -> new EntityNotFoundException("채널을 찾을 수 없습니다: " + channelName));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + email));

        ChannelChat channelChat = ChannelChat.builder()
                .content(content)
                .channel(channel)
                .user(user)
                .build();
        channelChatRepository.save(channelChat);

        return channelChatMapper.toChannelChatDto(channelChat);
    }
}
