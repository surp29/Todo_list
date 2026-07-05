import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { FiLock, FiUser, FiCheckSquare } from 'react-icons/fi';

const schema = yup.object({
  username: yup.string().trim().required('Vui lòng nhập tên đăng nhập'),
  password: yup.string().required('Vui lòng nhập mật khẩu'),
});

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [serverError, setServerError] = useState('');

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm({ resolver: yupResolver(schema) });

  const onSubmit = async ({ username, password }) => {
    setServerError('');
    try {
      const user = await login(username, password);
      const redirectTo = location.state?.from || (user.role === 'LEADER' ? '/leader' : '/employee');
      navigate(redirectTo, { replace: true });
    } catch (err) {
      setServerError(err.response?.data?.message || 'Đăng nhập thất bại, vui lòng thử lại');
    }
  };

  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden bg-slate-950 px-4">
      <div className="pointer-events-none absolute -left-32 -top-32 h-96 w-96 rounded-full bg-indigo-600/30 blur-3xl" />
      <div className="pointer-events-none absolute -bottom-32 -right-32 h-96 w-96 rounded-full bg-violet-600/30 blur-3xl" />

      <div className="relative w-full max-w-sm rounded-2xl border border-white/10 bg-white/5 p-8 shadow-2xl backdrop-blur-xl">
        <div className="mb-8 flex flex-col items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-500 to-violet-600 shadow-lg shadow-indigo-500/30">
            <FiCheckSquare className="h-6 w-6 text-white" />
          </div>
          <div className="text-center">
            <h1 className="text-xl font-semibold text-white">TaskFlow</h1>
            <p className="mt-1 text-sm text-slate-400">Đăng nhập để quản lý công việc nhóm</p>
          </div>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-300">Tên đăng nhập</label>
            <div className="relative">
              <FiUser className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
              <input
                type="text"
                autoComplete="username"
                {...register('username')}
                className="w-full rounded-lg border border-white/10 bg-white/5 py-2.5 pl-9 pr-3 text-sm text-white placeholder-slate-500 outline-none transition focus:border-indigo-400 focus:ring-2 focus:ring-indigo-400/30"
                placeholder="leader"
              />
            </div>
            {errors.username && <p className="mt-1 text-xs text-rose-400">{errors.username.message}</p>}
          </div>

          <div>
            <label className="mb-1.5 block text-sm font-medium text-slate-300">Mật khẩu</label>
            <div className="relative">
              <FiLock className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
              <input
                type="password"
                autoComplete="current-password"
                {...register('password')}
                className="w-full rounded-lg border border-white/10 bg-white/5 py-2.5 pl-9 pr-3 text-sm text-white placeholder-slate-500 outline-none transition focus:border-indigo-400 focus:ring-2 focus:ring-indigo-400/30"
                placeholder="••••••••"
              />
            </div>
            {errors.password && <p className="mt-1 text-xs text-rose-400">{errors.password.message}</p>}
          </div>

          {serverError && (
            <p className="rounded-lg border border-rose-500/30 bg-rose-500/10 px-3 py-2 text-xs text-rose-300">
              {serverError}
            </p>
          )}

          <button
            type="submit"
            disabled={isSubmitting}
            className="mt-2 w-full rounded-lg bg-gradient-to-r from-indigo-500 to-violet-600 py-2.5 text-sm font-medium text-white shadow-lg shadow-indigo-500/30 transition hover:brightness-110 disabled:opacity-60"
          >
            {isSubmitting ? 'Đang đăng nhập...' : 'Đăng nhập'}
          </button>
        </form>

        <p className="mt-6 text-center text-xs text-slate-500">
          Tài khoản nhân viên do Trưởng nhóm (Leader) tạo trong hệ thống.
        </p>
      </div>
    </div>
  );
}
