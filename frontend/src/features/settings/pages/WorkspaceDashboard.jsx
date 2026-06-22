import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  FolderKanban,
  Users,
  CheckCircle2,
  BarChart3,
  Loader2,
  ArrowRight,
  Calendar,
  TreePalm,
  Bell,
} from "lucide-react";
import { useWorkspace } from "../../../context/WorkspaceContext";
import { getProjects } from "../../projects/api/projectApi";
import { getWorkspaceMembers } from "../api/workspaceApi";
import { getMyAttendanceSummary, getMyLeaveBalances } from "../../hr/api/hrApi";
import { EmptyWorkspaceState } from "../../../components/EmptyStates";
const KPI_COLORS = {
  blue: "from-blue-500 to-blue-600",
  emerald: "from-emerald-500 to-emerald-600",
  purple: "from-purple-500 to-purple-600",
  amber: "from-amber-500 to-amber-600",
};
function KPICard({ icon: Icon, label, value, color = "blue", onClick }) {
  return (
    <button
      onClick={onClick}
      className="group rounded-2xl border border-slate-200 bg-white p-6 shadow-lg text-left hover:shadow-xl transition-all active:scale-[0.98]"
    >
       
      <div
        className={`flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br ${KPI_COLORS[color]} mb-4 shadow-sm`}
      >
         
        <Icon className="h-6 w-6 text-white" /> 
      </div> 
      <p className="text-sm font-medium text-slate-500 mb-1">{label}</p> 
      <p className="text-3xl font-bold text-slate-900">{value}</p> 
    </button>
  );
}
export default function WorkspaceDashboard() {
  const { workspace } = useWorkspace();
  const navigate = useNavigate();
  const [projects, setProjects] = useState([]);
  const [members, setMembers] = useState([]);
  const [attendanceSummary, setAttendanceSummary] = useState(null);
  const [leaveBalances, setLeaveBalances] = useState([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    if (!workspace) return;
    loadData();
  }, [workspace?.id]);
  const loadData = async () => {
    try {
      setLoading(true);
      const [projRes, memRes, attRes, leaveRes] = await Promise.allSettled([
        getProjects(),
        getWorkspaceMembers(workspace.id),
        getMyAttendanceSummary(),
        getMyLeaveBalances(),
      ]);
      if (projRes.status === "fulfilled") setProjects(projRes.value || []);
      if (memRes.status === "fulfilled")
        setMembers(memRes.value?.data?.data || []);
      if (attRes.status === "fulfilled") setAttendanceSummary(attRes.value || null);
      if (leaveRes.status === "fulfilled") setLeaveBalances(leaveRes.value || []);
    } catch {
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
  if (projects.length === 0) {
    return (
      <EmptyWorkspaceState
        onCreateProject={() => navigate(`/w/${workspace?.slug}/projects`)}
        onInvite={() => navigate(`/w/${workspace?.slug}/members`)}
      />
    );
  }
  return (
    <div>
       
      <div className="mb-8">
         
        <h1 className="text-3xl font-bold text-slate-900">
          {workspace?.name}
        </h1> 
        <p className="text-slate-500 mt-1">
          Workspace overview and key metrics
        </p> 
      </div> 
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
         
        <KPICard
          icon={FolderKanban}
          label="Projects"
          value={projects.length}
          color="blue"
          onClick={() => navigate(`/w/${workspace?.slug}/projects`)}
        /> 
        <KPICard
          icon={CheckCircle2}
          label="Active Tasks"
          value="ΓÇö"
          color="emerald"
        /> 
        <KPICard
          icon={Users}
          label="Members"
          value={members.length}
          color="purple"
          onClick={() => navigate(`/w/${workspace?.slug}/members`)}
        /> 
        <KPICard
          icon={BarChart3}
          label="Analytics"
          value="View"
          color="amber"
          onClick={() => navigate(`/w/${workspace?.slug}/analytics`)}
        /> 
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
        <button
          onClick={() => navigate(`/w/${workspace?.slug}/my-attendance`)}
          className="group rounded-2xl border border-slate-200 bg-white p-6 shadow-lg text-left hover:shadow-xl transition-all active:scale-[0.98]"
        >
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-emerald-500 to-emerald-600 mb-4 shadow-sm">
            <Calendar className="h-6 w-6 text-white" />
          </div>
          <p className="text-sm font-medium text-slate-500 mb-1">My Attendance</p>
          <p className="text-3xl font-bold text-slate-900">
            {attendanceSummary?.presentCount || 0}
          </p>
          <p className="text-xs text-slate-400 mt-1">days present this month</p>
        </button>

        <button
          onClick={() => navigate(`/w/${workspace?.slug}/my-leaves`)}
          className="group rounded-2xl border border-slate-200 bg-white p-6 shadow-lg text-left hover:shadow-xl transition-all active:scale-[0.98]"
        >
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-amber-500 to-amber-600 mb-4 shadow-sm">
            <TreePalm className="h-6 w-6 text-white" />
          </div>
          <p className="text-sm font-medium text-slate-500 mb-1">Leave Balance</p>
          <p className="text-3xl font-bold text-slate-900">
            {leaveBalances.length > 0 ? leaveBalances.reduce((sum, b) => sum + (b.availableDays || 0), 0) : 0}
          </p>
          <p className="text-xs text-slate-400 mt-1">days available</p>
        </button>

        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-lg">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-blue-500 to-blue-600 mb-4 shadow-sm">
            <Bell className="h-6 w-6 text-white" />
          </div>
          <p className="text-sm font-medium text-slate-500 mb-1">Announcements</p>
          <p className="text-lg font-bold text-slate-900">No new announcements</p>
          <p className="text-xs text-slate-400 mt-1">Check back later</p>
        </div>
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
         
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-lg">
           
          <h3 className="text-lg font-bold text-slate-900 mb-4">
            Recent Projects
          </h3> 
          {projects.length === 0 ? (
            <p className="text-sm text-slate-400">No projects yet</p>
          ) : (
            <div className="space-y-3">
               
              {projects.slice(0, 5).map((p) => (
                <button
                  key={p.id}
                  onClick={() =>
                    navigate(`/w/${workspace?.slug}/projects/${p.id}`)
                  }
                  className="flex w-full items-center gap-3 rounded-xl p-3 text-left hover:bg-slate-50 transition group"
                >
                   
                  <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-cyan-400 to-indigo-500 text-white">
                     
                    <FolderKanban className="h-5 w-5" /> 
                  </div> 
                  <div className="min-w-0 flex-1">
                     
                    <p className="font-medium text-slate-800 truncate">
                      {p.name}
                    </p> 
                    <p className="text-xs text-slate-400">
                       
                      {p.taskCount || 0} tasks 
                    </p> 
                  </div> 
                  <ArrowRight className="h-4 w-4 text-slate-300 group-hover:text-indigo-500 transition" /> 
                </button>
              ))} 
            </div>
          )} 
          <button
            onClick={() => navigate(`/w/${workspace?.slug}/projects`)}
            className="mt-4 text-sm font-medium text-indigo-600 hover:text-indigo-700"
          >
             
            View all projects ΓåÆ 
          </button> 
        </div> 
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-lg">
           
          <h3 className="text-lg font-bold text-slate-900 mb-4">
            Team Members
          </h3> 
          {members.length === 0 ? (
            <p className="text-sm text-slate-400">No members yet</p>
          ) : (
            <div className="space-y-3">
               
              {members.slice(0, 5).map((m) => (
                <div
                  key={m.id}
                  className="flex items-center gap-3 rounded-xl p-2"
                >
                   
                  <div className="flex h-9 w-9 items-center justify-center overflow-hidden rounded-xl bg-gradient-to-br from-indigo-500 to-fuchsia-500 text-sm font-bold text-white">
                     
                    {m.userName?.charAt(0).toUpperCase() || "U"} 
                  </div> 
                  <div className="min-w-0 flex-1">
                     
                    <p className="text-sm font-medium text-slate-800 truncate">
                      {m.userName}
                    </p> 
                    <p className="text-xs text-slate-400 truncate">
                      {m.userEmail}
                    </p> 
                  </div> 
                  <span className="shrink-0 rounded-full bg-slate-100 px-2.5 py-0.5 text-xs font-medium text-slate-500">
                     
                    {m.workspaceRole?.replace("WORKSPACE_", "")} 
                  </span> 
                </div>
              ))} 
            </div>
          )} 
          <button
            onClick={() => navigate(`/w/${workspace?.slug}/members`)}
            className="mt-4 text-sm font-medium text-indigo-600 hover:text-indigo-700"
          >
             
            View all members ΓåÆ 
          </button> 
        </div> 
      </div> 
    </div>
  );
}

