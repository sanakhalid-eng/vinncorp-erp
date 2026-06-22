import { useState, useEffect, useMemo } from "react";
import { useParams } from "react-router-dom";
import { getGanttData } from "../api/ganttApi";
import {
  ChevronLeft,
  ChevronRight,
  ChevronDown,
  ZoomIn,
  ZoomOut,
} from "lucide-react";
import { toast } from "sonner";
import { PageSkeleton } from "../../../components/LoadingSkeleton";
import ErrorState from "../../../components/ErrorState";
import EmptyState from "../../../components/EmptyState";

const DAY_WIDTH = 28;
const ROW_HEIGHT = 40;
const HEADER_HEIGHT = 60;
const SIDEBAR_WIDTH = 300;
const LABEL_WIDTH = 220;

function getStatusColor(status) {
  const colors = {
    "To Do": "#94a3b8",
    "In Progress": "#3b82f6",
    Done: "#22c55e",
    Review: "#f59e0b",
    Blocked: "#ef4444",
  };
  return colors[status] || "#64748b";
}

function getPriorityColor(priority) {
  const colors = {
    CRITICAL: "#ef4444",
    HIGH: "#f97316",
    MEDIUM: "#eab308",
    LOW: "#22c55e",
  };
  return colors[priority] || "#94a3b8";
}

