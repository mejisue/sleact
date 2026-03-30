import { describe, it, expect } from 'vitest';
import { getKoreanParticle } from './koreanParticle';

// describe: 연관된 테스트를 그룹으로 묶는 블록
describe('getKoreanParticle', () => {
  // it (또는 test): 하나의 테스트 케이스
  // "~이면 ~해야 한다" 형식으로 작성하는 것이 관례

  it('빈 문자열이면 "으로"를 반환한다', () => {
    expect(getKoreanParticle('')).toBe('으로');
    //     ↑ 검사할 값        ↑ 기대 결과 (toBe: 엄격한 일치)
  });

  it('받침 없는 한글 (가, 나, 소)이면 "로"를 반환한다', () => {
    expect(getKoreanParticle('가')).toBe('로');
    expect(getKoreanParticle('나')).toBe('로');
    expect(getKoreanParticle('소')).toBe('로');
  });

  it('받침 있는 한글 (각, 남, 슬)이면 "으로"를 반환한다', () => {
    expect(getKoreanParticle('각')).toBe('으로');
    expect(getKoreanParticle('남')).toBe('으로');
    expect(getKoreanParticle('슬')).toBe('으로');
  });

  it('한글이 아닌 문자 (영어, 숫자)이면 "으로"를 반환한다', () => {
    expect(getKoreanParticle('A')).toBe('으로');
    expect(getKoreanParticle('z')).toBe('으로');
    expect(getKoreanParticle('1')).toBe('으로');
  });

  it('여러 글자 문자열은 마지막 글자 기준으로 판단한다', () => {
    expect(getKoreanParticle('워크스페이스')).toBe('로');   // 마지막: 스 (받침X → '로')
    expect(getKoreanParticle('일반채널')).toBe('으로');      // 마지막: 널 (받침O → '으로')
  });
});
