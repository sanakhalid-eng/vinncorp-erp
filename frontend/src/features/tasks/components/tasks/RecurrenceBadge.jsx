import { Repeat, PauseCircle, PlayCircle } from "lucide-react";
import { cn } from "../../../../utils/cn";
const RECURRENCE_COLORS = {
  DAILY: "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400",
  WEEKLY:
    "bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400",
  MONTHLY:
    "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400",
  CUSTOM:
    "bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400",
};
export default function RecurrenceBadge({ template, className }) {
  if (!template) return null;
  const colorClass =
    RECURRENCE_COLORS[template.recurrenceType] || RECURRENCE_COLORS.DAILY;
  const label = {
    DAILY:
      template.intervalValue > 1
        ? `Every ${template.intervalValue} days`
        : "Daily",
    WEEKLY: "Weekly",
    MONTHLY: template.dayOfMonth ? `Day ${template.dayOfMonth}` : "Monthly",
    CUSTOM: `Every ${template.intervalValue} days`,
  }[template.recurrenceType];
  return (
    <div className={cn("flex items-center gap-1.5", className)}>
       
      <span
        className={cn(
          "inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-semibold",
          colorClass,
        )}
      >
         
        {template.paused ? (
          <PauseCircle className="h-3 w-3" />
        ) : (
          <Repeat className="h-3 w-3" />
        )} 
        {label} 
      </span> 
      {template.paused && (
        <span className="inline-flex items-center gap-1 rounded-full bg-yellow-100 dark:bg-yellow-900/30 px-2 py-0.5 text-xs font-semibold text-yellow-700 dark:text-yellow-400">
           
          <PauseCircle className="h-3 w-3" /> Paused 
        </span>
      )} 
      {template.nextRunAt && !template.paused && (
        <span className="text-xs text-gray-500 dark:text-gray-400">
           
          Next: {new Date(template.nextRunAt).toLocaleDateString()} 
        </span>
      )} 
    </div>
  );
}
