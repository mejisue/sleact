import type { IChat } from '@/types';
import { useEffect, useRef } from 'react';
import { useInView } from 'react-intersection-observer';
import Chat from '@/components/Chat';
import { DateLabel, DateSeparator, ScrollArea } from './styles';

interface Props {
  chatSections: Record<string, IChat[]>;
  isReachingEnd: boolean;
  onLoadMore: () => void;
  realTimeCount: number;
}

const ChatList = ({ chatSections, isReachingEnd, onLoadMore, realTimeCount }: Props) => {
  const scrollRef = useRef<HTMLDivElement>(null);
  const { ref: topRef, inView } = useInView({ threshold: 1.0, skip: isReachingEnd });
  const hasInitialScrolled = useRef(false);
  const prevScrollHeightRef = useRef(0);

  const scrollToBottom = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  };

  // 최초 데이터 로드 시 스크롤 최하단 이동
  useEffect(() => {
    if (!hasInitialScrolled.current && Object.keys(chatSections).length > 0) {
      scrollToBottom();
      hasInitialScrolled.current = true;
    }
  }, [chatSections]);

  // 실시간 메시지 수신/전송 시 스크롤 최하단 이동
  useEffect(() => {
    scrollToBottom();
  }, [realTimeCount]);

  // 최상단 감지 시: 로드 전 scrollHeight 저장 후 페이지 요청
  useEffect(() => {
    if (inView && scrollRef.current) {
      prevScrollHeightRef.current = scrollRef.current.scrollHeight;
      onLoadMore();
    }
  }, [inView, onLoadMore]);

  // 이전 페이지 로드 완료 후 스크롤 위치 복원
  useEffect(() => {
    if (!scrollRef.current || !hasInitialScrolled.current) return;
    const prev = prevScrollHeightRef.current;
    if (prev > 0) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight - prev;
      prevScrollHeightRef.current = 0;
    }
  }, [chatSections]);

  return (
    <ScrollArea ref={scrollRef}>
      <div ref={topRef} />
      {Object.entries(chatSections).map(([date, chats]) => (
        <div key={date}>
          <DateSeparator>
            <hr />
            <DateLabel>{date}</DateLabel>
            <hr />
          </DateSeparator>
          {chats.map((chat) => (
            <Chat key={chat.id} chat={chat} />
          ))}
        </div>
      ))}
    </ScrollArea>
  );
};

export default ChatList;
