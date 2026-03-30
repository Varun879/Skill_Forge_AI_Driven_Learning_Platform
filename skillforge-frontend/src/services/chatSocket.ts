import { Client, IMessage, StompSubscription } from '@stomp/stompjs';

let sharedClient: Client | null = null;
let connectPromise: Promise<Client> | null = null;

const getBrokerUrl = () => {
  const apiBase = (import.meta as any).env?.VITE_API_BASE_URL || 'http://localhost:8080/api';
  const backendBase = String(apiBase).replace(/\/api\/?$/, '');
  return backendBase.replace(/^http/i, 'ws') + '/ws/chat';
};

export const getChatSocketClient = async (): Promise<Client> => {
  if (sharedClient && sharedClient.connected) {
    return sharedClient;
  }

  if (connectPromise) {
    return connectPromise;
  }

  const client = new Client({
    brokerURL: getBrokerUrl(),
    reconnectDelay: 2000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
  });

  connectPromise = new Promise<Client>((resolve, reject) => {
    client.onConnect = () => {
      sharedClient = client;
      connectPromise = null;
      resolve(client);
    };

    client.onStompError = (frame) => {
      console.error('STOMP broker error', frame.headers['message'], frame.body);
    };

    client.onWebSocketError = (event) => {
      console.error('WebSocket error', event);
    };

    client.onDisconnect = () => {
      sharedClient = null;
    };

    client.activate();

    setTimeout(() => {
      if (!client.connected) {
        reject(new Error('Unable to connect to chat websocket'));
      }
    }, 6000);
  }).catch((err) => {
    connectPromise = null;
    throw err;
  });

  return connectPromise;
};

export const subscribeTopic = async (
  destination: string,
  handler: (message: IMessage) => void
): Promise<StompSubscription> => {
  const client = await getChatSocketClient();
  return client.subscribe(destination, handler);
};
