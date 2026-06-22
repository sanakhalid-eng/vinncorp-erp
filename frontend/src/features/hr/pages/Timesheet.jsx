import { useEffect, useState, useMemo } from "react";
import {
  Clock,
  Calendar,
  ChevronLeft,
  ChevronRight,
  Download,
} from "lucide-react";
import { toast, Toaster } from "sonner";
import { getUserTimesheet } from "../../../api/timeTrackingApi";
import ExportDropdown from "../../analytics/components/ExportDropdown";
const Timesheet = () => {
  const [timesheet, setTimesheet] = useState([]);
  const [loading, setLoading] = useState(true);
  const [range, setRange] = useState("weekly");
  const [currentDate, setCurrentDate] = useState(new Date());
  const dateRange = useMemo(() => {
    const endDate = new Date(currentDate);
    let startDate = new Date(currentDate);
    if (range === "monthly") {
      startDate.setMonth(startDate.getMonth() - 1);
    } else {
      startDate.setDate(startDate.getDate() - 7);
    }
    return { startDate, endDate };
  }, [currentDate, range]);
  const loadTimesheet = async () => {
    setLoading(true);
    try {
      const userId = JSON.parse(localStorage.getItem("user") || "{}").id;
      if (!userId) {
        toast.error("User not found");
        return;
      }
      const data = await getUserTimesheet(userId, range);
      setTimesheet(data || []);
    } catch (error) {
      toast.error("Failed to load timesheet");
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {
    loadTimesheet();
  }, [currentDate, range]);
  const navigateDate = (direction) => {
    setCurrentDate((prev) => {
      const newDate = new Date(prev);
      if (range === "monthly") {
        newDate.setMonth(newDate.getMonth() + direction);
      } else {
        newDate.setDate(newDate.getDate() + direction * 7);
      }
      return newDate;
    });
  };
  const totalHours = useMemo(() => {
    return timesheet.reduce((sum, entry) => sum + (entry[1] || 0), 0);
  }, [timesheet]);
  if (loading) {
    return (
      <>
         
        <Toaster position="top-right" /> 
        <div className="page-container max-w-8xl mx-auto">
           
          <div className="flex flex-col items-center justify-center py-12">
             
            <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div> 
            <p className="mt-2 text-surface-600 dark:text-surface-400">
              Loading timesheet...
            </p> 
          </div> 
        </div> 
      </>
    );
  }
  return (
    <>
       
      <Toaster position="top-right" /> 
      <div className="page-container max-w-8xl mx-auto">
         
        <div className="page-header">
           
          <h1 className="page-title flex items-center gap-2">
             
            <Clock size={28} /> Timesheet 
          </h1> 
          <ExportDropdown
            showTimesheet={true}
            userId={JSON.parse(localStorage.getItem("user") || "{}").id}
          /> 
        </div> 
        <div className="bg-white dark:bg-surface-900 p-4 rounded-xl shadow-soft border border-surface-200/50 dark:border-surface-800/50 mb-6">
           
          <div className="flex items-center justify-between">
             
            <button
              onClick={() => navigateDate(-1)}
              className="p-2 hover:bg-surface-100 dark:hover:bg-surface-800 rounded-lg transition-colors"
            >
               
              <ChevronLeft
                size={20}
                className="text-surface-600 dark:text-surface-400"
              /> 
            </button> 
            <div className="text-center">
               
              <h2 className="text-base md:text-lg font-semibold text-surface-800 dark:text-surface-200">
                 
                {range === "monthly" ? "Monthly View" : "Weekly View"} 
              </h2> 
              <p className="text-xs md:text-sm text-surface-500 dark:text-surface-400">
                 
                {dateRange.startDate.toLocaleDateString()} - 
                {dateRange.endDate.toLocaleDateString()} 
              </p> 
            </div> 
            <button
              onClick={() => navigateDate(1)}
              className="p-2 hover:bg-surface-100 dark:hover:bg-surface-800 rounded-lg transition-colors"
            >
               
              <ChevronRight
                size={20}
                className="text-surface-600 dark:text-surface-400"
              /> 
            </button> 
          </div> 
          <div className="flex flex-wrap justify-center gap-2 mt-4">
             
            <button
              onClick={() => setRange("weekly")}
              className={`px-4 py-2 text-sm rounded-lg transition-colors ${range === "weekly" ? "bg-primary-600 text-white" : "bg-surface-100 dark:bg-surface-800 text-surface-700 dark:text-surface-300 hover:bg-surface-200 dark:hover:bg-surface-700"}`}
            >
               
              Weekly 
            </button> 
            <button
              onClick={() => setRange("monthly")}
              className={`px-4 py-2 text-sm rounded-lg transition-colors ${range === "monthly" ? "bg-primary-600 text-white" : "bg-surface-100 dark:bg-surface-800 text-surface-700 dark:text-surface-300 hover:bg-surface-200 dark:hover:bg-surface-700"}`}
            >
               
              Monthly 
            </button> 
            <button
              onClick={() => setCurrentDate(new Date())}
              className="px-4 py-2 text-sm border border-surface-300 dark:border-surface-600 rounded-lg hover:bg-surface-50 dark:hover:bg-surface-800 text-surface-700 dark:text-surface-300 transition-colors"
            >
               
              Today 
            </button> 
          </div> 
        </div> 
        <div className="bg-white dark:bg-surface-900 p-4 md:p-6 rounded-xl shadow-soft border border-surface-200/50 dark:border-surface-800/50 mb-6">
           
          <div className="flex items-center gap-3">
             
            <div className="p-3 bg-primary-100 dark:bg-primary-900/30 rounded-lg">
               
              <Clock className="h-5 w-5 md:h-6 md:w-6 text-primary-600 dark:text-primary-400" /> 
            </div> 
            <div>
               
              <p className="text-xs md:text-sm text-surface-500 dark:text-surface-400">
                Total Hours
              </p> 
              <p className="text-2xl md:text-3xl font-bold text-surface-900 dark:text-surface-100">
                {totalHours.toFixed(1)}h
              </p> 
            </div> 
          </div> 
        </div> 
        <div className="bg-white dark:bg-surface-900 rounded-xl shadow-soft border border-surface-200/50 dark:border-surface-800/50">
           
          <div className="p-4 border-b border-surface-200 dark:border-surface-700">
             
            <h3 className="text-base md:text-lg font-semibold text-surface-800 dark:text-surface-200">
              Daily Hours
            </h3> 
          </div> 
          {timesheet.length === 0 ? (
            <div className="p-8 text-center">
               
              <Clock
                size={48}
                className="mx-auto text-surface-300 dark:text-surface-600 mb-4"
              /> 
              <p className="text-surface-500 dark:text-surface-400">
                No time logged in this period
              </p> 
            </div>
          ) : (
            <>
               
              <div className="hidden md:block table-container border-0 rounded-none">
                 
                <table className="table">
                   
                  <thead>
                     
                    <tr>
                       
                      <th className="text-left">Date</th> 
                      <th className="text-right">Hours</th> 
                    </tr> 
                  </thead> 
                  <tbody>
                     
                    {timesheet.map((entry, idx) => (
                      <tr key={idx}>
                         
                        <td className="text-surface-700 dark:text-surface-300">
                          {entry[0]}
                        </td> 
                        <td className="text-right font-medium text-surface-900 dark:text-surface-100">
                          {entry[1]?.toFixed(1)}h
                        </td> 
                      </tr>
                    ))} 
                    <tr className="bg-surface-50 dark:bg-surface-800/50">
                       
                      <td className="font-bold text-surface-700 dark:text-surface-300">
                        Total
                      </td> 
                      <td className="font-bold text-surface-900 dark:text-surface-100 text-right">
                        {totalHours.toFixed(1)}h
                      </td> 
                    </tr> 
                  </tbody> 
                </table> 
              </div> 
              <div className="md:hidden divide-y divide-surface-200 dark:divide-surface-700">
                 
                {timesheet.map((entry, idx) => (
                  <div
                    key={idx}
                    className="flex items-center justify-between p-4"
                  >
                     
                    <span className="text-sm text-surface-700 dark:text-surface-300">
                      {entry[0]}
                    </span> 
                    <span className="text-sm font-medium text-surface-900 dark:text-surface-100">
                      {entry[1]?.toFixed(1)}h
                    </span> 
                  </div>
                ))} 
                <div className="flex items-center justify-between p-4 bg-surface-50 dark:bg-surface-800/50">
                   
                  <span className="text-sm font-bold text-surface-700 dark:text-surface-300">
                    Total
                  </span> 
                  <span className="text-sm font-bold text-surface-900 dark:text-surface-100">
                    {totalHours.toFixed(1)}h
                  </span> 
                </div> 
              </div> 
            </>
          )} 
        </div> 
      </div> 
    </>
  );
};
export default Timesheet;
