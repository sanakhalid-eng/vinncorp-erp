export default function ProjectProgressBar({
  percent = 0,
  showLabel = true,
  size = "md",
}) {
  const heights = { sm: "h-1.5", md: "h-2", lg: "h-3" };
  const height = heights[size] || heights.md;
  return (
    <div>
       
      {showLabel && (
        <div className="mb-2 flex items-center justify-between text-sm">
           
          <span className="font-medium text-slate-700 dark:text-slate-300">
            Progress
          </span> 
          <span className="font-semibold text-primary-600 dark:text-primary-400">
            {percent}%
          </span> 
        </div>
      )} 
      <div
        className={`${height} w-full overflow-hidden rounded-full bg-slate-200 dark:bg-slate-800`}
      >
         
        <div
          className={`${height} rounded-full bg-gradient-to-r from-primary-500 to-indigo-600 transition-all duration-500`}
          style={{ width: `${Math.min(100, Math.max(0, percent))}%` }}
        /> 
      </div> 
    </div>
  );
}
