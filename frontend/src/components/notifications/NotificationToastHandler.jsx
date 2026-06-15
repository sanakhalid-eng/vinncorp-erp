import { useEffect, useRef } from "react";
import { toast } from "sonner";
import { wsService } from "../../services/websocket";
import { useNavigate } from "react-router-dom";
const EVENT_TO_ICON = {
  task_assigned: "📋",
  task_unassigned: "📋",
  status_changed: "🔄",
  comment_mention: "💬",
  comment_reply: "💬",
  due_soon: "⏰",
  due_overdue: "🚨",
  file_uploaded: "📎",
  project_invite: "✉️",
};
export default function NotificationToastHandler({ workspaceSlug }) {
  const navigate = useNavigate();
  const handledRef = useRef(new Set());
  useEffect(() => {
    const handler = (event) => {
      const data = event?.data || event;
      if (!data || !data.message) return;
      const dedupKey =
        data.id ||
        data.eventId ||
        `${data.type}:${data.entityId}:${data.message}`;
      if (handledRef.current.has(dedupKey)) return;
      handledRef.current.add(dedupKey);
      const icon = EVENT_TO_ICON[data.type?.toLowerCase()] || "🔔";
      const actionUrl =
        data.actionUrl ||
        (data.entityType === "task" && data.entityId
          ? `/w/${workspaceSlug}/tasks?taskId=${data.entityId}`
          : null);
      toast(
        <div className="flex flex-col gap-0.5">
           
          <div className="flex items-center gap-2 text-sm font-semibold text-surface-900 dark:text-surface-100">
             
            <span>{icon}</span> 
            <span className="capitalize">
              {data.type?.replace(/_/g, " ")}
            </span> 
          </div> 
          <p className="text-xs text-surface-600 dark:text-surface-400 ml-6">
            {data.message}
          </p> 
        </div>,
        actionUrl
          ? {
              action: { label: "View", onClick: () => navigate(actionUrl) },
              duration: 6000,
            }
          : { duration: 4000 },
      );
    };
    wsService.on("notification", handler);
    return () => wsService.off("notification", handler);
  }, [workspaceSlug, navigate]);
  return null;
}
