import { useEffect, useState, useMemo } from "react";
import {
  Calendar as CalendarIcon,
  ChevronLeft,
  ChevronRight,
  Clock,
  AlertTriangle,
  Filter,
  X,
  Eye,
  Download,
} from "lucide-react";
import { toast, Toaster } from "sonner";
import { getCalendarData } from "../api/calendarApi";
import { getUserProjects } from "../api/projectMembersApi";
import { getSprints } from "../api/sprintApi";
import ExportDropdown from "../components/ExportDropdown";

const PriorityDot = ({ priority }) => {
  const colors = {
    HIGH: "bg-red-500",
    MEDIUM: "bg-yellow-500",
    LOW: "bg-green-500",
  };
  return (
    <div
      className={`w-2 h-2 rounded-full ${colors[priority] || "bg-gray-400"}`}
    />
  );
};

const SprintBar = ({ sprint, onClick }) => {
  const statusColors = {
    PLANNED: "bg-gray-400",
    ACTIVE: "bg-blue-500",
    COMPLETED: "bg-green-500",
  };

  const startDate = new Date(sprint.startDate);
  const endDate = new Date(sprint.endDate);

  return (
    <div
      className={`text-xs px-2 py-1 rounded cursor-pointer hover:opacity-80 ${statusColors[sprint.status] || "bg-gray-400"} text-white truncate`}
      onClick={() => onClick?.(sprint)}
      title={`${sprint.name} - ${sprint.progressPercentage?.toFixed(0) || 0}% complete`}
    >
      {sprint.name}
    </div>
  );
};

const TaskCard = ({ task, onClick }) => (
  <div
    className="text-xs p-1.5 bg-white rounded border border-gray-200 cursor-pointer hover:border-blue-400 transition-colors"
    onClick={() => onClick?.(task)}
  >
    <div className="flex items-center gap-1">
      <PriorityDot priority={task.priority} />
      <span className="truncate font-medium text-gray-800">{task.title}</span>
    </div>
    {task.assigneeName && (
      <p className="text-gray-500 truncate mt-0.5">{task.assigneeName}</p>
    )}
  </div>
);

