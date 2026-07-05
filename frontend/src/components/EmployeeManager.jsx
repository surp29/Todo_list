import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { FiEdit2, FiKey, FiTrash2, FiUserPlus, FiUserX, FiX } from 'react-icons/fi';
import ConfirmDialog from './ConfirmDialog';

const createSchema = yup.object({
  fullName: yup.string().trim().required('Vui lòng nhập họ tên'),
  position: yup.string().trim().nullable(),
  username: yup
    .string()
    .trim()
    .min(3, 'Tên đăng nhập phải từ 3 ký tự')
    .required('Vui lòng nhập tên đăng nhập'),
  password: yup.string().min(6, 'Mật khẩu phải từ 6 ký tự').required('Vui lòng nhập mật khẩu'),
});

const editSchema = yup.object({
  fullName: yup.string().trim().required('Vui lòng nhập họ tên'),
  position: yup.string().trim().nullable(),
});

const passwordSchema = yup.object({
  newPassword: yup.string().min(6, 'Mật khẩu phải từ 6 ký tự').required('Vui lòng nhập mật khẩu mới'),
});

const inputClass =
  'w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 text-sm text-slate-800 outline-none transition focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100';

const smallInputClass =
  'w-full rounded-md border border-slate-200 bg-slate-50 px-2.5 py-1.5 text-sm text-slate-800 outline-none transition focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100';

function EmployeeRow({ employee, onUpdate, onResetPassword, onRequestRemove }) {
  const [mode, setMode] = useState('view'); // 'view' | 'edit' | 'password'
  const [rowError, setRowError] = useState('');

  const editForm = useForm({
    resolver: yupResolver(editSchema),
    defaultValues: { fullName: employee.fullName, position: employee.position || '' },
  });
  const passwordForm = useForm({ resolver: yupResolver(passwordSchema) });

  const cancel = () => {
    setMode('view');
    setRowError('');
    editForm.reset({ fullName: employee.fullName, position: employee.position || '' });
    passwordForm.reset({ newPassword: '' });
  };

  const submitEdit = async (values) => {
    setRowError('');
    try {
      await onUpdate(employee.id, values);
      setMode('view');
    } catch (err) {
      setRowError(err.response?.data?.message || 'Cập nhật thất bại');
    }
  };

  const submitPassword = async (values) => {
    setRowError('');
    try {
      await onResetPassword(employee.id, values.newPassword);
      setMode('view');
      passwordForm.reset({ newPassword: '' });
    } catch (err) {
      setRowError(err.response?.data?.message || 'Đặt lại mật khẩu thất bại');
    }
  };

  if (mode === 'edit') {
    return (
      <form
        onSubmit={editForm.handleSubmit(submitEdit)}
        className="space-y-2 rounded-lg border border-indigo-200 bg-indigo-50/40 p-3"
      >
        <div className="grid grid-cols-2 gap-2">
          <input {...editForm.register('fullName')} placeholder="Họ tên" className={smallInputClass} />
          <input {...editForm.register('position')} placeholder="Chức vụ" className={smallInputClass} />
        </div>
        {(editForm.formState.errors.fullName || rowError) && (
          <p className="text-xs text-rose-500">{editForm.formState.errors.fullName?.message || rowError}</p>
        )}
        <div className="flex justify-end gap-2">
          <button type="button" onClick={cancel} className="rounded-md px-3 py-1.5 text-xs text-slate-500 hover:bg-slate-100">
            Hủy
          </button>
          <button
            type="submit"
            disabled={editForm.formState.isSubmitting}
            className="rounded-md bg-indigo-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-indigo-700 disabled:opacity-60"
          >
            Lưu
          </button>
        </div>
      </form>
    );
  }

  if (mode === 'password') {
    return (
      <form
        onSubmit={passwordForm.handleSubmit(submitPassword)}
        className="space-y-2 rounded-lg border border-amber-200 bg-amber-50/40 p-3"
      >
        <p className="text-xs text-slate-500">
          Đặt mật khẩu mới cho <span className="font-medium text-slate-700">{employee.fullName}</span>
        </p>
        <input
          {...passwordForm.register('newPassword')}
          type="text"
          placeholder="Mật khẩu mới (tối thiểu 6 ký tự)"
          className={smallInputClass}
        />
        {(passwordForm.formState.errors.newPassword || rowError) && (
          <p className="text-xs text-rose-500">{passwordForm.formState.errors.newPassword?.message || rowError}</p>
        )}
        <div className="flex justify-end gap-2">
          <button type="button" onClick={cancel} className="rounded-md px-3 py-1.5 text-xs text-slate-500 hover:bg-slate-100">
            Hủy
          </button>
          <button
            type="submit"
            disabled={passwordForm.formState.isSubmitting}
            className="rounded-md bg-amber-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-amber-700 disabled:opacity-60"
          >
            Đặt lại mật khẩu
          </button>
        </div>
      </form>
    );
  }

  return (
    <div className="flex items-center gap-3 rounded-lg border border-slate-100 px-3 py-2.5">
      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-indigo-100 text-sm font-semibold text-indigo-700">
        {employee.fullName.charAt(0).toUpperCase()}
      </div>
      <div className="min-w-0 flex-1">
        <p className="flex items-center gap-1.5 text-sm font-medium text-slate-700">
          <span className="truncate">{employee.fullName}</span>
          {employee.position && <span className="font-normal text-slate-400">· {employee.position}</span>}
          {!employee.active && (
            <span className="shrink-0 rounded-full bg-slate-100 px-2 py-0.5 text-[11px] font-medium text-slate-500">
              Đã nghỉ việc
            </span>
          )}
        </p>
        <p className="text-xs text-slate-400">@{employee.username}</p>
      </div>

      {employee.active ? (
        <div className="flex shrink-0 items-center gap-1">
          <button
            onClick={() => setMode('edit')}
            title="Sửa thông tin"
            className="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-indigo-50 hover:text-indigo-600"
          >
            <FiEdit2 className="h-3.5 w-3.5" />
          </button>
          <button
            onClick={() => setMode('password')}
            title="Đổi mật khẩu"
            className="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-amber-50 hover:text-amber-600"
          >
            <FiKey className="h-3.5 w-3.5" />
          </button>
          <button
            onClick={() => onRequestRemove(employee, false)}
            title="Xóa nhân viên"
            className="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-rose-50 hover:text-rose-600"
          >
            <FiTrash2 className="h-3.5 w-3.5" />
          </button>
        </div>
      ) : (
        <button
          onClick={() => onRequestRemove(employee, true)}
          title="Xóa hẳn khỏi hệ thống"
          className="flex shrink-0 items-center gap-1.5 rounded-lg border border-rose-200 px-2.5 py-1.5 text-xs font-medium text-rose-600 transition hover:bg-rose-50"
        >
          <FiUserX className="h-3.5 w-3.5" />
          Xóa
        </button>
      )}
    </div>
  );
}

