import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import {
  Bell,
  CheckCheck,
  Trash2,
  ExternalLink,
  ChevronLeft,
  ChevronRight,
  Filter,
  Search,
  X,
} from "lucide-react";
import AppLayout from "../../../layouts/AppLayout";
import {
  getNotifications,
  getFilteredNotifications,
  getUnreadCountByCategory,
  markAsRead,
  markAllAsRead,
  deleteNotification,
} from "../api/notificationApi";
import { useNotificationWebSocket } from "../hooks/useNotificationWebSocket";
import { useAuth } from "../../../context/useAuth";

const CATEGORIES = [
  { key: "ALL", label: "All" },
  { key: "TASK", label: "Task" },
  { key: "COMMENT", label: "Comment" },
  { key: "SPRINT", label: "Sprint" },
  { key: "TIME_TRACKING", label: "Time" },
  { key: "INVITATION", label: "Invite" },
  { key: "SECURITY", label: "Security" },
  { key: "SYSTEM", label: "System" },
];

const TYPE_OPTIONS = [
  { value: "", label: "All Types" },
  { value: "TASK_ASSIGNED", label: "Task Assigned" },
  { value: "TASK_UNASSIGNED", label: "Task Unassigned" },
  { value: "STATUS_CHANGED", label: "Status Changed" },
  { value: "COMMENT_MENTION", label: "Comment Mention" },
  { value: "COMMENT_REPLY", label: "Comment Reply" },
  { value: "DUE_SOON", label: "Due Soon" },
  { value: "DUE_OVERDUE", label: "Due Overdue" },
  { value: "FILE_UPLOADED", label: "File Uploaded" },
  { value: "PROJECT_INVITE", label: "Project Invite" },
  { value: "DEADLINE_APPROACHING", label: "Deadline Approaching" },
];

