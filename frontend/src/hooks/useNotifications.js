import { useCallback, useEffect, useRef, useState } from 'react';
import * as notificationApi from '../api/notificationApi';
import { connectNotifications } from '../api/ws';
import { useAuth } from '../context/AuthContext';

export default function useNotifications() {
  const { token, isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const clientRef = useRef(null);

  useEffect(() => {
    if (!isAuthenticated || !token) return undefined;

    notificationApi.listNotifications().then(setNotifications).catch(() => {});

    clientRef.current = connectNotifications(token, (notification) => {
      setNotifications((prev) => [notification, ...prev]);
    });

    return () => {
      clientRef.current?.deactivate();
    };
  }, [isAuthenticated, token]);

  const markAsRead = useCallback((id) => {
    setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)));
    notificationApi.markNotificationAsRead(id).catch(() => {});
  }, []);

  const unreadCount = notifications.filter((n) => !n.read).length;

  return { notifications, unreadCount, markAsRead };
}
