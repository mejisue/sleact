import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { renderWithMentions } from './renderWithMentions';

describe('renderWithMentions', () => {

  // ── 일반 텍스트 ──────────────────────────────────────────────────────

  it('일반 텍스트는 그대로 렌더링한다', () => {
    const { container } = render(<p>{renderWithMentions('안녕하세요')}</p>);
    expect(container.textContent).toBe('안녕하세요');
  });

  it('@가 없으면 span 요소가 생성되지 않는다', () => {
    const { container } = render(<p>{renderWithMentions('일반 텍스트')}</p>);
    expect(container.querySelectorAll('span')).toHaveLength(0);
  });

  // ── 멘션 렌더링 ──────────────────────────────────────────────────────

  it('@nickname을 강조 span으로 렌더링한다', () => {
    render(<p>{renderWithMentions('@Alice')}</p>);
    expect(screen.getByText('@Alice').tagName).toBe('SPAN');
  });

  it('여러 멘션을 모두 강조 span으로 렌더링한다', () => {
    render(<p>{renderWithMentions('@Alice 그리고 @Bob')}</p>);
    expect(screen.getByText('@Alice').tagName).toBe('SPAN');
    expect(screen.getByText('@Bob').tagName).toBe('SPAN');
  });

  // ── 혼합 텍스트 ──────────────────────────────────────────────────────

  it('멘션과 일반 텍스트가 혼합된 경우 전체 텍스트 내용을 보존한다', () => {
    const { container } = render(<p>{renderWithMentions('안녕 @Alice 잘 지내?')}</p>);
    expect(container.textContent).toBe('안녕 @Alice 잘 지내?');
  });

  it('혼합 텍스트에서 멘션 부분만 span으로 감싼다', () => {
    render(<p>{renderWithMentions('안녕 @Alice 잘 지내?')}</p>);
    expect(screen.getByText('@Alice').tagName).toBe('SPAN');
  });

  // ── 엣지 케이스 ──────────────────────────────────────────────────────

  it('빈 문자열을 입력하면 아무것도 렌더링하지 않는다', () => {
    const { container } = render(<p>{renderWithMentions('')}</p>);
    expect(container.textContent).toBe('');
  });

  it('@ 단독 문자는 멘션으로 처리하지 않는다', () => {
    const { container } = render(<p>{renderWithMentions('@')}</p>);
    expect(container.querySelectorAll('span')).toHaveLength(0);
  });

  it('@ 뒤에 공백이 오면 멘션으로 처리하지 않는다', () => {
    const { container } = render(<p>{renderWithMentions('@ alice')}</p>);
    expect(container.querySelectorAll('span')).toHaveLength(0);
  });
});
