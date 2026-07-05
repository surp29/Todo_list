import { FiAlertTriangle } from 'react-icons/fi';

export default function ConfirmDialog({ title = 'Xác nhận', message, onConfirm, onCancel }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm">
      <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-2xl">
        <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-full bg-rose-50">
          <FiAlertTriangle className="h-5 w-5 text-rose-500" />
        </div>
        <h2 className="mb-1.5 text-lg font-semibold text-slate-800">{title}</h2>
        <p className="mb-6 text-sm text-slate-500">{message}</p>

        <div className="flex justify-end gap-2">
          <button
            onClick={onCancel}
            className="rounded-lg border border-slate-200 px-4 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
          >
            Hủy
          </button>
          <button
            onClick={onConfirm}
            className="rounded-lg bg-rose-600 px-4 py-2.5 text-sm font-medium text-white shadow-md shadow-rose-500/20 transition hover:bg-rose-700"
          >
            Xóa
          </button>
        </div>
      </div>
    </div>
  );
}
