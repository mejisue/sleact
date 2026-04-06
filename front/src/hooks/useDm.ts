import type { IDm } from '@/types';
import { useStompClient } from '@/providers/StompProvider';
import { useUser } from '@/store/auth';
import { useCallback, useEffect, useState } from 'react';

export const useDm = (_workspaceId: number, otherUserId: number) => {
  const { client, isConnected } = useStompClient();
  const myUserId = useUser()?.id;
  const [realTimeDms, setRealTimeDms] = useState<IDm[]>([]);

  useEffect(() => {
    if (!client || !isConnected || !myUserId) return;

    const subscription = client.subscribe(
      `/queue/chat/dm/${myUserId}`,
      (message) => {
        const dm: IDm = JSON.parse(message.body);
        const isCurrentConversation =
          (dm.senderId === otherUserId && dm.receiverId === myUserId) ||
          (dm.senderId === myUserId && dm.receiverId === otherUserId);

        if (isCurrentConversation) {
          setRealTimeDms((prev) => [...prev, dm]);
        }
      },
    );

    return () => {
      subscription.unsubscribe();
      setRealTimeDms([]);
    };
  }, [client, isConnected, myUserId, otherUserId]);

  const sendMessage = useCallback(
    (content: string) => {
      if (!client?.connected) return;
      client.publish({
        destination: `/publish/chat/dm/${otherUserId}`,
        body: JSON.stringify({ receiverId: otherUserId, content }),
      });
    },
    [client, otherUserId],
  );

  return { realTimeDms, sendMessage };
};
