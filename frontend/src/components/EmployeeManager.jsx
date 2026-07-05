import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { FiUserPlus, FiX } from 'react-icons/fi';

const schema = yup.object({
  fullName: yup.string().trim().required('Vui lòng nhập họ tên'),
  position: yup.string().trim().nullable(),
  username: yup
    .string()
    .trim()
    .min(3, 'Tên đăng nhập phải từ 3 ký tự')
    .required('Vui lòng nhập tên đăng nhập'),
  password: yup.string().min(6, 'Mật khẩu phải từ 6 ký tự').required('Vui lòng nhập mật khẩu'),
});

const inputClass =
  'w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 text-sm text-slate-800 outline-none transition focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100';

export default function EmployeeManager({ employees, onCreate, onClose }) {
  const [serverError, setServerError] = useState('');
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: yupResolver(schema) });

  const submitHandler = async (values) => {
    setServerError('');
    try {
      await onCreate(values);
      reset();
    } catch (err) {
      setServerError(err.response?.data?.message || 'Tạo tài khoản thất bại');
    }
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
              <div
                key={emp.id}
                className="flex items-center gap-3 rounded-lg border border-slate-100 px-3 py-2.5"
              >
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-indigo-100 text-sm font-semibold text-indigo-700">
                  {emp.fullName.charAt(0).toUpperCase()}
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-700">
                    {emp.fullName}
                    {emp.position && <span className="ml-1.5 font-normal text-slate-400">· {emp.position}</span>}
                  </p>
                  <p className="text-xs text-slate-400">@{emp.username}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