function formatDate(date) {
  if (!date) return "";
  const d = new Date(date);
  return d.toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

function daysBetween(start, end) {
  if (!start || !end) return 1;
  const s = new Date(start);
  const e = new Date(end);
  return Math.max(1, Math.ceil((e - s) / (1000 * 60 * 60 * 24)) + 1);
}

function toDate(date) {
  if (!date) return new Date();
  return new Date(date);
}

function addDays(date, days) {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
}

function normalizeDate(d) {
  return new Date(d.getFullYear(), d.getMonth(), d.getDate());
}

export default function GanttPage() {
  const { projectId } = useParams();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [scrollLeft, setScrollLeft] = useState(0);
  const [zoom, setZoom] = useState(1);
  const [expandedTasks, setExpandedTasks] = useState(new Set());

  useEffect(() => {
    if (!projectId) return;
    setLoading(true);
    getGanttData(projectId)
      .then((res) => {
        setData(res);
        if (res.tasks?.length > 0) {
          const parentIds = new Set(
            res.tasks.filter((t) => t.parentTaskId == null).map((t) => t.id)
          );
          setExpandedTasks(parentIds);
        }
      })
      .catch((err) => {
        toast.error("Failed to load Gantt data");
        setError(err.response?.data?.message || "Failed to load Gantt data");
        console.error(err);
      })
      .finally(() => setLoading(false));
  }, [projectId]);

  const { dateRange, totalDays, dayHeaders, tasks, sprints, dependencies } =
    useMemo(() => {
      if (!data) {
        return {
          dateRange: { start: new Date(), end: new Date() },
          totalDays: 30,
          dayHeaders: [],
          tasks: [],
          sprints: [],
          dependencies: [],
        };
      }

      const allDates = [];
      if (data.sprints?.length > 0) {
        data.sprints.forEach((s) => {
          if (s.startDate) allDates.push(toDate(s.startDate));
          if (s.endDate) allDates.push(toDate(s.endDate));
        });
      }
      if (data.tasks?.length > 0) {
        data.tasks.forEach((t) => {
          if (t.startDate) allDates.push(toDate(t.startDate));
          if (t.endDate) allDates.push(toDate(t.endDate));
          if (t.dueDate) allDates.push(toDate(t.dueDate));
        });
      }
      if (allDates.length === 0) {
        const now = new Date();
        return {
          dateRange: { start: now, end: addDays(now, 30) },
          totalDays: 30,
          dayHeaders: [],
          tasks: [],
          sprints: [],
          dependencies: [],
        };
      }

      const minDate = normalizeDate(
        allDates.reduce((a, b) => (a < b ? a : b))
      );
      const maxDate = normalizeDate(
        allDates.reduce((a, b) => (a > b ? a : b))
      );
      const rangeStart = addDays(minDate, -3);
      const rangeEnd = addDays(maxDate, 5);
      const days = Math.max(
        14,
        Math.ceil((rangeEnd - rangeStart) / (1000 * 60 * 60 * 24))
      );

      const headers = [];
      const months = [];
      for (let i = 0; i <= days; i++) {
        const d = addDays(rangeStart, i);
        headers.push(d);
        const monthKey = `${d.getFullYear()}-${d.getMonth()}`;
        if (!months.includes(monthKey)) {
          months.push(monthKey);
        }
      }

      const monthHeaders = months.map((m) => {
        const [y, mo] = m.split("-").map(Number);
        return {
          label: new Date(y, mo).toLocaleDateString("en-US", {
            month: "long",
            year: "numeric",
          }),
          start: new Date(y, mo),
          end: new Date(y, mo + 1, 0),
        };
      });

      const parentTasks = data.tasks?.filter((t) => t.parentTaskId == null) || [];
      const subtaskMap = {};
      (data.tasks || []).forEach((t) => {
        if (t.parentTaskId != null) {
          if (!subtaskMap[t.parentTaskId]) subtaskMap[t.parentTaskId] = [];
          subtaskMap[t.parentTaskId].push(t);
        }
      });

      const flatTasks = [];
      parentTasks.forEach((pt) => {
        flatTasks.push(pt);
        if (expandedTasks.has(pt.id)) {
          const subs = subtaskMap[pt.id] || [];
          subs.sort(
            (a, b) => toDate(a.startDate) - toDate(b.startDate)
          );
          subs.forEach((s) => flatTasks.push(s));
        }
      });

      return {
        dateRange: { start: rangeStart, end: rangeEnd },
        totalDays: days,
        dayHeaders: headers,
        monthHeaders,
        tasks: flatTasks,
        allTasks: data.tasks || [],
        sprints: data.sprints || [],
        dependencies: data.dependencies || [],
        subtaskMap,
      };
    }, [data, expandedTasks]);

  const effectiveDayWidth = DAY_WIDTH * zoom;
  const timelineWidth = totalDays * effectiveDayWidth;

  const getDayOffset = (date) => {
    if (!date) return -1;
    const d = normalizeDate(toDate(date));
    const diff = d - dateRange.start;
    return diff / (1000 * 60 * 60 * 24);
  };

  const getBarStyle = (startDate, endDate) => {
    const start = getDayOffset(startDate);
    const end = getDayOffset(endDate);
    if (start < 0) return {};
    const left = Math.max(0, start) * effectiveDayWidth;
    const width =
      end >= 0
        ? Math.max(effectiveDayWidth, (end - start + 1) * effectiveDayWidth)
        : effectiveDayWidth;
    return { left: `${left}px`, width: `${width}px` };
  };

  const handleWheel = (e) => {
    if (e.shiftKey) {
      e.preventDefault();
      setScrollLeft((prev) =>
        Math.max(0, Math.min(prev + e.deltaY, timelineWidth - window.innerWidth + SIDEBAR_WIDTH + LABEL_WIDTH))
      );
    }
  };

  const toggleExpand = (taskId) => {
    setExpandedTasks((prev) => {
      const next = new Set(prev);
      if (next.has(taskId)) next.delete(taskId);
      else next.add(taskId);
      return next;
    });
  };

  const isParent = (taskId) => {
    return data?.tasks?.some((t) => t.parentTaskId === taskId);
  };

  if (loading) {
    return <PageSkeleton />;
  }

  if (error) {
    return <ErrorState title="Failed to load Gantt chart" message={error} onRetry={() => { setLoading(true); getGanttData(projectId).then(setData).catch(e => setError(e.message)).finally(() => setLoading(false)); }} />;
  }

  if (!projectId) {
    return (
      <div className="flex h-[calc(100vh-4rem)] flex-col items-center justify-center gap-4 text-surface-500">
        <p className="text-lg">Select a project to view the Gantt chart</p>
      </div>
    );
  }

  return (
    <div className="flex h-[calc(100vh-4rem)] flex-col bg-white dark:bg-surface-950">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between border-b border-surface-200 dark:border-surface-800 px-4 py-2 sm:px-6 lg:px-8">
        <h1 className="text-lg font-bold text-surface-900 dark:text-white">
          Gantt Chart
        </h1>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setZoom((z) => Math.max(0.5, z - 0.25))}
            className="rounded-lg p-1.5 text-surface-600 hover:bg-surface-100 dark:hover:bg-surface-800"
          >
            <ZoomOut className="h-4 w-4" />
          </button>
          <span className="text-xs text-surface-500 min-w-[3rem] text-center">
            {Math.round(zoom * 100)}%
          </span>
          <button
            onClick={() => setZoom((z) => Math.min(3, z + 0.25))}
            className="rounded-lg p-1.5 text-surface-600 hover:bg-surface-100 dark:hover:bg-surface-800"
          >
            <ZoomIn className="h-4 w-4" />
          </button>
        </div>
      </div>

      <div className="flex flex-1 overflow-hidden" onWheel={handleWheel}>
        <div
          className="shrink-0 border-r border-surface-200 dark:border-surface-800 bg-surface-50 dark:bg-surface-900 overflow-y-auto"
          style={{ width: LABEL_WIDTH }}
        >
          <div
            className="sticky top-0 z-10 border-b border-surface-200 dark:border-surface-800 bg-surface-100 dark:bg-surface-800 px-3 py-3 text-xs font-semibold text-surface-500 uppercase"
            style={{ height: HEADER_HEIGHT }}
          >
            Tasks
          </div>
          {tasks.map((task, idx) => (
            <div
              key={task.id}
              className="flex items-center gap-2 border-b border-surface-100 dark:border-surface-800 px-3 hover:bg-surface-100 dark:hover:bg-surface-800 cursor-pointer transition-colors"
              style={{ height: ROW_HEIGHT }}
              onClick={() => {
                if (task.parentTaskId == null && isParent(task.id)) {
                  toggleExpand(task.id);
                }
              }}
            >
              {task.parentTaskId == null && isParent(task.id) && (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    toggleExpand(task.id);
                  }}
                  className="shrink-0 text-surface-400 hover:text-surface-600"
                >
                  {expandedTasks.has(task.id) ? (
                    <ChevronDown className="h-3.5 w-3.5" />
                  ) : (
                    <ChevronRight className="h-3.5 w-3.5" />
                  )}
                </button>
              )}
              {task.parentTaskId != null && (
                <div className="w-4 shrink-0" />
              )}
              <div
                className="h-2 w-2 shrink-0 rounded-full"
                style={{ backgroundColor: getPriorityColor(task.priority) }}
              />
              <span
                className={`truncate text-xs ${
                  task.parentTaskId != null
                    ? "text-surface-500"
                    : "font-medium text-surface-900 dark:text-white"
                }`}
              >
                {task.title}
              </span>
              {task.storyPoints && (
                <span className="ml-auto shrink-0 rounded bg-surface-200 dark:bg-surface-700 px-1.5 py-0.5 text-[10px] font-medium text-surface-500">
                  {task.storyPoints}pt
                </span>
              )}
            </div>
          ))}
        </div>

        <div
          className="flex-1 overflow-auto"
          onScroll={(e) => setScrollLeft(e.target.scrollLeft)}
        >
          <div style={{ width: timelineWidth, position: "relative" }}>
            <div
              className="sticky top-0 z-10 border-b border-surface-200 dark:border-surface-800 bg-white dark:bg-surface-950"
              style={{ height: HEADER_HEIGHT }}
            >
              <div className="flex" style={{ height: "50%" }}>
                {dayHeaders.map((d, i) => {
                  const isWeekend = d.getDay() === 0 || d.getDay() === 6;
                  const isFirst = d.getDate() === 1;
                  return (
                    <div
                      key={i}
                      className={`shrink-0 border-r border-surface-100 dark:border-surface-800 flex items-center justify-center text-[10px] font-medium ${
                        isWeekend
                          ? "bg-surface-50 dark:bg-surface-900 text-surface-400"
                          : "text-surface-600 dark:text-surface-400"
                      }`}
                      style={{ width: effectiveDayWidth }}
                    >
                      {d.getDate() === 1 || i === 0
                        ? d.toLocaleDateString("en-US", {
                            month: "short",
                            day: "numeric",
                          })
                        : d.getDate()}
                    </div>
                  );
                })}
              </div>
              <div className="flex" style={{ height: "50%" }}>
                {dayHeaders.map((d, i) => {
                  const isWeekend = d.getDay() === 0 || d.getDay() === 6;
                  return (
                    <div
                      key={i}
                      className={`shrink-0 border-r border-surface-100 dark:border-surface-800 flex items-center justify-center text-[9px] ${
                        isWeekend
                          ? "bg-surface-50 dark:bg-surface-900 text-surface-400"
                          : "text-surface-400"
                      }`}
                      style={{ width: effectiveDayWidth }}
                    >
                      {d.toLocaleDateString("en-US", { weekday: "narrow" })}
                    </div>
                  );
                })}
              </div>
            </div>

            {sprints.length > 0 && (
              <div
                className="relative border-b border-surface-200 dark:border-surface-800 bg-surface-50/50 dark:bg-surface-900/50"
                style={{ height: ROW_HEIGHT }}
              >
                {sprints.map((sprint) => {
                  const start = getDayOffset(sprint.startDate);
                  const end = getDayOffset(sprint.endDate);
                  if (start < 0 && end < 0) return null;
                  const left = Math.max(0, start) * effectiveDayWidth;
                  const width =
                    end >= 0
                      ? (end - Math.max(0, start) + 1) * effectiveDayWidth
                      : timelineWidth - left;
                  const sprintColors = {
                    ACTIVE: "bg-blue-100 border-blue-300 dark:bg-blue-900/30 dark:border-blue-700",
                    PLANNED:
                      "bg-surface-100 border-surface-300 dark:bg-surface-800 dark:border-surface-600",
                    COMPLETED:
                      "bg-green-100 border-green-300 dark:bg-green-900/30 dark:border-green-700",
                  };
                  const colorClass =
                    sprintColors[sprint.status] || sprintColors.PLANNED;
                  return (
                    <div
                      key={sprint.id}
                      className={`absolute top-0.5 h-[calc(100%-4px)] rounded border ${colorClass} flex items-center px-2 text-xs font-medium text-surface-700 dark:text-surface-300 overflow-hidden`}
                      style={{
                        left: `${left}px`,
                        width: `${Math.max(40, width)}px`,
                      }}
                    >
                      <span className="truncate">{sprint.name}</span>
                    </div>
                  );
                })}
              </div>
            )}

            <div className="relative">
              {tasks.map((task, idx) => {
                const barStyle = getBarStyle(
                  task.startDate || task.dueDate,
                  task.endDate || task.dueDate
                );
                const hasChildren = isParent(task.id);

                return (
                  <div
                    key={task.id}
                    className="relative border-b border-surface-100 dark:border-surface-800"
                    style={{ height: ROW_HEIGHT }}
                  >
                    <div
                      className="absolute top-1 h-[calc(100%-8px)] rounded-md flex items-center px-2 overflow-hidden transition-all hover:opacity-80 cursor-pointer group"
                      style={{
                        ...barStyle,
                        backgroundColor: getStatusColor(task.status),
                        opacity: task.parentTaskId != null ? 0.6 : 0.85,
                      }}
                      title={`${task.title} (${task.status || "No Status"})`}
                    >
                      <span className="truncate text-[11px] font-medium text-white drop-shadow-sm">
                        {task.title}
                      </span>
                      {task.progress > 0 && (
                        <div
                          className="absolute bottom-0 left-0 h-1 bg-white/30 rounded-b-md"
                          style={{
                            width: `${Math.min(100, task.progress)}%`,
                          }}
                        />
                      )}
                    </div>
                  </div>
                );
              })}
            </div>

            {dependencies.map((dep, idx) => {
              const source = tasks.find((t) => t.id === dep.sourceTaskId);
              const target = tasks.find((t) => t.id === dep.targetTaskId);
              if (!source || !target) return null;

              const sourceEnd = getDayOffset(source.endDate || source.dueDate);
              const targetStart = getDayOffset(
                target.startDate || target.dueDate
              );
              if (sourceEnd < 0 || targetStart < 0) return null;

              const x1 =
                (sourceEnd + 1) * effectiveDayWidth;
              const x2 = targetStart * effectiveDayWidth;
              const y1 = tasks.indexOf(source) * ROW_HEIGHT + ROW_HEIGHT / 2;
              const y2 = tasks.indexOf(target) * ROW_HEIGHT + ROW_HEIGHT / 2;
              const yMid = y1;
              const xMid = x1 + (x2 - x1) / 2;

              return (
                <svg
                  key={idx}
                  className="pointer-events-none absolute top-0 left-0"
                  style={{
                    width: timelineWidth,
                    height: tasks.length * ROW_HEIGHT,
                    overflow: "visible",
                  }}
                >
                  <path
                    d={`M ${x1} ${y1} C ${x1 + Math.abs(x2 - x1) / 2} ${y1}, ${x2 - Math.abs(x2 - x1) / 2} ${y2}, ${x2} ${y2}`}
                    fill="none"
                    stroke="#94a3b8"
                    strokeWidth="1.5"
                    strokeDasharray="4 2"
                    opacity="0.6"
                  />
                  <polygon
                    points={`${x2 - 4},${y2 - 4} ${x2},${y2} ${x2 - 4},${y2 + 4}`}
                    fill="#94a3b8"
                    opacity="0.6"
                  />
                </svg>
              );
            })}
          </div>
        </div>
      </div>

      {tasks.length === 0 && (
        <div className="absolute inset-0 flex items-center justify-center bg-white/80 dark:bg-surface-950/80">
          <EmptyState
            title="No tasks with dates"
            description="Add start and end dates to tasks to see them on the Gantt chart"
          />
        </div>
      )}
    </div>
  );
}


