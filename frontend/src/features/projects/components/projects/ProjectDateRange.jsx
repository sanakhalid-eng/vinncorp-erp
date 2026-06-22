import { Calendar } from "lucide-react";
const formatDate = (date) =>
  date ? new Date(date).toLocaleDateString() : null;
export default function ProjectDateRange({ startDate, endDate }) {
  return (
    <div className="flex items-center gap-2 text-slate-500 dark:text-slate-400">
       
      <Calendar className="w-4 h-4 shrink-0" /> 
      <span>
         
        {formatDate(startDate) || "No start date"} 
        {endDate && ` ΓåÆ ${formatDate(endDate)}`} 
      </span> 
    </div>
  );
}
