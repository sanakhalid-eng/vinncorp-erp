export default function ProjectStatsGrid({
  totalTasks = 0,
  completedTasks = 0,
  pendingTasks = 0,
}) {
  return (
    <div className="grid grid-cols-3 gap-3">
       
      <div className="rounded-2xl bg-slate-50 dark:bg-slate-800/70 p-3">
         
        <p className="text-xs text-slate-500">Tasks</p> 
        <p className="mt-1 text-lg font-bold text-slate-900 dark:text-white">
          {totalTasks}
        </p> 
      </div> 
      <div className="rounded-2xl bg-slate-50 dark:bg-slate-800/70 p-3">
         
        <p className="text-xs text-slate-500">Completed</p> 
        <p className="mt-1 text-lg font-bold text-emerald-600">
          {completedTasks}
        </p> 
      </div> 
      <div className="rounded-2xl bg-slate-50 dark:bg-slate-800/70 p-3">
         
        <p className="text-xs text-slate-500">Pending</p> 
        <p
          className={`mt-1 text-lg font-bold ${pendingTasks > 0 ? "text-amber-600" : "text-slate-500"}`}
        >
          {pendingTasks}
        </p> 
      </div> 
    </div>
  );
}
