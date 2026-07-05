import { FiChevronLeft, FiChevronRight } from 'react-icons/fi';
import { ALL_PAGE_SIZE, PAGE_SIZE_OPTIONS } from '../utils/constants';

export default function Pagination({
  currentPage,
  totalPages,
  totalElements,
  pageSize,
  isFirst,
  isLast,
  onPageChange,
  onPageSizeChange,
}) {
  if (totalElements === 0) return null;

  const start = currentPage * pageSize + 1;
  const end = Math.min(start + pageSize - 1, totalElements);

  return (
    <div className="mt-5 flex flex-col items-center justify-between gap-3 sm:flex-row">
      <p className="text-sm text-slate-500">
        Hiển thị <span className="font-medium text-slate-700">{start}-{end}</span> của{' '}
        <span className="font-medium text-slate-700">{totalElements}</span> công việc
      </p>

      <div className="flex items-center gap-3">
        <label className="flex items-center gap-1.5 text-sm text-slate-500">
          Hiển thị
          <select
            value={pageSize}
            onChange={(e) => onPageSizeChange(Number(e.target.value))}
            className="rounded-lg border border-slate-200 bg-slate-50 px-2 py-1.5 text-sm text-slate-700 outline-none transition focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100"
          >
            {PAGE_SIZE_OPTIONS.map((size) => (
              <option key={size} value={size}>
                {size === ALL_PAGE_SIZE ? 'Tất cả' : size}
              </option>
            ))}
          </select>
        </label>

        <div className="flex items-center gap-2">
          <button
            onClick={() => onPageChange(currentPage - 1)}
            disabled={isFirst}
            className="flex h-8 w-8 items-center justify-center rounded-lg border border-slate-200 text-slate-500 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
          >
            <FiChevronLeft className="h-4 w-4" />
          </button>
          <span className="min-w-[80px] text-center text-sm text-slate-600">
            Trang {totalPages === 0 ? 0 : currentPage + 1} / {totalPages}
          </span>
          <button
            onClick={() => onPageChange(currentPage + 1)}
            disabled={isLast}
            className="flex h-8 w-8 items-center justify-center rounded-lg border border-slate-200 text-slate-500 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-40"
          >
            <FiChevronRight className="h-4 w-4" />
          </button>
        </div>
      </div>
    </div>
  );
}
