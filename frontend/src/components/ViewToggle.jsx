import { FiColumns, FiList } from 'react-icons/fi';

export default function ViewToggle({ view, onChange }) {
  const options = [
    { value: 'list', label: 'Danh sách', icon: FiList },
    { value: 'board', label: 'Bảng Kanban', icon: FiColumns },
  ];

  return (
    <div className="inline-flex rounded-lg border border-slate-200 bg-white p-1">
      {options.map(({ value, label, icon: Icon }) => (
        <button
          key={value}
          onClick={() => onChange(value)}
          className={`flex items-center gap-1.5 rounded-md px-3 py-1.5 text-sm font-medium transition ${
            view === value ? 'bg-indigo-50 text-indigo-700' : 'text-slate-500 hover:bg-slate-50'
          }`}
        >
          <Icon className="h-3.5 w-3.5" />
          {label}
        </button>
      ))}
    </div>
  );
}
