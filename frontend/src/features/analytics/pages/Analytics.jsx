import { useEffect, useState, useMemo } from "react";
import {
  TrendingUp,
  TrendingDown,
  Minus,
  BarChart3,
  Target,
  AlertTriangle,
  Calendar,
  Clock,
  Download,
} from "lucide-react";
import { toast, Toaster } from "sonner";
import { getAnalyticsDashboard, getTimeAnalytics } from "../api/analyticsApi";
import { getUserProjects } from "../../projects/api/projectMembersApi";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts";
import ExportDropdown from "../../analytics/components/ExportDropdown";
const KPICard = ({ title, value, subtitle, icon: Icon, colorClass, trend }) => (
  <div className="bg-white dark:bg-surface-900 p-4 md:p-5 rounded-xl shadow-soft border border-surface-200/50 dark:border-surface-800/50">
     
    <div className="flex items-center justify-between mb-3">
       
      <p className="text-xs md:text-sm font-medium text-surface-600 dark:text-surface-400">
        {title}
      </p> 
      <div className={`p-2 rounded-lg ${colorClass}`}>
         
        <Icon size={18} className="text-white" /> 
      </div> 
    </div> 
    <p className="text-xl md:text-2xl font-bold text-surface-900 dark:text-surface-100">
      {value}
    </p> 
    {subtitle && (
      <p className="text-xs text-surface-500 dark:text-surface-400 mt-1">
        {subtitle}
      </p>
    )} 
    {trend && (
      <div className="flex items-center gap-1 mt-2">
         
        {trend === "INCREASING" && (
          <TrendingUp
            size={14}
            className="text-success-500 dark:text-success-400"
          />
        )} 
        {trend === "DECREASING" && (
          <TrendingDown
            size={14}
            className="text-danger-500 dark:text-danger-400"
          />
        )} 
        {trend === "STABLE" && (
          <Minus size={14} className="text-surface-500 dark:text-surface-400" />
        )} 
        <span
          className={`text-xs font-medium ${trend === "INCREASING" ? "text-success-600 dark:text-success-400" : trend === "DECREASING" ? "text-danger-600 dark:text-danger-400" : "text-surface-600 dark:text-surface-400"}`}
        >
          {trend}
        </span> 
      </div>
    )} 
  </div>
);
const TrendIndicator = ({ trend, changePercentage }) => {
  if (!trend || trend === "STABLE") {
    return (
      <span className="inline-flex items-center gap-1 text-surface-600 dark:text-surface-400">
        <Minus size={16} /> Stable
      </span>
    );
  }
  if (trend === "INCREASING") {
    return (
      <span className="inline-flex items-center gap-1 text-success-600 dark:text-success-400">
        <TrendingUp size={16} /> +{changePercentage}%
      </span>
    );
  }
  return (
    <span className="inline-flex items-center gap-1 text-danger-600 dark:text-danger-400">
      <TrendingDown size={16} /> {changePercentage}%
    </span>
  );
};
const BurndownInsightPanel = ({ insight }) => {
  if (!insight) return null;
  const statusConfig = {
    AHEAD_OF_SCHEDULE: {
      color: "success",
      text: "Ahead of Schedule",
      description: "Team is performing better than expected",
    },
    ON_TRACK: {
      color: "primary",
      text: "On Track",
      description: "Team is performing as expected",
    },
    BEHIND_SCHEDULE: {
      color: "danger",
      text: "Behind Schedule",
      description: "Team is falling behind the ideal pace",
    },
  };
  const config = statusConfig[insight.status] || statusConfig["ON_TRACK"];
  const bgColors = {
    success: "bg-success-50 dark:bg-success-900/10",
    danger: "bg-danger-50 dark:bg-danger-900/10",
    primary: "bg-primary-50 dark:bg-primary-900/10",
  };
  const borderColors = {
    success: "border-success-200 dark:border-success-800/50",
    danger: "border-danger-200 dark:border-danger-800/50",
    primary: "border-primary-200 dark:border-primary-800/50",
  };
  const textColors = {
    success: "text-success-700 dark:text-success-300",
    danger: "text-danger-700 dark:text-danger-300",
    primary: "text-primary-700 dark:text-primary-300",
  };
  const badgeColors = {
    success:
      "bg-success-200 dark:bg-success-800 text-success-800 dark:text-success-200",
    danger:
      "bg-danger-200 dark:bg-danger-800 text-danger-800 dark:text-danger-200",
    primary:
      "bg-primary-200 dark:bg-primary-800 text-primary-800 dark:text-primary-200",
  };
  return (
    <div
      className={`p-3 md:p-4 rounded-xl border ${bgColors[config.color]} ${borderColors[config.color]}`}
    >
       
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 mb-2">
         
        <h4
          className={`font-medium text-sm md:text-base ${textColors[config.color]}`}
        >
          {config.text}
        </h4> 
        <span
          className={`text-xs px-2 py-1 rounded-full shrink-0 ${badgeColors[config.color]}`}
        >
           
          {insight.behindScheduleCount}/{insight.totalDataPoints} days
          behind 
        </span> 
      </div> 
      <p className="text-xs text-surface-600 dark:text-surface-400">
        {config.description}
      </p> 
      {insight.averageDeviationFromIdeal !== 0 && (
        <p className="text-xs text-surface-500 dark:text-surface-400 mt-1">
           
          Avg deviation: {insight.averageDeviationFromIdeal} tasks 
        </p>
      )} 
    </div>
  );
};
const ForecastCard = ({ forecast }) => {
  if (!forecast) return null;
  return (
    <div className="bg-white dark:bg-surface-900 p-4 md:p-5 rounded-xl shadow-soft border border-surface-200/50 dark:border-surface-800/50">
       
      <h4 className="text-xs md:text-sm font-medium text-surface-600 dark:text-surface-400 mb-3 flex items-center gap-2">
         
        <Calendar size={16} /> Sprint Forecast 
      </h4> 
      <div className="space-y-3">
         
        <div>
           
          <p className="text-xs text-surface-500 dark:text-surface-400">
            Remaining Tasks
          </p> 
          <p className="text-lg md:text-xl font-bold text-surface-900 dark:text-surface-100">
            {forecast.remainingTasks || 0}
          </p> 
        </div> 
        <div>
           
          <p className="text-xs text-surface-500 dark:text-surface-400">
            Estimated Sprints Needed
          </p> 
          <div className="flex items-center gap-2">
             
            <p className="text-xl md:text-2xl font-bold text-primary-600 dark:text-primary-400">
              {forecast.estimatedSprints || 0}
            </p> 
            <span className="text-xs text-surface-500 dark:text-surface-400">
              sprints
            </span> 
          </div> 
        </div> 
        <div className="pt-3 border-t border-surface-200 dark:border-surface-700">
           
          <p className="text-xs text-surface-500 dark:text-surface-400">
            Based on avg velocity of
          </p> 
          <p className="text-sm font-medium text-surface-700 dark:text-surface-300">
            {forecast.averageVelocity || 0} tasks/sprint
          </p> 
        </div> 
      </div> 
    </div>
  );
};
const Analytics = () => {
  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState("");
  const [dashboardData, setDashboardData] = useState(null);
  const [timeAnalytics, setTimeAnalytics] = useState(null);
  const [loading, setLoading] = useState(false);
  useEffect(() => {
    const loadProjects = async () => {
      try {
        const data = await getUserProjects();
        setProjects(data || []);
        if (data && data.length > 0 && !selectedProjectId) {
          setSelectedProjectId(String(data[0].id));
        }
      } catch (error) {
        toast.error("Failed to load projects");
      }
    };
    loadProjects();
  }, []);
  useEffect(() => {
    if (!selectedProjectId) return;
    loadDashboard();
    loadTimeAnalytics();
  }, [selectedProjectId]);
  const loadDashboard = async () => {
    setLoading(true);
    try {
      const data = await getAnalyticsDashboard(selectedProjectId);
      setDashboardData(data);
    } catch (error) {
      toast.error("Failed to load analytics data");
      setDashboardData(null);
    } finally {
      setLoading(false);
    }
  };
  const loadTimeAnalytics = async () => {
    try {
      const data = await getTimeAnalytics(selectedProjectId);
      setTimeAnalytics(data);
    } catch (error) {
      console.error("Failed to load time analytics");
    }
  };
  const velocityChartData = useMemo(() => {
    if (!dashboardData?.velocity?.history) return [];
    return dashboardData.velocity.history.map((item, index) => ({
      name: item.sprintName || `Sprint ${index + 1}`,
      tasks: item.completedTasks || 0,
      sprintId: item.sprintId,
    }));
  }, [dashboardData]);
  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white dark:bg-surface-800 p-3 border border-surface-200 dark:border-surface-700 rounded shadow-md">
           
          <p className="text-sm font-medium text-surface-900 dark:text-surface-100">
            {label}
          </p> 
          <p className="text-xs text-primary-600 dark:text-primary-400">
            Completed: {payload[0]?.value || 0} tasks
          </p> 
        </div>
      );
    }
    return null;
  };
  if (loading) {
    return (
      <>
         
        <Toaster position="top-right" /> 
        <div className="page-container max-w-8xl mx-auto">
           
          <div className="flex flex-col items-center justify-center py-12">
             
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div> 
            <p className="mt-2 text-surface-600 dark:text-surface-400">
              Loading analytics...
            </p> 
          </div> 
        </div> 
      </>
    );
  }
  return (
    <>
       
      <Toaster position="top-right" /> 
      <div className="page-container max-w-8xl mx-auto">
         
        <div className="page-header">
           
          <h1 className="page-title flex items-center gap-2">
             
            <BarChart3 size={28} /> Analytics Dashboard 
          </h1> 
          <div className="flex flex-col sm:flex-row gap-3">
             
            <select
              value={selectedProjectId}
              onChange={(e) => setSelectedProjectId(e.target.value)}
              className="input-field sm:w-48"
            >
               
              <option value="">Select Project</option> 
              {projects.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))} 
            </select> 
            {selectedProjectId && (
              <ExportDropdown
                projectId={selectedProjectId}
                showAnalytics={true}
              />
            )} 
          </div> 
        </div> 
        {!dashboardData && (
          <div className="text-center py-12 bg-white dark:bg-surface-900 rounded-xl shadow-soft border border-surface-200/50 dark:border-surface-800/50">
             
            <BarChart3
              size={48}
              className="mx-auto text-surface-300 dark:text-surface-600 mb-4"
            /> 
            <p className="text-surface-500 dark:text-surface-400">
              Select a project to view analytics
            </p> 
            {projects.length === 0 && (
              <p className="text-sm text-surface-400 dark:text-surface-500 mt-2">
                No projects found. Create a project first.
              </p>
            )} 
          </div>
        )} 
        {dashboardData && (
          <div className="space-y-6">
             
            <div className="grid-responsive-4">
               
              <KPICard
                title="Avg Velocity"
                value={dashboardData.velocity?.average?.toFixed(1) || "0.0"}
                subtitle="tasks per sprint"
                icon={TrendingUp}
                colorClass="bg-primary-500"
                trend={dashboardData.velocity?.trend}
              /> 
              <KPICard
                title="Completion Rate"
                value={`${dashboardData.completionRate || 0}%`}
                subtitle="last completed sprint"
                icon={Target}
                colorClass={
                  dashboardData.completionRate >= 80
                    ? "bg-success-500"
                    : dashboardData.completionRate >= 50
                      ? "bg-warning-500"
                      : "bg-danger-500"
                }
              /> 
              <KPICard
                title="Blocked Work Ratio"
                value={`${dashboardData.blockedWorkRatio || 0}%`}
                subtitle="blocked tasks"
                icon={AlertTriangle}
                colorClass={
                  dashboardData.blockedWorkRatio <= 10
                    ? "bg-success-500"
                    : dashboardData.blockedWorkRatio <= 25
                      ? "bg-warning-500"
                      : "bg-danger-500"
                }
              /> 
              <KPICard
                title="Velocity Trend"
                value={
                  <TrendIndicator
                    trend={dashboardData.velocity?.trend}
                    changePercentage={dashboardData.velocity?.changePercentage}
                  />
                }
                icon={
                  dashboardData.velocity?.trend === "INCREASING"
                    ? TrendingUp
                    : dashboardData.velocity?.trend === "DECREASING"
                      ? TrendingDown
                      : Minus
                }
                colorClass={
                  dashboardData.velocity?.trend === "INCREASING"
                    ? "bg-success-500"
                    : dashboardData.velocity?.trend === "DECREASING"
                      ? "bg-danger-500"
                      : "bg-surface-500"
                }
              /> 
              <KPICard
                title="Total Time Logged"
                value={`${timeAnalytics?.totalHours?.toFixed(1) || "0.0"}h`}
                subtitle={`${timeAnalytics?.tasksWithTimeCount || 0} tasks`}
                icon={Clock}
                colorClass="bg-purple-500"
              /> 
              <KPICard
                title="Avg Time per Task"
                value={`${timeAnalytics?.averageHoursPerTask?.toFixed(1) || "0.0"}h`}
                subtitle={`Recent: ${timeAnalytics?.recentHours?.toFixed(1) || "0.0"}h`}
                icon={Clock}
                colorClass="bg-indigo-500"
                trend={
                  timeAnalytics?.changePercentage > 0
                    ? "INCREASING"
                    : timeAnalytics?.changePercentage < 0
                      ? "DECREASING"
                      : "STABLE"
                }
              /> 
            </div> 
            {dashboardData.burndownInsight && (
              <BurndownInsightPanel insight={dashboardData.burndownInsight} />
            )} 
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 md:gap-6">
               
              <div className="lg:col-span-2 bg-white dark:bg-surface-900 p-4 md:p-5 rounded-xl shadow-soft border border-surface-200/50 dark:border-surface-800/50">
                 
                <h3 className="text-base md:text-lg font-semibold text-surface-800 dark:text-surface-200 mb-4">
                  Velocity History
                </h3> 
                {velocityChartData.length === 0 ? (
                  <div className="text-center py-8">
                     
                    <p className="text-surface-500 dark:text-surface-400">
                      No completed sprints yet
                    </p> 
                    <p className="text-sm text-surface-400 dark:text-surface-500 mt-1">
                      Complete a sprint to see velocity data
                    </p> 
                  </div>
                ) : (
                  <ResponsiveContainer width="100%" height={250}>
                     
                    <LineChart data={velocityChartData}>
                       
                      <CartesianGrid
                        strokeDasharray="3 3"
                        stroke="currentColor"
                        className="text-surface-200 dark:text-surface-700"
                      /> 
                      <XAxis
                        dataKey="name"
                        tick={{ fontSize: 11 }}
                        stroke="currentColor"
                        className="text-surface-500 dark:text-surface-400"
                      /> 
                      <YAxis
                        tick={{ fontSize: 11 }}
                        allowDecimals={false}
                        stroke="currentColor"
                        className="text-surface-500 dark:text-surface-400"
                      /> 
                      <Tooltip content={<CustomTooltip />} /> <Legend /> 
                      <Line
                        type="monotone"
                        dataKey="tasks"
                        stroke="#3b82f6"
                        strokeWidth={2}
                        name="Completed Tasks"
                        dot={{ r: 4 }}
                        activeDot={{ r: 6 }}
                      /> 
                    </LineChart> 
                  </ResponsiveContainer>
                )} 
                {dashboardData.velocity?.history &&
                  dashboardData.velocity.history.length > 0 && (
                    <div className="mt-3 pt-3 border-t border-surface-200 dark:border-surface-700">
                       
                      <TrendIndicator
                        trend={dashboardData.velocity.trend}
                        changePercentage={
                          dashboardData.velocity.changePercentage
                        }
                      /> 
                    </div>
                  )} 
              </div> 
              <ForecastCard forecast={dashboardData.forecast} /> 
            </div> 
            {dashboardData.velocity?.history &&
              dashboardData.velocity.history.length > 0 && (
                <div className="bg-white dark:bg-surface-900 p-4 md:p-5 rounded-xl shadow-soft border border-surface-200/50 dark:border-surface-800/50">
                   
                  <h3 className="text-base md:text-lg font-semibold text-surface-800 dark:text-surface-200 mb-4">
                    Sprint Velocity Details
                  </h3> 
                  <div className="table-container">
                     
                    <table className="table">
                       
                      <thead>
                         
                        <tr>
                           
                          <th className="text-left">Sprint</th> 
                          <th className="text-right">Completed Tasks</th> 
                          <th className="text-right">Completed Date</th> 
                        </tr> 
                      </thead> 
                      <tbody>
                         
                        {dashboardData.velocity.history.map((sprint, index) => (
                          <tr key={sprint.sprintId}>
                             
                            <td className="text-surface-900 dark:text-surface-100">
                              {sprint.sprintName}
                            </td> 
                            <td className="text-right font-medium text-surface-900 dark:text-surface-100">
                              {sprint.completedTasks}
                            </td> 
                            <td className="text-right text-surface-500 dark:text-surface-400">
                              {sprint.completedAt || "N/A"}
                            </td> 
                          </tr>
                        ))} 
                      </tbody> 
                    </table> 
                  </div> 
                </div>
              )} 
          </div>
        )} 
      </div> 
    </>
  );
};
export default Analytics;
