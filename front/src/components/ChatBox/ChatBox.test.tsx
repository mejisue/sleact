import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useState } from 'react';
import ChatBox from './index';
import type { IUser } from '@/types';

const members: IUser[] = [
  { id: 1, nickname: 'Alice', email: 'alice@test.com', role: 'USER' },
  { id: 2, nickname: 'Bob', email: 'bob@test.com', role: 'USER' },
  { id: 3, nickname: 'Aline', email: 'aline@test.com', role: 'USER' },
];

function Wrapper({ members: m }: { members?: IUser[] }) {
  const [chat, setChat] = useState('');
  return (
    <ChatBox
      chat={chat}
      onChangeChat={(e) => setChat(e.target.value)}
      onSubmitForm={vi.fn()}
      placeholder="메시지 보내기"
      members={m}
      setChat={setChat}
    />
  );
}

describe('ChatBox - @ 멘션 드롭다운', () => {

  // ── 드롭다운 표시/숨김 ────────────────────────────────────────────────

  it('@를 입력하면 전체 멤버 목록이 표시된다', async () => {
    const user = userEvent.setup();
    render(<Wrapper members={members} />);

    await user.type(screen.getByRole('textbox'), '@');

    expect(screen.getByText('Alice')).toBeInTheDocument();
    expect(screen.getByText('Bob')).toBeInTheDocument();
    expect(screen.getByText('Aline')).toBeInTheDocument();
  });

  it('드롭다운 하단에 키보드 조작 안내 힌트가 표시된다', async () => {
    const user = userEvent.setup();
    render(<Wrapper members={members} />);

    await user.type(screen.getByRole('textbox'), '@');

    expect(screen.getByText(/↑↓ 키로 이동/)).toBeInTheDocument();
  });

  it('members prop이 없으면 @ 입력 후 드롭다운이 표시되지 않는다', async () => {
    const user = userEvent.setup();
    render(<Wrapper />);

    await user.type(screen.getByRole('textbox'), '@');

    expect(screen.queryByText(/↑↓ 키로 이동/)).not.toBeInTheDocument();
  });

  it('Esc 키를 누르면 드롭다운이 닫힌다', async () => {
    const user = userEvent.setup();
    render(<Wrapper members={members} />);

    const textarea = screen.getByRole('textbox');
    await user.type(textarea, '@');
    expect(screen.getByText('Alice')).toBeInTheDocument();

    await user.keyboard('{Escape}');

    expect(screen.queryByText('Alice')).not.toBeInTheDocument();
  });

  it('@ 뒤에 공백을 입력하면 드롭다운이 닫힌다', async () => {
    const user = userEvent.setup();
    render(<Wrapper members={members} />);

    await user.type(screen.getByRole('textbox'), '@ ');

    expect(screen.queryByText(/↑↓ 키로 이동/)).not.toBeInTheDocument();
  });

  // ── 필터링 ──────────────────────────────────────────────────────────

  it('@A 입력 시 "A"를 포함한 멤버만 필터링된다', async () => {
    const user = userEvent.setup();
    render(<Wrapper members={members} />);

    await user.type(screen.getByRole('textbox'), '@A');

    expect(screen.getByText('Alice')).toBeInTheDocument();
    expect(screen.getByText('Aline')).toBeInTheDocument();
    expect(screen.queryByText('Bob')).not.toBeInTheDocument();
  });

  it('매칭되는 멤버가 없으면 드롭다운이 표시되지 않는다', async () => {
    const user = userEvent.setup();
    render(<Wrapper members={members} />);

    await user.type(screen.getByRole('textbox'), '@xyz');

    expect(screen.queryByText(/↑↓ 키로 이동/)).not.toBeInTheDocument();
  });

  // ── 멤버 선택 ────────────────────────────────────────────────────────

  it('멤버를 클릭하면 @nickname 형태로 textarea에 입력된다', async () => {
    const user = userEvent.setup();
    render(<Wrapper members={members} />);

    const textarea = screen.getByRole('textbox');
    await user.type(textarea, '@');
    await user.click(screen.getByText('Alice'));

    expect(textarea).toHaveValue('@Alice ');
  });

  it('Enter 키로 첫 번째 멤버가 선택된다', async () => {
    const user = userEvent.setup();
    render(<Wrapper members={members} />);

    const textarea = screen.getByRole('textbox');
    await user.type(textarea, '@A');
    await user.keyboard('{Enter}');

    expect(textarea).toHaveValue('@Alice ');
  });

  it('ArrowDown 후 Enter로 다음 멤버가 선택된다', async () => {
    const user = userEvent.setup();
    render(<Wrapper members={members} />);

    const textarea = screen.getByRole('textbox');
    await user.type(textarea, '@A');
    await user.keyboard('{ArrowDown}{Enter}');

    expect(textarea).toHaveValue('@Aline ');
  });

  it('ArrowUp으로 인덱스가 순환되어 마지막 멤버가 선택된다', async () => {
    const user = userEvent.setup();
    render(<Wrapper members={members} />);

    const textarea = screen.getByRole('textbox');
    await user.type(textarea, '@A');
    // 초기 index=0, ArrowUp → (0 - 1 + 2) % 2 = 1 → Aline
    await user.keyboard('{ArrowUp}{Enter}');

    expect(textarea).toHaveValue('@Aline ');
  });

  it('멘션 선택 후 드롭다운이 닫힌다', async () => {
    const user = userEvent.setup();
    render(<Wrapper members={members} />);

    const textarea = screen.getByRole('textbox');
    await user.type(textarea, '@');
    await user.click(screen.getByText('Alice'));

    expect(screen.queryByText(/↑↓ 키로 이동/)).not.toBeInTheDocument();
  });

  it('기존 텍스트 중간에서 멘션을 선택하면 해당 위치에만 삽입된다', async () => {
    const user = userEvent.setup();
    render(<Wrapper members={members} />);

    const textarea = screen.getByRole('textbox');
    // "안녕 @" 입력 후 Alice 선택
    await user.type(textarea, '안녕 @');
    await user.click(screen.getByText('Alice'));

    expect(textarea).toHaveValue('안녕 @Alice ');
  });

  // ── 폼 제출 ──────────────────────────────────────────────────────────

  it('드롭다운이 없을 때 Enter 키를 누르면 onSubmitForm이 호출된다', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    const [chat, setChat] = [{ value: '' }, { setter: vi.fn() }];

    function SubmitWrapper() {
      const [value, setValue] = useState('hello');
      return (
        <ChatBox
          chat={value}
          onChangeChat={(e) => setValue(e.target.value)}
          onSubmitForm={onSubmit}
          placeholder="메시지 보내기"
          setChat={setValue}
        />
      );
    }

    render(<SubmitWrapper />);
    await user.click(screen.getByRole('textbox'));
    await user.keyboard('{Enter}');

    expect(onSubmit).toHaveBeenCalledTimes(1);
    void chat; void setChat;
  });

  it('Shift+Enter는 onSubmitForm을 호출하지 않는다', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();

    function SubmitWrapper() {
      const [value, setValue] = useState('hello');
      return (
        <ChatBox
          chat={value}
          onChangeChat={(e) => setValue(e.target.value)}
          onSubmitForm={onSubmit}
          placeholder="메시지 보내기"
          setChat={setValue}
        />
      );
    }

    render(<SubmitWrapper />);
    await user.click(screen.getByRole('textbox'));
    await user.keyboard('{Shift>}{Enter}{/Shift}');

    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('입력값이 비어있으면 전송 버튼이 비활성화된다', () => {
    render(<Wrapper members={members} />);
    expect(screen.getByRole('button')).toBeDisabled();
  });

  it('입력값이 있으면 전송 버튼이 활성화된다', async () => {
    const user = userEvent.setup();
    render(<Wrapper members={members} />);

    await user.type(screen.getByRole('textbox'), '안녕');

    expect(screen.getByRole('button')).not.toBeDisabled();
  });
});
