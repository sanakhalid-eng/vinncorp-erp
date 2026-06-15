import { CheckCircle2 } from "lucide-react";
import { cn } from "../../utils/cn";
const styles = {
  PLANNING: "bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300",
  ACTIVE:
    "bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400",
  COMPLETED: "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400",
  ON_HOLD:
    "bg-yellow-100 text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400",
};
export default function ProjectStatusBadge({ status, showIcon = true }) {
  const style = styles[status] || styles.ACTIVE;
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1 rounded-full px-3 py-1 text-xs font-semibold",
        style,
      )}
    >
       
      {showIcon && <CheckCircle2 className="w-3 h-3" />} 
      {status || "ACTIVE"} 
    </span>
  );
}
