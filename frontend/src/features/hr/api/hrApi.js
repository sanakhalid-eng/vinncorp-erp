import API from '../../../api/axios';
import { getProjects } from '../../projects/api/projectApi';

const handleErr = (e, fallback) =>
  e?.response?.data?.message || e?.message || fallback;

const unwrap = (res) => res?.data?.data ?? res?.data ?? [];

const normalizeEmployee = (e = {}) => ({
  id: e.id,
  workspaceId: e.workspaceId,
  employeeCode: e.employeeCode ?? '',
  firstName: e.firstName ?? '',
  lastName: e.lastName ?? '',
  fullName: e.fullName || `${e.firstName ?? ''} ${e.lastName ?? ''}`.trim(),
  workEmail: e.workEmail ?? '',
  personalEmail: e.personalEmail ?? '',
  phone: e.phone ?? '',
  employmentType: e.employmentType ?? 'FULL_TIME',
  status: e.status ?? 'ACTIVE',
  dateOfBirth: e.dateOfBirth ?? null,
  hireDate: e.hireDate ?? null,
  terminationDate: e.terminationDate ?? null,
  jobTitle: e.jobTitle ?? '',
  timezone: e.timezone ?? '',
  locale: e.locale ?? '',
  managerId: e.managerId ?? null,
  userId: e.userId ?? null,
  departmentId: e.departmentId ?? null,
  designationId: e.designationId ?? null,
  createdAt: e.createdAt ?? null,
  updatedAt: e.updatedAt ?? null,
});

const normalizeDepartment = (d = {}) => ({
  id: d.id,
  workspaceId: d.workspaceId,
  name: d.name ?? '',
  code: d.code ?? '',
  description: d.description ?? '',
  headEmployeeId: d.headEmployeeId ?? null,
  parentDepartmentId: d.parentDepartmentId ?? null,
  active: !!d.active,
  createdAt: d.createdAt ?? null,
  updatedAt: d.updatedAt ?? null,
});

const normalizeDesignation = (d = {}) => ({
  id: d.id,
  workspaceId: d.workspaceId,
  title: d.title ?? '',
  code: d.code ?? '',
  description: d.description ?? '',
  level: d.level ?? 0,
  active: !!d.active,
  createdAt: d.createdAt ?? null,
  updatedAt: d.updatedAt ?? null,
});

export const listEmployees = async (params = {}) => {
  try {
    const res = await API.get('/hr/employees', { params });
    return (unwrap(res) || []).map(normalizeEmployee);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load employees'));
  }
};

