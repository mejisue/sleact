import { describe, it, expect } from 'vitest';
import makeSection from './makeSection';

// makeSection은 createdAt: string 필드를 가진 모든 타입에 사용할 수 있는 제네릭 함수

describe('makeSection', () => {

  // ── IChat 형태 ──────────────────────────────────────────────────────

  it('같은 날 메시지들은 하나의 섹션으로 묶인다', () => {
    const messages = [
      { id: 1, content: '첫 번째', createdAt: '2024-03-01T10:00:00' },
      { id: 2, content: '두 번째', createdAt: '2024-03-01T11:00:00' },
      { id: 3, content: '세 번째', createdAt: '2024-03-01T23:59:00' },
    ];

    const result = makeSection(messages);

    // 섹션이 1개만 생성됨
    expect(Object.keys(result)).toHaveLength(1);
    // 해당 섹션에 메시지 3개 포함
    const section = Object.values(result)[0];
    expect(section).toHaveLength(3);
  });

  it('다른 날 메시지들은 날짜별로 분리된다', () => {
    const messages = [
      { id: 1, content: '어제 메시지', createdAt: '2024-03-01T10:00:00' },
      { id: 2, content: '오늘 메시지', createdAt: '2024-03-02T10:00:00' },
    ];

    const result = makeSection(messages);

    expect(Object.keys(result)).toHaveLength(2);
  });

  it('섹션 키는 한국어 날짜 형식(YYYY. MM. DD.)이다', () => {
    const messages = [
      { id: 1, content: '메시지', createdAt: '2024-03-05T10:00:00' },
    ];

    const result = makeSection(messages);
    const dateKey = Object.keys(result)[0];

    // ko-KR 로케일 날짜 형식: "2024. 03. 05." 형태
    expect(dateKey).toMatch(/2024/);
    expect(dateKey).toMatch(/03/);
    expect(dateKey).toMatch(/05/);
  });

  it('빈 배열을 전달하면 빈 객체를 반환한다', () => {
    const result = makeSection([]);

    expect(result).toEqual({});
  });

  it('메시지가 1개이면 섹션 1개, 항목 1개를 반환한다', () => {
    const messages = [
      { id: 1, content: '혼자', createdAt: '2024-03-01T10:00:00' },
    ];

    const result = makeSection(messages);

    expect(Object.keys(result)).toHaveLength(1);
    expect(Object.values(result)[0]).toHaveLength(1);
  });

  it('섹션 내 메시지 순서가 입력 순서와 동일하게 유지된다', () => {
    const messages = [
      { id: 1, content: '첫 번째', createdAt: '2024-03-01T09:00:00' },
      { id: 2, content: '두 번째', createdAt: '2024-03-01T10:00:00' },
      { id: 3, content: '세 번째', createdAt: '2024-03-01T11:00:00' },
    ];

    const result = makeSection(messages);
    const section = Object.values(result)[0];

    expect(section[0].content).toBe('첫 번째');
    expect(section[1].content).toBe('두 번째');
    expect(section[2].content).toBe('세 번째');
  });

  // ── IDm 형태 (제네릭 검증) ──────────────────────────────────────────

  it('IDm 형태 객체도 올바르게 섹션으로 묶인다', () => {
    // makeSection이 IChat뿐 아니라 IDm 등 createdAt을 가진 모든 타입에 동작함을 확인
    const dms = [
      { id: 1, content: 'dm1', senderId: 1, receiverId: 2, createdAt: '2024-03-01T10:00:00' },
      { id: 2, content: 'dm2', senderId: 2, receiverId: 1, createdAt: '2024-03-01T11:00:00' },
      { id: 3, content: 'dm3', senderId: 1, receiverId: 2, createdAt: '2024-03-02T10:00:00' },
    ];

    const result = makeSection(dms);

    // 3월 1일 2개, 3월 2일 1개 → 섹션 2개
    expect(Object.keys(result)).toHaveLength(2);
    const firstDaySection = Object.values(result)[0];
    expect(firstDaySection).toHaveLength(2);
  });
});
