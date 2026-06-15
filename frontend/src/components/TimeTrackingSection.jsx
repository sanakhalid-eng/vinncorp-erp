import { useState, useEffect, useRef } from "react";
import { Timer, Play, Square, Trash2, Clock } from "lucide-react";
import {
  logTime,
  getTaskTimeLogs,
  deleteTimeLog,
  startTimer,
  stopTimer,
  getActiveTimer,
} from "../api/timeTrackingApi";
import { toast } from "sonner";

const IDLE_TIMEOUT = 5 * 60 * 1000;
// 5 minutes

const TimeTrackingSection = ({ taskId, userId }) => {
  const [timeLogs, setTimeLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [hours, setHours] = useState("");
  const [description, setDescription] = useState("");
  const [logDate, setLogDate] = useState(
    new Date().toISOString().split("T")[0],
  );

  // Time range logging
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");
  const [useTimeRange, setUseTimeRange] = useState(false);

  // Timer mode state
  const [isTimerRunning, setIsTimerRunning] = useState(false);
  const [timerStart, setTimerStart] = useState(null);
  const [timerDescription, setTimerDescription] = useState("");
  const [elapsedTime, setElapsedTime] = useState(0);
  const timerInterval = useRef(null);
  const idleTimer = useRef(null);
  const lastActivity = useRef(Date.now());
  const [showIdleModal, setShowIdleModal] = useState(false);
  const [idleStartTime, setIdleStartTime] = useState(null);

  const loadTimeLogs = async () => {
    try {
      const data = await getTaskTimeLogs(taskId);
      setTimeLogs(data || []);
    } catch (error) {
      console.error("Failed to load time logs");
    }
  };

  useEffect(() => {
    if (taskId) {
      loadTimeLogs();
    }
  }, [taskId]);

  const handleLogTime = async (e) => {
    e.preventDefault();

    // Validate based on logging mode
    if (useTimeRange) {
      if (!startTime || !endTime) {
        toast.error("Please enter both start and end time");
        return;
      }
    } else {
      if (!hours || parseFloat(hours) <= 0) {
        toast.error("Please enter valid hours");
        return;
      }
    }

    setLoading(true);
    try {
      const logData = {
        description,
        logDate,
      };

      if (useTimeRange) {
        logData.startTime = startTime;
        logData.endTime = endTime;
      } else {
        logData.hours = parseFloat(hours);
      }

      await logTime(taskId, userId, logData);
      toast.success("Time logged successfully");
      setHours("");
      setDescription("");
      setStartTime("");
      setEndTime("");
      setShowForm(false);
      loadTimeLogs();
    } catch (error) {
      toast.error("Failed to log time");
    } finally {
      setLoading(false);
    }
  };

  const handleLogSameAsYesterday = async () => {
    if (timeLogs.length === 0) {
      toast.error("No previous time logs found");
      return;
    }

    // Find yesterday's log
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    const yesterdayStr = yesterday.toISOString().split("T")[0];

    const yesterdayLog = timeLogs.find((log) => log.logDate === yesterdayStr);
    if (!yesterdayLog) {
      toast.error("No time log found for yesterday");
      return;
    }

    setHours(yesterdayLog.hours?.toString() || "");
    setDescription(yesterdayLog.description || "");
    setLogDate(yesterdayStr);
    toast.info("Loaded yesterday's time log data");
  };

  const handleDeleteLog = async (logId) => {
    if (!confirm("Delete this time log?")) return;
    try {
      await deleteTimeLog(logId, userId);
      toast.success("Time log deleted");
      loadTimeLogs();
    } catch (error) {
      toast.error("Failed to delete time log");
    }
  };

  // Timer mode functions
  const checkActiveTimer = async () => {
    try {
      const activeTimer = await getActiveTimer(userId);
      if (activeTimer && activeTimer.task?.id === taskId) {
        setIsTimerRunning(true);
        setTimerStart(new Date(activeTimer.startedAt));
        setTimerDescription(activeTimer.description || "");
        startTimerInterval(new Date(activeTimer.startedAt));
      }
    } catch (error) {
      // No active timer
    }
  };

  const startTimerInterval = (startTime) => {
    if (timerInterval.current) clearInterval(timerInterval.current);
    timerInterval.current = setInterval(() => {
      const elapsed = (Date.now() - startTime.getTime()) / 1000 / 3600;
      // hours
      setElapsedTime(elapsed);
    }, 1000);
  };

  const handleStartTimer = async () => {
    try {
      setLoading(true);
      await startTimer(taskId, userId, timerDescription || null);
      const now = new Date();
      setIsTimerRunning(true);
      setTimerStart(now);
      startTimerInterval(now);
      toast.success("Timer started");
    } catch (error) {
      toast.error(
        "Failed to start timer - you may have an active timer already",
      );
    } finally {
      setLoading(false);
    }
  };

  const handleStopTimer = async (isIdleStop = false) => {
    try {
      setLoading(true);
      const timeLog = await stopTimer(userId, timerDescription || null);
      setIsTimerRunning(false);
      setTimerStart(null);
      setElapsedTime(0);
      setTimerDescription("");
      if (timerInterval.current) clearInterval(timerInterval.current);
      if (idleTimer.current) clearTimeout(idleTimer.current);

      if (isIdleStop) {
        toast.info(
          `Timer auto-stopped due to inactivity. Logged ${timeLog.hours?.toFixed(2)} hours`,
        );
      } else {
        toast.success(
          `Timer stopped. Logged ${timeLog.hours?.toFixed(2)} hours`,
        );
      }
      loadTimeLogs();
    } catch (error) {
      toast.error("Failed to stop timer");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkActiveTimer();
    return () => {
      if (timerInterval.current) clearInterval(timerInterval.current);
      if (idleTimer.current) clearTimeout(idleTimer.current);
    };
  }, [taskId]);

  // Idle detection
  const resetIdleTimer = () => {
    lastActivity.current = Date.now();
    if (idleTimer.current) {
      clearTimeout(idleTimer.current);
    }
    if (isTimerRunning) {
      idleTimer.current = setTimeout(() => {
        setIdleStartTime(Date.now());
        setShowIdleModal(true);

        // Auto-pause timer
        handleStopTimer(true);
        // true indicates idle stop
      }, IDLE_TIMEOUT);
    }
  };

  const handleActivity = () => {
    lastActivity.current = Date.now();
    if (showIdleModal) {
      // User is back, resume timer if they want
      setShowIdleModal(false);
      if (idleStartTime) {
        const idleDuration = (Date.now() - idleStartTime) / 1000 / 3600;
        // hours
        setElapsedTime((prev) => prev + idleDuration);
      }
    }
  };

  useEffect(() => {
    if (isTimerRunning) {
      // Add event listeners for user activity
      window.addEventListener("mousemove", handleActivity);
      window.addEventListener("keydown", handleActivity);
      resetIdleTimer();

      return () => {
        window.removeEventListener("mousemove", handleActivity);
        window.removeEventListener("keydown", handleActivity);
        if (idleTimer.current) clearTimeout(idleTimer.current);
      };
    }
  }, [isTimerRunning]);

  const totalHours = timeLogs.reduce((sum, log) => sum + (log.hours || 0), 0);

  const formatElapsedTime = (hours) => {
    const totalSeconds = Math.floor(hours * 3600);
    const h = Math.floor(totalSeconds / 3600);
    const m = Math.floor((totalSeconds % 3600) / 60);
    const s = totalSeconds % 60;
    return `${h.toString().padStart(2, "0")}:${m.toString().padStart(2, "0")}:${s.toString().padStart(2, "0")}`;
  };

  return (
    <div className="border-t pt-6">
      <div className="mb-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Timer className="h-5 w-5 text-gray-500" />
          <h4 className="text-sm font-semibold text-gray-700">Time Tracking</h4>
          {totalHours > 0 && (
            <span className="text-xs bg-blue-100 text-blue-800 px-2 py-0.5 rounded-full">
              {totalHours.toFixed(1)}h total
            </span>
          )}
        </div>
        <button
          onClick={() => setShowForm(!showForm)}
          className="text-xs text-blue-600 hover:text-blue-800"
        >
          {showForm ? "Cancel" : "+ Log Time"}
        </button>
      </div>

      {/* Timer Mode */}
      <div className="mb-4 p-3 bg-gray-50 rounded-lg">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs font-medium text-gray-700">
            {isTimerRunning ? "Timer Running" : "Quick Timer"}
          </span>
          {isTimerRunning && (
            <span className="text-lg font-mono font-bold text-blue-600">
              {formatElapsedTime(elapsedTime)}
            </span>
          )}
        </div>
        {isTimerRunning ? (
          <div className="space-y-2">
            <input
              type="text"
              value={timerDescription}
              onChange={(e) => setTimerDescription(e.target.value)}
              placeholder="What are you working on?"
              className="w-full px-2 py-1 text-xs border border-gray-300 rounded"
            />
            <button
              onClick={handleStopTimer}
              disabled={loading}
              className="w-full py-1.5 text-xs bg-red-600 text-white rounded hover:bg-red-700 disabled:opacity-50 flex items-center justify-center gap-1"
            >
              <Square size={12} />
              {loading ? "Stopping..." : "Stop Timer"}
            </button>
          </div>
        ) : (
          <div className="space-y-2">
            <input
              type="text"
              value={timerDescription}
              onChange={(e) => setTimerDescription(e.target.value)}
              placeholder="What will you work on?"
              className="w-full px-2 py-1 text-xs border border-gray-300 rounded"
            />
            <button
              onClick={handleStartTimer}
              disabled={loading}
              className="w-full py-1.5 text-xs bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-50 flex items-center justify-center gap-1"
            >
              <Play size={12} />
              {loading ? "Starting..." : "Start Timer"}
            </button>
          </div>
        )}
      </div>

      {showForm && (
        <form
          onSubmit={handleLogTime}
          className="mb-4 p-3 bg-gray-50 rounded-lg space-y-3"
        >
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-medium text-gray-700">Log Time</span>
            <button
              type="button"
              onClick={() => setUseTimeRange(!useTimeRange)}
              className="text-xs text-blue-600 hover:text-blue-800"
            >
              {useTimeRange ? "Use Hours" : "Use Time Range"}
            </button>
          </div>

          {useTimeRange ? (
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-xs text-gray-600">Start Time</label>
                <input
                  type="time"
                  value={startTime}
                  onChange={(e) => setStartTime(e.target.value)}
                  className="w-full px-2 py-1 text-sm border border-gray-300 rounded"
                  required
                />
              </div>
              <div>
                <label className="text-xs text-gray-600">End Time</label>
                <input
                  type="time"
                  value={endTime}
                  onChange={(e) => setEndTime(e.target.value)}
                  className="w-full px-2 py-1 text-sm border border-gray-300 rounded"
                  required
                />
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="text-xs text-gray-600">Hours</label>
                <input
                  type="number"
                  step="0.25"
                  min="0.25"
                  max="24"
                  value={hours}
                  onChange={(e) => setHours(e.target.value)}
                  className="w-full px-2 py-1 text-sm border border-gray-300 rounded"
                  placeholder="1.5"
                  required
                />
              </div>
              <div>
                <label className="text-xs text-gray-600">Date</label>
                <input
                  type="date"
                  value={logDate}
                  onChange={(e) => setLogDate(e.target.value)}
                  className="w-full px-2 py-1 text-sm border border-gray-300 rounded"
                  required
                />
              </div>
            </div>
          )}

          <div>
            <label className="text-xs text-gray-600">Description</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-2 py-1 text-sm border border-gray-300 rounded"
              rows="2"
              placeholder="What did you work on?"
            />
          </div>

          <div className="flex gap-2">
            <button
              type="button"
              onClick={handleLogSameAsYesterday}
              disabled={loading}
              className="flex-1 py-1.5 text-xs bg-gray-200 text-gray-700 rounded hover:bg-gray-300 disabled:opacity-50"
            >
              <Clock size={12} className="inline mr-1" />
              Same as Yesterday
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 py-1.5 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
            >
              {loading ? "Logging..." : "Log Time"}
            </button>
          </div>
        </form>
      )}

      {timeLogs.length === 0 ? (
        <p className="text-xs text-gray-500 text-center py-2">
          No time logged yet
        </p>
      ) : (
        <div className="space-y-2 max-h-48 overflow-y-auto">
          {timeLogs.map((log) => (
            <div key={log.id} className="p-2 bg-gray-50 rounded text-xs">
              <div className="flex items-center justify-between">
                <span className="font-medium text-gray-700">{log.hours}h</span>
                <button
                  onClick={() => handleDeleteLog(log.id)}
                  className="text-red-500 hover:text-red-700"
                >
                  <Trash2 size={12} />
                </button>
              </div>
              <p className="text-gray-500">{log.logDate}</p>
              {log.description && (
                <p className="text-gray-600 mt-1">{log.description}</p>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Idle Modal */}
      {showIdleModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded-xl max-w-md w-full mx-4">
            <h3 className="text-lg font-semibold mb-4">Timer Auto-Stopped</h3>
            <p className="text-gray-600 mb-4">
              You were idle for 5 minutes. The timer has been stopped.
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => {
                  setShowIdleModal(false);

                  // Option to resume timer with accumulated time
                  if (timerStart && idleStartTime) {
                    const idleDuration =
                      (Date.now() - idleStartTime) / 1000 / 3600;
                    setElapsedTime((prev) => prev + idleDuration);
                    setShowIdleModal(false);
                  }
                }}
                className="flex-1 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
              >
                Resume
              </button>
              <button
                onClick={() => {
                  setShowIdleModal(false);
                  setIdleStartTime(null);
                }}
                className="flex-1 py-2 bg-gray-200 text-gray-700 rounded hover:bg-gray-300"
              >
                Discard Time
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default TimeTrackingSection;
