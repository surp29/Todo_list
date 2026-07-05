import apiClient from './client';

// Leader only (enforced server-side).
export function createEmployee(data) {
  return apiClient.post('/users', data).then((res) => res.data);
}

export function listEmployees() {
  return apiClient.get('/users').then((res) => res.data.data);
}