const OverduePanel = ({ tasks, onTaskClick }) => {
  if (!tasks || tasks.length === 0) return null;

  return (
    <div className="bg-red-50 border border-red-200 rounded-lg p-4">
      <h4 className="text-sm font-semibold text-red-800 mb-3 flex items-center gap-2">
        <AlertTriangle size={16} />
        Overdue Tasks ({tasks.length})
      </h4>
      <div className="space-y-2 max-h-48 overflow-y-auto">
        {tasks.map((task) => (
          <div
            key={task.id}
            onClick={() => onTaskClick?.(task)}
            className="p-2 bg-white rounded border border-red-200 cursor-pointer hover:border-red-400 transition-colors"
          >
            <div className="flex items-center gap-2">
              <PriorityDot priority={task.priority} />
              <span className="text-sm font-medium text-gray-800">
                {task.title}
              </span>
            </div>
            <p className="text-xs text-red-600 mt-1">Due: {task.dueDate}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

const CalendarPage = () => {
  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState("");
  const [calendarData, setCalendarData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [currentDate, setCurrentDate] = useState(new Date());
  const [viewMode, setViewMode] = useState("month");
  // 'month' or 'week'

  const [filters, setFilters] = useState({
    sprintId: "",
    assignee: "",
    status: "",
    priority: "",
  });
  const [showFilters, setShowFilters] = useState(false);
  const [sprints, setSprints] = useState([]);

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
    loadCalendarData();
    loadSprints();
  }, [selectedProjectId]);

  const loadCalendarData = async () => {
    setLoading(true);
    try {
      const data = await getCalendarData(selectedProjectId);
      setCalendarData(data);
    } catch (error) {
      toast.error("Failed to load calendar data");
      setCalendarData(null);
    } finally {
      setLoading(false);
    }
  };

  const loadSprints = async () => {
    try {
      const data = await getSprints(selectedProjectId);
      setSprints(data || []);
    } catch (error) {
      console.error("Failed to load sprints");
    }
  };

  const calendarDays = useMemo(() => {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const days = [];

    // Add empty cells for days before the first of the month

    for (let i = 0; i < firstDay.getDay(); i++) {
      days.push(null);
    }

    // Add all days of the month

    for (let d = 1; d <= lastDay.getDate(); d++) {
      days.push(new Date(year, month, d));
    }

    return days;
  }, [currentDate]);

  const weekDays = useMemo(() => {
    if (viewMode !== "week") return [];
    const startOfWeek = new Date(currentDate);
    const day = startOfWeek.getDay();
    const diff = startOfWeek.getDate() - day + (day === 0 ? -6 : 1);
    // Adjust when day is Sunday
    startOfWeek.setDate(diff);

    const days = [];
    for (let i = 0; i < 7; i++) {
      const date = new Date(startOfWeek);
      date.setDate(date.getDate() + i);
      days.push(date);
    }
    return days;
  }, [currentDate, viewMode]);

  const getTasksForDate = (date) => {
    if (!calendarData?.tasks || !date) return [];
    const dateStr = date.toISOString().split("T")[0];
    return calendarData.tasks.filter((task) => {
      if (task.dueDate !== dateStr) return false;

      // Apply filters

      if (filters.sprintId && String(task.sprintId) !== filters.sprintId)
        return false;
      if (filters.assignee && task.assigneeName !== filters.assignee)
        return false;
      if (filters.status && task.status !== filters.status) return false;
      if (filters.priority && task.priority !== filters.priority) return false;

      return true;
    });
  };

  const getSprintsForDate = (date) => {
    if (!calendarData?.sprints || !date) return [];
    const dateStr = date.toISOString().split("T")[0];
    return calendarData.sprints.filter((sprint) => {
      return dateStr >= sprint.startDate && dateStr <= sprint.endDate;
    });
  };

  const isToday = (date) => {
    const today = new Date();
    return date?.toDateString() === today.toDateString();
  };

  const navigateMonth = (direction) => {
    setCurrentDate((prev) => {
      const newDate = new Date(prev);
      newDate.setMonth(prev.getMonth() + direction);
      return newDate;
    });
  };

  const navigateWeek = (direction) => {
    setCurrentDate((prev) => {
      const newDate = new Date(prev);
      newDate.setDate(prev.getDate() + direction * 7);
      return newDate;
    });
  };

  const goToToday = () => setCurrentDate(new Date());

  const overdueTasks = useMemo(() => {
    if (!calendarData?.tasks) return [];
    const today = new Date().toISOString().split("T")[0];
    return calendarData.tasks.filter((t) => t.dueDate && t.dueDate < today);
  }, [calendarData]);

  const uniqueAssignees = useMemo(() => {
    if (!calendarData?.tasks) return [];
    return [
      ...new Set(calendarData.tasks.map((t) => t.assigneeName).filter(Boolean)),
    ];
  }, [calendarData]);

  const uniqueStatuses = useMemo(() => {
    if (!calendarData?.tasks) return [];
    return [
      ...new Set(calendarData.tasks.map((t) => t.status).filter(Boolean)),
    ];
  }, [calendarData]);

  if (loading) {
    return (
      <>
        <Toaster position="top-right" />
        <div className="p-6 max-w-7xl mx-auto">
          <div className="text-center py-12">
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
            <p className="mt-2 text-gray-600">Loading calendar...</p>
          </div>
        </div>
      </>
    );
  }

  const dayNames = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

  return (
    <>
      <Toaster position="top-right" />
      <div className="p-6 max-w-7xl mx-auto">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <CalendarIcon size={28} /> Calendar
          </h1>
          <div className="flex items-center gap-3">
            <select
              value={selectedProjectId}
              onChange={(e) => setSelectedProjectId(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
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
                showCalendar={true}
              />
            )}
            <button
              onClick={() => setShowFilters(!showFilters)}
              className={`p-2 rounded-lg border ${showFilters ? "bg-blue-50 border-blue-300" : "border-gray-300 hover:bg-gray-50"}`}
            >
              <Filter size={18} />
            </button>
          </div>
        </div>

        {!calendarData && (
          <div className="text-center py-12 bg-white rounded-xl shadow-sm border border-gray-100">
            <CalendarIcon size={48} className="mx-auto text-gray-300 mb-4" />
            <p className="text-gray-500">Select a project to view calendar</p>
          </div>
        )}

        {calendarData && (
          <div className="space-y-6">
            {/* Overdue Panel */}
            <OverduePanel tasks={overdueTasks} />

            {/* Filters */}
            {showFilters && (
              <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
                <div className="flex items-center justify-between mb-3">
                  <h4 className="text-sm font-semibold text-gray-700">
                    Filters
                  </h4>
                  <button
                    onClick={() =>
                      setFilters({
                        sprintId: "",
                        assignee: "",
                        status: "",
                        priority: "",
                      })
                    }
                  >
                    <X
                      size={16}
                      className="text-gray-400 hover:text-gray-600"
                    />
                  </button>
                </div>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                  <select
                    value={filters.sprintId}
                    onChange={(e) =>
                      setFilters({ ...filters, sprintId: e.target.value })
                    }
                    className="px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  >
                    <option value="">All Sprints</option>
                    {sprints.map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.name}
                      </option>
                    ))}
                  </select>
                  <select
                    value={filters.assignee}
                    onChange={(e) =>
                      setFilters({ ...filters, assignee: e.target.value })
                    }
                    className="px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  >
                    <option value="">All Assignees</option>
                    {uniqueAssignees.map((a) => (
                      <option key={a} value={a}>
                        {a}
                      </option>
                    ))}
                  </select>
                  <select
                    value={filters.status}
                    onChange={(e) =>
                      setFilters({ ...filters, status: e.target.value })
                    }
                    className="px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  >
                    <option value="">All Statuses</option>
                    {uniqueStatuses.map((s) => (
                      <option key={s} value={s}>
                        {s}
                      </option>
                    ))}
                  </select>
                  <select
                    value={filters.priority}
                    onChange={(e) =>
                      setFilters({ ...filters, priority: e.target.value })
                    }
                    className="px-3 py-2 border border-gray-300 rounded-lg text-sm"
                  >
                    <option value="">All Priorities</option>
                    <option value="HIGH">High</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="LOW">Low</option>
                  </select>
                </div>
              </div>
            )}

            {/* Calendar Header */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center gap-3">
                  <button
                    onClick={() =>
                      viewMode === "month"
                        ? navigateMonth(-1)
                        : navigateWeek(-1)
                    }
                    className="p-2 hover:bg-gray-100 rounded-lg"
                  >
                    <ChevronLeft size={20} />
                  </button>
                  <h2 className="text-lg font-semibold text-gray-800 min-w-[200px] text-center">
                    {viewMode === "month"
                      ? currentDate.toLocaleString("default", {
                          month: "long",
                          year: "numeric",
                        })
                      : `Week of ${weekDays[0]?.toLocaleDateString() || ""}`}
                  </h2>
                  <button
                    onClick={() =>
                      viewMode === "month" ? navigateMonth(1) : navigateWeek(1)
                    }
                    className="p-2 hover:bg-gray-100 rounded-lg"
                  >
                    <ChevronRight size={20} />
                  </button>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={goToToday}
                    className="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 flex items-center gap-1"
                  >
                    <Clock size={14} /> Today
                  </button>
                  <div className="flex border border-gray-300 rounded-lg overflow-hidden">
                    <button
                      onClick={() => setViewMode("month")}
                      className={`px-3 py-1.5 text-sm ${viewMode === "month" ? "bg-blue-500 text-white" : "bg-white hover:bg-gray-50"}`}
                    >
                      Month
                    </button>
                    <button
                      onClick={() => setViewMode("week")}
                      className={`px-3 py-1.5 text-sm ${viewMode === "week" ? "bg-blue-500 text-white" : "bg-white hover:bg-gray-50"}`}
                    >
                      Week
                    </button>
                  </div>
                </div>
              </div>

              {/* Calendar Grid */}
              {viewMode === "month" ? (
                <>
                  {/* Day headers */}
                  <div className="grid grid-cols-7 gap-1 mb-1">
                    {dayNames.map((day) => (
                      <div
                        key={day}
                        className="text-center text-xs font-medium text-gray-500 py-2"
                      >
                        {day}
                      </div>
                    ))}
                  </div>

                  {/* Calendar days */}
                  <div className="grid grid-cols-7 gap-1">
                    {calendarDays.map((day, index) => (
                      <div
                        key={index}
                        className={`min-h-[100px] p-1 border rounded-lg ${
                          !day
                            ? "bg-gray-50"
                            : isToday(day)
                              ? "bg-blue-50 border-blue-300"
                              : "bg-white border-gray-200"
                        }`}
                      >
                        {day && (
                          <>
                            <div
                              className={`text-xs font-medium mb-1 ${isToday(day) ? "text-blue-600" : "text-gray-700"}`}
                            >
                              {day.getDate()}
                            </div>
                            <div className="space-y-1">
                              {getSprintsForDate(day).map((sprint) => (
                                <SprintBar key={sprint.id} sprint={sprint} />
                              ))}
                              {getTasksForDate(day)
                                .slice(0, 3)
                                .map((task) => (
                                  <TaskCard key={task.id} task={task} />
                                ))}
                              {getTasksForDate(day).length > 3 && (
                                <p className="text-xs text-gray-500">
                                  +{getTasksForDate(day).length - 3} more
                                </p>
                              )}
                            </div>
                          </>
                        )}
                      </div>
                    ))}
                  </div>
                </>
              ) : (
                /* Week view */
                <div className="space-y-2">
                  {weekDays.map((day, index) => (
                    <div
                      key={index}
                      className={`border rounded-lg p-3 ${isToday(day) ? "bg-blue-50 border-blue-300" : "bg-white border-gray-200"}`}
                    >
                      <div
                        className={`text-sm font-medium mb-2 ${isToday(day) ? "text-blue-600" : "text-gray-700"}`}
                      >
                        {day.toLocaleDateString("default", {
                          weekday: "short",
                          month: "short",
                          day: "numeric",
                        })}
                      </div>
                      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-2">
                        {getSprintsForDate(day).map((sprint) => (
                          <SprintBar key={sprint.id} sprint={sprint} />
                        ))}
                        {getTasksForDate(day).map((task) => (
                          <TaskCard key={task.id} task={task} />
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Legend */}
            <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
              <h4 className="text-sm font-semibold text-gray-700 mb-3">
                Legend
              </h4>
              <div className="flex flex-wrap gap-4">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded-full bg-red-500" />
                  <span className="text-xs text-gray-600">High Priority</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded-full bg-yellow-500" />
                  <span className="text-xs text-gray-600">Medium Priority</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded-full bg-green-500" />
                  <span className="text-xs text-gray-600">Low Priority</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded bg-gray-400" />
                  <span className="text-xs text-gray-600">Planned Sprint</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded bg-blue-500" />
                  <span className="text-xs text-gray-600">Active Sprint</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded bg-green-500" />
                  <span className="text-xs text-gray-600">
                    Completed Sprint
                  </span>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </>
  );
};

export default CalendarPage;
