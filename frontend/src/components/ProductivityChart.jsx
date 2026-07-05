import { useState } from 'react';
import { FiAward, FiCheckCircle, FiClipboard, FiTrendingUp } from 'react-icons/fi';
import useProductivity from '../hooks/useProductivity';

const BAR_COLOR = '#4a3aa7';

function StatTile({ icon: Icon, label, value }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-4">
      <div className="flex items-center gap-2 text-slate-400">
        <Icon className="h-4 w-4" />
        <span className="text-xs font-medium">{label}</span>
      </div>
      <p className="mt-2 text-2xl font-semibold text-slate-800">{value}</p>
    </div>
  );
}

function ProductivitySkeleton() {
  return (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="h-20 animate-pulse rounded-xl bg-slate-100" />
        ))}
      </div>
      <div className="h-64 animate-pulse rounded-xl bg-slate-100" />
    </div>
  );
}

/**
 * Ranks employees by productivity score (priority-weighted completion rate,
 * penalized for late finishes — see AnalyticsServiceImpl for the formula).
 */
export default function ProductivityChart() {
  const { overview, loading, error } = useProductivity();
  const [hoveredId, setHoveredId] = useState(null);

  if (loading) return <ProductivitySkeleton />;

  if (error) {
    return <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-600">{error}</div>;
  }

  const employees = overview?.employees || [];
  const topPerformer = employees[0];

  if (employees.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-slate-300 p-10 text-center text-sm text-slate-400">
        Chưa có nhân viên nào để thống kê năng suất.
      </div>
    );
  }

  return (
    <div className="space-y-5">
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <StatTile icon={FiClipboard} label="Tổng công việc đã giao" value={overview.totalAssigned} />
        <StatTile icon={FiCheckCircle} label="Đã hoàn thành" value={overview.totalCompleted} />
        <StatTile icon={FiTrendingUp} label="Điểm năng suất TB nhóm" value={`${overview.teamAverageScore}%`} />
        <StatTile icon={FiAward} label="Xuất sắc nhất" value={topPerformer?.employeeName || '—'} />
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-5">
        <h3 className="mb-1 text-sm font-semibold text-slate-800">Điểm năng suất theo nhân viên</h3>
        <p className="mb-4 text-xs text-slate-400">
          Tính theo mức độ ưu tiên công việc hoàn thành và đúng hạn — xem chi tiết ở bảng bên dưới
        </p>

        <div className="space-y-3">
          {employees.map((emp) => (
            <div key={emp.employeeId} className="group relative flex items-center gap-3">
              <div className="w-32 shrink-0 truncate text-sm text-slate-600" title={emp.employeeName}>
                {emp.employeeName}
              </div>
              <div className="relative flex-1">
                <div
                  className="h-5 rounded-r bg-[var(--bar-color)] transition-all"
                  style={{ width: `${Math.max(emp.productivityScore, 2)}%`, '--bar-color': BAR_COLOR }}
                  onMouseEnter={() => setHoveredId(emp.employeeId)}
                  onMouseLeave={() => setHoveredId(null)}
                />
              </div>
              <div className="w-12 shrink-0 text-sm font-medium text-slate-700">{emp.productivityScore}%</div>

              {hoveredId === emp.employeeId && (
                <div className="absolute left-32 top-full z-10 mt-1 w-56 rounded-lg border border-slate-200 bg-white p-3 text-xs shadow-lg">
                  <p className="font-semibold text-slate-700">{emp.employeeName}</p>
                  {emp.position && <p className="text-slate-400">{emp.position}</p>}
                  <dl className="mt-2 space-y-1 text-slate-500">
                    <div className="flex justify-between">
                      <dt>Tổng công việc</dt>
                      <dd className="font-medium text-slate-700">{emp.totalAssigned}</dd>
                    </div>
                    <div className="flex justify-between">
                      <dt>Tỷ lệ hoàn thành</dt>
                      <dd className="font-medium text-slate-700">{emp.completionRate}%</dd>
                    </div>
                    <div className="flex justify-between">
                      <dt>Tỷ lệ đúng hạn</dt>
                      <dd className="font-medium text-slate-700">{emp.onTimeRate}%</dd>
                    </div>
                    <div className="flex justify-between">
                      <dt>Quá hạn</dt>
                      <dd className="font-medium text-slate-700">{emp.overdueCount}</dd>
                    </div>
                  </dl>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
        <table className="w-full text-left text-sm">
          <thead>
            <tr className="border-b border-slate-100 text-xs text-slate-400">
              <th className="px-4 py-3 font-medium">Nhân viên</th>
              <th className="px-4 py-3 font-medium">Chức vụ</th>
              <th className="px-4 py-3 font-medium">Tổng việc</th>
              <th className="px-4 py-3 font-medium">Chưa làm</th>
              <th className="px-4 py-3 font-medium">Đang làm</th>
              <th className="px-4 py-3 font-medium">Hoàn thành</th>
              <th className="px-4 py-3 font-medium">Quá hạn</th>
              <th className="px-4 py-3 font-medium">Tỷ lệ hoàn thành</th>
              <th className="px-4 py-3 font-medium">Tỷ lệ đúng hạn</th>
              <th className="px-4 py-3 font-medium">Điểm năng suất</th>
            </tr>
          </thead>
          <tbody>
            {employees.map((emp) => (
              <tr key={emp.employeeId} className="border-b border-slate-50 text-slate-700 last:border-0">
                <td className="px-4 py-2.5 font-medium">{emp.employeeName}</td>
                <td className="px-4 py-2.5 text-slate-500">{emp.position || '—'}</td>
                <td className="px-4 py-2.5 tabular-nums">{emp.totalAssigned}</td>
                <td className="px-4 py-2.5 tabular-nums">{emp.pendingCount}</td>
                <td className="px-4 py-2.5 tabular-nums">{emp.inProgressCount}</td>
                <td className="px-4 py-2.5 tabular-nums">{emp.completedCount}</td>
                <td className="px-4 py-2.5 tabular-nums">{emp.overdueCount}</td>
                <td className="px-4 py-2.5 tabular-nums">{emp.completionRate}%</td>
                <td className="px-4 py-2.5 tabular-nums">{emp.onTimeRate}%</td>
                <td className="px-4 py-2.5 font-semibold tabular-nums">{emp.productivityScore}%</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
