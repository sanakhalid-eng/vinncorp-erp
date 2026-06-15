import { useState, useEffect } from "react";
import { getProjectActivities, getEntityActivities } from "../api/activityApi";
const ACTION_ICONS = {
  CREATED: "✨",
  UPDATED: "✏️",
  DELETED: "🗑️",
  ASSIGNED: "👤",
  UNASSIGNED: "🚫",
  STATUS_CHANGED: "🔄",
  COMMENT_ADDED: "💬",
  FILE_UPLOADED: "📎",
  MEMBER_ADDED: "➕",
  MEMBER_REMOVED: "➖",
  PRIORITY_CHANGED: "⚡",
  DUE_DATE_CHANGED: "📅",
};
const ACTION_COLORS = {
  CREATED: "bg-green-100 text-green-700",
  UPDATED: "bg-blue-100 text-blue-700",
  DELETED: "bg-red-100 text-red-700",
  ASSIGNED: "bg-purple-100 text-purple-700",
  UNASSIGNED: "bg-gray-100 text-gray-700",
  STATUS_CHANGED: "bg-indigo-100 text-indigo-700",
  COMMENT_ADDED: "bg-yellow-100 text-yellow-700",
  FILE_UPLOADED: "bg-orange-100 text-orange-700",
  MEMBER_ADDED: "bg-teal-100 text-teal-700",
  MEMBER_REMOVED: "bg-rose-100 text-rose-700",
  PRIORITY_CHANGED: "bg-amber-100 text-amber-700",
  DUE_DATE_CHANGED: "bg-cyan-100 text-cyan-700",
};
function formatTimeAgo(dateString) {
  if (!dateString) return "";
  const date = new Date(dateString);
  const now = new Date();
  const seconds = Math.floor((now - date) / 1000);
  if (seconds < 60) return "just now";
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}h ago`;
  if (seconds < 604800) return `${Math.floor(seconds / 86400)}d ago`;
  return date.toLocaleDateString();
}
export default function ActivityFeed({
  projectId,
  entityType,
  entityId,
  limit = 20,
}) {
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  useEffect(() => {
    const fetchActivities = async () => {
      setLoading(true);
      setError(null);
      try {
        let response;
        if (projectId) {
          response = await getProjectActivities(projectId, 0, limit);
        } else if (entityType && entityId) {
          response = await getEntityActivities(entityType, entityId, 0, limit);
        } else {
          setActivities([]);
          setLoading(false);
          return;
        }
        setActivities(response?.content ?? []);
      } catch (err) {
        console.error("Failed to fetch activities:", err);
        setError("Failed to load activity log");
      } finally {
        setLoading(false);
      }
    };
    fetchActivities();
  }, [projectId, entityType, entityId, limit]);
  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
         
        <div className="animate-spin h-6 w-6 border-2 border-gray-300 border-t-blue-500 rounded-full" /> 
        <span className="ml-3 text-sm text-gray-500">
          Loading activity log...
        </span> 
      </div>
    );
  }
  if (error) {
    return (
      <div className="text-center py-8 text-red-500 text-sm"> {error} </div>
    );
  }
  if (activities.length === 0) {
    return (
      <div className="text-center py-8 text-gray-400">
         
        <svg
          className="mx-auto h-12 w-12 text-gray-300"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
           
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={1.5}
            d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
          /> 
        </svg> 
        <p className="mt-2 text-sm">No activity yet</p> 
      </div>
    );
  }
  return (
    <div className="space-y-3">
       
      {activities.map((activity) => (
        <div
          key={activity.id}
          className="flex items-start gap-3 p-3 rounded-lg hover:bg-gray-50 transition-colors border border-gray-100"
        >
           
          <div
            className={`flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center text-sm ${ACTION_COLORS[activity.action] ?? "bg-gray-100 text-gray-600"}`}
          >
             
            {ACTION_ICONS[activity.action] ?? "•"} 
          </div> 
          <div className="flex-1 min-w-0">
             
            <div className="flex items-center gap-2 flex-wrap">
               
              <span className="font-medium text-sm text-gray-900">
                 
                {activity.user?.name ?? "Unknown User"} 
              </span> 
              <span className="text-sm text-gray-500">
                 
                {activity.description} 
              </span> 
            </div> 
            <div className="flex items-center gap-2 mt-1">
               
              <span className="text-xs text-gray-400">
                 
                {formatTimeAgo(activity.createdAt)} 
              </span> 
              {activity.entityType && (
                <span className="text-xs px-2 py-0.5 rounded-full bg-gray-100 text-gray-500">
                   
                  {activity.entityType} #{activity.entityId} 
                </span>
              )} 
            </div> 
          </div> 
        </div>
      ))} 
    </div>
  );
}
