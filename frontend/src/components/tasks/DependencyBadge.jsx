import {
  AlertTriangle,
  Link2,
  ArrowRight,
  GitBranch,
  Clock,
  CheckCircle2,
} from "lucide-react";
import { cn } from "../../utils/cn";
export default function DependencyBadge({
  blocked = false,
  blockingCount = 0,
  dependencyCount = 0,
  className,
}) {
  if (!blocked && dependencyCount === 0) return null;
  return (
    <div className={cn("flex items-center gap-2", className)}>
       
      {blocked && (
        <span className="inline-flex items-center gap-1 rounded-full bg-danger-100 dark:bg-danger-900/30 px-2.5 py-1 text-xs font-semibold text-danger-700 dark:text-danger-400 ring-1 ring-danger-200 dark:ring-danger-800/50">
           
          <AlertTriangle className="h-3 w-3" /> Blocked 
        </span>
      )} 
      {dependencyCount > 0 && (
        <span className="inline-flex items-center gap-1 rounded-full bg-surface-100 dark:bg-surface-800 px-2.5 py-1 text-xs font-medium text-surface-600 dark:text-surface-300 ring-1 ring-surface-200 dark:ring-surface-700">
           
          <Link2 className="h-3 w-3" /> {dependencyCount} 
        </span>
      )} 
    </div>
  );
}
