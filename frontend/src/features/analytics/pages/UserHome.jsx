import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../../context/useAuth.js";
import { usePermission } from "../../../context/usePermission.js";
import { getDashboardStats } from "../api/dashboardApi.js";
import { getUnreadCount } from "../../notifications/api/notificationApi.js";
import {
  FolderKanban,
  ListTodo,
  Clock,
  CheckCircle2,
  Plus,
  Bell,
  ArrowRight,
  Users,
  Calendar,
  TrendingUp,
  AlertCircle,
} from "lucide-react";
import { PieChart, Pie, Cell, ResponsiveContainer } from "recharts";
import EmptyState from "../../../components/EmptyState";

const STATUS_COLORS = {
  TODO: "#6366f1",
  IN_PROGRESS: "#f59e0b",
  REVIEW: "#8b5cf6",
  DONE: "#22c55e",
};
const PRIORITY_COLORS = { HIGH: "#ef4444", MEDIUM: "#f59e0b", LOW: "#22c55e" };

function KpiCard({ icon: Icon, label, value, to, color, alert }) {
  const navigate = useNavigate();
  return (
    <Link
      to={to}
      className="bg-white rounded-xl border border-gray-200 p-5 hover:shadow-md transition-shadow group"
    >
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm text-gray-500 mb-1">{label}</p>
          <p
            className={`text-3xl font-bold transition-colors ${alert ? "text-red-600" : "text-gray-900 group-hover:text-indigo-600"}`}
          >
            {value}
          </p>
        </div>
        <div className={`p-2.5 rounded-lg ${color}`}>
          <Icon className="w-5 h-5 text-white" />
        </div>
      </div>
    </Link>
  );
}

