export const STATUS = {
  PENDING: 'PENDING',
  IN_PROGRESS: 'IN_PROGRESS',
  ON_HOLD: 'ON_HOLD',
  COMPLETED: 'COMPLETED',
};

export const STATUS_LABEL = {
  [STATUS.PENDING]: 'Chưa làm',
  [STATUS.IN_PROGRESS]: 'Đang làm',
  [STATUS.ON_HOLD]: 'Tạm hoãn',
  [STATUS.COMPLETED]: 'Hoàn thành',
};

export const STATUS_BADGE_CLASS = {
  [STATUS.PENDING]: 'bg-slate-100 text-slate-600 ring-1 ring-inset ring-slate-200',
  [STATUS.IN_PROGRESS]: 'bg-blue-50 text-blue-700 ring-1 ring-inset ring-blue-200',
  [STATUS.ON_HOLD]: 'bg-amber-50 text-amber-700 ring-1 ring-inset ring-amber-200',
  [STATUS.COMPLETED]: 'bg-emerald-50 text-emerald-700 ring-1 ring-inset ring-emerald-200',
};

export const STATUS_DOT_CLASS = {
  [STATUS.PENDING]: 'bg-slate-400',
  [STATUS.IN_PROGRESS]: 'bg-blue-500',
  [STATUS.ON_HOLD]: 'bg-amber-500',
  [STATUS.COMPLETED]: 'bg-emerald-500',
};

export const PRIORITY = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
};

export const PRIORITY_LABEL = {
  [PRIORITY.LOW]: 'Thấp',
  [PRIORITY.MEDIUM]: 'Trung bình',
  [PRIORITY.HIGH]: 'Cao',
};

export const PRIORITY_BADGE_CLASS = {
  [PRIORITY.LOW]: 'bg-teal-50 text-teal-700 ring-1 ring-inset ring-teal-200',
  [PRIORITY.MEDIUM]: 'bg-amber-50 text-amber-700 ring-1 ring-inset ring-amber-200',
  [PRIORITY.HIGH]: 'bg-rose-50 text-rose-700 ring-1 ring-inset ring-rose-200',
};

export const ROLE = {
  LEADER: 'LEADER',
  EMPLOYEE: 'EMPLOYEE',
};

export const ROLE_LABEL = {
  [ROLE.LEADER]: 'Trưởng nhóm',
  [ROLE.EMPLOYEE]: 'Nhân viên',
};

export const DEFAULT_PAGE_SIZE = 10;
export const DEBOUNCE_DELAY_MS = 300;
export const TOAST_DURATION_MS = 3000;

// Sentinel size sent to the API for the "ALL" option (matches the backend's max page size).
export const ALL_PAGE_SIZE = 1000;
export const PAGE_SIZE_OPTIONS = [5, 10, 15, 20, 30, 50, ALL_PAGE_SIZE];
