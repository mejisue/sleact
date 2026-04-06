import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import InviteMemberModal from './index';

// ─── 모킹(Mocking) 설명 ────────────────────────────────────────────
// vi.mock(): 특정 모듈을 가짜(mock)로 교체
// 실제 API를 호출하지 않고, 우리가 원하는 값을 반환하도록 제어할 수 있음

// API 모킹: 실제 서버 없이 성공/실패 시나리오를 테스트
vi.mock('@/api/workspace', () => ({
  inviteMember: vi.fn(),
  // vi.fn(): 호출 기록을 추적하는 가짜 함수(호출 여부, 호출 횟수, 전달된 인자를 모두 기록)
}));

vi.mock('@/api/channel', () => ({
  getChannels: vi.fn().mockResolvedValue({
    // mockResolvedValue: 비동기 함수가 성공적으로 이 값을 반환하도록 설정
    data: [
      { id: 1, name: '일반' },
      { id: 2, name: '개발' },
      { id: 3, name: '디자인' },
      { id: 4, name: '마케팅' },
    ],
  }),
}));

// sonner의 toast를 모킹: 실제 알림 대신 함수 호출만 추적
vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

// Zustand store 모킹: 모달이 항상 열려 있는 상태로 고정
vi.mock('@/store/modal', () => ({
  useInviteModalOpen: () => true,  // 모달이 열린 상태
  useModalActions: () => ({
    closeInviteModal: vi.fn(),
    openInviteModal: vi.fn(),
    openWorkspaceModal: vi.fn(),
    closeWorkspaceModal: vi.fn(),
    openChannelModal: vi.fn(),
    closeChannelModal: vi.fn(),
  }),
}));

// ─── 테스트 헬퍼 ───────────────────────────────────────────────────

// React Query는 Provider가 필요 → 테스트용 wrapper 생성
function createWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        // 테스트에서 재시도 비활성화 (실패 시 즉시 에러 처리)
        retry: false,
      },
    },
  });

  // MemoryRouter: 브라우저 없이 라우팅을 시뮬레이션
  // useParams()가 workspaceId를 읽을 수 있도록 경로 설정
  return function Wrapper({ children }: { children: React.ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/workspace/1/channel/1']}>
          <Routes>
            <Route path="/workspace/:workspaceId/channel/:channelId" element={children} />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );
  };
}

// ─── 테스트 ────────────────────────────────────────────────────────

