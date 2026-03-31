import type { IDm } from '@/types';
import { useEffect, useRef } from 'react';
import { useInView } from 'react-intersection-observer';
import DmChat from '@/components/DmChat';
import { useUser } from '@/store/auth';
import { DateLabel, DateSeparator, ScrollArea } from '@/components/ChatList/styles';

interface Props {
  dmSections: Record<string, IDm[]>;
  isReachingEnd: boolean;
  onLoadMore: () => void;
  realTimeCount: number;
}

const DmList = ({ dmSections, isReachingEnd, onLoadMore, realTimeCount }: Props) => {
  const myUserId = useUser()?.id ?? 0;
  const scrollRef = useRef<HTMLDivElement>(null);
  const { ref: topRef, inView } = useInView({ threshold: 1.0, skip: isReachingEnd });
  const hasInitialScrolled = useRef(false);
  const prevScrollHeightRef = useRef(0);

  const scrollToBottom = () => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  };

  useEffect(() => {
    if (!hasInitialScrolled.current && Object.keys(dmSections).length > 0) {
      scrollToBottom();
      hasInitialScrolled.current = true;
    }
  }, [dmSections]);

  useEffect(() => {
    scrollToBottom();
  }, [realTimeCount]);

  useEffect(() => {
    if (inView && scrollRef.current) {
      prevScrollHeightRef.current = scrollRef.current.scrollHeight;
      onLoadMore();
    }
  }, [inView, onLoadMore]);

  useEffect(() => {
    if (!scrollRef.current || !hasInitialScrolled.current) return;
    const prev = prevScrollHeightRef.current;
    if (prev > 0) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight - prev;
      prevScrollHeightRef.current = 0;
    }
  }, [dmSections]);

  return (
    <ScrollArea ref={scrollRef}>
      <div ref={topRef} />
      {Object.entries(dmSections).map(([date, dms]) => (
        <div key={date}>
          <DateSeparator>
            <hr />
            <DateLabel>{date}</DateLabel>
            <hr />
          </DateSeparator>
          {dms.map((dm) => (
            <DmChat key={dm.id} dm={dm} myUserId={myUserId} />
          ))}
        </div>
      ))}
    </ScrollArea>
  );
};

export default DmList;
