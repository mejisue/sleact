package mejisue.sleact.dm.service;

import jakarta.persistence.EntityNotFoundException;
import mejisue.sleact.dm.domain.Dm;
import mejisue.sleact.dm.dto.DmDto;
import mejisue.sleact.dm.repository.DmRepository;
import mejisue.sleact.user.domain.User;
import mejisue.sleact.workspace.domain.Workspace;
import mejisue.sleact.workspaceMember.domain.WorkspaceMember;
import mejisue.sleact.workspaceMember.repository.WorkspaceMemberRepository;
import mejisue.sleact.workspace.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DmServiceTest {

    @InjectMocks
    private DmService dmService;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private DmRepository dmRepository;

    @Mock
    private DmMapper dmMapper;

    private User sender;
    private User receiver;
    private Workspace workspace;
    private WorkspaceMember senderMember;
    private WorkspaceMember receiverMember;

    @BeforeEach
    void setUp() {
        sender   = User.builder().id(1L).email("sender@test.com").nickname("발신자").build();
        receiver = User.builder().id(2L).email("receiver@test.com").nickname("수신자").build();
        workspace = Workspace.builder().id(10L).name("테스트워크스페이스").url("test-ws").owner(sender).build();
        senderMember   = WorkspaceMember.builder().workspace(workspace).user(sender).build();
        receiverMember = WorkspaceMember.builder().workspace(workspace).user(receiver).build();
    }

    // =====================
    // createDm
    // =====================

    @Test
    @DisplayName("DM 생성 성공 - 발신자와 수신자가 모두 워크스페이스 멤버인 경우")
    void createDm_success() {
        // given
        Dm dm = Dm.builder().content("안녕하세요").workspace(workspace).sender(sender).receiver(receiver).build();
        DmDto expectedDto = new DmDto();
        expectedDto.setSenderId(1L);
        expectedDto.setReceiverId(2L);
        expectedDto.setContent("안녕하세요");

        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(10L, "sender@test.com"))
                .willReturn(Optional.of(senderMember));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserId(10L, 2L))
                .willReturn(Optional.of(receiverMember));
        given(dmMapper.toIDMDto(any(Dm.class))).willReturn(expectedDto);

        // when
        DmDto result = dmService.createDm(10L, 2L, "안녕하세요", "sender@test.com");

        // then
        assertThat(result.getContent()).isEqualTo("안녕하세요");
        assertThat(result.getSenderId()).isEqualTo(1L);
        assertThat(result.getReceiverId()).isEqualTo(2L);
        then(dmRepository).should().save(any(Dm.class));
    }

    @Test
    @DisplayName("DM 생성 실패 - 존재하지 않는 워크스페이스")
    void createDm_workspaceNotFound_throwsEntityNotFoundException() {
        // given
        given(workspaceRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dmService.createDm(999L, 2L, "안녕하세요", "sender@test.com"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("DM 생성 실패 - 발신자가 워크스페이스 멤버가 아닌 경우")
    void createDm_senderNotMember_throwsEntityNotFoundException() {
        // given
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(10L, "outsider@test.com"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dmService.createDm(10L, 2L, "안녕하세요", "outsider@test.com"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("보내는 유저");
    }

    @Test
    @DisplayName("DM 생성 실패 - 수신자가 워크스페이스 멤버가 아닌 경우")
    void createDm_receiverNotMember_throwsEntityNotFoundException() {
        // given
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(10L, "sender@test.com"))
                .willReturn(Optional.of(senderMember));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserId(10L, 999L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dmService.createDm(10L, 999L, "안녕하세요", "sender@test.com"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("받는 유저");
    }

    // =====================
    // getDms
    // =====================

    @Test
    @DisplayName("DM 목록 조회 성공 - 두 유저 간 메시지 반환")
    void getDms_success() {
        // given
        Dm dm1 = Dm.builder().content("첫 번째").workspace(workspace).sender(sender).receiver(receiver).build();
        Dm dm2 = Dm.builder().content("두 번째").workspace(workspace).sender(receiver).receiver(sender).build();
        Page<Dm> page = new PageImpl<>(List.of(dm1, dm2));

        DmDto dto1 = new DmDto();
        dto1.setContent("첫 번째");
        DmDto dto2 = new DmDto();
        dto2.setContent("두 번째");

        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(10L, "sender@test.com"))
                .willReturn(Optional.of(senderMember));
        given(dmRepository.findDmsBetweenUsers(eq(10L), eq(1L), eq(2L), any(Pageable.class)))
                .willReturn(page);
        given(dmMapper.toIDMDto(dm1)).willReturn(dto1);
        given(dmMapper.toIDMDto(dm2)).willReturn(dto2);

        // when
        List<DmDto> result = dmService.getDms(10L, 2L, "sender@test.com", 1, 20);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getContent()).isEqualTo("첫 번째");
        assertThat(result.get(1).getContent()).isEqualTo("두 번째");
    }

    @Test
    @DisplayName("DM 목록 조회 성공 - 대화 내역이 없으면 빈 리스트 반환")
    void getDms_noMessages_returnsEmptyList() {
        // given
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(10L, "sender@test.com"))
                .willReturn(Optional.of(senderMember));
        given(dmRepository.findDmsBetweenUsers(eq(10L), eq(1L), eq(2L), any(Pageable.class)))
                .willReturn(Page.empty());

        // when
        List<DmDto> result = dmService.getDms(10L, 2L, "sender@test.com", 1, 20);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("DM 목록 조회 실패 - 존재하지 않는 워크스페이스")
    void getDms_workspaceNotFound_throwsEntityNotFoundException() {
        // given
        given(workspaceRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dmService.getDms(999L, 2L, "sender@test.com", 1, 20))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("DM 목록 조회 실패 - 조회자가 워크스페이스 멤버가 아닌 경우")
    void getDms_callerNotMember_throwsEntityNotFoundException() {
        // given
        given(workspaceRepository.findById(10L)).willReturn(Optional.of(workspace));
        given(workspaceMemberRepository.findByWorkspaceIdAndUserEmail(10L, "outsider@test.com"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dmService.getDms(10L, 2L, "outsider@test.com", 1, 20))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("멤버");
    }
}
