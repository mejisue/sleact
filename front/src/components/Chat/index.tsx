import type { IChat } from '@/types';
import BoringAvatar from 'boring-avatars';
import { renderWithMentions } from '@/utils/renderWithMentions';
import { ChatWrapper, Content, Meta, Nickname, Text, Timestamp } from './styles';

interface Props {
  chat: IChat;
}

const Chat = ({ chat }: Props) => {
  const time = new Date(chat.createdAt).toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <ChatWrapper>
      <BoringAvatar size={36} name={chat.user.nickname} variant="beam" />
      <Content>
        <Meta>
          <Nickname>{chat.user.nickname}</Nickname>
          <Timestamp>{time}</Timestamp>
        </Meta>
        <Text>{renderWithMentions(chat.content)}</Text>
      </Content>
    </ChatWrapper>
  );
};

export default Chat;
