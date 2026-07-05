import apiClient from './client';

// Employees always see only their own assigned todos (enforced server-side);
// `assigneeId` only has an effect for Leaders.
export function getTodos({
  keyword,
  status,
  priority,
  assigneeId,
  page = 0,
  size = 10,
  sortBy = 'createdAt',
  sortDir = 'DESC',
} = {}) {
  return apiClient
    .get('/todos', { params: { keyword, status, priority, assigneeId, page, size, sortBy, sortDir } })
    .then((res) => res.data);
}

export function getTodoById(id) {
  return apiClient.get(`/todos/${id}`).then((res) => res.data);
}

// Leader only (enforced server-side).
export function createTodo(data) {
  return apiClient.post('/todos', data).then((res) => res.data);
}

// Leader only (enforced server-side).
export function updateTodo(id, data) {
  return apiClient.put(`/todos/${id}`, data).then((res) => res.data);
}

export function updateTodoStatus(id, status) {
  return apiClient.patch(`/todos/${id}/status`, { status }).then((res) => res.data);
}

// Leader only (enforced server-side).
export function deleteTodo(id) {
  return apiClient.delete(`/todos/${id}`).then((res) => res.data);
}

export default apiClient;
