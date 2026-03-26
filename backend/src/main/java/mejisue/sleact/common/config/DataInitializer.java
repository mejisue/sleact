package mejisue.sleact.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mejisue.sleact.channel.domain.Channel;
import mejisue.sleact.channel.repository.ChannelRepository;
import mejisue.sleact.channelMember.domain.ChannelMember;
import mejisue.sleact.channelMember.repository.ChannelMemberRepository;
import mejisue.sleact.user.domain.Role;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.repository.UserRepository;
import mejisue.sleact.workspace.domain.Workspace;
import mejisue.sleact.workspace.repository.WorkspaceRepository;
import mejisue.sleact.workspaceMember.domain.WorkspaceMember;
import mejisue.sleact.workspaceMember.repository.WorkspaceMemberRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ChannelRepository channelRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User adminUser = userRepository.findByEmail("admin@sleact.com").orElseGet(() -> {
            User admin = User.builder()
                    .email("admin@sleact.com")
                    .nickname("관리자")
                    .password(passwordEncoder.encode("admin123!"))
                    .role(Role.ADMIN)
                    .build();
            return userRepository.save(admin);
        });

        Workspace defaultWS = workspaceRepository.findByName("sleact").orElseGet(() ->
                workspaceRepository.save(Workspace.builder()
                        .name("sleact")
                        .url("sleact")
                        .owner(adminUser)
                        .build())
        );

        Channel defaultChannel = channelRepository.findByWorkspaceAndName(defaultWS, "일반").orElseGet(() ->
                channelRepository.save(Channel.builder()
                        .name("일반")
                        .workspace(defaultWS)
                        .build())
        );

        if (workspaceMemberRepository.findByWorkspaceIdAndUserId(defaultWS.getId(), adminUser.getId()).isEmpty()) {
            workspaceMemberRepository.save(WorkspaceMember.builder()
                    .workspace(defaultWS)
                    .user(adminUser)
                    .build());
        }

        if (channelMemberRepository.findByChannelIdAndUserEmail(defaultChannel.getId(), adminUser.getEmail()).isEmpty()) {
            channelMemberRepository.save(ChannelMember.builder()
                    .channel(defaultChannel)
                    .user(adminUser)
                    .build());
        }

        log.info("[DataInitializer] 기본 데이터 초기화 완료 - admin: {}, workspace: {}, channel: {}",
                adminUser.getEmail(), defaultWS.getName(), defaultChannel.getName());
    }
}
