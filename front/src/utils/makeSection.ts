import type { IChat } from '@/types';

const makeSection = (chatList: IChat[]): Record<string, IChat[]> => {
  return chatList.reduce<Record<string, IChat[]>>((sections, chat) => {
    const date = new Date(chat.createdAt).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
    if (!sections[date]) {
      sections[date] = [];
    }
    sections[date].push(chat);
    return sections;
  }, {});
};

export default makeSection;
