import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { FiX } from 'react-icons/fi';
import { STATUS, STATUS_LABEL, PRIORITY, PRIORITY_LABEL } from '../utils/constants';

const schema = yup.object({
  title: yup
    .string()
    .trim()
    .required('Tiêu đề không được để trống')
    .max(255, 'Tiêu đề không được vượt quá 255 ký tự'),
  description: yup.string().max(1000, 'Mô tả không được vượt quá 1000 ký tự').nullable(),
  status: yup.string().oneOf(Object.values(STATUS)).required('Trạng thái không được để trống'),
  priority: yup.string().oneOf(Object.values(PRIORITY)).required('Độ ưu tiên không được để trống'),
  assigneeId: yup
    .number()
    .typeError('Phải chọn người được giao việc')
    .required('Phải chọn người được giao việc'),
  dueDate: yup
    .string()
    .nullable()
    .test('not-in-past', 'Ngày hết hạn không được là ngày quá khứ', (value) => {
      if (!value) return true;
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return new Date(value) >= today;
    }),
});

const defaultValues = {
  title: '',
  description: '',
  status: STATUS.PENDING,
  priority: PRIORITY.MEDIUM,
  assigneeId: '',
  dueDate: '',
};

const inputClass =
  'w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 text-sm text-slate-800 outline-none transition focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100';
const labelClass = 'mb-1.5 block text-sm font-medium text-slate-600';

// Passing `initialData` switches the form from create mode to edit mode.
export default function TodoForm({ initialData, employees, onSubmit, onClose }) {
  const isEditMode = Boolean(initialData);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: yupResolver(schema),
    defaultValues,
  });

  useEffect(() => {
    if (initialData) {
      reset({
        title: initialData.title || '',
        description: initialData.description || '',
        status: initialData.status,
        priority: initialData.priority,
        assigneeId: initialData.assigneeId || '',
        dueDate: initialData.dueDate || '',
      });
    } else {
      reset(defaultValues);
    }
  }, [initialData, reset]);

  const submitHandler = async (values) => {
    const payload = {
      ...values,
      assigneeId: Number(values.assigneeId),
      dueDate: values.dueDate || null,
    };
    const success = await onSubmit(payload);
    if (success) {
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm">
      <div className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl">
        <div className="mb-5 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-slate-800">
            {isEditMode ? 'Sửa công việc' : 'Giao việc mới'}
          </h2>
          <button
            onClick={onClose}
            className="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
          >
            <FiX className="h-4 w-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit(submitHandler)} className="space-y-4">
          <div>
            <label className={labelClass}>
              Tiêu đề <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              {...register('title')}
              className={inputClass}
              placeholder="Nhập tiêu đề công việc"
            />
            {errors.title && <p className="mt-1 text-xs text-rose-500">{errors.title.message}</p>}
          </div>

          <div>
            <label className={labelClass}>Mô tả</label>
            <textarea
              {...register('description')}
              rows={3}
              className={inputClass}
              placeholder="Mô tả chi tiết (không bắt buộc)"
            />
            {errors.description && <p className="mt-1 text-xs text-rose-500">{errors.description.message}</p>}
          </div>

          <div>
            <label className={labelClass}>
              Giao cho <span className="text-rose-500">*</span>
            </label>
            <select {...register('assigneeId')} className={inputClass}>
              <option value="">-- Chọn nhân viên --</option>
              {employees?.map((emp) => (
                <option key={emp.id} value={emp.id}>
                  {emp.fullName}
                </option>
              ))}
            </select>
            {errors.assigneeId && <p className="mt-1 text-xs text-rose-500">{errors.assigneeId.message}</p>}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className={labelClass}>
                Trạng thái <span className="text-rose-500">*</span>
              </label>
              <select {...register('status')} className={inputClass}>
                {Object.values(STATUS).map((s) => (
                  <option key={s} value={s}>
                    {STATUS_LABEL[s]}
                  </option>
                ))}
              </select>
              {errors.status && <p className="mt-1 text-xs text-rose-500">{errors.status.message}</p>}
            </div>

            <div>
              <label className={labelClass}>
                Độ ưu tiên <span className="text-rose-500">*</span>
              </label>
              <select {...register('priority')} className={inputClass}>
                {Object.values(PRIORITY).map((p) => (
                  <option key={p} value={p}>
                    {PRIORITY_LABEL[p]}
                  </option>
                ))}
              </select>
              {errors.priority && <p className="mt-1 text-xs text-rose-500">{errors.priority.message}</p>}
            </div>
          </div>

          <div>
            <label className={labelClass}>Ngày hết hạn</label>
            <input type="date" {...register('dueDate')} className={inputClass} />
            {errors.dueDate && <p className="mt-1 text-xs text-rose-500">{errors.dueDate.message}</p>}
          </div>

          <div className="flex justify-end gap-2 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg border border-slate-200 px-4 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-slate-50"
            >
              Hủy
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="rounded-lg bg-gradient-to-r from-indigo-500 to-violet-600 px-4 py-2.5 text-sm font-medium text-white shadow-md shadow-indigo-500/20 transition hover:brightness-110 disabled:opacity-60"
            >
              {isSubmitting ? 'Đang lưu...' : isEditMode ? 'Cập nhật' : 'Giao việc'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
