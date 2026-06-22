import { useState, useEffect, useCallback } from "react";
import {
  Activity,
  CheckCircle,
  XCircle,
  Clock,
  RefreshCw,
  Server,
  AlertTriangle,
  Play,
} from "lucide-react";
import { getJobStatuses } from "../../system/api/systemApi";
import { toast } from "sonner";
const STATUS_STYLES = {
  SUCCESS: { bg: "bg-green-50 text-green-700", dot: "bg-green-500" },
  FAILED: { bg: "bg-red-50 text-red-700", dot: "bg-red-500" },
  RUNNING: { bg: "bg-yellow-50 text-yellow-700", dot: "bg-yellow-500" },
  UNKNOWN: { bg: "bg-gray-50 text-gray-500", dot: "bg-gray-400" },
};
function SummaryCard({ icon: Icon, label, value, color, alert }) {
  return (
    <div className="bg-white rounded-xl border border-gray-200 p-5 hover:shadow-md transition-shadow">
       
      <div className="flex items-start justify-between">
         
        <div>
           
          <p className="text-sm text-gray-500 mb-1">{label}</p> 
          <p
            className={`text-3xl font-bold ${alert ? "text-red-600" : "text-gray-900"}`}
          >
             
            {value ?? "-"} 
          </p> 
        </div> 
        <div className={`p-2.5 rounded-lg ${color}`}>
           
          <Icon className="w-5 h-5 text-white" /> 
        </div> 
      </div> 
    </div>
  );
}
function JobStatusBadge({ status }) {
  const style = STATUS_STYLES[status] || STATUS_STYLES.UNKNOWN;
  return (
    <span
      className={`inline-flex items-center gap-1.5 text-xs px-2.5 py-1 rounded-full font-medium ${style.bg}`}
    >
       
      <span className={`w-1.5 h-1.5 rounded-full ${style.dot}`} /> 
      {status || "UNKNOWN"} 
    </span>
  );
}
function formatDuration(ms) {
  if (ms == null) return "-";
  if (ms < 1000) return `${ms}ms`;
  if (ms < 60000) return `${(ms / 1000).toFixed(1)}s`;
  return `${(ms / 60000).toFixed(1)}m`;
}
function formatDate(dateStr) {
  if (!dateStr) return "-";
  const d = new Date(dateStr);
  const now = new Date();
  const diffMs = now - d;
  const diffMin = Math.floor(diffMs / 60000);
  if (diffMin < 1) return "Just now";
  if (diffMin < 60) return `${diffMin}m ago`;
  if (diffMin < 1440) return `${Math.floor(diffMin / 60)}h ago`;
  return d.toLocaleDateString();
}
export default function JobDashboard() {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const fetchJobs = useCallback(async (showToast = false) => {
    try {
      const res = await getJobStatuses();
      const data = Array.isArray(res.data?.data)
        ? res.data.data
        : Array.isArray(res.data)
          ? res.data
          : [];
      setJobs(data);
      setError(null);
      if (showToast) toast.success("Job data refreshed");
    } catch (e) {
      setError("Failed to fetch job statuses");
      if (showToast) toast.error("Failed to refresh job data");
    } finally {
      setLoading(false);
    }
  }, []);
  useEffect(() => {
    fetchJobs();
    const interval = setInterval(() => fetchJobs(), 30000);
    return () => clearInterval(interval);
  }, [fetchJobs]);
  const totalJobs = jobs.length;
  const healthyJobs = jobs.filter((j) => j.lastStatus === "SUCCESS").length;
  const failedJobs = jobs.filter((j) => j.lastStatus === "FAILED").length;
  const successRate =
    totalJobs > 0 ? Math.round((healthyJobs / totalJobs) * 100) : 0;
  if (loading) {
    return (
      <div className="p-6 space-y-6 animate-pulse">
         
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
        <div className="bg-white rounded-xl border border-gray-200 p-5">
           
          <div className="h-6 w-32 bg-gray-200 rounded mb-4" /> 
          <div className="space-y-3">
             
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="h-10 bg-gray-100 rounded" />
            ))} 
          </div> 
        </div> 
      </div>
    );
  }
  if (error) {
    return (
      <div className="p-6">
         
        <div className="bg-red-50 border border-red-200 rounded-xl p-6 text-center">
           
          <AlertTriangle className="w-12 h-12 text-red-400 mx-auto mb-3" /> 
          <h3 className="text-lg font-semibold text-red-800 mb-1">
            Unable to load job data
          </h3> 
          <p className="text-sm text-red-600 mb-4">{error}</p> 
          <button
            onClick={() => {
              setLoading(true);
              fetchJobs();
            }}
            className="px-4 py-2 bg-red-600 text-white rounded-lg text-sm hover:bg-red-700 transition-colors"
          >
             
            Retry 
          </button> 
        </div> 
      </div>
    );
  }
  return (
    <div className="p-6 space-y-6">
       
      <div className="flex items-center justify-between flex-wrap gap-4">
         
        <div className="flex items-center gap-4">
           
          <div className="w-14 h-14 bg-gradient-to-r from-emerald-500 to-teal-600 rounded-2xl flex items-center justify-center shadow-lg">
             
            <Server className="w-7 h-7 text-white" /> 
          </div> 
          <div>
             
            <h1 className="text-2xl font-bold text-gray-900">
              Job Dashboard
            </h1> 
            <p className="text-sm text-gray-500 mt-0.5">
               
              {totalJobs} job{totalJobs !== 1 ? "s" : ""} tracked 
            </p> 
          </div> 
        </div> 
        <button
          onClick={() => fetchJobs(true)}
          className="flex items-center gap-2 px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-sm font-medium text-gray-700 hover:bg-gray-50 hover:shadow-sm transition-all"
        >
           
          <RefreshCw className="w-4 h-4" /> Refresh 
        </button> 
      </div> 
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
         
        <SummaryCard
          icon={Activity}
          label="Total Jobs"
          value={totalJobs}
          color="bg-indigo-500"
        /> 
        <SummaryCard
          icon={CheckCircle}
          label="Healthy Jobs"
          value={healthyJobs}
          color="bg-green-500"
        /> 
        <SummaryCard
          icon={XCircle}
          label="Failed Jobs"
          value={failedJobs}
          color={failedJobs > 0 ? "bg-red-500" : "bg-gray-500"}
          alert={failedJobs > 0}
        /> 
        <SummaryCard
          icon={Clock}
          label="Success Rate"
          value={totalJobs > 0 ? `${successRate}%` : "-"}
          color="bg-blue-500"
        /> 
      </div> 
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
         
        <div className="px-5 py-4 border-b border-gray-100">
           
          <h3 className="text-sm font-semibold text-gray-900">
            Scheduled Jobs
          </h3> 
        </div> 
        <div className="overflow-x-auto">
           
          <table className="w-full text-sm">
             
            <thead>
               
              <tr className="bg-gray-50 border-b border-gray-100">
                 
                <th className="text-left px-5 py-3 font-medium text-gray-500 text-xs uppercase tracking-wider">
                  Job Name
                </th> 
                <th className="text-left px-5 py-3 font-medium text-gray-500 text-xs uppercase tracking-wider">
                  Last Run
                </th> 
                <th className="text-left px-5 py-3 font-medium text-gray-500 text-xs uppercase tracking-wider">
                  Duration
                </th> 
                <th className="text-left px-5 py-3 font-medium text-gray-500 text-xs uppercase tracking-wider">
                  Status
                </th> 
                <th className="text-center px-5 py-3 font-medium text-gray-500 text-xs uppercase tracking-wider">
                  Total Runs
                </th> 
                <th className="text-center px-5 py-3 font-medium text-gray-500 text-xs uppercase tracking-wider">
                  Success
                </th> 
                <th className="text-center px-5 py-3 font-medium text-gray-500 text-xs uppercase tracking-wider">
                  Failures
                </th> 
              </tr> 
            </thead> 
            <tbody className="divide-y divide-gray-100">
               
              {jobs.length === 0 ? (
                <tr>
                   
                  <td
                    colSpan={7}
                    className="text-center py-12 text-gray-400 text-sm"
                  >
                    No jobs found
                  </td> 
                </tr>
              ) : (
                jobs.map((job, idx) => (
                  <tr
                    key={job.id || idx}
                    className="hover:bg-gray-50 transition-colors"
                  >
                     
                    <td className="px-5 py-3.5">
                       
                      <div>
                         
                        <p className="font-medium text-gray-900">
                          {job.jobName}
                        </p> 
                        {job.description && (
                          <p className="text-xs text-gray-400 mt-0.5">
                            {job.description}
                          </p>
                        )} 
                      </div> 
                    </td> 
                    <td className="px-5 py-3.5 text-gray-600 whitespace-nowrap">
                       
                      <div className="flex items-center gap-1.5">
                         
                        <Clock className="w-3.5 h-3.5 text-gray-400" /> 
                        {formatDate(job.lastRunAt)} 
                      </div> 
                    </td> 
                    <td className="px-5 py-3.5 text-gray-600 whitespace-nowrap">
                      {formatDuration(job.lastDurationMs)}
                    </td> 
                    <td className="px-5 py-3.5 whitespace-nowrap">
                      <JobStatusBadge status={job.lastStatus} />
                    </td> 
                    <td className="px-5 py-3.5 text-center text-gray-600">
                      {job.totalRuns ?? "-"}
                    </td> 
                    <td className="px-5 py-3.5 text-center text-green-600 font-medium">
                      {job.successRuns ?? "-"}
                    </td> 
                    <td className="px-5 py-3.5 text-center text-red-600 font-medium">
                      {job.failureRuns ?? "-"}
                    </td> 
                  </tr>
                ))
              )} 
            </tbody> 
          </table> 
        </div> 
      </div> 
    </div>
  );
}
