import { useEffect, useRef, useState } from 'react';
import debounce from 'lodash.debounce';
import { FiSearch, FiX } from 'react-icons/fi';
import { STATUS, STATUS_LABEL, PRIORITY, PRIORITY_LABEL, DEBOUNCE_DELAY_MS } from '../utils/constants';

const selectClass =
  'rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 text-sm text-slate-700 outline-none transition focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100';

// The assignee dropdown only renders when `employees` is passed (Leader view).
export default function TodoFilter({ filters, employees, onChange, onReset }) {
  const [keywordInput, setKeywordInput] = useState(filters.keyword);
  const debouncedChange = useRef(debounce((value) => onChange({ keyword: value }), DEBOUNCE_DELAY_MS));

  useEffect(() => {
    setKeywordInput(filters.keyword);
  }, [filters.keyword]);

  useEffect(() => {
    const debounced = debouncedChange.current;
    return () => debounced.cancel();
  }, []);

  const handleKeywordChange = (e) => {
    const value = e.target.value;
    setKeywordInput(value);
    debouncedChange.current(value);
  };

  return (
    <div className="mb-5 flex flex-col gap-3 rounded-xl border border-slate-200 bg-white p-4 sm:flex-row sm:items-center">
      <div className="relative flex-1">
        <FiSearch className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          type="text"
          value={keywordInput}
          onChange={handleKeywordChange}
          placeholder="Tìm kiếm theo tiêu đề hoặc mô tả..."
          className="w-full rounded-lg border border-slate-200 bg-slate-50 py-2.5 pl-9 pr-3 text-sm text-slate-700 outline-none transition focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100"
        />
      </div>

      <select value={filters.status} onChange={(e) => onChange({ status: e.target.value })} className={selectClass}>
        <option value="">Tất cả trạng thái</option>
        {Object.values(STATUS).map((s) => (
          <option key={s} value={s}>
            {STATUS_LABEL[s]}
          </option>
        ))}
      </select>

      <select
        value={filters.priority}
        onChange={(e) => onChange({ priority: e.target.value })}
        className={selectClass}
      >
        <option value="">Tất cả độ ưu tiên</option>
        {Object.values(PRIORITY).map((p) => (
          <option key={p} value={p}>
            {PRIORITY_LABEL[p]}
          </option>
        ))}
      </select>

      {employees && (
        <select
          value={filters.assigneeId}
          onChange={(e) => onChange({ assigneeId: e.target.value })}
          className={selectClass}
        >
          <option value="">Tất cả nhân viên</option>
          {employees.map((emp) => (
            <option key={emp.id} value={emp.id}>
              {emp.fullName}
            </option>
          ))}
        </select>
      )}

      <button
        onClick={onReset}
        className="flex items-center justify-center gap-1.5 rounded-lg border border-slate-200 px-4 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
      >
        <FiX className="h-3.5 w-3.5" />
        Reset
      </button>
    </div>
  );
}
