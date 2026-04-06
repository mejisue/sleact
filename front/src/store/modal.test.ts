import { describe, it, expect } from 'vitest';
import { create } from 'zustand';
import { combine } from 'zustand/middleware';

// ─── Zustand 스토어 테스트 설명 ────────────────────────────────────
// Zustand 훅(useModalStore, useModalActions 등)은 React 훅이므로
// 컴포넌트 밖에서 직접 호출하면 "Invalid hook call" 에러가 발생.
//
// 해결 방법 두 가지:
// 1. store.getState()로 훅 없이 상태에 직접 접근 (비리액트 코드 테스트)
// 2. renderHook()으로 훅을 컴포넌트처럼 실행 (훅 자체를 테스트)

describe('modal store 로직 검증', () => {
  // 각 테스트마다 독립적인 스토어 인스턴스를 생성
  // → 테스트 간 상태 오염 없음
  function makeStore() {
    return create(
      combine({ isInviteModalOpen: false }, (set) => ({
        openInviteModal: () => set({ isInviteModalOpen: true }),
        closeInviteModal: () => set({ isInviteModalOpen: false }),
      })),
    );
  }

  it('초기 상태에서 isInviteModalOpen은 false다', () => {
    const store = makeStore();
    // getState(): React 렌더링 없이 스토어 상태를 직접 읽음
    expect(store.getState().isInviteModalOpen).toBe(false);
  });

  it('openInviteModal 호출 시 isInviteModalOpen이 true가 된다', () => {
    const store = makeStore();

    store.getState().openInviteModal();

    expect(store.getState().isInviteModalOpen).toBe(true);
  });

  it('closeInviteModal 호출 시 isInviteModalOpen이 false가 된다', () => {
    const store = makeStore();

    store.getState().openInviteModal();   // 먼저 열기
    store.getState().closeInviteModal();  // 닫기

    expect(store.getState().isInviteModalOpen).toBe(false);
  });

  it('open → close → open 반복 동작이 올바르게 작동한다', () => {
    const store = makeStore();

    store.getState().openInviteModal();
    expect(store.getState().isInviteModalOpen).toBe(true);

    store.getState().closeInviteModal();
    expect(store.getState().isInviteModalOpen).toBe(false);

    store.getState().openInviteModal();
    expect(store.getState().isInviteModalOpen).toBe(true);
  });
});
