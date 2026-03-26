package mejisue.sleact.user.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.channel.repository.ChannelRepository;
import mejisue.sleact.channelMember.domain.ChannelMember;
import mejisue.sleact.channelMember.repository.ChannelMemberRepository;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.dto.UserLoginReqDto;
import mejisue.sleact.user.dto.UserResDto;
import mejisue.sleact.user.dto.UserSaveReqDto;
import mejisue.sleact.user.repository.UserRepository;
import mejisue.sleact.workspace.domain.Workspace;
import mejisue.sleact.workspace.repository.WorkspaceRepository;
import mejisue.sleact.workspaceMember.domain.WorkspaceMember;
import mejisue.sleact.workspaceMember.repository.WorkspaceMemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ChannelRepository channelRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public void signup(UserSaveReqDto dto) {
        validateDuplicatedEmail(dto);

        User newUser = User.builder()
                .email(dto.getEmail())
                .nickname(dto.getNickname())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build();
        userRepository.save(newUser);

        Workspace defaultWS = workspaceRepository.findByName("sleact").orElseGet(() -> {
            User adminUser = userRepository.findByEmail("admin@sleact.com")
                    .orElseThrow(() -> new EntityNotFoundException("어드민 유저가 존재하지 않습니다."));
            return workspaceRepository.save(Workspace.builder()
                    .name("sleact")
                    .url("sleact")
                    .owner(adminUser)
                    .build());
        });

        Channel defaultChannel = channelRepository.findByWorkspaceAndName(defaultWS, "일반").orElseGet(() ->
                channelRepository.save(Channel.builder()
                        .name("일반")
                        .workspace(defaultWS)
                        .build())
        );

        workspaceMemberRepository.save(WorkspaceMember.builder()
                .workspace(defaultWS)
                .user(newUser)
                .build());

        channelMemberRepository.save(ChannelMember.builder()
                .channel(defaultChannel)
                .user(newUser)
                .build());
    }

    @Transactional(readOnly = true)
    public UserResDto getUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일입니다."));
        return userMapper.toUserDto(user);
    }

    public User login(UserLoginReqDto memberLoginDto) {
        User member = userRepository.findByEmail(memberLoginDto.getEmail()).orElseThrow(
                () -> new EntityNotFoundException("존재하지 않는 이메일입니다."));
        if (!passwordEncoder.matches(memberLoginDto.getPassword(), member.getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    private void validateDuplicatedEmail(UserSaveReqDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
    }
}
