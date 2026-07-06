import { useCallback, useState } from 'react';
import { FiBarChart2, FiPlus, FiUsers } from 'react-icons/fi';
import Layout from '../components/Layout';
import TodoList from '../components/TodoList';
import KanbanBoard from '../components/KanbanBoard';
import ViewToggle from '../components/ViewToggle';
import TodoForm from '../components/TodoForm';
import TodoFilter from '../components/TodoFilter';
import Pagination from '../components/Pagination';
import ConfirmDialog from '../components/ConfirmDialog';
import Toast from '../components/Toast';
import EmployeeManager from '../components/EmployeeManager';
import ProductivityChart from '../components/ProductivityChart';
import useTodos from '../hooks/useTodos';
import useEmployees from '../hooks/useEmployees';
import { useNotificationListener } from '../context/NotificationContext';
import { ALL_PAGE_SIZE, DEFAULT_PAGE_SIZE } from '../utils/constants';

export default function LeaderDashboard() {
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
    addTodo,
    editTodo,
    removeTodo,
    changeStatus,
    fetchTodos,
    clearToast,
  } = useTodos({ withAssignee: true });

  const { employees, activeEmployees, createEmployee, updateEmployee, resetPassword, removeEmployee } = useEmployees();

  // An employee completing a task changes it in a session this dashboard doesn't
  // own — refetch live instead of waiting for the Leader to reload the page.
  useNotificationListener(
    useCallback(
      (notification) => {
        if (notification.type === 'TASK_COMPLETED') fetchTodos();
      },
      [fetchTodos]
    )
  );

  const [tab, setTab] = useState('tasks'); // 'tasks' | 'productivity'
  const [view, setView] = useState('list'); // 'list' | 'board'
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingTodo, setEditingTodo] = useState(null);
  const [deletingTodo, setDeletingTodo] = useState(null);
  const [isEmployeeManagerOpen, setIsEmployeeManagerOpen] = useState(false);
  const [employeeToast, setEmployeeToast] = useState(null);

  const changeView = (newView) => {
    setView(newView);
    // The board needs every matching task visible at once to be a usable drop target set.
    setSize(newView === 'board' ? ALL_PAGE_SIZE : DEFAULT_PAGE_SIZE);
  };

  const openAddForm = () => {
    setEditingTodo(null);
    setIsFormOpen(true);
  };

  const openEditForm = (todo) => {
    setEditingTodo(todo);
    setIsFormOpen(true);
  };

  const closeForm = () => {
    setIsFormOpen(false);
    setEditingTodo(null);
  };

  const handleFormSubmit = (data) => {
    return editingTodo ? editTodo(editingTodo.id, data) : addTodo(data);
  };

  const handleConfirmDelete = async () => {
    if (deletingTodo) {
      await removeTodo(deletingTodo.id);
      setDeletingTodo(null);
    }
  };

  const handleBoardStatusChange = async (todoId, newStatus) => {
    const success = await changeStatus(todoId, newStatus);
    if (!success) await fetchTodos();
  };

  const handleUpdateEmployee = async (id, data) => {
    const result = await updateEmployee(id, data);
    setEmployeeToast({ type: 'success', message: 'Cập nhật thông tin nhân viên thành công', id: Date.now() });
    return result;
  };

  const handleResetPassword = async (id, newPassword) => {
    const result = await resetPassword(id, newPassword);
    setEmployeeToast({ type: 'success', message: 'Đặt lại mật khẩu thành công', id: Date.now() });
    return result;
  };

  const handleRemoveEmployee = async (id, force) => {
    try {
      const result = await removeEmployee(id, force);
      setEmployeeToast({ type: 'success', message: result.message, id: Date.now() });
    } catch (err) {
      setEmployeeToast({
        type: 'error',
        message: err.response?.data?.message || 'Xóa nhân viên thất bại',
        id: Date.now(),
      });
    }
  };

  return (
    <Layout>
      <div className="mb-6 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Công việc nhóm</h1>
          <p className="mt-0.5 text-sm text-slate-500">Giao việc và theo dõi tiến độ của nhân viên</p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => setIsEmployeeManagerOpen(true)}
            className="flex items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-4 py-2.5 text-sm font-medium text-slate-600 shadow-sm transition hover:bg-slate-50"
          >
            <FiUsers className="h-4 w-4" />
            Nhân viên
          </button>
          <button
            onClick={openAddForm}
            className="flex items-center gap-1.5 rounded-lg bg-gradient-to-r from-indigo-500 to-violet-600 px-4 py-2.5 text-sm font-medium text-white shadow-md shadow-indigo-500/20 transition hover:brightness-110"
          >
            <FiPlus className="h-4 w-4" />
            Giao việc mới
          </button>
        </div>
      </div>

      <div className="mb-5 flex items-center gap-2 border-b border-slate-200">
        <button
          onClick={() => setTab('tasks')}
          className={`border-b-2 px-3 py-2.5 text-sm font-medium transition ${
            tab === 'tasks' ? 'border-indigo-600 text-indigo-700' : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
        >
          Công việc
        </button>
        <button
          onClick={() => setTab('productivity')}
          className={`flex items-center gap-1.5 border-b-2 px-3 py-2.5 text-sm font-medium transition ${
            tab === 'productivity'
              ? 'border-indigo-600 text-indigo-700'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
        >
          <FiBarChart2 className="h-3.5 w-3.5" />
          Năng suất
        </button>
      </div>

      {tab === 'productivity' ? (
        <ProductivityChart />
      ) : (
        <>
          <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
            <TodoFilter filters={filters} employees={activeEmployees} onChange={updateFilters} onReset={resetFilters} />
            <ViewToggle view={view} onChange={changeView} />
          </div>

          {view === 'board' ? (
            <KanbanBoard todos={todos} showAssignee onStatusChange={handleBoardStatusChange} />
          ) : (
            <>
              <TodoList
                todos={todos}
                loading={loading}
                error={error}
                canManage
                showAssignee
                emptyMessage="Chưa có công việc nào. Hãy giao việc đầu tiên cho nhân viên!"
                onEdit={openEditForm}
                onDelete={setDeletingTodo}
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
        </>
      )}

      {isFormOpen && (
        <TodoForm
          initialData={editingTodo}
          employees={activeEmployees}
          onSubmit={handleFormSubmit}
          onClose={closeForm}
        />
      )}

      {isEmployeeManagerOpen && (
        <EmployeeManager
          employees={employees}
          onCreate={createEmployee}
          onUpdate={handleUpdateEmployee}
          onResetPassword={handleResetPassword}
          onRemove={handleRemoveEmployee}
          onClose={() => setIsEmployeeManagerOpen(false)}
        />
      )}

      {deletingTodo && (
        <ConfirmDialog
          title="Xóa công việc"
          message={`Bạn có chắc chắn muốn xóa "${deletingTodo.title}" không?`}
          onConfirm={handleConfirmDelete}
          onCancel={() => setDeletingTodo(null)}
        />
      )}

      <Toast toast={toast} onClose={clearToast} />
      <Toast toast={employeeToast} onClose={() => setEmployeeToast(null)} />
    </Layout>
  );
}
