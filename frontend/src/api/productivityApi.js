import apiClient from './client';

// Leader only (enforced server-side).
export function getProductivityOverview() {
  return apiClient.get('/analytics/productivity').then((res) => res.data.data);
}