export default function EmployeeManager({ employees, onCreate, onUpdate, onResetPassword, onRemove, onClose }) {
  const [serverError, setServerError] = useState('');
  const [removeRequest, setRemoveRequest] = useState(null); // { employee, force }
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: yupResolver(createSchema) });

  const submitHandler = async (values) => {
    setServerError('');
    try {
      await onCreate(values);
      reset();
    } catch (err) {
      setServerError(err.response?.data?.message || 'Tạo tài khoản thất bại');
    }
  };

  const confirmRemove = async () => {
    const { employee, force } = removeRequest;
    setRemoveRequest(null);
    await onRemove(employee.id, force);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm">
      <div className="flex max-h-[85vh] w-full max-w-2xl flex-col rounded-2xl bg-white shadow-2xl">
        <div className="flex items-center justify-between border-b border-slate-100 px-6 py-4">
          <h2 className="text-lg font-semibold text-slate-800">Quản lý nhân viên</h2>
          <button
            onClick={onClose}
            className="flex h-8 w-8 items-center justify-center rounded-lg text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
          >
            <FiX className="h-4 w-4" />
          </button>
        </div>

        <div className="flex-1 overflow-y-auto px-6 py-5">
          <form onSubmit={handleSubmit(submitHandler)} className="mb-6 grid grid-cols-1 gap-3 sm:grid-cols-2">
            <input {...register('fullName')} placeholder="Họ tên" className={inputClass} />
            <input {...register('position')} placeholder="Chức vụ (VD: Developer, Designer...)" className={inputClass} />
            <input {...register('username')} placeholder="Tên đăng nhập" className={inputClass} />
            <input {...register('password')} type="password" placeholder="Mật khẩu" className={inputClass} />

            {(errors.fullName || errors.username || errors.password || serverError) && (
              <div className="sm:col-span-2 space-y-1">
                {errors.fullName && <p className="text-xs text-rose-500">{errors.fullName.message}</p>}
                {errors.username && <p className="text-xs text-rose-500">{errors.username.message}</p>}
                {errors.password && <p className="text-xs text-rose-500">{errors.password.message}</p>}
                {serverError && <p className="text-xs text-rose-500">{serverError}</p>}
              </div>
            )}

            <button
              type="submit"
              disabled={isSubmitting}
              className="flex items-center justify-center gap-1.5 rounded-lg bg-gradient-to-r from-indigo-500 to-violet-600 px-4 py-2.5 text-sm font-medium text-white shadow-md shadow-indigo-500/20 transition hover:brightness-110 disabled:opacity-60 sm:col-span-2"
            >
              <FiUserPlus className="h-4 w-4" />
              {isSubmitting ? 'Đang tạo...' : 'Tạo tài khoản nhân viên'}
            </button>
          </form>

          <h3 className="mb-2 text-sm font-medium text-slate-500">Danh sách nhân viên ({employees.length})</h3>
          <div className="space-y-2">
            {employees.length === 0 && (
              <p className="rounded-lg border border-dashed border-slate-300 p-4 text-center text-sm text-slate-400">
                Chưa có nhân viên nào.
              </p>
            )}
            {employees.map((emp) => (
              <EmployeeRow
                key={emp.id}
                employee={emp}
                onUpdate={onUpdate}
                onResetPassword={onResetPassword}
                onRequestRemove={(employee, force) => setRemoveRequest({ employee, force })}
              />
            ))}
          </div>
        </div>
      </div>

      {removeRequest && (
        <ConfirmDialog
          title={removeRequest.force ? 'Xóa hẳn khỏi hệ thống' : 'Xóa nhân viên'}
          message={
            removeRequest.force
              ? `Xóa vĩnh viễn tài khoản "${removeRequest.employee.fullName}" cùng TOÀN BỘ công việc và lịch sử năng suất liên quan? Hành động này không thể hoàn tác. Username sẽ được giải phóng để tạo tài khoản mới.`
              : `Bạn có chắc muốn xóa "${removeRequest.employee.fullName}"? Nếu nhân viên đã có lịch sử công việc, tài khoản sẽ được vô hiệu hóa thay vì xóa hẳn để giữ số liệu năng suất.`
          }
          onConfirm={confirmRemove}
          onCancel={() => setRemoveRequest(null)}
        />
      )}
    </div>
  );
}
