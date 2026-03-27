import { Client } from '@stomp/stompjs';
import { createContext, useContext, useEffect, useRef, useState, type ReactNode } from 'react';
import SockJS from 'sockjs-client';

interface StompContextType {
  client: Client | null;
  isConnected: boolean;
}

const StompContext = createContext<StompContextType>({ client: null, isConnected: false });

export const useStompClient = () => useContext(StompContext);

export default function StompProvider({ children }: { children: ReactNode }) {
  const clientRef = useRef<Client | null>(null);
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/connect'),
      onConnect: () => setIsConnected(true),
      onDisconnect: () => setIsConnected(false),
      onStompError: (frame) => console.error('STOMP error:', frame),
    });

    clientRef.current = client;
    client.activate();

    return () => {
      client.deactivate();
    };
  }, []);

  return (
    <StompContext.Provider value={{ client: clientRef.current, isConnected }}>
      {children}
    </StompContext.Provider>
  );
}
