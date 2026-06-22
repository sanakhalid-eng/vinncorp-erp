import { useEffect, useState, useCallback, useRef } from "react";
import { Activity, Loader2, Filter, Clock } from "lucide-react";
import { useWorkspace } from "../../../context/WorkspaceContext";
import ActivityFeed from "../../analytics/components/ActivityFeed";
const FILTERS = [
  { value: "ALL", label: "All Activity" },
  { value: "TASK", label: "Tasks" },
  { value: "COMMENT", label: "Comments" },
  { value: "PROJECT", label: "Projects" },
  { value: "SPRINT", label: "Sprints" },
  { value: "MEMBER", label: "Members" },
];
export default function WorkspaceActivity() {
  const { workspace } = useWorkspace();
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("ALL");
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const loaderRef = useRef(null);
  const loadActivities = useCallback(
    async (pageNum = 0, append = false) => {
      if (!workspace) return;
      try {
        setLoading(true);
        const params = new URLSearchParams({ page: pageNum, size: 20 });
        if (filter !== "ALL") params.set("entityType", filter);
        const res = await fetch(`/api/activities?${params}`, {
          headers: { "X-Workspace-Id": workspace.id },
        });
        const data = await res.json();
        const items = data.data?.content || data.data || [];
        setActivities((prev) => (append ? [...prev, ...items] : items));
        setHasMore(items.length === 20);
      } catch {
      } finally {
        setLoading(false);
      }
    },
    [workspace?.id, filter],
  );
  useEffect(() => {
    setPage(0);
    setActivities([]);
    loadActivities(0);
  }, [workspace?.id, filter, loadActivities]);
  useEffect(() => {
    if (!loaderRef.current) return;
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore && !loading) {
          const nextPage = page + 1;
          setPage(nextPage);
          loadActivities(nextPage, true);
        }
      },
      { threshold: 0.1 },
    );
    observer.observe(loaderRef.current);
    return () => observer.disconnect();
  }, [hasMore, loading, page, loadActivities]);
  return (
    <div>
       
      <div className="flex items-center justify-between mb-8">
         
        <div>
           
          <h1 className="text-3xl font-bold text-slate-900">Activity</h1> 
          <p className="text-slate-500 mt-1">
            Recent activity in {workspace?.name}
          </p> 
        </div> 
        <div className="flex items-center gap-2">
           
          <Filter className="h-4 w-4 text-slate-400" /> 
          <select
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-indigo-400"
          >
             
            {FILTERS.map((f) => (
              <option key={f.value} value={f.value}>
                {f.label}
              </option>
            ))} 
          </select> 
        </div> 
      </div> 
      {loading && activities.length === 0 ? (
        <div className="flex justify-center py-20">
           
          <Loader2 className="w-8 h-8 animate-spin text-indigo-600" /> 
        </div>
      ) : activities.length === 0 ? (
        <div className="text-center py-20">
           
          <Activity className="w-16 h-16 text-slate-300 mx-auto mb-4" /> 
          <h3 className="text-lg font-semibold text-slate-600 mb-2">
            No activity yet
          </h3> 
          <p className="text-slate-400">
            Activity from your workspace will appear here
          </p> 
        </div>
      ) : (
        <ActivityFeed activities={activities} />
      )} 
      {hasMore && (
        <div ref={loaderRef} className="flex justify-center py-6">
           
          {loading && (
            <Loader2 className="w-6 h-6 animate-spin text-indigo-400" />
          )} 
        </div>
      )} 
    </div>
  );
}
