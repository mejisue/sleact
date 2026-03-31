import { getDms } from '@/api/dm';
import { getWorkspaceMembers } from '@/api/workspace';
import ChatBox from '@/components/ChatBox';
import DmList from '@/components/DmList';
import { useDm } from '@/hooks/useDm';
import makeSection from '@/utils/makeSection';
import { useInfiniteQuery, useQuery } from '@tanstack/react-query';
import { MessageSquare } from 'lucide-react';
import { useCallback, useMemo, useState, type ChangeEvent } from 'react';
import { useParams } from 'react-router-dom';
import { Container, Header } from '@/pages/Channel/styles';

const PAGE_SIZE = 20;

const DirectMessage = () => {
  const { workspaceId, userId } = useParams<{ workspaceId: string; userId: string }>();
  const otherUserId = Number(userId);
  const [chat, setChat] = useState('');

  const { realTimeDms, sendMessage } = useDm(Number(workspaceId), otherUserId);

  // Workspace 레이아웃에서 이미 캐시된 데이터를 재사용 (네트워크 요청 없음)
  const { data: members } = useQuery({
    queryKey: ['workspaceMembers', workspaceId],
    queryFn: () => getWorkspaceMembers(Number(workspaceId)).then((res) => res.data),
    enabled: !!workspaceId,
  });
  const otherUser = members?.find((m) => m.id === otherUserId);

  const {
    data: dmPages,
    fetchNextPage,
    hasNextPage,
  } = useInfiniteQuery({
    queryKey: ['dms', workspaceId, userId],
    queryFn: ({ pageParam }) =>
      getDms(Number(workspaceId), otherUserId, pageParam, PAGE_SIZE).then((res) => res.data),
    initialPageParam: 1,
    getNextPageParam: (lastPage, allPages) =>
      lastPage.length < PAGE_SIZE ? undefined : allPages.length + 1,
    enabled: !!workspaceId && !!userId,
  });

  const historicalDms = useMemo(
    () => dmPages?.pages.flat().reverse() ?? [],
    [dmPages],
  );

  const newRealTimeDms = useMemo(() => {
    const historicalIds = new Set(historicalDms.map((d) => d.id));
    return realTimeDms.filter((d) => !historicalIds.has(d.id));
  }, [historicalDms, realTimeDms]);

  const dmSections = useMemo(
    () => makeSection([...historicalDms, ...newRealTimeDms]),
    [historicalDms, newRealTimeDms],
  );

  const isReachingEnd = !hasNextPage;

  const onChangeChat = useCallback((e: ChangeEvent<HTMLTextAreaElement>) => {
    setChat(e.target.value);
  }, []);

  const onSubmitForm = useCallback(() => {
    if (!chat.trim()) return;
    sendMessage(chat);
    setChat('');
  }, [chat, sendMessage]);

  return (
    <Container>
      <Header>
        <MessageSquare size={18} style={{ opacity: 0.7 }} />
        <span>{otherUser?.nickname ?? '...'}</span>
      </Header>
      <DmList
        dmSections={dmSections}
        isReachingEnd={isReachingEnd}
        onLoadMore={fetchNextPage}
        realTimeCount={newRealTimeDms.length}
      />
      <ChatBox
        chat={chat}
        onChangeChat={onChangeChat}
        onSubmitForm={onSubmitForm}
        placeholder={`${otherUser?.nickname ?? ''}에게 메시지 보내기`}
      />
    </Container>
  );
};

export default DirectMessage;