function StatusDonut({ data }) {
  const chartData = Object.entries(data || {}).map(([name, value]) => ({
    name,
    value,
  }));
  const total = chartData.reduce((s, d) => s + d.value, 0);

  if (total === 0) return <EmptyChart message="No tasks yet" />;

  return (
    <div className="flex items-center gap-6">
      <div className="w-32 h-32 shrink-0">
        <ResponsiveContainer width="100%" height={320}>
          <PieChart>
            <Pie
              data={chartData}
              cx="50%"
              cy="50%"
              innerRadius={28}
              outerRadius={50}
              dataKey="value"
              strokeWidth={0}
            >
              {chartData.map((entry) => (
                <Cell
                  key={entry.name}
                  fill={STATUS_COLORS[entry.name] || "#d1d5db"}
                />
              ))}
            </Pie>
          </PieChart>
        </ResponsiveContainer>
      </div>
      <div className="space-y-1.5">
        {chartData.map(({ name, value }) => (
          <div key={name} className="flex items-center gap-2 text-sm">
            <span
              className="w-2.5 h-2.5 rounded-full shrink-0"
              style={{ backgroundColor: STATUS_COLORS[name] || "#d1d5db" }}
            />
            <span className="text-gray-600 min-w-[80px]">
              {name.replace("_", " ")}
            </span>
            <span className="font-medium text-gray-900">{value}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function PriorityBar({ data }) {
  const chartData = Object.entries(data || {}).map(([name, value]) => ({
    name,
    value,
  }));
  const total = chartData.reduce((s, d) => s + d.value, 0);

  if (total === 0) return <EmptyChart message="No tasks yet" />;

  return (
    <div className="space-y-3">
      {chartData.map(({ name, value }) => {
        const pct = total > 0 ? Math.round((value / total) * 100) : 0;
        return (
          <div key={name}>
            <div className="flex justify-between text-sm mb-1">
              <span className="text-gray-600">{name}</span>
              <span className="font-medium text-gray-900">{value}</span>
            </div>
            <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
              <div
                className="h-full rounded-full transition-all duration-500"
                style={{
                  width: `${pct}%`,
                  backgroundColor: PRIORITY_COLORS[name] || "#d1d5db",
                }}
              />
            </div>
          </div>
        );
      })}
    </div>
  );
}

function EmptyChart({ message }) {
  return (
    <div className="flex items-center justify-center h-32 text-gray-400 text-sm">
      {message}
    </div>
  );
}

export default function UserHome() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { hasPermission, canCreateProject } = usePermission();
  const [stats, setStats] = useState(null);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);

  const [error, setError] = useState(null);

  useEffect(() => {
    Promise.all([
      getDashboardStats()
        .then(setStats)
        .catch((e) => setError("Failed to load dashboard data")),
      getUnreadCount()
        .then((res) => setUnreadCount(res?.data?.count ?? res?.count ?? 0))
        .catch(() => {}),
    ]).finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto space-y-6 animate-pulse">
        <div className="h-8 w-48 bg-gray-200 rounded" />
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {[1, 2, 3, 4].map((i) => (
            <div
              key={i}
              className="bg-white rounded-xl border border-gray-200 p-5"
            >
              <div className="h-4 w-20 bg-gray-200 rounded mb-3" />
              <div className="h-8 w-16 bg-gray-200 rounded" />
            </div>
          ))}
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {[1, 2].map((i) => (
            <div
              key={i}
              className="bg-white rounded-xl border border-gray-200 p-6"
            >
              <div className="h-5 w-24 bg-gray-200 rounded mb-4" />
              <div className="h-32 bg-gray-100 rounded" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto">
        <EmptyState
          icon={AlertCircle}
          title="Something went wrong"
          description={error}
          action={{ label: "Retry", onClick: () => window.location.reload() }}
        />
      </div>
    );
  }

  const isEmpty = !stats?.totalProjects && !stats?.totalTasks;

  if (isEmpty) {
    return (
      <div className="max-w-7xl mx-auto">
        <EmptyState
          icon={TrendingUp}
          title="Create or join a project to get started"
          description="Collaborate with your team by creating a new project or joining an existing one via invitation."
          action={
            canCreateProject
              ? {
                  label: "Create Project",
                  icon: Plus,
                  onClick: () => navigate("/projects"),
                }
              : undefined
          }
          secondaryAction={{
            label: "Have an invitation? Check your email",
            onClick: () => {},
          }}
        />
      </div>
    );
  }

  const dueSoonCount = stats?.dueSoonTasks || 0;

  return (
    <div className="max-w-7xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
          <p className="text-sm text-gray-500 mt-1">
            Good 
            {new Date().getHours() < 12
              ? "morning"
              : new Date().getHours() < 18
                ? "afternoon"
                : "evening"}
            , {user?.name || "User"}
          </p>
        </div>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <KpiCard
          icon={FolderKanban}
          label="Total Projects"
          value={stats?.totalProjects || 0}
          to="/projects"
          color="bg-indigo-500"
        />
        <KpiCard
          icon={ListTodo}
          label="My Tasks"
          value={stats?.myTasks || 0}
          to="/tasks"
          color="bg-blue-500"
        />
        <KpiCard
          icon={Clock}
          label="Due Soon"
          value={dueSoonCount}
          to="/tasks"
          color={dueSoonCount > 0 ? "bg-red-500" : "bg-amber-500"}
          alert={dueSoonCount > 0}
        />
        <KpiCard
          icon={CheckCircle2}
          label="Completed"
          value={stats?.completedTasks || 0}
          to="/tasks"
          color="bg-green-500"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h3 className="text-sm font-semibold text-gray-900 mb-4">
            Task Status
          </h3>
          <StatusDonut data={stats?.tasksByStatus} />
        </div>
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h3 className="text-sm font-semibold text-gray-900 mb-4">
            Task Priority
          </h3>
          <PriorityBar data={stats?.tasksByPriority} />
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-gray-900">
              Upcoming Tasks
            </h3>
            <Link
              to="/tasks"
              className="text-xs text-indigo-600 hover:text-indigo-700"
            >
              View All
            </Link>
          </div>
          {(stats?.recentTasks || []).slice(0, 5).length > 0 ? (
            <div className="space-y-2">
              {stats.recentTasks.slice(0, 5).map((task) => (
                <Link
                  key={task.id}
                  to={`/tasks`}
                  className="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-50 transition-colors group"
                >
                  <span
                    className="w-2.5 h-2.5 rounded-full shrink-0"
                    style={{
                      backgroundColor:
                        PRIORITY_COLORS[task.priority] || "#d1d5db",
                    }}
                  />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate group-hover:text-indigo-600">
                      {task.title}
                    </p>
                    <div className="flex items-center gap-2 mt-0.5">
                      <span className="text-xs px-1.5 py-0.5 bg-gray-100 text-gray-600 rounded">
                        {task.projectName}
                      </span>
                      {task.dueDate && (
                        <span className="text-xs text-gray-400">
                          {new Date(task.dueDate).toLocaleDateString()}
                        </span>
                      )}
                    </div>
                  </div>
                  <span
                    className={`text-xs px-2 py-0.5 rounded-full ${
                      task.status === "TODO"
                        ? "bg-indigo-50 text-indigo-700"
                        : task.status === "IN_PROGRESS"
                          ? "bg-amber-50 text-amber-700"
                          : task.status === "REVIEW"
                            ? "bg-purple-50 text-purple-700"
                            : "bg-green-50 text-green-700"
                    }`}
                  >
                    {task.status.replace("_", " ")}
                  </span>
                </Link>
              ))}
            </div>
          ) : (
            <div className="text-center py-8 text-gray-400 text-sm">
              No tasks assigned
            </div>
          )}
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-gray-900">
              Recent Projects
            </h3>
            <Link
              to="/projects"
              className="text-xs text-indigo-600 hover:text-indigo-700"
            >
              View All
            </Link>
          </div>
          {(stats?.recentProjects || []).length > 0 ? (
            <div className="space-y-2">
              {stats.recentProjects.map((project) => (
                <Link
                  key={project.id}
                  to={`/projects/${project.id}`}
                  className="flex items-center gap-3 p-3 rounded-lg hover:bg-gray-50 transition-colors group"
                >
                  <div className="w-9 h-9 bg-indigo-50 rounded-lg flex items-center justify-center shrink-0">
                    <FolderKanban className="w-4 h-4 text-indigo-600" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">
                      {project.name}
                    </p>
                    <p className="text-xs text-gray-400">
                      {project.memberCount} member
                      {project.memberCount !== 1 ? "s" : ""}
                      {project.status && ` ┬╖ ${project.status}`}
                    </p>
                  </div>
                  <ArrowRight className="w-4 h-4 text-gray-300 group-hover:text-indigo-500" />
                </Link>
              ))}
            </div>
          ) : (
            <div className="text-center py-8 text-gray-400 text-sm">
              No projects yet
            </div>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center gap-2 mb-3">
            <Bell className="w-4 h-4 text-indigo-600" />
            <h3 className="text-sm font-semibold text-gray-900">
              Notifications
            </h3>
          </div>
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-2xl font-bold text-gray-900">
                {unreadCount}
              </span>
              <span className="text-sm text-gray-500">unread</span>
            </div>
            <Link
              to="/notifications"
              className="text-sm text-indigo-600 hover:text-indigo-700 font-medium"
            >
              View All
            </Link>
          </div>
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center gap-2 mb-3">
            <Calendar className="w-4 h-4 text-indigo-600" />
            <h3 className="text-sm font-semibold text-gray-900">
              Quick Actions
            </h3>
          </div>
          <div className="space-y-2">
            <button
              onClick={() => navigate("/tasks")}
              className="w-full text-left px-3 py-2 bg-indigo-50 text-indigo-700 rounded-lg hover:bg-indigo-100 transition-colors text-sm flex items-center gap-2"
            >
              <Plus className="w-4 h-4" />
              New Task
            </button>
            {canCreateProject && (
              <button
                onClick={() => navigate("/projects")}
                className="w-full text-left px-3 py-2 bg-green-50 text-green-700 rounded-lg hover:bg-green-100 transition-colors text-sm flex items-center gap-2"
              >
                <FolderKanban className="w-4 h-4" />
                New Project
              </button>
            )}
          </div>
        </div>

        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <div className="flex items-center gap-2 mb-3">
            <Users className="w-4 h-4 text-indigo-600" />
            <h3 className="text-sm font-semibold text-gray-900">Team</h3>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-2xl font-bold text-gray-900">
              {stats?.roleCounts?.["USER"] || 0}
            </span>
            <span className="text-sm text-gray-500">team members</span>
          </div>
        </div>
      </div>
    </div>
  );
}
