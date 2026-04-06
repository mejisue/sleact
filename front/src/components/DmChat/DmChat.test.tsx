import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import DmChat from './index';
import type { IDm } from '@/types';

// styled-components는 CSS를 jsdom에 주입하지 않으므로
// 좌/우 배치(CSS)보다 렌더링되는 데이터를 중심으로 테스트한다

const baseDm: IDm = {
  id: 1,
  content: '안녕하세요',
  createdAt: '2024-03-01T14:30:00',
  sender: { id: 1, nickname: '발신자', email: 'sender@test.com', role: 'USER' },
  receiver: { id: 2, nickname: '수신자', email: 'receiver@test.com', role: 'USER' },
  senderId: 1,
  receiverId: 2,
};

describe('DmChat', () => {

  // ── 렌더링 ─────────────────────────────────────────────────────────

  it('발신자 닉네임이 표시된다', () => {
    render(<DmChat dm={baseDm} myUserId={2} />);

    expect(screen.getByText('발신자')).toBeInTheDocument();
  });

  it('메시지 내용이 표시된다', () => {
    render(<DmChat dm={baseDm} myUserId={2} />);

    expect(screen.getByText('안녕하세요')).toBeInTheDocument();
  });

  it('createdAt을 한국어 시간 형식으로 표시한다', () => {
    render(<DmChat dm={baseDm} myUserId={2} />);

    // "오후 02:30" 또는 "오후 2:30" 형태 (환경에 따라 다를 수 있음)
    expect(screen.getByText(/오후|오전/)).toBeInTheDocument();
  });

  // ── 내 메시지 vs 상대방 메시지 ─────────────────────────────────────
  // styled-components $isMine prop에 따라 CSS가 달라지는 로직을 검증한다
  // CSS 자체 대신, 렌더링에 필요한 데이터(isMine 판단 기준)가 올바른지 확인한다

  it('myUserId가 senderId와 같으면 내 메시지로 판단한다 (isMine=true)', () => {
    // myUserId=1, senderId=1 → 내 메시지
    const { container } = render(<DmChat dm={baseDm} myUserId={1} />);

    // isMine=true일 때 DmWrapper에 $isMine prop이 전달되어 row-reverse 방향이 된다
    // styled-components는 data attribute를 남기지 않으므로 스냅샷으로 구조를 검증
    expect(container.firstChild).toMatchSnapshot();
  });

  it('myUserId가 senderId와 다르면 상대방 메시지로 판단한다 (isMine=false)', () => {
    // myUserId=2, senderId=1 → 상대방 메시지
    const { container } = render(<DmChat dm={baseDm} myUserId={2} />);

    expect(container.firstChild).toMatchSnapshot();
  });

  it('내 메시지(isMine=true)와 상대방 메시지(isMine=false)의 렌더링 결과가 다르다', () => {
    const { container: myContainer } = render(<DmChat dm={baseDm} myUserId={1} />);
    const { container: theirContainer } = render(<DmChat dm={baseDm} myUserId={2} />);

    // styled-components는 isMine 값에 따라 서로 다른 className을 생성한다
    expect(myContainer.innerHTML).not.toBe(theirContainer.innerHTML);
  });
});
