import { getChannelChats, getChannelMembers } from '@/api/channel';
import ChatBox from '@/components/ChatBox';
import ChatList from '@/components/ChatList';
import { useChannelChat } from '@/hooks/useChannelChat';
import { useUser } from '@/store/auth';
import makeSection from '@/utils/makeSection';
import { useInfiniteQuery, useQuery } from '@tanstack/react-query';
import { Hash } from 'lucide-react';
import { useCallback, useState, type ChangeEvent } from 'react';
import { useParams } from 'react-router-dom';
import { Container, Header } from './styles';

const PAGE_SIZE = 20;

const Channel = () => {
  const { workspaceId, channelName } = useParams<{ workspaceId: string; channelName: string }>();
  const user = useUser();
  const [chat, setChat] = useState('');

  const { realTimeChats, sendMessage } = useChannelChat(Number(workspaceId), channelName ?? '');

  const { data: channelMembers } = useQuery({
    queryKey: ['channelMembers', workspaceId, channelName],
    queryFn: () => getChannelMembers(Number(workspaceId), channelName!).then((res) => res.data),
    enabled: !!workspaceId && !!channelName,
  });

  const {
    data: chatPages,
    fetchNextPage,
    hasNextPage,
  } = useInfiniteQuery({
    queryKey: ['channelChats', workspaceId, channelName],
    queryFn: ({ pageParam }) =>
      getChannelChats(Number(workspaceId), channelName!, pageParam, PAGE_SIZE).then((res) => res.data),
    initialPageParam: 1,
    getNextPageParam: (lastPage, allPages) =>
      lastPage.length < PAGE_SIZE ? undefined : allPages.length + 1,
    enabled: !!workspaceId && !!channelName,
  });

  const historicalChats = chatPages?.pages.flat().reverse() ?? [];
  const historicalIds = new Set(historicalChats.map((c) => c.id));
  const newRealTimeChats = realTimeChats.filter((c) => !historicalIds.has(c.id));
  const chatSections = makeSection([...historicalChats, ...newRealTimeChats]);
  const isReachingEnd = !hasNextPage;

  const onChangeChat = useCallback((e: ChangeEvent<HTMLTextAreaElement>) => {
    setChat(e.target.value);
  }, []);

  const onSubmitForm = useCallback(() => {
    if (!chat.trim() || !user) return;
    sendMessage(chat);
    setChat('');
  }, [chat, user, sendMessage]);

  return (
    <Container>
      <Header>
        <Hash size={18} style={{ opacity: 0.7 }} />
        <span>{channelName}</span>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginLeft: 'auto' }}>
          <span style={{ fontSize: 14, fontWeight: 400, color: 'rgba(255,255,255,0.5)' }}>
            {channelMembers?.length}명
          </span>
        </div>
      </Header>
      <ChatList
        chatSections={chatSections}
        isReachingEnd={isReachingEnd}
        onLoadMore={fetchNextPage}
        realTimeCount={newRealTimeChats.length}
      />
      <ChatBox
        chat={chat}
        onChangeChat={onChangeChat}
        onSubmitForm={onSubmitForm}
        placeholder={`#${channelName}에 메시지 보내기`}
      />
    </Container>
  );
};

export default Channel;
