package mejisue.sleact.workspaceMember.service;

import jakarta.persistence.EntityNotFoundException;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.user.repository.UserRepository;
import mejisue.sleact.workspace.domain.Workspace;
import mejisue.sleact.workspace.dto.WorkspaceResDto;
import mejisue.sleact.workspaceMember.domain.WorkspaceMember;
import mejisue.sleact.workspaceMember.repository.WorkspaceMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WorkspaceMemberServiceTest {

    @InjectMocks
    private WorkspaceMemberService workspaceMemberService;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("내 워크스페이스 목록 조회 성공")
    void findWorkspacesByEmail_success() {
        // given
        User owner = User.builder().email("test@test.com").build();
        Workspace workspace1 = Workspace.builder().name("워크스페이스1").url("ws1").owner(owner).build();
        Workspace workspace2 = Workspace.builder().name("워크스페이스2").url("ws2").owner(owner).build();
        List<WorkspaceMember> members = List.of(
                WorkspaceMember.builder().workspace(workspace1).user(owner).build(),
                WorkspaceMember.builder().workspace(workspace2).user(owner).build()
        );

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(owner));
        given(workspaceMemberRepository.findByUserId(owner.getId())).willReturn(members);

        // when
        List<WorkspaceResDto> result = workspaceMemberService.findWorkspacesByEmail("test@test.com");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("워크스페이스1");
        assertThat(result.get(1).getName()).isEqualTo("워크스페이스2");
    }

    @Test
    @DisplayName("존재하지 않는 유저 조회 시 EntityNotFoundException 발생")
    void findWorkspacesByEmail_userNotFound_throwsEntityNotFoundException() {
        // given
        given(userRepository.findByEmail("notfound@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workspaceMemberService.findWorkspacesByEmail("notfound@test.com"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }
}
