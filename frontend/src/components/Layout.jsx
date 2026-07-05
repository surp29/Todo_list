import { FiCheckSquare, FiLogOut } from 'react-icons/fi';
import { useAuth } from '../context/AuthContext';
import { ROLE, ROLE_LABEL } from '../utils/constants';
import NotificationBell from './NotificationBell';

export default function Layout({ children }) {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/80 backdrop-blur-sm">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3 sm:px-6">
          <div className="flex items-center gap-2.5">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-indigo-500 to-violet-600">
              <FiCheckSquare className="h-4 w-4 text-white" />
            </div>
            <span className="text-base font-semibold text-slate-800">TaskFlow</span>
          </div>

          <div className="flex items-center gap-3">
            {user?.role === ROLE.LEADER && <NotificationBell />}

            <div className="hidden text-right sm:block">
              <p className="text-sm font-medium leading-tight text-slate-700">{user?.fullName}</p>
              <p className="text-xs leading-tight text-slate-400">{ROLE_LABEL[user?.role]}</p>
            </div>

            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-indigo-100 text-sm font-semibold text-indigo-700">
              {user?.fullName?.charAt(0)?.toUpperCase()}
            </div>

            <button
              onClick={logout}
              title="Đăng xuất"
              className="flex h-9 w-9 items-center justify-center rounded-lg text-slate-500 transition hover:bg-slate-100 hover:text-rose-600"
            >
              <FiLogOut className="h-4 w-4" />
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-6 sm:px-6">{children}</main>
    </div>
  );
}
