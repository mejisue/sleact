import type { IDm } from '@/types';
import BoringAvatar from 'boring-avatars';
import { Bubble, Content, DmWrapper, Meta, Nickname, Timestamp } from './styles';

interface Props {
  dm: IDm;
  myUserId: number;
}

const DmChat = ({ dm, myUserId }: Props) => {
  const isMine = myUserId === dm.senderId;

  const time = new Date(dm.createdAt).toLocaleTimeString('ko-KR', {
    hour: '2-digit',
    minute: '2-digit',
  });

  return (
    <DmWrapper $isMine={isMine}>
      <BoringAvatar size={36} name={dm.sender.nickname} variant="beam" />
      <Content $isMine={isMine}>
        <Meta $isMine={isMine}>
          <Nickname>{dm.sender.nickname}</Nickname>
          <Timestamp>{time}</Timestamp>
        </Meta>
        <Bubble $isMine={isMine}>{dm.content}</Bubble>
      </Content>
    </DmWrapper>
  );
};

export default DmChat;
