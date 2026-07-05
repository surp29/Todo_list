import apiClient from './client';

export function login(username, password) {
  return apiClient.post('/auth/login', { username, password }).then((res) => res.data.data);
}

export function fetchCurrentUser() {
  return apiClient.get('/auth/me').then((res) => res.data.data);
}
