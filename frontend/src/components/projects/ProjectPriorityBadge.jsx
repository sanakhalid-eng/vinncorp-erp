import { Flag } from "lucide-react";
import { cn } from "../../utils/cn";
const colors = {
  LOW: "text-emerald-600 dark:text-emerald-400",
  MEDIUM: "text-amber-600 dark:text-amber-400",
  HIGH: "text-orange-600 dark:text-orange-400",
  CRITICAL: "text-red-600 dark:text-red-400",
};
export default function ProjectPriorityBadge({ priority, showIcon = true }) {
  const color = colors[priority] || colors.MEDIUM;
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1 text-xs font-semibold",
        color,
      )}
    >
       
      {showIcon && <Flag className="w-3 h-3" />} {priority || "MEDIUM"} 
    </span>
  );
}
