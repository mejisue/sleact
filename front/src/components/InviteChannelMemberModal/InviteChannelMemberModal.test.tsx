import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import InviteChannelMemberModal from './index';

// ─── 모킹 설정 ────────────────────────────────────────────────────────
// inviteMemberToChannel만 사용하므로 해당 함수만 모킹
vi.mock('@/api/channel', () => ({
  inviteMemberToChannel: vi.fn(),
}));

vi.mock('sonner', () => ({
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}));

// 모달이 항상 열려 있는 상태로 고정
vi.mock('@/store/modal', () => ({
  useInviteChannelMemberModalOpen: () => true,
  useModalActions: () => ({
    closeInviteChannelMemberModal: vi.fn(),
  }),
}));

// ─── 테스트 헬퍼 ───────────────────────────────────────────────────────
// useParams()가 workspaceId와 channelName을 읽을 수 있도록 경로 설정
function createWrapper(channelName = '개발') {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });

  return function Wrapper({ children }: { children: React.ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={[`/workspace/1/channel/${channelName}`]}>
          <Routes>
            <Route
              path="/workspace/:workspaceId/channel/:channelName"
              element={children}
            />
          </Routes>
        </MemoryRouter>
      </QueryClientProvider>
    );
  };
}

// ─── 테스트 ────────────────────────────────────────────────────────────

describe('InviteChannelMemberModal', () => {
  let mockInviteMemberToChannel: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    vi.clearAllMocks();
    const { inviteMemberToChannel } = await import('@/api/channel');
    mockInviteMemberToChannel = inviteMemberToChannel as ReturnType<typeof vi.fn>;
  });

  // ── 렌더링 테스트 ──────────────────────────────────────────────────

  it('채널명이 모달 제목에 표시된다', () => {
    render(<InviteChannelMemberModal />, { wrapper: createWrapper('개발') });

    // 채널명이 제목에 포함되어야 함
    expect(screen.getByText(/#개발에 사람 추가/)).toBeInTheDocument();
  });

  it('이메일 입력 필드가 렌더링된다', () => {
    render(<InviteChannelMemberModal />, { wrapper: createWrapper() });

    expect(screen.getByPlaceholderText('name@gmail.com')).toBeInTheDocument();
  });

  it('추가하기 버튼이 처음에는 비활성화되어 있다', () => {
    render(<InviteChannelMemberModal />, { wrapper: createWrapper() });

    expect(screen.getByRole('button', { name: '추가하기' })).toBeDisabled();
  });

  // ── 상호작용 테스트 ────────────────────────────────────────────────

  it('이메일 입력 시 추가하기 버튼이 활성화된다', async () => {
    const user = userEvent.setup();
    render(<InviteChannelMemberModal />, { wrapper: createWrapper() });

    await user.type(screen.getByPlaceholderText('name@gmail.com'), 'test@example.com');

    expect(screen.getByRole('button', { name: '추가하기' })).toBeEnabled();
  });

  it('추가하기 버튼 클릭 시 올바른 인자로 API가 호출된다', async () => {
    const user = userEvent.setup();
    mockInviteMemberToChannel.mockResolvedValue({ data: {} });

    render(<InviteChannelMemberModal />, { wrapper: createWrapper('개발') });

    await user.type(screen.getByPlaceholderText('name@gmail.com'), 'test@example.com');
    await user.click(screen.getByRole('button', { name: '추가하기' }));

    await waitFor(() => {
      // workspaceId=1, channelName='개발', email로 호출되었는지 확인
      expect(mockInviteMemberToChannel).toHaveBeenCalledWith(1, '개발', {
        email: 'test@example.com',
      });
    });
  });

  it('초대 성공 시 성공 토스트가 표시된다', async () => {
    const user = userEvent.setup();
    const { toast } = await import('sonner');
    mockInviteMemberToChannel.mockResolvedValue({ data: {} });

    render(<InviteChannelMemberModal />, { wrapper: createWrapper('개발') });

    await user.type(screen.getByPlaceholderText('name@gmail.com'), 'test@example.com');
    await user.click(screen.getByRole('button', { name: '추가하기' }));

    await waitFor(() => {
      expect(toast.success).toHaveBeenCalledWith(
        'test@example.com 님을 #개발 채널에 추가했습니다.',
      );
    });
  });

  // ── 에러 처리 테스트 ───────────────────────────────────────────────

  it('404 에러 시 워크스페이스 멤버가 아니라는 에러 토스트가 표시된다', async () => {
    const user = userEvent.setup();
    const { toast } = await import('sonner');
    mockInviteMemberToChannel.mockRejectedValue({
      response: { status: 404, data: { message: '존재하지 않습니다' } },
    });

    render(<InviteChannelMemberModal />, { wrapper: createWrapper() });

    await user.type(screen.getByPlaceholderText('name@gmail.com'), 'none@example.com');
    await user.click(screen.getByRole('button', { name: '추가하기' }));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith(
        '해당 이메일로 가입된 사용자가 없거나, 워크스페이스 멤버가 아닙니다.',
      );
    });
  });

  it('이미 채널 멤버인 경우 에러 토스트가 표시된다', async () => {
    const user = userEvent.setup();
    const { toast } = await import('sonner');
    mockInviteMemberToChannel.mockRejectedValue({
      response: { status: 400, data: { message: '이미 채널 멤버입니다' } },
    });

    render(<InviteChannelMemberModal />, { wrapper: createWrapper() });

    await user.type(screen.getByPlaceholderText('name@gmail.com'), 'already@example.com');
    await user.click(screen.getByRole('button', { name: '추가하기' }));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('이미 채널 멤버입니다.');
    });
  });

  it('그 외 에러 시 기본 에러 토스트가 표시된다', async () => {
    const user = userEvent.setup();
    const { toast } = await import('sonner');
    mockInviteMemberToChannel.mockRejectedValue({
      response: { status: 500, data: { message: '서버 오류' } },
    });

    render(<InviteChannelMemberModal />, { wrapper: createWrapper() });

    await user.type(screen.getByPlaceholderText('name@gmail.com'), 'test@example.com');
    await user.click(screen.getByRole('button', { name: '추가하기' }));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith('추가에 실패했습니다. 다시 시도해주세요.');
    });
  });
});