export const getEmployee = async (id) => {
  try {
    const res = await API.get(`/hr/employees/${id}`);
    return normalizeEmployee(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load employee'));
  }
};

export const createEmployee = async (payload) => {
  try {
    const res = await API.post('/hr/employees', payload);
    return normalizeEmployee(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to create employee'));
  }
};

export const updateEmployee = async (id, payload) => {
  try {
    const res = await API.put(`/hr/employees/${id}`, payload);
    return normalizeEmployee(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to update employee'));
  }
};

export const deleteEmployee = async (id) => {
  try {
    await API.delete(`/hr/employees/${id}`);
    return true;
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to delete employee'));
  }
};

export const listDepartments = async (activeOnly = false) => {
  try {
    const res = await API.get('/hr/departments', { params: { activeOnly } });
    return (unwrap(res) || []).map(normalizeDepartment);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load departments'));
  }
};

export const getDepartment = async (id) => {
  try {
    const res = await API.get(`/hr/departments/${id}`);
    return normalizeDepartment(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load department'));
  }
};

export const createDepartment = async (payload) => {
  try {
    const res = await API.post('/hr/departments', payload);
    return normalizeDepartment(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to create department'));
  }
};

export const updateDepartment = async (id, payload) => {
  try {
    const res = await API.put(`/hr/departments/${id}`, payload);
    return normalizeDepartment(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to update department'));
  }
};

export const deleteDepartment = async (id) => {
  try {
    await API.delete(`/hr/departments/${id}`);
    return true;
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to delete department'));
  }
};

export const listDesignations = async (activeOnly = false) => {
  try {
    const res = await API.get('/hr/designations', { params: { activeOnly } });
    return (unwrap(res) || []).map(normalizeDesignation);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load designations'));
  }
};

export const getDesignation = async (id) => {
  try {
    const res = await API.get(`/hr/designations/${id}`);
    return normalizeDesignation(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load designation'));
  }
};

export const createDesignation = async (payload) => {
  try {
    const res = await API.post('/hr/designations', payload);
    return normalizeDesignation(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to create designation'));
  }
};

export const updateDesignation = async (id, payload) => {
  try {
    const res = await API.put(`/hr/designations/${id}`, payload);
    return normalizeDesignation(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to update designation'));
  }
};

export const deleteDesignation = async (id) => {
  try {
    await API.delete(`/hr/designations/${id}`);
    return true;
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to delete designation'));
  }
};

export const EmployeeStatuses = [
  'ACTIVE',
  'ON_LEAVE',
  'SUSPENDED',
  'TERMINATED',
  'PROBATION',
];

export const EmploymentTypes = [
  'FULL_TIME',
  'PART_TIME',
  'CONTRACT',
  'INTERN',
  'CONSULTANT',
];

// ΓöÇΓöÇ Attendance ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ

const normalizeAttendance = (a = {}) => ({
  id: a.id,
  employeeId: a.employeeId,
  employeeName: a.employeeName ?? '',
  employeeEmail: a.employeeEmail ?? '',
  attendanceDate: a.attendanceDate ?? null,
  checkInTime: a.checkInTime ?? null,
  checkOutTime: a.checkOutTime ?? null,
  status: a.status ?? 'ABSENT',
  shiftId: a.shiftId ?? null,
  shiftName: a.shiftName ?? '',
  workHours: a.workHours ?? 0,
  overtimeHours: a.overtimeHours ?? 0,
  lateMinutes: a.lateMinutes ?? 0,
  earlyLeaveMinutes: a.earlyLeaveMinutes ?? 0,
  notes: a.notes ?? '',
  createdAt: a.createdAt ?? null,
  updatedAt: a.updatedAt ?? null,
});

const normalizeDashboard = (d = {}) => ({
  date: d.date ?? null,
  totalEmployees: d.totalEmployees ?? 0,
  presentCount: d.presentCount ?? 0,
  absentCount: d.absentCount ?? 0,
  lateCount: d.lateCount ?? 0,
  onLeaveCount: d.onLeaveCount ?? 0,
  halfDayCount: d.halfDayCount ?? 0,
});

export const checkIn = async (payload) => {
  try {
    const res = await API.post('/hr/attendance/check-in', payload);
    return normalizeAttendance(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Check-in failed'));
  }
};

export const checkOut = async (payload) => {
  try {
    const res = await API.post('/hr/attendance/check-out', payload);
    return normalizeAttendance(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Check-out failed'));
  }
};

export const getAttendance = async (id) => {
  try {
    const res = await API.get(`/hr/attendance/${id}`);
    return normalizeAttendance(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load attendance'));
  }
};

export const getEmployeeAttendance = async (employeeId, startDate, endDate) => {
  try {
    const res = await API.get(`/hr/attendance/employee/${employeeId}`, {
      params: { startDate, endDate },
    });
    return (unwrap(res) || []).map(normalizeAttendance);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load attendance'));
  }
};

export const getAttendanceByDate = async (date) => {
  try {
    const res = await API.get(`/hr/attendance/date/${date}`);
    return (unwrap(res) || []).map(normalizeAttendance);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load attendance'));
  }
};

export const getAttendanceDashboard = async (date) => {
  try {
    const params = date ? { date } : {};
    const res = await API.get('/hr/attendance/dashboard', { params });
    return normalizeDashboard(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load dashboard'));
  }
};

export const updateAttendance = async (id, payload) => {
  try {
    const res = await API.put(`/hr/attendance/${id}`, payload);
    return normalizeAttendance(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to update attendance'));
  }
};

export const deleteAttendance = async (id) => {
  try {
    await API.delete(`/hr/attendance/${id}`);
    return true;
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to delete attendance'));
  }
};

export const AttendanceStatuses = [
  'PRESENT',
  'ABSENT',
  'LATE',
  'HALF_DAY',
  'ON_LEAVE',
];

// ΓöÇΓöÇ Shifts ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ

const normalizeShift = (s = {}) => ({
  id: s.id,
  name: s.name ?? '',
  startTime: s.startTime ?? '',
  endTime: s.endTime ?? '',
  breakMinutes: s.breakMinutes ?? 0,
  gracePeriodMinutes: s.gracePeriodMinutes ?? 0,
  active: s.active ?? true,
  createdAt: s.createdAt ?? null,
  updatedAt: s.updatedAt ?? null,
});

export const listShifts = async () => {
  try {
    const res = await API.get('/hr/shifts');
    return (unwrap(res) || []).map(normalizeShift);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load shifts'));
  }
};

export const listActiveShifts = async () => {
  try {
    const res = await API.get('/hr/shifts/active');
    return (unwrap(res) || []).map(normalizeShift);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load shifts'));
  }
};

export const getShift = async (id) => {
  try {
    const res = await API.get(`/hr/shifts/${id}`);
    return normalizeShift(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load shift'));
  }
};

export const createShift = async (payload) => {
  try {
    const res = await API.post('/hr/shifts', payload);
    return normalizeShift(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to create shift'));
  }
};

export const updateShift = async (id, payload) => {
  try {
    const res = await API.put(`/hr/shifts/${id}`, payload);
    return normalizeShift(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to update shift'));
  }
};

export const deleteShift = async (id) => {
  try {
    await API.delete(`/hr/shifts/${id}`);
    return true;
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to delete shift'));
  }
};

// ΓöÇΓöÇ Holidays ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ

const normalizeHoliday = (h = {}) => ({
  id: h.id,
  name: h.name ?? '',
  holidayDate: h.holidayDate ?? null,
  description: h.description ?? '',
  createdAt: h.createdAt ?? null,
  updatedAt: h.updatedAt ?? null,
});

export const listHolidays = async () => {
  try {
    const res = await API.get('/hr/holidays');
    return (unwrap(res) || []).map(normalizeHoliday);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load holidays'));
  }
};

export const getHolidaysByRange = async (startDate, endDate) => {
  try {
    const res = await API.get('/hr/holidays/range', {
      params: { startDate, endDate },
    });
    return (unwrap(res) || []).map(normalizeHoliday);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load holidays'));
  }
};

export const getHoliday = async (id) => {
  try {
    const res = await API.get(`/hr/holidays/${id}`);
    return normalizeHoliday(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load holiday'));
  }
};

export const createHoliday = async (payload) => {
  try {
    const res = await API.post('/hr/holidays', payload);
    return normalizeHoliday(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to create holiday'));
  }
};

export const updateHoliday = async (id, payload) => {
  try {
    const res = await API.put(`/hr/holidays/${id}`, payload);
    return normalizeHoliday(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to update holiday'));
  }
};

export const deleteHoliday = async (id) => {
  try {
    await API.delete(`/hr/holidays/${id}`);
    return true;
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to delete holiday'));
  }
};

// ΓöÇΓöÇ Leave Types ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ

const normalizeLeaveType = (lt = {}) => ({
  id: lt.id,
  name: lt.name ?? '',
  code: lt.code ?? '',
  description: lt.description ?? '',
  defaultDays: lt.defaultDays ?? 0,
  isPaid: lt.isPaid ?? true,
  isActive: lt.isActive ?? true,
});

export const listLeaveTypes = async () => {
  try {
    const res = await API.get('/hr/leave-types');
    return (unwrap(res) || []).map(normalizeLeaveType);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load leave types'));
  }
};

export const listActiveLeaveTypes = async () => {
  try {
    const res = await API.get('/hr/leave-types/active');
    return (unwrap(res) || []).map(normalizeLeaveType);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load leave types'));
  }
};

export const createLeaveType = async (payload) => {
  try {
    const res = await API.post('/hr/leave-types', payload);
    return normalizeLeaveType(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to create leave type'));
  }
};

export const updateLeaveType = async (id, payload) => {
  try {
    const res = await API.put(`/hr/leave-types/${id}`, payload);
    return normalizeLeaveType(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to update leave type'));
  }
};

export const deleteLeaveType = async (id) => {
  try {
    await API.delete(`/hr/leave-types/${id}`);
    return true;
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to delete leave type'));
  }
};

// ΓöÇΓöÇ Leave Requests ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ

const normalizeLeaveRequest = (lr = {}) => ({
  id: lr.id,
  employeeId: lr.employeeId,
  employeeName: lr.employeeName ?? '',
  employeeEmail: lr.employeeEmail ?? '',
  leaveTypeId: lr.leaveTypeId,
  leaveTypeName: lr.leaveTypeName ?? '',
  startDate: lr.startDate ?? null,
  endDate: lr.endDate ?? null,
  totalDays: lr.totalDays ?? 0,
  reason: lr.reason ?? '',
  status: lr.status ?? 'PENDING',
  approvedBy: lr.approvedBy ?? null,
  approvedByName: lr.approvedByName ?? '',
  approvedAt: lr.approvedAt ?? null,
  rejectionReason: lr.rejectionReason ?? '',
  cancelledAt: lr.cancelledAt ?? null,
  cancelledBy: lr.cancelledBy ?? null,
  createdAt: lr.createdAt ?? null,
});

export const applyLeave = async (payload) => {
  try {
    const res = await API.post('/hr/leave-requests', payload);
    return normalizeLeaveRequest(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to apply for leave'));
  }
};

export const approveLeave = async (id) => {
  try {
    const res = await API.post(`/hr/leave-requests/${id}/approve`);
    return normalizeLeaveRequest(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to approve leave'));
  }
};

export const rejectLeave = async (id, payload = {}) => {
  try {
    const res = await API.post(`/hr/leave-requests/${id}/reject`, payload);
    return normalizeLeaveRequest(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to reject leave'));
  }
};

export const cancelLeave = async (id) => {
  try {
    const res = await API.post(`/hr/leave-requests/${id}/cancel`);
    return normalizeLeaveRequest(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to cancel leave'));
  }
};

export const getLeaveRequest = async (id) => {
  try {
    const res = await API.get(`/hr/leave-requests/${id}`);
    return normalizeLeaveRequest(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load leave request'));
  }
};

export const listLeaveRequests = async (params = {}) => {
  try {
    const res = await API.get('/hr/leave-requests', { params });
    return (unwrap(res) || []).map(normalizeLeaveRequest);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load leave requests'));
  }
};

export const getLeaveDashboard = async () => {
  try {
    const res = await API.get('/hr/leave-requests/dashboard');
    return res?.data?.data ?? res?.data ?? {};
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load dashboard'));
  }
};

export const LeaveRequestStatuses = [
  'PENDING',
  'APPROVED',
  'REJECTED',
  'CANCELLED',
];

// ΓöÇΓöÇ Leave Balances ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ

const normalizeLeaveBalance = (lb = {}) => ({
  id: lb.id,
  employeeId: lb.employeeId,
  employeeName: lb.employeeName ?? '',
  leaveTypeId: lb.leaveTypeId,
  leaveTypeName: lb.leaveTypeName ?? '',
  year: lb.year,
  totalDays: lb.totalDays ?? 0,
  usedDays: lb.usedDays ?? 0,
  pendingDays: lb.pendingDays ?? 0,
  carriedOverDays: lb.carriedOverDays ?? 0,
  availableDays: lb.availableDays ?? 0,
});

export const getEmployeeLeaveBalances = async (employeeId, year) => {
  try {
    const params = year ? { year } : {};
    const res = await API.get(`/hr/leave-balances/employee/${employeeId}`, { params });
    return (unwrap(res) || []).map(normalizeLeaveBalance);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load leave balances'));
  }
};

export const getLeaveBalances = async (year) => {
  try {
    const params = year ? { year } : {};
    const res = await API.get('/hr/leave-balances', { params });
    return (unwrap(res) || []).map(normalizeLeaveBalance);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load leave balances'));
  }
};

export const seedLeaveBalance = async (payload) => {
  try {
    const res = await API.post('/hr/leave-balances', payload);
    return normalizeLeaveBalance(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to seed leave balance'));
  }
};

// ΓöÇΓöÇ Self-Service ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ

export const listProjects = async () => {
  try {
    const res = await getProjects();
    return res || [];
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load projects'));
  }
};

export const getMyProfile = async () => {
  try {
    const res = await API.get('/hr/self/profile');
    return normalizeEmployee(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load profile'));
  }
};

export const getMyAttendance = async (startDate, endDate) => {
  try {
    const res = await API.get('/hr/self/attendance', { params: { startDate, endDate } });
    return (unwrap(res) || []).map(normalizeAttendance);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load attendance'));
  }
};

export const getMyAttendanceSummary = async (year, month) => {
  try {
    const params = {};
    if (year) params.year = year;
    if (month) params.month = month;
    const res = await API.get('/hr/self/attendance/summary', { params });
    return res?.data?.data ?? res?.data ?? {};
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load attendance summary'));
  }
};

export const getMyLeaves = async () => {
  try {
    const res = await API.get('/hr/self/leaves');
    return (unwrap(res) || []).map(normalizeLeaveRequest);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load leaves'));
  }
};

export const getMyLeaveBalances = async (year) => {
  try {
    const params = year ? { year } : {};
    const res = await API.get('/hr/self/leaves/balance', { params });
    return (unwrap(res) || []).map(normalizeLeaveBalance);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load leave balances'));
  }
};

export const getMyDocuments = async () => {
  try {
    const res = await API.get('/hr/self/documents');
    return res?.data?.data ?? res?.data ?? {};
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load documents'));
  }
};

// ΓöÇΓöÇ Project Assignments ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ

const normalizeProjectAssignment = (pa = {}) => ({
  id: pa.id,
  employeeId: pa.employeeId,
  employeeName: pa.employeeName ?? '',
  employeeCode: pa.employeeCode ?? '',
  projectId: pa.projectId,
  projectName: pa.projectName ?? '',
  roleInProject: pa.roleInProject ?? '',
  startDate: pa.startDate ?? null,
  endDate: pa.endDate ?? null,
  allocationPercentage: pa.allocationPercentage ?? 0,
  status: pa.status ?? 'ACTIVE',
  notes: pa.notes ?? '',
  createdAt: pa.createdAt ?? null,
  updatedAt: pa.updatedAt ?? null,
});

export const listProjectAssignments = async () => {
  try {
    const res = await API.get('/hr/project-assignments');
    return (unwrap(res) || []).map(normalizeProjectAssignment);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load project assignments'));
  }
};

export const getEmployeeProjectAssignments = async (employeeId) => {
  try {
    const res = await API.get(`/hr/project-assignments/employee/${employeeId}`);
    return (unwrap(res) || []).map(normalizeProjectAssignment);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load project assignments'));
  }
};

export const getProjectAssignments = async (projectId) => {
  try {
    const res = await API.get(`/hr/project-assignments/project/${projectId}`);
    return (unwrap(res) || []).map(normalizeProjectAssignment);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load project assignments'));
  }
};

export const createProjectAssignment = async (payload) => {
  try {
    const res = await API.post('/hr/project-assignments', payload);
    return normalizeProjectAssignment(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to create project assignment'));
  }
};

export const updateProjectAssignment = async (id, payload) => {
  try {
    const res = await API.put(`/hr/project-assignments/${id}`, payload);
    return normalizeProjectAssignment(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to update project assignment'));
  }
};

export const unassignProject = async (id) => {
  try {
    const res = await API.post(`/hr/project-assignments/${id}/unassign`);
    return normalizeProjectAssignment(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to unassign from project'));
  }
};

// ΓöÇΓöÇ Utilization Reports ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ

const normalizeUtilization = (u = {}) => ({
  employeeId: u.employeeId,
  employeeName: u.employeeName ?? '',
  employeeCode: u.employeeCode ?? '',
  department: u.department ?? '',
  designation: u.designation ?? '',
  userId: u.userId ?? null,
  periodStart: u.periodStart ?? null,
  periodEnd: u.periodEnd ?? null,
  loggedHours: u.loggedHours ?? 0,
  billableHours: u.billableHours ?? 0,
  expectedHours: u.expectedHours ?? 0,
  overtimeHours: u.overtimeHours ?? 0,
  utilizationPercentage: u.utilizationPercentage ?? 0,
  productivityScore: u.productivityScore ?? 0,
  totalTasks: u.totalTasks ?? 0,
  completedTasks: u.completedTasks ?? 0,
  activeTasks: u.activeTasks ?? 0,
  workingDays: u.workingDays ?? 0,
  attendanceDays: u.attendanceDays ?? 0,
  attendanceRate: u.attendanceRate ?? 0,
  rating: u.rating ?? 'N/A',
});

const normalizeSummary = (s = {}) => ({
  periodStart: s.periodStart ?? null,
  periodEnd: s.periodEnd ?? null,
  totalEmployees: s.totalEmployees ?? 0,
  activeEmployees: s.activeEmployees ?? 0,
  employeesWithData: s.employeesWithData ?? 0,
  averageUtilization: s.averageUtilization ?? 0,
  averageAttendanceRate: s.averageAttendanceRate ?? 0,
  totalLoggedHours: s.totalLoggedHours ?? 0,
  totalOvertimeHours: s.totalOvertimeHours ?? 0,
  totalTasksAssigned: s.totalTasksAssigned ?? 0,
  totalTasksCompleted: s.totalTasksCompleted ?? 0,
  topPerformers: (s.topPerformers || []).map(normalizeUtilization),
  underUtilized: (s.underUtilized || []).map(normalizeUtilization),
  byDepartment: (s.byDepartment || []).map((d) => ({
    departmentId: d.departmentId,
    departmentName: d.departmentName ?? '',
    employeeCount: d.employeeCount ?? 0,
    averageUtilization: d.averageUtilization ?? 0,
    totalLoggedHours: d.totalLoggedHours ?? 0,
    averageAttendanceRate: d.averageAttendanceRate ?? 0,
  })),
  byProject: (s.byProject || []).map((p) => ({
    projectId: p.projectId,
    projectName: p.projectName ?? '',
    memberCount: p.memberCount ?? 0,
    totalHours: p.totalHours ?? 0,
    averageHoursPerMember: p.averageHoursPerMember ?? 0,
  })),
});

export const getUtilizationSummary = async (startDate, endDate) => {
  try {
    const res = await API.get('/hr/utilization/summary', { params: { startDate, endDate } });
    return normalizeSummary(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load utilization summary'));
  }
};

export const getEmployeeUtilization = async (startDate, endDate) => {
  try {
    const res = await API.get('/hr/utilization/employees', { params: { startDate, endDate } });
    return (unwrap(res) || []).map(normalizeUtilization);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load utilization'));
  }
};

export const getEmployeeUtilizationById = async (employeeId, startDate, endDate) => {
  try {
    const res = await API.get(`/hr/utilization/employees/${employeeId}`, { params: { startDate, endDate } });
    return normalizeUtilization(res?.data?.data ?? res?.data);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load employee utilization'));
  }
};

export const getDepartmentUtilization = async (departmentId, startDate, endDate) => {
  try {
    const res = await API.get(`/hr/utilization/departments/${departmentId}`, { params: { startDate, endDate } });
    return (unwrap(res) || []).map(normalizeUtilization);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load department utilization'));
  }
};

export const getProjectUtilization = async (projectId, startDate, endDate) => {
  try {
    const res = await API.get(`/hr/utilization/projects/${projectId}`, { params: { startDate, endDate } });
    return (unwrap(res) || []).map(normalizeUtilization);
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to load project utilization'));
  }
};

export const exportUtilizationReport = async (startDate, endDate, format = 'csv') => {
  try {
    const res = await API.get('/hr/utilization/export', {
      params: { startDate, endDate, format },
      responseType: 'blob',
    });
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `utilization-report-${startDate}-to-${endDate}.csv`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
    return true;
  } catch (e) {
    throw new Error(handleErr(e, 'Failed to export report'));
  }
};
