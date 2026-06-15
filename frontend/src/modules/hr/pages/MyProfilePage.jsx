import { useEffect, useState } from 'react';
import { useAuth } from '../../../context/useAuth.js';
import {
  User,
  Mail,
  Phone,
  Briefcase,
  Building2,
  Calendar,
  Loader2,
  MapPin,
} from 'lucide-react';
import { toast } from 'sonner';
import { getMyProfile } from '../api/hrApi';

const STATUS_BADGE = {
  ACTIVE: 'bg-emerald-100 text-emerald-700',
  ON_LEAVE: 'bg-amber-100 text-amber-700',
  SUSPENDED: 'bg-rose-100 text-rose-700',
  TERMINATED: 'bg-slate-200 text-slate-700',
  PROBATION: 'bg-indigo-100 text-indigo-700',
};

export default function MyProfilePage() {
  const { user } = useAuth();
  const [employee, setEmployee] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    setLoading(true);
    try {
      const data = await getMyProfile();
      setEmployee(data);
    } catch (e) {
      toast.error(e.message || 'Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="w-8 h-8 animate-spin text-indigo-600" />
      </div>
    );
  }

  if (!employee) {
    return (
      <div className="text-center py-20 bg-white rounded-2xl border border-slate-200">
        <User className="w-16 h-16 text-slate-300 mx-auto mb-4" />
        <h3 className="text-lg font-semibold text-slate-600 mb-2">No employee profile found</h3>
        <p className="text-slate-500 text-sm">Please contact HR to set up your employee profile.</p>
      </div>
    );
  }

  return (
    <div>
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
          <User className="w-7 h-7 text-indigo-600" /> My Profile
        </h1>
        <p className="text-slate-500 mt-1">View and manage your employee information</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1">
          <div className="bg-white rounded-2xl border border-slate-200 p-6 text-center">
            <div className="w-24 h-24 rounded-full bg-gradient-to-br from-indigo-500 to-fuchsia-500 mx-auto mb-4 flex items-center justify-center text-3xl font-bold text-white">
              {user?.avatarUrl ? (
                <img src={user.avatarUrl} alt="Profile" className="w-full h-full rounded-full object-cover" />
              ) : (
                employee.firstName?.charAt(0) || 'E'
              )}
            </div>
            <h2 className="text-xl font-bold text-slate-900">{employee.fullName}</h2>
            <p className="text-slate-500 text-sm mt-1">{employee.jobTitle || 'Employee'}</p>
            <span className={`inline-block mt-3 px-3 py-1 rounded-full text-xs font-medium ${STATUS_BADGE[employee.status] || 'bg-slate-100 text-slate-600'}`}>
              {employee.status?.replace('_', ' ')}
            </span>
          </div>
        </div>

        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white rounded-2xl border border-slate-200 p-6">
            <h3 className="text-lg font-semibold text-slate-900 mb-4">Personal Information</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-50">
                <User className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="text-xs text-slate-500">Employee Code</p>
                  <p className="text-sm font-medium text-slate-900">{employee.employeeCode}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-50">
                <Mail className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="text-xs text-slate-500">Work Email</p>
                  <p className="text-sm font-medium text-slate-900">{employee.workEmail || '—'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-50">
                <Mail className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="text-xs text-slate-500">Personal Email</p>
                  <p className="text-sm font-medium text-slate-900">{employee.personalEmail || '—'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-50">
                <Phone className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="text-xs text-slate-500">Phone</p>
                  <p className="text-sm font-medium text-slate-900">{employee.phone || '—'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-50">
                <Calendar className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="text-xs text-slate-500">Date of Birth</p>
                  <p className="text-sm font-medium text-slate-900">{employee.dateOfBirth || '—'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-50">
                <MapPin className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="text-xs text-slate-500">Timezone</p>
                  <p className="text-sm font-medium text-slate-900">{employee.timezone || '—'}</p>
                </div>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 p-6">
            <h3 className="text-lg font-semibold text-slate-900 mb-4">Employment Details</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-50">
                <Briefcase className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="text-xs text-slate-500">Employment Type</p>
                  <p className="text-sm font-medium text-slate-900">{employee.employmentType?.replace('_', ' ')}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-50">
                <Building2 className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="text-xs text-slate-500">Department ID</p>
                  <p className="text-sm font-medium text-slate-900">{employee.departmentId || '—'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-50">
                <Briefcase className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="text-xs text-slate-500">Designation ID</p>
                  <p className="text-sm font-medium text-slate-900">{employee.designationId || '—'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-50">
                <Calendar className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="text-xs text-slate-500">Hire Date</p>
                  <p className="text-sm font-medium text-slate-900">{employee.hireDate || '—'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-50">
                <User className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="text-xs text-slate-500">Manager ID</p>
                  <p className="text-sm font-medium text-slate-900">{employee.managerId || '—'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-50">
                <Calendar className="w-5 h-5 text-slate-400" />
                <div>
                  <p className="text-xs text-slate-500">Termination Date</p>
                  <p className="text-sm font-medium text-slate-900">{employee.terminationDate || '—'}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
