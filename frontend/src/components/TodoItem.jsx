import { FiCalendar, FiEdit2, FiTrash2, FiUser } from 'react-icons/fi';
import {
  STATUS,
  STATUS_LABEL,
  STATUS_BADGE_CLASS,
  STATUS_DOT_CLASS,
  PRIORITY_LABEL,
  PRIORITY_BADGE_CLASS,
} from '../utils/constants';

const MAX_DESCRIPTION_LENGTH = 140;

function truncate(text, maxLength) {
  if (!text) return '';
  return text.length > maxLength ? `${text.slice(0, maxLength)}...` : text;
}

function isOverdue(dueDate, status) {
  if (!dueDate || status === STATUS.COMPLETED) return false;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return new Date(dueDate) < today;
}

// `canManage` (Leader) shows edit/delete actions; the status dropdown is always editable.
export default function TodoItem({ todo, canManage, showAssignee, onEdit, onDelete, onStatusChange }) {
  const isCompleted = todo.status === STATUS.COMPLETED;
  const overdue = isOverdue(todo.dueDate, todo.status);

  return (
    <div className="group rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition hover:shadow-md hover:shadow-slate-200/60">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2">
            <span className={`h-2 w-2 shrink-0 rounded-full ${STATUS_DOT_CLASS[todo.status]}`} />
            <h3
              className={`truncate text-[15px] font-semibold text-slate-800 ${
                isCompleted ? 'text-slate-400 line-through' : ''
              }`}
            >
              {todo.title}
            </h3>
          </div>

          {todo.description && (
            <p className="mt-1.5 pl-4 text-sm text-slate-500">
              {truncate(todo.description, MAX_DESCRIPTION_LENGTH)}
            </p>
          )}

          <div className="mt-3 flex flex-wrap items-center gap-2 pl-4">
            <span className={`rounded-full px-2.5 py-1 text-xs font-medium ${PRIORITY_BADGE_CLASS[todo.priority]}`}>
              {PRIORITY_LABEL[todo.priority]}
            </span>

            {showAssignee && todo.assigneeName && (
              <span className="inline-flex items-center gap-1 rounded-full bg-indigo-50 px-2.5 py-1 text-xs font-medium text-indigo-700 ring-1 ring-inset ring-indigo-100">
                <FiUser className="h-3 w-3" />
                {todo.assigneeName}
              </span>
            )}

            {todo.dueDate && (
              <span
                className={`inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-medium ${
                  overdue
                    ? 'bg-rose-50 text-rose-600 ring-1 ring-inset ring-rose-200'
                    : 'bg-slate-50 text-slate-500 ring-1 ring-inset ring-slate-200'
                }`}
              >
                <FiCalendar className="h-3 w-3" />
                {todo.dueDate}
                {overdue && ' · Quá hạn'}
              </span>
            )}
          </div>
        </div>

        <div className="flex shrink-0 items-center gap-1.5">
          <select
            value={todo.status}
            onChange={(e) => onStatusChange(todo.id, e.target.value)}
            className={`cursor-pointer rounded-lg border-none px-2.5 py-1.5 text-xs font-medium outline-none ring-1 ring-inset ring-transparent transition focus:ring-indigo-300 ${STATUS_BADGE_CLASS[todo.status]}`}
          >
            {Object.values(STATUS).map((s) => (
              <option key={s} value={s}>
                {STATUS_LABEL[s]}
              </option>
            ))}
          </select>

          {canManage && (
            <>
              <button
                onClick={() => onEdit(todo)}
                title="Sửa"
                className="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 opacity-0 transition group-hover:opacity-100 hover:bg-indigo-50 hover:text-indigo-600"
              >
                <FiEdit2 className="h-3.5 w-3.5" />
              </button>
              <button
                onClick={() => onDelete(todo)}
                title="Xóa"
                className="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 opacity-0 transition group-hover:opacity-100 hover:bg-rose-50 hover:text-rose-600"
              >
                <FiTrash2 className="h-3.5 w-3.5" />
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
