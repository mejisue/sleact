import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import React from 'react';
import { render, screen, waitFor, act } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter, Route, Routes } from 'react-router-dom';

// ── 모킹 선언 ────────────────────────────────────────────────────────────────

vi.mock('@/providers/StompProvider', () => ({
  default: ({ children }: { children: React.ReactNode }) => <>{children}</>,
  useStompClient: vi.fn(),
}));

vi.mock('@/store/auth', () => ({
  useUser: vi.fn(),
  useSetUser: () => vi.fn(),
}));

vi.mock('@/store/modal', () => ({
  useModalActions: () => ({
    openWorkspaceModal: vi.fn(),
    openChannelModal: vi.fn(),
    openInviteModal: vi.fn(),
  }),
}));

vi.mock('@/api/auth', () => ({ logout: vi.fn().mockResolvedValue({}) }));

vi.mock('@/api/workspace', () => ({
  getWorkspaces: vi.fn().mockResolvedValue({
    data: [{ id: 1, name: '테스트 워크스페이스', url: 'test' }],
  }),
  getWorkspaceMembers: vi.fn().mockResolvedValue({
    data: [
      { id: 1, email: 'me@test.com', nickname: 'Me', role: 'USER' },
      { id: 11, email: 'member11@test.com', nickname: 'Member11', role: 'USER' },
    ],
  }),
  getOnlineMembers: vi.fn().mockResolvedValue({ data: [] }),
}));

vi.mock('@/api/channel', () => ({
  getChannels: vi.fn().mockResolvedValue({ data: [{ id: 1, name: '일반' }] }),
}));

vi.mock('@/components/CreateWorkspaceModal', () => ({ default: () => null }));
vi.mock('@/components/CreateChannelModal', () => ({ default: () => null }));
vi.mock('@/components/InviteMemberModal', () => ({ default: () => null }));

// ── 임포트 (mock 선언 이후) ───────────────────────────────────────────────────
import { useStompClient } from '@/providers/StompProvider';
import { useUser } from '@/store/auth';
import Workspace from './index';

// ── 헬퍼 ─────────────────────────────────────────────────────────────────────

const mockUser = { id: 1, email: 'me@test.com', nickname: 'Me', role: 'USER' as const };

function makeSubscribeSpy(onPresenceCallback?: (cb: (msg: { body: string }) => void) => void) {
  return vi.fn((topic: string, cb: (msg: { body: string }) => void) => {
    if (topic.includes('/presence') && onPresenceCallback) {
      onPresenceCallback(cb);
    }
    return { unsubscribe: vi.fn() };
  });
}

