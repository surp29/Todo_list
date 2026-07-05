import { useEffect } from 'react';
import { FiAlertCircle, FiCheckCircle } from 'react-icons/fi';
import { TOAST_DURATION_MS } from '../utils/constants';

export default function Toast({ toast, onClose }) {
  useEffect(() => {
    if (!toast) return undefined;
    const timer = setTimeout(onClose, TOAST_DURATION_MS);
    return () => clearTimeout(timer);
  }, [toast, onClose]);

  if (!toast) return null;

  const isError = toast.type === 'error';

  return (
    <div className="fixed bottom-5 right-5 z-50 animate-[fadeIn_0.2s_ease-out]">
      <div
        className={`flex items-center gap-2.5 rounded-xl px-4 py-3 text-sm font-medium text-white shadow-xl ${
          isError ? 'bg-rose-600' : 'bg-emerald-600'
        }`}
      >
        {isError ? <FiAlertCircle className="h-4 w-4 shrink-0" /> : <FiCheckCircle className="h-4 w-4 shrink-0" />}
        {toast.message}
      </div>
    </div>
  );
}
