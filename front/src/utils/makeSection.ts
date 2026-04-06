const makeSection = <T extends { createdAt: string }>(list: T[]): Record<string, T[]> => {
  return list.reduce<Record<string, T[]>>((sections, item) => {
    const date = new Date(item.createdAt).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    });
    if (!sections[date]) {
      sections[date] = [];
    }
    sections[date].push(item);
    return sections;
  }, {});
};

export default makeSection;
