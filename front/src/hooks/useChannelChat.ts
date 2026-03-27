import type { IChat } from '@/types';
import { useStompClient } from '@/providers/StompProvider';
import { useCallback, useEffect, useState } from 'react';

export const useChannelChat = (workspaceId: number, channelName: string) => {
  const { client, isConnected } = useStompClient();
  const [realTimeChats, setRealTimeChats] = useState<IChat[]>([]);

  useEffect(() => {
    if (!client || !isConnected) return;

    const subscription = client.subscribe(
      `/topic/chat/channel/${channelName}`,
      (message) => {
        const chat: IChat = JSON.parse(message.body);
        setRealTimeChats((prev) => [...prev, chat]);
      },
    );

    return () => {
      subscription.unsubscribe();
      setRealTimeChats([]);
    };
  }, [client, isConnected, channelName]);

  const sendMessage = useCallback(
    (content: string) => {
      if (!client?.connected) return;
      client.publish({
        destination: `/publish/chat/channel/${channelName}`,
        body: JSON.stringify({ workspaceId, content }),
      });
    },
    [client, workspaceId, channelName],
  );

  return { realTimeChats, sendMessage };
};