describe('InviteMemberModal', () => {
  // 모킹된 함수들을 import해서 테스트에서 사용
  let mockInviteMember: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    // 각 테스트 전에 mock 상태 초기화 (이전 테스트의 호출 기록 제거)
    vi.clearAllMocks();

    const { inviteMember } = await import('@/api/workspace');
    mockInviteMember = inviteMember as ReturnType<typeof vi.fn>;
  });

  // ── 렌더링 테스트 ──────────────────────────────────────────────

  it('모달이 열려 있을 때 제목이 렌더링된다', () => {
    const { getByText } = render(
      <InviteMemberModal workspaceName="슬랙팀" />,
      { wrapper: createWrapper() },
    );

    // getByText: 해당 텍스트를 포함한 요소를 찾음 (없으면 테스트 실패)
    expect(getByText(/사용자 초대/)).toBeInTheDocument();
    // toBeInTheDocument(): DOM에 실제로 존재하는지 확인 (@testing-library/jest-dom 제공)
  });

  it('이메일 입력 필드가 렌더링된다', () => {
    render(<InviteMemberModal workspaceName="슬랙팀" />, { wrapper: createWrapper() });

    // getByPlaceholderText: placeholder 텍스트로 input 요소 찾기
    expect(screen.getByPlaceholderText('name@gmail.com')).toBeInTheDocument();
  });

  it('보내기 버튼이 처음에는 비활성화되어 있다', () => {
    render(<InviteMemberModal workspaceName="슬랙팀" />, { wrapper: createWrapper() });

    const sendButton = screen.getByRole('button', { name: '보내기' });
    // toBeDisabled(): 버튼이 disabled 상태인지 확인
    expect(sendButton).toBeDisabled();
  });

  // ── 상호작용 테스트 ────────────────────────────────────────────

  it('이메일 입력 시 보내기 버튼이 활성화된다', async () => {
    // userEvent: 실제 사용자 행동을 시뮬레이션 (클릭, 타이핑 등)
    const user = userEvent.setup();

    render(<InviteMemberModal workspaceName="슬랙팀" />, { wrapper: createWrapper() });

    const emailInput = screen.getByPlaceholderText('name@gmail.com');
    // user.type(): 키보드 입력 시뮬레이션
    await user.type(emailInput, 'test@example.com');

    const sendButton = screen.getByRole('button', { name: '보내기' });
    // toBeEnabled(): 버튼이 활성화 상태인지 확인
    expect(sendButton).toBeEnabled();
  });

  it('이메일 입력 후 보내기 버튼 클릭 시 inviteMember API가 호출된다', async () => {
    //하나의 사용자 세션을 만든다. 이 상태를 유지하는 컨텍스트 생성
    const user = userEvent.setup();
    // API 호출 성공 시나리오 설정
    // mockResolvedValue는 "이 비동기 함수가 성공하면 이 값을 반환해" 라고 설정
    mockInviteMember.mockResolvedValue({ data: {} });

    render(<InviteMemberModal workspaceName="슬랙팀" />, { wrapper: createWrapper() });

    await user.type(screen.getByPlaceholderText('name@gmail.com'), 'test@example.com');
    await user.click(screen.getByRole('button', { name: '보내기' }));

    // waitFor: 비동기 작업(API 호출) 완료를 기다림
    await waitFor(() => {
      // toHaveBeenCalledWith: 특정 인자로 호출되었는지 확인
      expect(mockInviteMember).toHaveBeenCalledWith(1, {
        email: 'test@example.com',
        channelNames: [],  // 채널 미선택 시 빈 배열
      });
    });
  });

  // ── 채널 검색 유효성 검사 테스트 ──────────────────────────────

  it('존재하지 않는 채널 검색 시 에러 메시지가 표시된다', async () => {
    const user = userEvent.setup();

    render(<InviteMemberModal workspaceName="슬랙팀" />, { wrapper: createWrapper() });

    // 채널 목록이 로드될 때까지 대기
    await waitFor(() => {
      expect(screen.getByPlaceholderText('채널 검색')).toBeInTheDocument();
    });

    await user.type(screen.getByPlaceholderText('채널 검색'), '없는채널');

    // findByText: 비동기적으로 텍스트 요소를 찾음 (나타날 때까지 대기)
    expect(await screen.findByText('존재하지 않는 채널입니다.')).toBeInTheDocument();
  });

  it('존재하지 않는 채널 검색 시 보내기 버튼이 비활성화된다', async () => {
    const user = userEvent.setup();

    render(<InviteMemberModal workspaceName="슬랙팀" />, { wrapper: createWrapper() });

    await waitFor(() => {
      expect(screen.getByPlaceholderText('채널 검색')).toBeInTheDocument();
    });

    // 이메일 입력 (버튼 활성화 조건 충족)
    await user.type(screen.getByPlaceholderText('name@gmail.com'), 'test@example.com');
    // 존재하지 않는 채널 검색
    await user.type(screen.getByPlaceholderText('채널 검색'), '없는채널xyz');

    await waitFor(() => {
      expect(screen.getByRole('button', { name: '보내기' })).toBeDisabled();
    });
  });

  // ── API 에러 처리 테스트 ───────────────────────────────────────

  it('404 에러 응답 시 "해당 이메일로 가입된 사용자가 없습니다." 토스트가 표시된다', async () => {
    const user = userEvent.setup();
    const { toast } = await import('sonner');

    // mockRejectedValue: API 호출이 실패하는 시나리오 설정
    mockInviteMember.mockRejectedValue({
      response: { status: 404, data: { message: '존재하지 않습니다' } },
    });

    render(<InviteMemberModal workspaceName="슬랙팀" />, { wrapper: createWrapper() });

    await user.type(screen.getByPlaceholderText('name@gmail.com'), 'notexist@example.com');
    await user.click(screen.getByRole('button', { name: '보내기' }));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('해당 이메일로 가입된 사용자가 없습니다.');
    });
  });

  it('이미 멤버인 경우 "이미 워크스페이스 멤버입니다." 토스트가 표시된다', async () => {
    const user = userEvent.setup();
    const { toast } = await import('sonner');

    mockInviteMember.mockRejectedValue({
      response: { status: 400, data: { message: '이미 회원이 워크스페이스에 소속' } },
    });

    render(<InviteMemberModal workspaceName="슬랙팀" />, { wrapper: createWrapper() });

    await user.type(screen.getByPlaceholderText('name@gmail.com'), 'already@example.com');
    await user.click(screen.getByRole('button', { name: '보내기' }));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('이미 워크스페이스 멤버입니다.');
    });
  });
});
