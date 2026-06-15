import { TrendingUp, TrendingDown, Minus } from "lucide-react";

const COLOR_MAP = {
  blue: "from-blue-500 to-blue-600",
  indigo: "from-indigo-500 to-indigo-600",
  emerald: "from-emerald-500 to-emerald-600",
  purple: "from-purple-500 to-purple-600",
  amber: "from-amber-500 to-amber-600",
  rose: "from-rose-500 to-rose-600",
  cyan: "from-cyan-500 to-cyan-600",
  teal: "from-teal-500 to-teal-600",
  orange: "from-orange-500 to-orange-600",
  pink: "from-pink-500 to-pink-600",
};

export default function StatCard({
  icon: Icon,
  label,
  value,
  color = "indigo",
  sub,
  change,
  changeType = "neutral",
  onClick,
}) {
  const gradientClass = COLOR_MAP[color] || color;
  const Tag = onClick ? "button" : "div";

  return (
    <Tag
      onClick={onClick}
      className={`rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:shadow-md ${
        onClick ? "cursor-pointer text-left" : ""
      }`}
    >
      <div className="flex items-center gap-4">
        <div
          className={`flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br ${gradientClass} text-white shadow-lg`}
        >
          <Icon className="h-6 w-6" />
        </div>
        <div className="min-w-0 flex-1">
          <p className="text-sm font-medium text-slate-500">{label}</p>
          <p className="text-2xl font-bold text-slate-800">{value}</p>
          {sub && <p className="text-xs text-slate-400">{sub}</p>}
        </div>
        {change != null && (
          <div className="flex items-center gap-1">
            {changeType === "up" && <TrendingUp className="h-4 w-4 text-emerald-500" />}
            {changeType === "down" && <TrendingDown className="h-4 w-4 text-rose-500" />}
            {changeType === "neutral" && <Minus className="h-4 w-4 text-slate-400" />}
            <span
              className={`text-sm font-medium ${
                changeType === "up"
                  ? "text-emerald-600"
                  : changeType === "down"
                  ? "text-rose-600"
                  : "text-slate-500"
              }`}
            >
              {change}
            </span>
          </div>
        )}
      </div>
    </Tag>
  );
}
