import { useCallback, useEffect, useState } from 'react';
import * as todoApi from '../api/todoApi';
import { DEFAULT_PAGE_SIZE } from '../utils/constants';

// Pass `withAssignee: true` (Leader view) to expose an `assigneeId` filter.
export default function useTodos({ withAssignee = false } = {}) {
  const [todos, setTodos] = useState([]);
  const [pagination, setPagination] = useState({
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    pageSize: DEFAULT_PAGE_SIZE,
    isFirst: true,
    isLast: true,
  });
  const [filters, setFilters] = useState({ keyword: '', status: '', priority: '', assigneeId: '' });
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(DEFAULT_PAGE_SIZE);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [toast, setToast] = useState(null);

  const showToast = useCallback((type, message) => {
    setToast({ type, message, id: Date.now() });
  }, []);

  const fetchTodos = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await todoApi.getTodos({
        keyword: filters.keyword || undefined,
        status: filters.status || undefined,
        priority: filters.priority || undefined,
        assigneeId: withAssignee ? filters.assigneeId || undefined : undefined,
        page,
        size,
      });
      const data = response.data;
      setTodos(data.content);
      setPagination({
        currentPage: data.currentPage,
        totalPages: data.totalPages,
        totalElements: data.totalElements,
        pageSize: data.pageSize,
        isFirst: data.isFirst,
        isLast: data.isLast,
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Không thể tải danh sách công việc');
    } finally {
      setLoading(false);
    }
  }, [filters, page, size, withAssignee]);

  useEffect(() => {
    fetchTodos();
  }, [fetchTodos]);

  const updateFilters = useCallback((newFilters) => {
    setPage(0);
    setFilters((prev) => ({ ...prev, ...newFilters }));
  }, []);

  const resetFilters = useCallback(() => {
    setPage(0);
    setFilters({ keyword: '', status: '', priority: '', assigneeId: '' });
  }, []);

  const changePageSize = useCallback((newSize) => {
    setPage(0);
    setSize(newSize);
  }, []);

  const addTodo = useCallback(
    async (data) => {
      try {
        await todoApi.createTodo(data);
        showToast('success', 'Giao việc thành công');
        await fetchTodos();
        return true;
      } catch (err) {
        showToast('error', err.response?.data?.message || 'Giao việc thất bại');
        return false;
      }
    },
    [fetchTodos, showToast]
  );

  const editTodo = useCallback(
    async (id, data) => {
      try {
        await todoApi.updateTodo(id, data);
        showToast('success', 'Cập nhật công việc thành công');
        await fetchTodos();
        return true;
      } catch (err) {
        showToast('error', err.response?.data?.message || 'Cập nhật công việc thất bại');
        return false;
      }
    },
    [fetchTodos, showToast]
  );

  const removeTodo = useCallback(
    async (id) => {
      try {
        await todoApi.deleteTodo(id);
        showToast('success', 'Xóa công việc thành công');
        await fetchTodos();
        return true;
      } catch (err) {
        showToast('error', err.response?.data?.message || 'Xóa công việc thất bại');
        return false;
      }
    },
    [fetchTodos, showToast]
  );

  const changeStatus = useCallback(
    async (id, status) => {
      try {
        await todoApi.updateTodoStatus(id, status);
        showToast('success', 'Cập nhật trạng thái thành công');
        await fetchTodos();
        return true;
      } catch (err) {
        showToast('error', err.response?.data?.message || 'Cập nhật trạng thái thất bại');
        return false;
      }
    },
    [fetchTodos, showToast]
  );

  return {
    todos,
    pagination,
    filters,
    page,
    size,
    loading,
    error,
    toast,
    setPage,
    setSize,
    changePageSize,
    updateFilters,
    resetFilters,
    addTodo,
    editTodo,
    removeTodo,
    changeStatus,
    fetchTodos,
    clearToast: () => setToast(null),
  };
}
