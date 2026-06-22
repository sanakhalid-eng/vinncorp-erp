import { useState, useEffect } from "react";
import { X, Calendar, CheckCircle, Clock, AlertCircle } from "lucide-react";
import { cn } from "../../../../utils/cn";
import { getOccurrences } from "../../api/recurringApi";
const STATUS_ICONS = {
  GENERATED: CheckCircle,
  SKIPPED: AlertCircle,
  PENDING: Clock,
};
const STATUS_COLORS = {
  GENERATED: "text-green-600 dark:text-green-400",
  SKIPPED: "text-yellow-600 dark:text-yellow-400",
  PENDING: "text-gray-500 dark:text-gray-400",
};
export default function RecurrenceHistory({ templateId, onClose }) {
  const [occurrences, setOccurrences] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  useEffect(() => {
    loadOccurrences();
  }, [templateId]);
  const loadOccurrences = async () => {
    setLoading(true);
    try {
      const data = await getOccurrences(templateId);
      setOccurrences(data);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to load occurrences");
    } finally {
      setLoading(false);
    }
  };
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
       
      <div className="bg-white dark:bg-gray-900 rounded-2xl shadow-2xl w-full max-w-lg mx-4 max-h-[90vh] overflow-y-auto">
         
        <div className="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700">
           
          <div className="flex items-center gap-2">
             
            <Calendar className="h-5 w-5 text-indigo-600" /> 
            <h2 className="text-lg font-bold text-gray-900 dark:text-white">
               
              Occurrence History 
            </h2> 
          </div> 
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-xl text-gray-500"
          >
             
            <X className="h-5 w-5" /> 
          </button> 
        </div> 
        <div className="p-6">
           
          {loading && (
            <div className="flex items-center justify-center py-12">
               
              <Clock className="h-6 w-6 animate-spin text-indigo-600" /> 
            </div>
          )} 
          {error && (
            <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl p-3 text-sm text-red-700 dark:text-red-400">
               
              {error} 
            </div>
          )} 
          {!loading && !error && occurrences.length === 0 && (
            <div className="text-center py-12 text-gray-500 dark:text-gray-400">
               
              <Calendar className="h-12 w-12 mx-auto mb-3 opacity-40" /> 
              <p className="font-semibold">No occurrences yet</p> 
              <p className="text-sm">
                Tasks will be generated based on the schedule.
              </p> 
            </div>
          )} 
          {!loading && occurrences.length > 0 && (
            <div className="space-y-2">
               
              {occurrences.map((occ) => {
                const StatusIcon =
                  STATUS_ICONS[occ.generationStatus] || CheckCircle;
                return (
                  <div
                    key={occ.id}
                    className="flex items-center gap-3 p-3 rounded-xl bg-gray-50 dark:bg-gray-800/50 border border-gray-100 dark:border-gray-700"
                  >
                     
                    <StatusIcon
                      className={cn(
                        "h-5 w-5 flex-shrink-0",
                        STATUS_COLORS[occ.generationStatus] ||
                          STATUS_COLORS.GENERATED,
                      )}
                    /> 
                    <div className="flex-1 min-w-0">
                       
                      <div className="text-sm font-semibold text-gray-900 dark:text-white truncate">
                         
                        {occ.generatedTaskTitle ||
                          `Task #${occ.generatedTaskId}`} 
                      </div> 
                      <div className="text-xs text-gray-500 dark:text-gray-400">
                         
                        {occ.occurrenceDate
                          ? new Date(
                              occ.occurrenceDate + "T00:00:00",
                            ).toLocaleDateString("en-US", {
                              weekday: "short",
                              year: "numeric",
                              month: "short",
                              day: "numeric",
                            })
                          : "Unknown date"} 
                      </div> 
                    </div> 
                    <span
                      className={cn(
                        "text-xs font-semibold px-2 py-0.5 rounded-full",
                        occ.generationStatus === "GENERATED"
                          ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400"
                          : "bg-gray-100 text-gray-600 dark:bg-gray-700 dark:text-gray-300",
                      )}
                    >
                       
                      {occ.generationStatus} 
                    </span> 
                  </div>
                );
              })} 
            </div>
          )} 
        </div> 
      </div> 
    </div>
  );
}
