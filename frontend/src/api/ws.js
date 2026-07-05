import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { API_BASE_URL } from './client';

/**
 * Opens a STOMP-over-SockJS connection authenticated with the given JWT and
 * subscribes to the current user's private notification queue.
 *
 * @param {string} token       the JWT access token
 * @param {(notification: object) => void} onNotification called for each incoming notification
 * @returns {Client} the STOMP client (call `.deactivate()` to close it)
 */
export function connectNotifications(token, onNotification) {
  const client = new Client({
    webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws`),
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    reconnectDelay: 5000,
    onConnect: () => {
      client.subscribe('/user/queue/notifications', (message) => {
        try {
          onNotification(JSON.parse(message.body));
        } catch {
          // ignore malformed payloads
        }
      });
    },
  });

  client.activate();
  return client;
}
