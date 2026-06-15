import { forwardRef } from "react";
import { cn } from "../../utils/cn.js";
const Badge = forwardRef(function Badge(
  { className, variant = "default", ...props },
  ref,
) {
  return (
    <div
      ref={ref}
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-slate-950/25 focus:ring-offset-2 ${variant === "destructive" ? "bg-destructive text-destructive-foreground hover:bg-destructive/80" : variant === "outline" ? "border border-slate-200 hover:bg-slate-100 hover:text-accent-foreground" : variant === "secondary" ? "bg-slate-100 text-slate-900 hover:bg-slate-200" : "bg-slate-900 text-white hover:bg-slate-800"} ${className || ""}`}
      {...props}
    />
  );
});
Badge.displayName = "Badge";
export { Badge };
