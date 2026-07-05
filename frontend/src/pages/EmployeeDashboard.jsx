import { useState } from 'react';
import Layout from '../components/Layout';
import TodoList from '../components/TodoList';
import KanbanBoard from '../components/KanbanBoard';
import ViewToggle from '../components/ViewToggle';
import TodoFilter from '../components/TodoFilter';
import Pagination from '../components/Pagination';
import Toast from '../components/Toast';
import useTodos from '../hooks/useTodos';
import { useAuth } from '../context/AuthContext';
import { ALL_PAGE_SIZE, DEFAULT_PAGE_SIZE } from '../utils/constants';

export default function EmployeeDashboard() {
  const { user } = useAuth();
  const {
    todos,
    pagination,
    filters,
    size,
    loading,
    error,
    toast,
    setPage,
    setSize,
    changePageSize,
    updateFilters,
    resetFilters,
    changeStatus,
    fetchTodos,
    clearToast,
  } = useTodos();

  const [view, setView] = useState('list');

  const changeView = (newView) => {
    setView(newView);
    setSize(newView === 'board' ? ALL_PAGE_SIZE : DEFAULT_PAGE_SIZE);
  };

  const handleBoardStatusChange = async (todoId, newStatus) => {
    const success = await changeStatus(todoId, newStatus);
    if (!success) await fetchTodos();
  };

  return (
    <Layout>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-slate-800">Công việc của tôi</h1>
        <p className="mt-0.5 text-sm text-slate-500">
          Chào {user?.fullName}, đây là các công việc được giao cho bạn.
        </p>
      </div>

      <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
        <TodoFilter filters={filters} onChange={updateFilters} onReset={resetFilters} />
        <ViewToggle view={view} onChange={changeView} />
      </div>

      {view === 'board' ? (
        <KanbanBoard todos={todos} showAssignee={false} onStatusChange={handleBoardStatusChange} />
      ) : (
        <>
          <TodoList
            todos={todos}
            loading={loading}
            error={error}
            canManage={false}
            showAssignee={false}
            emptyMessage="Bạn chưa được giao công việc nào."
            onStatusChange={changeStatus}
          />

          <Pagination
            currentPage={pagination.currentPage}
            totalPages={pagination.totalPages}
            totalElements={pagination.totalElements}
            pageSize={size}
            isFirst={pagination.isFirst}
            isLast={pagination.isLast}
            onPageChange={setPage}
            onPageSizeChange={changePageSize}
          />
        </>
      )}

      <Toast toast={toast} onClose={clearToast} />
    </Layout>
  );
}
