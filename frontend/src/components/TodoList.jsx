import TodoItem from './TodoItem';

function TodoSkeleton() {
  return (
    <div className="animate-pulse rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
      <div className="h-4 w-2/3 rounded bg-slate-200" />
      <div className="mt-2 h-3 w-full rounded bg-slate-100" />
      <div className="mt-3 flex gap-2">
        <div className="h-5 w-16 rounded-full bg-slate-100" />
        <div className="h-5 w-16 rounded-full bg-slate-100" />
      </div>
    </div>
  );
}

export default function TodoList({
  todos,
  loading,
  error,
  canManage,
  showAssignee,
  emptyMessage,
  onEdit,
  onDelete,
  onStatusChange,
}) {
  if (loading) {
    return (
      <div className="space-y-3">
        {Array.from({ length: 4 }).map((_, index) => (
          <TodoSkeleton key={index} />
        ))}
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-600">{error}</div>
    );
  }

  if (!todos || todos.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-slate-300 p-10 text-center text-sm text-slate-400">
        {emptyMessage || 'Chưa có công việc nào.'}
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {todos.map((todo) => (
        <TodoItem
          key={todo.id}
          todo={todo}
          canManage={canManage}
          showAssignee={showAssignee}
          onEdit={onEdit}
          onDelete={onDelete}
          onStatusChange={onStatusChange}
        />
      ))}
    </div>
  );
}