const TYPE_ICONS = {
  TASK_ASSIGNED: "≡ƒôï",
  TASK_UNASSIGNED: "≡ƒôñ",
  STATUS_CHANGED: "≡ƒöä",
  COMMENT_MENTION: "≡ƒÆ¼",
  COMMENT_REPLY: "Γå⌐∩╕Å",
  DUE_SOON: "ΓÅ░",
  DUE_OVERDUE: "≡ƒÜ¿",
  FILE_UPLOADED: "≡ƒôÄ",
  PROJECT_INVITE: "≡ƒôó",
  DEADLINE_APPROACHING: "ΓÜá∩╕Å",
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

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [unreadByCategory, setUnreadByCategory] = useState({});
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [selectedCategory, setSelectedCategory] = useState("ALL");
  const [selectedType, setSelectedType] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [showFilters, setShowFilters] = useState(false);
  const pageSize = 20;
  const navigate = useNavigate();
  const { user } = useAuth();

  const handleNewNotification = useCallback((notification) => {
    setNotifications((prev) => [notification, ...prev].slice(0, 50));
    setUnreadCount((prev) => prev + 1);
  }, []);

  useNotificationWebSocket(handleNewNotification, user?.id);

  const fetchNotifications = async (pageNum = 0) => {
    setLoading(true);
    try {
      let result;
      if (selectedCategory !== "ALL" || selectedType) {
        result = await getFilteredNotifications({
          category: selectedCategory !== "ALL" ? selectedCategory : undefined,
          type: selectedType || undefined,
          page: pageNum,
          size: pageSize,
        });
        const { page: pageData } = result;
        setNotifications(pageData?.content ?? []);
        setPage(pageData?.page ?? 0);
        setTotalPages(pageData?.totalPages ?? 0);
        setTotalElements(pageData?.totalElements ?? 0);
      } else {
        result = await getNotifications(pageNum, pageSize);
        const { page: pageData, unreadCount: count } = result;
        setNotifications(pageData?.content ?? []);
        setUnreadCount(count);
        setPage(pageData?.page ?? 0);
        setTotalPages(pageData?.totalPages ?? 0);
        setTotalElements(pageData?.totalElements ?? 0);
      }
    } catch {
      setNotifications([]);
      setUnreadCount(0);
    } finally {
      setLoading(false);
    }
  };

  const fetchUnreadByCategory = async () => {
    try {
      const result = await getUnreadCountByCategory();
      setUnreadByCategory(result?.data ?? {});
    } catch {
      // ignore
    }
  };

  useEffect(() => {
    fetchNotifications(0);
    fetchUnreadByCategory();
  }, []);

  useEffect(() => {
    fetchNotifications(0);
  }, [selectedCategory, selectedType]);

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

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      fetchNotifications(newPage);
    }
  };

  const filteredNotifications = searchQuery
    ? notifications.filter(
        (n) =>
          n.message?.toLowerCase().includes(searchQuery.toLowerCase()) ||
          n.category?.toLowerCase().includes(searchQuery.toLowerCase()),
      )
    : notifications;

  const groupedByDate = groupByDate(filteredNotifications);

  return (
    <AppLayout>
      <div className="page-container max-w-4xl mx-auto">
        <div className="page-header">
          <div>
            <h1 className="page-title flex items-center gap-2">
              <Bell size={24} /> Notifications
            </h1>
            <p className="page-subtitle">
              {totalElements} notification{totalElements !== 1 ? "s" : ""}
              {unreadCount > 0 && ` (${unreadCount} unread)`}
            </p>
          </div>
          {unreadCount > 0 && (
            <button
              onClick={handleMarkAllRead}
              className="btn btn-sm btn-secondary"
            >
              <CheckCheck className="h-4 w-4" />
              Mark all read
            </button>
          )}
        </div>

        <div className="flex flex-col sm:flex-row gap-3 mb-6">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-surface-400" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search notifications..."
              className="input-field pl-10"
            />
            {searchQuery && (
              <button
                onClick={() => setSearchQuery("")}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-surface-400 hover:text-surface-600"
              >
                <X className="h-4 w-4" />
              </button>
            )}
          </div>
          <button
            onClick={() => setShowFilters(!showFilters)}
            className={`btn btn-sm ${showFilters ? "btn-primary" : "btn-secondary"}`}
          >
            <Filter className="h-4 w-4" />
            Filters
          </button>
        </div>

        {showFilters && (
          <div className="mb-4 p-4 rounded-xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-900">
            <div className="flex flex-wrap gap-2 mb-3">
              {CATEGORIES.map((cat) => (
                <button
                  key={cat.key}
                  onClick={() => setSelectedCategory(cat.key)}
                  className={`relative rounded-full px-3 py-1.5 text-sm font-medium transition-colors ${
                    selectedCategory === cat.key
                      ? "bg-primary-600 text-white"
                      : "bg-surface-100 dark:bg-surface-800 text-surface-600 dark:text-surface-400 hover:bg-surface-200 dark:hover:bg-surface-700"
                  }`}
                >
                  {cat.label}
                  {cat.key !== "ALL" && unreadByCategory[cat.key] > 0 && (
                    <span className="ml-1.5 inline-flex h-4 min-w-[16px] items-center justify-center rounded-full bg-danger-500 px-1 text-[10px] font-bold text-white">
                      {unreadByCategory[cat.key]}
                    </span>
                  )}
                </button>
              ))}
            </div>
            <div className="flex items-center gap-2">
              <Filter className="h-4 w-4 text-surface-400" />
              <select
                value={selectedType}
                onChange={(e) => setSelectedType(e.target.value)}
                className="input-field py-1.5 text-sm"
              >
                {TYPE_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </div>
          </div>
        )}

        {loading ? (
          <div className="flex items-center justify-center py-20">
            <div className="h-8 w-8 animate-spin rounded-full border-4 border-surface-200 dark:border-surface-700 border-t-primary-500" />
          </div>
        ) : filteredNotifications.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-surface-400 dark:text-surface-500">
            <Bell className="mb-3 h-12 w-12" />
            <p className="text-lg font-medium">No notifications</p>
            <p className="mt-1 text-sm">You're all caught up!</p>
          </div>
        ) : (
          <div className="overflow-hidden rounded-xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-900 shadow-soft">
            {Object.entries(groupedByDate).map(([label, items]) => (
              <div key={label}>
                <div className="border-b border-surface-100 dark:border-surface-800 bg-surface-50 dark:bg-surface-800/50 px-4 py-2 text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  {label}
                </div>
                {items.map((notification) => (
                  <div
                    key={notification.id}
                    onClick={() => handleNotificationClick(notification)}
                    className={`flex cursor-pointer items-start gap-3 border-b border-surface-50 dark:border-surface-800 px-4 py-3 transition-colors hover:bg-surface-50 dark:hover:bg-surface-800/50 ${
                      !notification.isRead
                        ? "bg-primary-50/30 dark:bg-primary-900/10"
                        : ""
                    }`}
                  >
                    <span className="mt-0.5 text-lg">
                      {TYPE_ICONS[notification.type] ?? "≡ƒöö"}
                    </span>
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <p
                          className={`text-sm leading-snug ${!notification.isRead ? "font-medium text-surface-900 dark:text-surface-100" : "text-surface-600 dark:text-surface-400"}`}
                        >
                          {notification.message}
                        </p>
                        {notification.category && (
                          <span className="shrink-0 rounded-full bg-surface-100 dark:bg-surface-800 px-1.5 py-0.5 text-[10px] font-medium text-surface-500 dark:text-surface-400">
                            {notification.category}
                          </span>
                        )}
                      </div>
                      <p className="mt-1 text-xs text-surface-400">
                        {formatTime(notification.createdAt)}
                      </p>
                    </div>
                    <div className="flex items-center gap-1">
                      {!notification.isRead && (
                        <span className="h-2 w-2 rounded-full bg-primary-500" />
                      )}
                      <button
                        onClick={(e) => handleDelete(e, notification.id)}
                        className="rounded p-1.5 text-surface-400 opacity-100 md:opacity-0 transition-opacity hover:bg-danger-50 dark:hover:bg-danger-900/20 hover:text-danger-500 md:group-hover:opacity-100"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                      {notification.actionUrl && (
                        <ExternalLink className="h-3.5 w-3.5 text-surface-400" />
                      )}
                    </div>
                  </div>
                ))}
              </div>
            ))}

            {totalPages > 1 && (
              <div className="flex items-center justify-between border-t border-surface-100 dark:border-surface-800 px-4 py-3">
                <button
                  onClick={() => handlePageChange(page - 1)}
                  disabled={page === 0}
                  className="btn btn-sm btn-secondary disabled:opacity-40"
                >
                  <ChevronLeft className="h-4 w-4" /> Previous
                </button>
                <span className="text-sm text-surface-500 dark:text-surface-400">
                  Page {page + 1} of {totalPages}
                </span>
                <button
                  onClick={() => handlePageChange(page + 1)}
                  disabled={page >= totalPages - 1}
                  className="btn btn-sm btn-secondary disabled:opacity-40"
                >
                  Next <ChevronRight className="h-4 w-4" />
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </AppLayout>
  );
}
