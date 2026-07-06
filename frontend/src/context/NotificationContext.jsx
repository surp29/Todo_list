import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import * as notificationApi from '../api/notificationApi';
import { connectNotifications } from '../api/ws';
import { useAuth } from './AuthContext';

const NotificationContext = createContext(null);

// Single shared WebSocket connection for the whole app (instead of one per
// component that needs notifications), so both the bell icon and dashboards
// react to the same incoming events without opening duplicate connections.
export function NotificationProvider({ children }) {
  const { token, isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const clientRef = useRef(null);
  const listenersRef = useRef(new Set());

  useEffect(() => {
    if (!isAuthenticated || !token) {
      setNotifications([]);
      return undefined;
    }

    notificationApi.listNotifications().then(setNotifications).catch(() => {});

    clientRef.current = connectNotifications(token, (notification) => {
      setNotifications((prev) => [notification, ...prev]);
      listenersRef.current.forEach((listener) => listener(notification));
    });

    return () => {
      clientRef.current?.deactivate();
    };
  }, [isAuthenticated, token]);

  const markAsRead = useCallback((id) => {
    setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)));
    notificationApi.markNotificationAsRead(id).catch(() => {});
  }, []);

  const markAllAsRead = useCallback(() => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    notificationApi.markAllNotificationsAsRead().catch(() => {});
  }, []);

  // Lets a page subscribe to freshly-arrived notifications (e.g. to refetch its
  // own todo list live) without opening a second WebSocket connection.
  const subscribe = useCallback((listener) => {
    listenersRef.current.add(listener);
    return () => listenersRef.current.delete(listener);
  }, []);

  const unreadCount = notifications.filter((n) => !n.read).length;

  const value = { notifications, unreadCount, markAsRead, markAllAsRead, subscribe };

  return <NotificationContext.Provider value={value}>{children}</NotificationContext.Provider>;
}

export function useNotifications() {
  const ctx = useContext(NotificationContext);
  if (!ctx) {
    throw new Error('useNotifications must be used within a NotificationProvider');
  }
  return ctx;
}

// Calls `onNotification` whenever a new notification arrives over WebSocket —
// use this in a dashboard to refetch its todo list live instead of requiring F5.
export function useNotificationListener(onNotification) {
  const { subscribe } = useNotifications();

  useEffect(() => subscribe(onNotification), [subscribe, onNotification]);
}
