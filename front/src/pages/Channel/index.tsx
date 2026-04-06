import { getChannelChats, getChannelMembers } from '@/api/channel';
import ChatBox from '@/components/ChatBox';
import ChatList from '@/components/ChatList';
import ChannelMembersModal from '@/components/ChannelMembersModal';
import InviteChannelMemberModal from '@/components/InviteChannelMemberModal';
import { useChannelChat } from '@/hooks/useChannelChat';
import { useModalActions } from '@/store/modal';
import { useUser } from '@/store/auth';
import makeSection from '@/utils/makeSection';
import ErrorPage from '@/pages/error-page';
import { useInfiniteQuery, useQuery } from '@tanstack/react-query';
import type { AxiosError } from 'axios';
import { Hash, UserPlus } from 'lucide-react';
import { useCallback, useState, type ChangeEvent } from 'react';
import { useParams } from 'react-router-dom';
import {
  AvatarOverlap,
  Container,
  Header,
  InviteButton,
  MemberAvatarButton,
  MiniAvatar,
  Tooltip,
  getAvatarColor,
} from './styles';

const PAGE_SIZE = 20;

const Channel = () => {
  const { workspaceId, channelName } = useParams<{ workspaceId: string; channelName: string }>();
  const user = useUser();
  const { openInviteChannelMemberModal } = useModalActions();
  const [chat, setChat] = useState('');
  const [isMembersModalOpen, setIsMembersModalOpen] = useState(false);

  const { realTimeChats, sendMessage } = useChannelChat(Number(workspaceId), channelName ?? '');

  const { data: channelMembers = [] } = useQuery({
    queryKey: ['channelMembers', workspaceId, channelName],
    queryFn: () => getChannelMembers(Number(workspaceId), channelName!).then((res) => res.data),
    enabled: !!workspaceId && !!channelName,
  });

  const {
    data: chatPages,
    fetchNextPage,
    hasNextPage,
    isError: isChatsError,
    error: chatsError,
  } = useInfiniteQuery({
    queryKey: ['channelChats', workspaceId, channelName],
    queryFn: ({ pageParam }) =>
      getChannelChats(Number(workspaceId), channelName!, pageParam, PAGE_SIZE).then((res) => res.data),
    initialPageParam: 1,
    getNextPageParam: (lastPage, allPages) =>
      lastPage.length < PAGE_SIZE ? undefined : allPages.length + 1,
    enabled: !!workspaceId && !!channelName,
    retry: false,
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

  if (isChatsError) {
    const status = (chatsError as AxiosError)?.response?.status ?? 400;
    return <ErrorPage status={status} />;
  }

  const previewNames = channelMembers.slice(0, 3).map((m) => m.nickname).join(', ');
  const tooltipText = channelMembers.length > 3
    ? `${previewNames} 외 ${channelMembers.length - 3}명 포함`
    : previewNames;

  return (
    <Container>
      <InviteChannelMemberModal />
      {isMembersModalOpen && (
        <ChannelMembersModal
          channelName={channelName ?? ''}
          members={channelMembers}
          onClose={() => setIsMembersModalOpen(false)}
        />
      )}
      <Header>
        <Hash size={18} style={{ opacity: 0.7 }} />
        <span>{channelName}</span>
        <MemberAvatarButton onClick={() => setIsMembersModalOpen(true)}>
          <AvatarOverlap>
            {channelMembers.slice(0, 2).map((m, i) => (
              <MiniAvatar key={m.id} $color={getAvatarColor(m.id)} $index={i}>
                {m.nickname.slice(0, 1).toUpperCase()}
              </MiniAvatar>
            ))}
          </AvatarOverlap>
          {channelMembers.length}
          <Tooltip data-tooltip>
            이 channel의 모든 멤버 보기
            <br />
            <span style={{ color: 'rgba(255,255,255,0.6)', fontSize: 12 }}>{tooltipText}</span>
          </Tooltip>
        </MemberAvatarButton>
        <InviteButton onClick={openInviteChannelMemberModal}>
          <UserPlus size={14} />
          팀원 초대하기
        </InviteButton>
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
        members={channelMembers}
        setChat={setChat}
      />
    </Container>
  );
};

export default Channel;
