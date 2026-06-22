import { TrendingUp, TrendingDown, Minus } from "lucide-react";
const StatCard = ({
  title,
  value,
  change,
  changeType = "up",
  trendColor = "indigo",
  helperText = "",
}) => {
  const Icon =
    changeType === "up"
      ? TrendingUp
      : changeType === "down"
        ? TrendingDown
        : Minus;
  return (
    <div className="group bg-white/70 backdrop-blur-xl rounded-2xl p-6 shadow-xl hover:shadow-2xl transition-all duration-500 border border-white/50 hover:border-indigo-200">
       
      <div className="flex items-center justify-between mb-4">
         
        <div
          className="p-3 rounded-xl bg-gradient-to-br group-hover:scale-105 transition-transform duration-300"
          style={{
            background: `linear-gradient(135deg, var(--${trendColor}-light), var(--${trendColor}-dark))`,
          }}
        >
           
          <Icon className="w-6 h-6 text-white" /> 
        </div> 
      </div> 
      <div className="space-y-1">
         
        <p className="text-sm font-medium text-gray-600 uppercase tracking-wide">
          {title}
        </p> 
        <p className="text-3xl font-bold bg-gradient-to-r from-gray-900 to-gray-700 bg-clip-text text-transparent">
          {value}
        </p> 
        {helperText ? (
          <p className="text-sm font-medium text-gray-500">{helperText}</p>
        ) : (
          <p
            className={`text-sm font-semibold flex items-center gap-1 ${changeType === "up" ? "text-emerald-600" : changeType === "down" ? "text-red-600" : "text-gray-500"}`}
          >
             
            {changeType !== "neutral" &&
              `${changeType === "up" ? "+" : ""}${change}%`} 
            <Icon className="w-4 h-4" /> 
          </p>
        )} 
      </div> 
    </div>
  );
};
export default StatCard;
