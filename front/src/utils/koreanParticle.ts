/**
 * 한국어 조사 선택 유틸
 * 마지막 글자의 받침 유무에 따라 '으로' 또는 '로' 반환
 */
export function getKoreanParticle(name: string): string {
  if (!name) return '으로';
  const code = name.charCodeAt(name.length - 1);
  if (code < 0xac00 || code > 0xd7a3) return '으로';
  return (code - 0xac00) % 28 === 0 ? '로' : '으로';
}
