import { useState, useEffect, useRef, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { Bell, CheckCheck, Trash2, ExternalLink } from "lucide-react";
import {
  getNotifications,
  getUnreadCount,
  getUnreadCountByCategory,
  markAsRead,
  markAllAsRead,
  deleteNotification,
} from "../../api/notificationApi";
import { useNotificationWebSocket } from "../../hooks/useNotificationWebSocket";
import { useAuth } from "../../context/useAuth";

const TYPE_ICONS = {
  TASK_ASSIGNED: "\uD83D\uDCCB",
  TASK_UNASSIGNED: "\uD83D\uDCE4",
  STATUS_CHANGED: "\uD83D\uDD04",
  COMMENT_MENTION: "\uD83D\uDCAC",
  COMMENT_REPLY: "\u21A9\uFE0F",
  DUE_SOON: "\u23F0",
  DUE_OVERDUE: "\uD83D\uDEA8",
  FILE_UPLOADED: "\uD83D\uDCCE",
  PROJECT_INVITE: "\uD83D\uDCE2",
  DEADLINE_APPROACHING: "\u26A0\uFE0F",
};

const CATEGORY_LABELS = {
  TASK: "Tasks",
  COMMENT: "Comments",
  SPRINT: "Sprints",
  TIME_TRACKING: "Time Tracking",
  INVITATION: "Invitations",
  SECURITY: "Security",
  SYSTEM: "System",
};

function formatTime(dateString) {
  if (!dateString) return "";
  const date = new Date(dateString);
  const now = new Date();
  const diff = now - date;
  const minutes = Math.floor(diff / 60000);
  if (minutes < 1) return "just now";
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
}

function groupByDate(notifications) {
  const groups = {};
  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const yesterday = new Date(today.getTime() - 86400000);

  notifications.forEach((n) => {
    const date = new Date(n.createdAt);
    let label;
    if (date >= today) label = "Today";
    else if (date >= yesterday) label = "Yesterday";
    else label = date.toLocaleDateString();

    if (!groups[label]) groups[label] = [];
    groups[label].push(n);
  });

  return groups;
}

function groupByCategory(notifications) {
  const groups = {};
  notifications.forEach((n) => {
    const cat = n.category || "UNCATEGORIZED";
    if (!groups[cat]) groups[cat] = [];
    groups[cat].push(n);
  });
  return groups;
}

export default function NotificationDropdown() {
  const [isOpen, setIsOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [unreadByCategory, setUnreadByCategory] = useState({});
  const [loading, setLoading] = useState(false);
  const dropdownRef = useRef(null);
  const navigate = useNavigate();
  const { user } = useAuth();

  const handleNewNotification = useCallback((notification) => {
    setNotifications((prev) => [notification, ...prev].slice(0, 50));
    setUnreadCount((prev) => prev + 1);
    if (notification.category) {
      setUnreadByCategory((prev) => ({
        ...prev,
        [notification.category]: (prev[notification.category] || 0) + 1,
      }));
    }
  }, []);

  const { isConnected } = useNotificationWebSocket(
    handleNewNotification,
    user?.id,
  );

  useEffect(() => {
    const interval = setInterval(async () => {
      try {
        const result = await getUnreadCount();
        const count =
          typeof result === "object"
            ? (result?.unreadCount ?? 0)
            : (result ?? 0);
        if (typeof count === "number") setUnreadCount(count);
      } catch {
        // ignore
      }
      try {
        const result = await getUnreadCountByCategory();
        setUnreadByCategory(result?.data ?? {});
      } catch {
        // ignore
      }
    }, 30000);
    return () => clearInterval(interval);
  }, []);

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const { page, unreadCount: count } = await getNotifications(0, 10);
      setNotifications(page?.content ?? []);
      setUnreadCount(count);
    } catch {
      setNotifications([]);
      setUnreadCount(0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
      }
    };
    if (isOpen) document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [isOpen]);

  const handleNotificationClick = async (notification) => {
    if (!notification.isRead) {
      await markAsRead(notification.id);
      setUnreadCount((prev) => Math.max(0, prev - 1));
      setNotifications((prev) =>
        prev.map((n) =>
          n.id === notification.id ? { ...n, isRead: true } : n,
        ),
      );
    }
    setIsOpen(false);
    if (notification.actionUrl) {
      navigate(notification.actionUrl);
    }
  };

  const handleMarkAllRead = async () => {
    await markAllAsRead();
    setUnreadCount(0);
    setUnreadByCategory({});
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
  };

  const handleDelete = async (e, notificationId) => {
    e.stopPropagation();
    const wasUnread =
      notifications.find((n) => n.id === notificationId)?.isRead === false;
    await deleteNotification(notificationId);
    setNotifications((prev) => prev.filter((n) => n.id !== notificationId));
    if (wasUnread) setUnreadCount((prev) => Math.max(0, prev - 1));
  };

  const grouped = groupByCategory(notifications);

  return (
    <div className="relative" ref={dropdownRef}>
      <button
        onClick={() => {
          setIsOpen(!isOpen);
          if (!isOpen) fetchNotifications();
        }}
        className="relative rounded-lg p-2 text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-700"
      >
        <Bell className="h-5 w-5" />
        {isConnected && (
          <span className="absolute -right-0.5 -top-0.5 h-2 w-2 rounded-full bg-green-500 ring-2 ring-white" />
        )}
        {unreadCount > 0 && (
          <span
            className={`absolute -right-0.5 -top-0.5 flex h-4 w-4 items-center justify-center rounded-full text-[10px] font-bold text-white ${isConnected ? "bg-red-500" : "bg-red-400"}`}
          >
            {unreadCount > 9 ? "9+" : unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="absolute right-0 top-full z-50 mt-2 w-80 rounded-xl border border-gray-200 bg-white shadow-xl">
          <div className="flex items-center justify-between border-b border-gray-100 p-3">
            <h3 className="font-semibold text-gray-900">Notifications</h3>
            {unreadCount > 0 && (
              <button
                onClick={handleMarkAllRead}
                className="flex items-center gap-1 text-xs font-medium text-blue-600 hover:text-blue-700"
              >
                <CheckCheck className="h-3.5 w-3.5" />
                Mark all read
              </button>
            )}
          </div>

          {Object.keys(unreadByCategory).length > 0 && (
            <div className="flex flex-wrap gap-1 border-b border-gray-100 px-3 py-2">
              {Object.entries(unreadByCategory).map(([cat, count]) =>
                count > 0 ? (
                  <span
                    key={cat}
                    className="inline-flex items-center gap-1 rounded-full bg-blue-50 px-2 py-0.5 text-[10px] font-medium text-blue-700"
                  >
                    {CATEGORY_LABELS[cat] || cat}
                    <span className="inline-flex h-3.5 min-w-[14px] items-center justify-center rounded-full bg-blue-500 px-1 text-[9px] font-bold text-white">
                      {count}
                    </span>
                  </span>
                ) : null,
              )}
            </div>
          )}

          {loading ? (
            <div className="flex items-center justify-center py-8">
              <div className="h-5 w-5 animate-spin rounded-full border-2 border-gray-300 border-t-blue-500" />
            </div>
          ) : notifications.length === 0 ? (
            <div className="py-8 text-center text-sm text-gray-400">
              No notifications
            </div>
          ) : (
            <div className="max-h-96 overflow-y-auto">
              {Object.entries(grouped).map(([category, items]) => (
                <div key={category}>
                  <div className="bg-gray-50 px-3 py-1.5 text-[10px] font-semibold uppercase tracking-wider text-gray-400">
                    {CATEGORY_LABELS[category] || category}
                  </div>
                  {items.slice(0, 5).map((notification) => (
                    <div
                      key={notification.id}
                      onClick={() => handleNotificationClick(notification)}
                      className={`group cursor-pointer border-b border-gray-50 p-3 transition-colors hover:bg-gray-50 ${
                        !notification.isRead ? "bg-blue-50/30" : ""
                      }`}
                    >
                      <div className="flex items-start gap-2.5">
                        <span className="mt-0.5 text-base">
                          {TYPE_ICONS[notification.type] ?? "\uD83D\uDD14"}
                        </span>
                        <div className="min-w-0 flex-1">
                          <p
                            className={`text-sm leading-snug ${!notification.isRead ? "font-medium text-gray-900" : "text-gray-600"}`}
                          >
                            {notification.message}
                          </p>
                          <div className="mt-1 flex items-center gap-1 text-xs text-gray-400">
                            <span>{formatTime(notification.createdAt)}</span>
                            {!notification.isRead && (
                              <span className="h-1.5 w-1.5 rounded-full bg-blue-500" />
                            )}
                          </div>
                        </div>
                        <div className="flex items-center gap-1 opacity-100 md:opacity-0 md:transition-opacity md:group-hover:opacity-100">
                          <button
                            onClick={(e) => handleDelete(e, notification.id)}
                            className="rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-500"
                          >
                            <Trash2 className="h-3.5 w-3.5" />
                          </button>
                          {notification.actionUrl && (
                            <ExternalLink className="h-3.5 w-3.5 text-gray-400" />
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ))}
            </div>
          )}

          <div className="border-t border-gray-100 p-2 text-center">
            <button
              onClick={() => {
                setIsOpen(false);
                navigate("/notifications");
              }}
              className="text-xs font-medium text-blue-600 hover:text-blue-700"
            >
              View all notifications
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
