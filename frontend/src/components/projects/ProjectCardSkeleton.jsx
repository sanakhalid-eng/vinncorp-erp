export default function ProjectCardSkeleton({ count = 1 }) {
  const renderSkeleton = (key) => (
    <div
      key={key}
      className="animate-pulse rounded-3xl border border-slate-200/70 dark:border-slate-800/80 bg-white/90 dark:bg-slate-900/90 overflow-hidden"
    >
       
      <div className="h-2 w-full bg-slate-200 dark:bg-slate-800" /> 
      <div className="p-6 space-y-4">
         
        <div className="flex justify-between items-start">
           
          <div className="space-y-2 flex-1">
             
            <div className="h-5 bg-slate-200 dark:bg-slate-800 rounded w-3/4" /> 
            <div className="h-3 bg-slate-200 dark:bg-slate-800 rounded w-1/3" /> 
          </div> 
          <div className="h-11 w-11 rounded-2xl bg-slate-200 dark:bg-slate-800" /> 
        </div> 
        <div className="h-4 bg-slate-200 dark:bg-slate-800 rounded w-full" /> 
        <div className="h-4 bg-slate-200 dark:bg-slate-800 rounded w-2/3" /> 
        <div className="flex gap-2">
           
          <div className="h-6 w-16 bg-slate-200 dark:bg-slate-800 rounded-full" /> 
          <div className="h-6 w-16 bg-slate-200 dark:bg-slate-800 rounded-full" /> 
        </div> 
        <div className="space-y-1">
           
          <div className="h-3 bg-slate-200 dark:bg-slate-800 rounded w-full" /> 
          <div className="h-2 bg-slate-200 dark:bg-slate-800 rounded w-full" /> 
        </div> 
        <div className="grid grid-cols-3 gap-3">
           
          {[...Array(3)].map((_, i) => (
            <div
              key={i}
              className="h-16 bg-slate-200 dark:bg-slate-800 rounded-2xl"
            />
          ))} 
        </div> 
        <div className="h-12 bg-slate-200 dark:bg-slate-800 rounded-2xl" /> 
      </div> 
    </div>
  );
  if (count > 1) {
    return <>{Array.from({ length: count }, (_, i) => renderSkeleton(i))}</>;
  }
  return renderSkeleton(0);
}
