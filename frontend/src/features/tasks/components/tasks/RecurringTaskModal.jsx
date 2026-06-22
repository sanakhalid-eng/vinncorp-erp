import { useState, useEffect } from "react";
import { X, Repeat, Loader2 } from "lucide-react";
import { cn } from "../../../../utils/cn";
import {
  createRecurring,
  updateRecurring,
  pauseRecurring,
  resumeRecurring,
  stopRecurring,
} from "../../api/recurringApi";
const RECURRENCE_TYPES = [
  { value: "DAILY", label: "Daily", desc: "Repeats every N days" },
  { value: "WEEKLY", label: "Weekly", desc: "Repeats on selected days" },
  { value: "MONTHLY", label: "Monthly", desc: "Repeats on a specific day" },
  { value: "CUSTOM", label: "Custom", desc: "Custom interval in days" },
];
const WEEKDAYS = [
  { value: "MONDAY", label: "Mon" },
  { value: "TUESDAY", label: "Tue" },
  { value: "WEDNESDAY", label: "Wed" },
  { value: "THURSDAY", label: "Thu" },
  { value: "FRIDAY", label: "Fri" },
  { value: "SATURDAY", label: "Sat" },
  { value: "SUNDAY", label: "Sun" },
];
export default function RecurringTaskModal({
  taskId,
  task,
  template,
  onClose,
  onUpdate,
}) {
  const [recurrenceType, setRecurrenceType] = useState(
    template?.recurrenceType || "DAILY",
  );
  const [intervalValue, setIntervalValue] = useState(
    template?.intervalValue || 1,
  );
  const [daysOfWeek, setDaysOfWeek] = useState(template?.daysOfWeek || []);
  const [dayOfMonth, setDayOfMonth] = useState(template?.dayOfMonth || 1);
  const [startDate, setStartDate] = useState("");
  const [endsAt, setEndsAt] = useState(
    template?.endsAt ? template.endsAt.slice(0, 16) : "",
  );
  const [maxOccurrences, setMaxOccurrences] = useState(
    template?.maxOccurrences || "",
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const isEditing = !!template;
  useEffect(() => {
    if (!template && task?.createdAt) {
      const d = new Date(task.createdAt);
      d.setDate(d.getDate() + 1);
      setStartDate(d.toISOString().slice(0, 16));
    }
    if (template?.nextRunAt) {
      setStartDate(template.nextRunAt.slice(0, 16));
    }
  }, [template, task]);
  const toggleDay = (day) => {
    setDaysOfWeek((prev) =>
      prev.includes(day) ? prev.filter((d) => d !== day) : [...prev, day],
    );
  };
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const payload = {
        recurrenceType,
        intervalValue,
        startDate: new Date(startDate).toISOString(),
        ...(endsAt && { endsAt: new Date(endsAt).toISOString() }),
        ...(maxOccurrences && { maxOccurrences: Number(maxOccurrences) }),
      };
      if (recurrenceType === "WEEKLY") {
        payload.daysOfWeek = daysOfWeek;
      }
      if (recurrenceType === "MONTHLY") {
        payload.dayOfMonth = dayOfMonth;
      }
      if (isEditing) {
        await updateRecurring(template.id, payload);
      } else {
        await createRecurring(taskId, payload);
      }
      onUpdate?.();
      onClose();
    } catch (err) {
      setError(
        err.response?.data?.message || "Failed to save recurring schedule",
      );
    } finally {
      setLoading(false);
    }
  };
  const handlePauseResume = async () => {
    try {
      if (template.paused) {
        await resumeRecurring(template.id);
      } else {
        await pauseRecurring(template.id);
      }
      onUpdate?.();
    } catch (err) {
      setError(
        err.response?.data?.message || "Failed to update recurring schedule",
      );
    }
  };
  const handleStop = async () => {
    if (
      !confirm(
        "Stop this recurring schedule? Future tasks will NOT be generated.",
      )
    )
      return;
    try {
      await stopRecurring(template.id);
      onUpdate?.();
      onClose();
    } catch (err) {
      setError(
        err.response?.data?.message || "Failed to stop recurring schedule",
      );
    }
  };
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
       
      <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto">
         
        <div className="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700">
           
          <div className="flex items-center gap-2">
             
            <Repeat className="h-5 w-5 text-indigo-600" /> 
            <h2 className="text-lg font-bold text-gray-900 dark:text-white">
               
              {isEditing
                ? "Edit Recurring Schedule"
                : "Create Recurring Schedule"} 
            </h2> 
          </div> 
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-xl text-gray-500"
          >
             
            <X className="h-5 w-5" /> 
          </button> 
        </div> 
        <form onSubmit={handleSubmit} className="p-6 space-y-5">
           
          {error && (
            <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl p-3 text-sm text-red-700 dark:text-red-400">
               
              {error} 
            </div>
          )} 
          {isEditing && (
            <div className="flex gap-2">
               
              <button
                type="button"
                onClick={handlePauseResume}
                className={cn(
                  "flex-1 px-4 py-2 rounded-xl text-sm font-semibold transition-colors",
                  template.paused
                    ? "bg-green-100 text-green-700 hover:bg-green-200 dark:bg-green-900/30 dark:text-green-400"
                    : "bg-yellow-100 text-yellow-700 hover:bg-yellow-200 dark:bg-yellow-900/30 dark:text-yellow-400",
                )}
              >
                 
                {template.paused ? "Resume" : "Pause"} 
              </button> 
              <button
                type="button"
                onClick={handleStop}
                className="flex-1 px-4 py-2 rounded-xl text-sm font-semibold bg-red-100 text-red-700 hover:bg-red-200 dark:bg-red-900/30 dark:text-red-400 transition-colors"
              >
                 
                Stop 
              </button> 
            </div>
          )} 
          <div>
             
            <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">
               
              Recurrence Type 
            </label> 
            <div className="grid grid-cols-2 gap-2">
               
              {RECURRENCE_TYPES.map((type) => (
                <button
                  key={type.value}
                  type="button"
                  onClick={() => setRecurrenceType(type.value)}
                  className={cn(
                    "p-3 rounded-xl border-2 text-left transition-all",
                    recurrenceType === type.value
                      ? "border-indigo-500 bg-indigo-50 dark:bg-indigo-900/20 dark:border-indigo-400"
                      : "border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600",
                  )}
                >
                   
                  <div className="font-semibold text-gray-900 dark:text-white text-sm">
                    {type.label}
                  </div> 
                  <div className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                    {type.desc}
                  </div> 
                </button>
              ))} 
            </div> 
          </div> 
          <div className="flex gap-4">
             
            <div className="flex-1">
               
              <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-1">
                 
                Every 
              </label> 
              <input
                type="number"
                min="1"
                value={intervalValue}
                onChange={(e) =>
                  setIntervalValue(Math.max(1, parseInt(e.target.value) || 1))
                }
                className="w-full px-3 py-2 rounded-xl border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              /> 
            </div> 
            <div className="flex-1">
               
              <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-1">
                 
                Start Date 
              </label> 
              <input
                type="datetime-local"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                required
                className="w-full px-3 py-2 rounded-xl border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              /> 
            </div> 
          </div> 
          {recurrenceType === "WEEKLY" && (
            <div>
               
              <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">
                 
                Days of Week 
              </label> 
              <div className="flex flex-wrap gap-1.5">
                 
                {WEEKDAYS.map((day) => (
                  <button
                    key={day.value}
                    type="button"
                    onClick={() => toggleDay(day.value)}
                    className={cn(
                      "px-3 py-1.5 rounded-lg text-xs font-semibold transition-all border",
                      daysOfWeek.includes(day.value)
                        ? "bg-indigo-600 text-white border-indigo-600"
                        : "bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400 border-gray-200 dark:border-gray-700 hover:border-indigo-300",
                    )}
                  >
                     
                    {day.label} 
                  </button>
                ))} 
              </div> 
            </div>
          )} 
          {recurrenceType === "MONTHLY" && (
            <div>
               
              <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-1">
                 
                Day of Month 
              </label> 
              <input
                type="number"
                min="1"
                max="31"
                value={dayOfMonth}
                onChange={(e) =>
                  setDayOfMonth(
                    Math.min(31, Math.max(1, parseInt(e.target.value) || 1)),
                  )
                }
                className="w-full px-3 py-2 rounded-xl border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              /> 
            </div>
          )} 
          <div className="flex gap-4">
             
            <div className="flex-1">
               
              <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-1">
                 
                Ends At (optional) 
              </label> 
              <input
                type="datetime-local"
                value={endsAt}
                onChange={(e) => setEndsAt(e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              /> 
            </div> 
            <div className="flex-1">
               
              <label className="block text-sm font-semibold text-gray-700 dark:text-gray-300 mb-1">
                 
                Max Occurrences 
              </label> 
              <input
                type="number"
                min="1"
                value={maxOccurrences}
                onChange={(e) => setMaxOccurrences(e.target.value)}
                placeholder="Unlimited"
                className="w-full px-3 py-2 rounded-xl border border-gray-300 dark:border-gray-600 dark:bg-gray-800 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              /> 
            </div> 
          </div> 
          <div className="flex gap-3 pt-2">
             
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2.5 rounded-xl border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 font-semibold text-sm hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors"
            >
               
              Cancel 
            </button> 
            <button
              type="submit"
              disabled={loading}
              className="flex-1 px-4 py-2.5 rounded-xl bg-indigo-600 text-white font-semibold text-sm hover:bg-indigo-700 disabled:opacity-50 transition-colors flex items-center justify-center gap-2"
            >
               
              {loading && <Loader2 className="h-4 w-4 animate-spin" />} 
              {isEditing ? "Update Schedule" : "Create Schedule"} 
            </button> 
          </div> 
        </form> 
      </div> 
    </div>
  );
}
