import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

let client: Client | null = null;
let connected = false;

export interface WsCallbacks {
  onLeaderboard?: (data: any) => void;
  onNotification?: (data: any) => void;
  onConnect?: () => void;
}

export function connectWs(token: string, cbs: WsCallbacks): () => void {
  if (client) disconnectWs();

  client = new Client({
    webSocketFactory: () => new SockJS('/ws') as any,
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 4000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: () => {
      connected = true;
      cbs.onConnect?.();

      client!.subscribe('/topic/leaderboard/all-time', (m: IMessage) => {
        try { cbs.onLeaderboard?.(JSON.parse(m.body)); } catch {}
      });
      client!.subscribe('/user/queue/notifications', (m: IMessage) => {
        try { cbs.onNotification?.(JSON.parse(m.body)); } catch {}
      });

      // snapshot inicial
      client!.publish({
        destination: '/app/leaderboard/snapshot',
        body: JSON.stringify({ period: 'all-time', limit: 10 }),
      });
    },
    onStompError: (f) => console.warn('[stomp] error', f.headers['message']),
    onWebSocketClose: () => { connected = false; },
  });

  client.activate();

  return () => disconnectWs();
}

export function disconnectWs() {
  if (client) {
    client.deactivate();
    client = null;
    connected = false;
  }
}

export function isWsConnected() { return connected; }