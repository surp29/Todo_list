import apiClient from './client';

// Leader only (enforced server-side).
export function createEmployee(data) {
  return apiClient.post('/users', data).then((res) => res.data);
}

export function listEmployees() {
  return apiClient.get('/users').then((res) => res.data.data);
}

export function updateEmployee(id, data) {
  return apiClient.put(`/users/${id}`, data).then((res) => res.data);
}

export function resetEmployeePassword(id, newPassword) {
  return apiClient.patch(`/users/${id}/password`, { newPassword }).then((res) => res.data);
}

export function removeEmployee(id, force = false) {
  return apiClient.delete(`/users/${id}`, { params: { force } }).then((res) => res.data);
}
