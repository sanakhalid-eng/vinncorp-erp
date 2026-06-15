import { useState, useEffect, useCallback } from "react";
import {
  Activity,
  Users,
  Webhook,
  AlertTriangle,
  Bell,
  Clock,
  Wifi,
  RefreshCw,
  Shield,
  Mail,
  Server,
} from "lucide-react";
import { getSystemHealth } from "../api/systemApi";
import { toast } from "sonner";
function KpiCard({ icon: Icon, label, value, color, alert }) {
  return (
    <div className="bg-white dark:bg-surface-900 rounded-xl border border-surface-200/50 dark:border-surface-800/50 p-4 md:p-5 hover:shadow-soft transition-shadow">
       
      <div className="flex items-start justify-between">
         
        <div>
           
          <p className="text-xs md:text-sm text-surface-500 dark:text-surface-400 mb-1">
            {label}
          </p> 
          <p
            className={`text-2xl md:text-3xl font-bold transition-colors ${alert ? "text-danger-600 dark:text-danger-400" : "text-surface-900 dark:text-surface-100"}`}
          >
             
            {value ?? "-"} 
          </p> 
        </div> 
        <div className={`p-2 md:p-2.5 rounded-lg ${color}`}>
           
          <Icon className="w-4 h-4 md:w-5 md:h-5 text-white" /> 
        </div> 
      </div> 
    </div>
  );
}
function ServiceCard({ name, icon: Icon, status, detail }) {
  const statusConfig = {
    operational: {
      dot: "bg-success-500",
      label: "Operational",
      bg: "bg-success-50 dark:bg-success-900/20 text-success-700 dark:text-success-400",
    },
    degraded: {
      dot: "bg-warning-500",
      label: "Degraded",
      bg: "bg-warning-50 dark:bg-warning-900/20 text-warning-700 dark:text-warning-400",
    },
    down: {
      dot: "bg-danger-500",
      label: "Down",
      bg: "bg-danger-50 dark:bg-danger-900/20 text-danger-700 dark:text-danger-400",
    },
  };
  const cfg = statusConfig[status] || statusConfig.operational;
  return (
    <div className="bg-white dark:bg-surface-900 rounded-xl border border-surface-200/50 dark:border-surface-800/50 p-4 md:p-5 hover:shadow-soft transition-shadow">
       
      <div className="flex items-center gap-3 md:gap-4">
         
        <div
          className={`p-2 md:p-2.5 rounded-lg ${status === "degraded" ? "bg-warning-500" : status === "down" ? "bg-danger-500" : "bg-success-500"}`}
        >
           
          <Icon className="w-4 h-4 md:w-5 md:h-5 text-white" /> 
        </div> 
        <div className="flex-1 min-w-0">
           
          <p className="text-sm font-semibold text-surface-900 dark:text-surface-100">
            {name}
          </p> 
          <p className="text-xs text-surface-500 dark:text-surface-400 mt-0.5 truncate">
            {detail}
          </p> 
        </div> 
        <span
          className={`text-xs px-2 md:px-2.5 py-1 rounded-full font-medium shrink-0 ${cfg.bg}`}
        >
           
          <span
            className={`inline-block w-1.5 h-1.5 rounded-full ${cfg.dot} mr-1.5 align-middle`}
          /> 
          {cfg.label} 
        </span> 
      </div> 
    </div>
  );
}
export default function SystemHealth() {
  const [health, setHealth] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const fetchHealth = useCallback(async (showToast = false) => {
    try {
      const res = await getSystemHealth();
      setHealth(res.data.data);
      setError(null);
      if (showToast) toast.success("Health data refreshed");
    } catch (e) {
      setError("Failed to fetch system health");
      if (showToast) toast.error("Failed to refresh health data");
    } finally {
      setLoading(false);
    }
  }, []);
  useEffect(() => {
    fetchHealth();
    const interval = setInterval(() => fetchHealth(), 30000);
    return () => clearInterval(interval);
  }, [fetchHealth]);
  const isHealthy = health?.status === "healthy";
  const hasFailedJobs = (health?.failedJobs ?? 0) > 0;
  const hasDeadLetters = (health?.webhookDeadLetters ?? 0) > 0;
  const services = [
    {
      name: "Webhook System",
      icon: Webhook,
      status: hasDeadLetters ? "degraded" : "operational",
      detail: `${health?.webhookDeadLetters ?? 0} dead letters`,
    },
    {
      name: "Email System",
      icon: Mail,
      status: "operational",
      detail: "All clear",
    },
    {
      name: "Job Scheduler",
      icon: Server,
      status: hasFailedJobs ? "degraded" : "operational",
      detail: `${health?.failedJobs ?? 0} failed jobs`,
    },
    {
      name: "WebSocket Server",
      icon: Wifi,
      status: "operational",
      detail: `${health?.activeWebSocketConnections ?? 0} active connections`,
    },
  ];
  if (loading) {
    return (
      <div className="page-container space-y-6 animate-pulse">
         
        <div className="h-8 w-48 bg-surface-200 dark:bg-surface-800 rounded" /> 
        <div className="grid-responsive-3">
           
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div
              key={i}
              className="bg-white dark:bg-surface-900 rounded-xl border border-surface-200 dark:border-surface-800 p-5"
            >
               
              <div className="h-4 w-20 bg-surface-200 dark:bg-surface-800 rounded mb-3" /> 
              <div className="h-8 w-16 bg-surface-200 dark:bg-surface-800 rounded" /> 
            </div>
          ))} 
        </div> 
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
           
          {[1, 2, 3, 4].map((i) => (
            <div
              key={i}
              className="bg-white dark:bg-surface-900 rounded-xl border border-surface-200 dark:border-surface-800 p-5"
            >
               
              <div className="h-4 w-32 bg-surface-200 dark:bg-surface-800 rounded mb-3" /> 
              <div className="h-4 w-48 bg-surface-200 dark:bg-surface-800 rounded" /> 
            </div>
          ))} 
        </div> 
      </div>
    );
  }
  if (error) {
    return (
      <div className="page-container">
         
        <div className="bg-danger-50 dark:bg-danger-900/10 border border-danger-200 dark:border-danger-800/50 rounded-xl p-6 text-center">
           
          <AlertTriangle className="w-12 h-12 text-danger-400 mx-auto mb-3" /> 
          <h3 className="text-lg font-semibold text-danger-800 dark:text-danger-300 mb-1">
            Unable to load system health
          </h3> 
          <p className="text-sm text-danger-600 dark:text-danger-400 mb-4">
            {error}
          </p> 
          <button
            onClick={() => {
              setLoading(true);
              fetchHealth();
            }}
            className="btn btn-sm bg-danger-600 text-white hover:bg-danger-700"
          >
             
            Retry 
          </button> 
        </div> 
      </div>
    );
  }
  return (
    <div className="page-container space-y-6">
       
      <div className="page-header">
         
        <div className="flex items-center gap-3 md:gap-4">
           
          <div className="w-10 h-10 md:w-12 md:h-12 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-xl md:rounded-2xl flex items-center justify-center shadow-lg">
             
            <Activity className="w-5 h-5 md:w-6 md:h-6 text-white" /> 
          </div> 
          <div>
             
            <div className="flex flex-wrap items-center gap-2 md:gap-3">
               
              <h1 className="text-xl md:text-2xl font-bold text-surface-900 dark:text-surface-100">
                System Health
              </h1> 
              <span
                className={`flex items-center gap-1.5 text-xs px-2 md:px-3 py-1 rounded-full font-medium ${isHealthy ? "bg-success-50 dark:bg-success-900/20 text-success-700 dark:text-success-400" : "bg-danger-50 dark:bg-danger-900/20 text-danger-700 dark:text-danger-400"}`}
              >
                 
                <span
                  className={`w-2 h-2 rounded-full ${isHealthy ? "bg-success-500" : "bg-danger-500"}`}
                /> 
                {isHealthy ? "All Systems Healthy" : "Issues Detected"} 
              </span> 
            </div> 
            <p className="text-xs md:text-sm text-surface-500 dark:text-surface-400 mt-0.5">
               
              Last updated: 
              {health?.timestamp
                ? new Date(health.timestamp).toLocaleString()
                : "-"} 
            </p> 
          </div> 
        </div> 
        <button
          onClick={() => fetchHealth(true)}
          className="btn btn-secondary btn-sm"
        >
           
          <RefreshCw className="w-4 h-4" /> Refresh 
        </button> 
      </div> 
      <div className="grid-responsive-3">
         
        <KpiCard
          icon={Users}
          label="Active Users"
          value={health?.activeUsers}
          color="bg-indigo-500"
        /> 
        <KpiCard
          icon={Webhook}
          label="Webhook Dead Letters"
          value={health?.webhookDeadLetters}
          color="bg-purple-500"
          alert={hasDeadLetters}
        /> 
        <KpiCard
          icon={AlertTriangle}
          label="Failed Jobs"
          value={health?.failedJobs}
          color={hasFailedJobs ? "bg-danger-500" : "bg-amber-500"}
          alert={hasFailedJobs}
        /> 
        <KpiCard
          icon={Clock}
          label="Queue Backlog"
          value={health?.queueBacklog}
          color="bg-primary-500"
        /> 
        <KpiCard
          icon={Bell}
          label="Notification Backlog"
          value={health?.notificationBacklog}
          color="bg-pink-500"
        /> 
        <KpiCard
          icon={Wifi}
          label="Active WebSocket Connections"
          value={health?.activeWebSocketConnections}
          color="bg-cyan-500"
        /> 
      </div> 
      <div>
         
        <h2 className="text-base md:text-lg font-semibold text-surface-900 dark:text-surface-100 mb-4">
          System Services
        </h2> 
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
           
          {services.map((svc) => (
            <ServiceCard key={svc.name} {...svc} />
          ))} 
        </div> 
      </div> 
      <div className="bg-white dark:bg-surface-900 rounded-xl border border-surface-200/50 dark:border-surface-800/50 p-4 md:p-5">
         
        <div className="flex items-center gap-2 mb-3">
           
          <Shield className="w-4 h-4 text-indigo-600 dark:text-indigo-400" /> 
          <h3 className="text-sm font-semibold text-surface-900 dark:text-surface-100">
            Health Summary
          </h3> 
        </div> 
        <p className="text-sm text-surface-600 dark:text-surface-400">
           
          The system is currently 
          <strong
            className={
              isHealthy
                ? "text-success-600 dark:text-success-400"
                : "text-danger-600 dark:text-danger-400"
            }
          >
            {isHealthy ? "healthy" : "experiencing issues"}
          </strong>
          . There {health?.failedJobs === 1 ? "is" : "are"} 
          <strong>{health?.failedJobs ?? 0}</strong> failed job
          {health?.failedJobs !== 1 ? "s" : ""}, 
          <strong> {health?.webhookDeadLetters ?? 0}</strong> webhook dead
          letter{health?.webhookDeadLetters !== 1 ? "s" : ""}, and 
          <strong> {health?.queueBacklog ?? 0}</strong> item
          {health?.queueBacklog !== 1 ? "s" : ""} in the retry queue. There 
          {health?.activeWebSocketConnections === 1 ? "is" : "are"} currently 
          <strong>{health?.activeWebSocketConnections ?? 0}</strong> active
          WebSocket connection
          {health?.activeWebSocketConnections !== 1 ? "s" : ""}. 
        </p> 
      </div> 
    </div>
  );
}
