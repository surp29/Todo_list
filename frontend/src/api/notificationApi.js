import apiClient from './client';

export function listNotifications() {
  return apiClient.get('/notifications').then((res) => res.data.data);
}

export function markNotificationAsRead(id) {
  return apiClient.patch(`/notifications/${id}/read`).then((res) => res.data);
}

export function markAllNotificationsAsRead() {
  return apiClient.patch('/notifications/read-all').then((res) => res.data);
}