function createWrapper(queryClient: QueryClient) {
  return function Wrapper({ children }: { children: React.ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={['/workspace/1/channel/일반']}>
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

// ── 테스트 ────────────────────────────────────────────────────────────────────

describe('WorkspaceLayout - 온라인 멤버 상태 관리', () => {
  let queryClient: QueryClient;
  let refetchQueriesSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    refetchQueriesSpy = vi.spyOn(queryClient, 'refetchQueries');
    vi.mocked(useUser).mockReturnValue(mockUser);
  });

  afterEach(() => {
    vi.clearAllMocks();
    queryClient.clear();
  });

  // ── 렌더링 ──────────────────────────────────────────────────────────────────

  it('workspaceMembers 중 본인을 제외한 멤버가 사이드바에 렌더링된다', async () => {
    vi.mocked(useStompClient).mockReturnValue({
      client: { subscribe: makeSubscribeSpy() } as any,
      isConnected: true,
    });

    render(<Workspace />, { wrapper: createWrapper(queryClient) });

    await waitFor(() => {
      expect(screen.getByText('Member11')).toBeInTheDocument();
    });
  });

  // ── STOMP 연결 시 refetch ────────────────────────────────────────────────────

  it('STOMP 연결(isConnected: false → true) 시 온라인 멤버 목록을 즉시 재조회한다', async () => {
    // 초기: 연결 안 됨
    vi.mocked(useStompClient).mockReturnValue({
      client: null,
      isConnected: false,
    });

    const { rerender } = render(<Workspace />, { wrapper: createWrapper(queryClient) });

    // 연결 완료로 전환
    vi.mocked(useStompClient).mockReturnValue({
      client: { subscribe: makeSubscribeSpy() } as any,
      isConnected: true,
    });
    rerender(<Workspace />);

    await waitFor(() => {
      expect(refetchQueriesSpy).toHaveBeenCalledWith(
        expect.objectContaining({ queryKey: ['onlineMembers', '1'] }),
      );
    });
  });

  // ── stompPresence 초기화 (핵심 버그 수정 검증) ──────────────────────────────

  it('STOMP 재연결 시 이전 stompPresence가 초기화되어 onlineMembers를 다시 조회한다', async () => {
    /**
     * 시나리오:
     * 1. STOMP 연결 → member11의 "online" 메시지 수신 (stompPresence에 추가)
     * 2. STOMP 재연결 (isConnected: true → false → true)
     * 3. stompPresence가 초기화되고 refetchQueries가 호출됨
     *    → REST가 member11을 오프라인으로 반환하면 올바르게 오프라인으로 표시됨
     */

    let capturedPresenceCb: ((msg: { body: string }) => void) | null = null;

    vi.mocked(useStompClient).mockReturnValue({
      client: {
        subscribe: makeSubscribeSpy((cb) => { capturedPresenceCb = cb; }),
      } as any,
      isConnected: true,
    });

    const { rerender } = render(<Workspace />, { wrapper: createWrapper(queryClient) });

    // presence 구독이 설정될 때까지 대기
    await waitFor(() => expect(capturedPresenceCb).not.toBeNull());

    // member11의 "online" 메시지 수신 → stompPresence에 추가
    act(() => {
      capturedPresenceCb!({ body: JSON.stringify({ email: 'member11@test.com', status: 'online' }) });
    });

    // 연결 끊김
    vi.mocked(useStompClient).mockReturnValue({ client: null, isConnected: false });
    rerender(<Workspace />);

    // 재연결 — 이 시점에 stompPresence가 초기화되고 refetchQueries가 호출되어야 함
    vi.mocked(useStompClient).mockReturnValue({
      client: {
        subscribe: makeSubscribeSpy((cb) => { capturedPresenceCb = cb; }),
      } as any,
      isConnected: true,
    });
    rerender(<Workspace />);

    // refetchQueries가 재연결 시에도 호출됨 (총 2회 이상: 최초 연결 + 재연결)
    await waitFor(() => {
      expect(refetchQueriesSpy).toHaveBeenCalledTimes(2);
    });
  });

  it('STOMP "online" 메시지 수신 후 재연결 없이는 stompPresence가 유지된다', async () => {
    /**
     * 정상 케이스 검증:
     * 재연결 없이 STOMP 메시지만 수신한 경우, stompPresence가 유지되어야 함
     * (초기 연결 시 refetchQueries 1회만 호출)
     */

    let capturedPresenceCb: ((msg: { body: string }) => void) | null = null;

    vi.mocked(useStompClient).mockReturnValue({
      client: {
        subscribe: makeSubscribeSpy((cb) => { capturedPresenceCb = cb; }),
      } as any,
      isConnected: true,
    });

    render(<Workspace />, { wrapper: createWrapper(queryClient) });

    await waitFor(() => expect(capturedPresenceCb).not.toBeNull());

    act(() => {
      capturedPresenceCb!({ body: JSON.stringify({ email: 'member11@test.com', status: 'online' }) });
    });

    // 재연결 없음 → refetchQueries는 최초 1회만 호출됨
    await waitFor(() => {
      expect(refetchQueriesSpy).toHaveBeenCalledTimes(1);
    });
  });
});
