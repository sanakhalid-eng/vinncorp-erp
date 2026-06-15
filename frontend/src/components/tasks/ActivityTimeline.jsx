import { useEffect, useState } from "react";
import {
  Activity,
  Clock,
  User,
  Tag,
  ArrowRight,
  Plus,
  Edit3,
  Trash2,
  MessageSquare,
  Paperclip,
  Loader2,
} from "lucide-react";
import { getEntityActivities } from "../../api/activityApi";

const ACTION_ICONS = {
  CREATED: Plus,
  UPDATED: Edit3,
  DELETED: Trash2,
  COMMENTED: MessageSquare,
  STATUS_CHANGED: ArrowRight,
  ASSIGNED: User,
  LABEL_CHANGED: Tag,
  ATTACHMENT_ADDED: Paperclip,
};

const ACTION_COLORS = {
  CREATED: "text-green-600 bg-green-100",
  UPDATED: "text-blue-600 bg-blue-100",
  DELETED: "text-red-600 bg-red-100",
  COMMENTED: "text-purple-600 bg-purple-100",
  STATUS_CHANGED: "text-orange-600 bg-orange-100",
  ASSIGNED: "text-indigo-600 bg-indigo-100",
  LABEL_CHANGED: "text-pink-600 bg-pink-100",
  ATTACHMENT_ADDED: "text-cyan-600 bg-cyan-100",
};

export default function ActivityTimeline({ taskId }) {
  const [activities, setActivities] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!taskId) return;
    loadActivities(0, true);
  }, [taskId]);

  const loadActivities = async (p = 0, reset = false) => {
    setLoading(true);
    try {
      const res = await getEntityActivities("TASK", taskId, p, 20);
      const data = res?.content ?? res?.data ?? [];
      setActivities(reset ? data : [...activities, ...data]);
      setHasMore(!res?.last ?? data.length >= 20);
      setPage(p);
    } catch {
      // ignore
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return "";
    const d = new Date(dateStr);
    const now = new Date();
    const diff = now - d;
    const mins = Math.floor(diff / 60000);
    if (mins < 1) return "just now";
    if (mins < 60) return `${mins}m ago`;
    if (mins < 1440) return `${Math.floor(mins / 60)}h ago`;
    return d.toLocaleDateString();
  };

  return (
    <div>
      <div className="flex items-center gap-2 mb-4">
        <Activity className="h-5 w-5 text-indigo-500" />
        <h3 className="font-semibold text-gray-900">Activity Log</h3>
      </div>

      <div className="relative">
        <div className="absolute left-[17px] top-2 bottom-2 w-0.5 bg-gray-200" />

        <div className="space-y-0 max-h-[500px] overflow-y-auto">
          {activities.length === 0 && !loading && (
            <p className="text-sm text-gray-400 text-center py-6 relative z-10">
              No activity recorded yet.
            </p>
          )}

          {activities.map((act, idx) => {
            const Icon = ACTION_ICONS[act.action] ?? Activity;
            const color = ACTION_COLORS[act.action] ?? "text-gray-600 bg-gray-100";
            return (
              <div key={act.id || idx} className="relative flex items-start gap-4 pb-4 pl-0">
                <div className={`relative z-10 w-9 h-9 rounded-full flex items-center justify-center flex-shrink-0 ${color}`}>
                  <Icon className="h-4 w-4" />
                </div>
                <div className="flex-1 min-w-0 pt-1">
                  <p className="text-sm text-gray-800">
                    <span className="font-semibold">
                      {act.user?.name || act.user?.username || "Someone"}
                    </span>{" "}
                    {act.description || act.action?.toLowerCase().replace(/_/g, " ")}
                  </p>
                  <span className="text-xs text-gray-400">{formatDate(act.createdAt)}</span>
                </div>
              </div>
            );
          })}

          {loading && (
            <div className="flex justify-center py-4 relative z-10">
              <Loader2 className="h-5 w-5 text-indigo-500 animate-spin" />
            </div>
          )}
        </div>
      </div>

      {hasMore && !loading && activities.length > 0 && (
        <button
          type="button"
          onClick={() => loadActivities(page + 1)}
          className="w-full mt-2 py-2 text-sm text-indigo-600 hover:text-indigo-700 font-medium rounded-lg hover:bg-indigo-50 transition-colors"
        >
          Load more
        </button>
      )}
    </div>
  );
}
