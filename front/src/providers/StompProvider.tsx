import { Client } from '@stomp/stompjs';
import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import SockJS from 'sockjs-client';

interface StompContextType {
  client: Client | null;
  isConnected: boolean;
}

const StompContext = createContext<StompContextType>({ client: null, isConnected: false });

// eslint-disable-next-line react-refresh/only-export-components
export const useStompClient = () => useContext(StompContext);

export default function StompProvider({ children, workspaceId }: { children: ReactNode; workspaceId?: string }) {
  const [client, setClient] = useState<Client | null>(null);
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    if (!workspaceId) return;

    const stompClient = new Client({
      webSocketFactory: () => new SockJS(`${import.meta.env.VITE_WS_BASE_URL}/connect?workspace=${workspaceId}`),
      onConnect: () => setIsConnected(true),
      onDisconnect: () => setIsConnected(false),
      onStompError: (frame) => console.error('STOMP error:', frame),
    });

    setClient(stompClient);
    stompClient.activate();

    return () => {
      stompClient.deactivate();
      setClient(null);
    };
  }, [workspaceId]);

  return (
    <StompContext.Provider value={{ client, isConnected }}>
      {children}
    </StompContext.Provider>
  );
}
