import { useState, useEffect, useMemo } from "react";
import { TrendingUp, TrendingDown, Minus } from "lucide-react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
  ReferenceLine,
} from "recharts";
import {
  getProjectVelocityHistory,
  getProjectSprints,
} from "../../api/sprintApi";

const trendIcon = (trend) => {
  if (trend === "INCREASING")
    return <TrendingUp className="h-4 w-4 text-success-500" />;
  if (trend === "DECREASING")
    return <TrendingDown className="h-4 w-4 text-danger-500" />;
  return <Minus className="h-4 w-4 text-surface-400" />;
};

export default function SprintVelocityChart({ projectId }) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!projectId) return;
    const fetch = async () => {
      setLoading(true);
      try {
        let result = await getProjectVelocityHistory(projectId);

        if (!result?.history || result.history.length === 0) {
          const sprints = await getProjectSprints(projectId);
          if (sprints && sprints.length > 0) {
            const history = sprints.map((s) => ({
              sprintId: s.id,
              sprintName: s.name,
              committedPoints: s.totalTasks || 0,
              completedPoints: s.completedTasks || 0,
              spilloverPoints: 0,
              completionRate: s.progressPercentage
                ? s.progressPercentage / 100
                : 0,
            }));
            const completedPoints = history.map((h) => h.completedPoints);
            const tot = completedPoints.reduce((a, b) => a + b, 0);
            const avgVelocity = completedPoints.length > 0 ? tot / completedPoints.length : 0;
            const bestSprint = history.reduce(
              (best, curr) =>
                curr.completedPoints > (best?.completedPoints ?? -1) ? curr : best,
              null,
            );
            result = {
              history,
              averageVelocity: avgVelocity,
              trend: "NEUTRAL",
              changePercentage: 0,
              bestSprint,
              worstSprint: null,
            };
          }
        }

        setData(result);
      } catch (err) {
        console.error("Failed to load velocity history:", err);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, [projectId]);

  const chartData = useMemo(() => {
    if (!data?.history) return [];
    return data.history.map((v) => ({
      name:
        v.sprintName?.length > 12
          ? v.sprintName.slice(0, 12) + "ΓÇª"
          : v.sprintName || `Sprint ${v.sprintId}`,
      committed: v.committedPoints,
      completed: v.completedPoints,
      spillover: v.spilloverPoints,
      rate: +(v.completionRate * 100).toFixed(1),
    }));
  }, [data]);

  const avgLast3 = useMemo(() => {
    if (!chartData || chartData.length === 0) return 0;
    const last3 = chartData.slice(-3);
    const sum = last3.reduce((a, b) => a + b.completed, 0);
    return (sum / last3.length).toFixed(1);
  }, [chartData]);

  const overallCompletionRate = useMemo(() => {
    if (!chartData || chartData.length === 0) return 0;
    const totalCommitted = chartData.reduce((sum, d) => sum + d.committed, 0);
    const totalCompleted = chartData.reduce((sum, d) => sum + d.completed, 0);
    return totalCommitted > 0
      ? ((totalCompleted / totalCommitted) * 100).toFixed(1)
      : 0;
  }, [chartData]);

  if (!projectId) return null;

  const best = data?.bestSprint;

  return (
    <div className="space-y-4">
      {loading ? (
        <div className="flex items-center justify-center py-8">
          <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary-600" />
        </div>
      ) : !data || chartData.length === 0 ? (
        <p className="text-sm text-surface-500 dark:text-surface-400 text-center py-4">
          No velocity data available
        </p>
      ) : (
        <>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
            <div className="rounded-xl bg-surface-50 dark:bg-surface-800/50 p-3">
              <p className="text-[10px] font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                Avg Velocity (3)
              </p>
              <p className="text-lg font-bold text-surface-900 dark:text-surface-100 mt-1">
                {avgLast3}
              </p>
            </div>
            <div className="rounded-xl bg-surface-50 dark:bg-surface-800/50 p-3">
              <p className="text-[10px] font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                Completion Rate
              </p>
              <p className="text-lg font-bold text-surface-900 dark:text-surface-100 mt-1">
                {overallCompletionRate}%
              </p>
            </div>
            <div className="rounded-xl bg-surface-50 dark:bg-surface-800/50 p-3">
              <p className="text-[10px] font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                Trend
              </p>
              <div className="flex items-center gap-1.5 mt-1">
                {trendIcon(data.trend)}
                <span className="text-sm font-semibold text-surface-900 dark:text-surface-100">
                  {data.changePercentage != null
                    ? `${data.changePercentage > 0 ? "+" : ""}${data.changePercentage.toFixed(1)}%`
                    : "ΓÇö"}
                </span>
              </div>
            </div>
            <div className="rounded-xl bg-success-50 dark:bg-success-900/10 p-3">
              <p className="text-[10px] font-semibold uppercase tracking-wider text-success-600 dark:text-success-400">
                Best Sprint
              </p>
              <p className="text-sm font-bold text-success-700 dark:text-success-300 mt-0.5">
                {best?.sprintName || "ΓÇö"}
              </p>
              {best && (
                <p className="text-[10px] text-success-600 dark:text-success-400">
                  {best.completedPoints}/{best.committedPoints} pts
                </p>
              )}
            </div>
          </div>
          <div className="h-64">
            <ResponsiveContainer width="100%" height={320}>
              <BarChart
                data={chartData}
                margin={{ top: 5, right: 10, left: -10, bottom: 5 }}
              >
                <CartesianGrid
                  strokeDasharray="3 3"
                  className="text-surface-200 dark:text-surface-700"
                />
                <XAxis
                  dataKey="name"
                  tick={{ fontSize: 10 }}
                  className="text-surface-500 dark:text-surface-400"
                />
                <YAxis
                  tick={{ fontSize: 10 }}
                  className="text-surface-500 dark:text-surface-400"
                />
                <Tooltip
                  content={({ active, payload, label }) => {
                    if (!active || !payload?.length) return null;
                    const d = payload[0]?.payload;
                    return (
                      <div className="bg-white dark:bg-surface-800 p-3 border border-surface-200 dark:border-surface-700 rounded shadow-md text-xs">
                        <p className="font-semibold text-surface-900 dark:text-surface-100 mb-1">
                          {label}
                        </p>
                        <p className="text-primary-600">
                          Committed: {d.committed}
                        </p>
                        <p className="text-success-600">
                          Completed: {d.completed}
                        </p>
                        <p className="text-warning-600">
                          Spillover: {d.spillover}
                        </p>
                        <p className="text-surface-500">Rate: {d.rate}%</p>
                      </div>
                    );
                  }}
                />
                <Legend />
                <Bar
                  dataKey="committed"
                  fill="#94a3b8"
                  name="Committed"
                  radius={[4, 4, 0, 0]}
                />
                <Bar
                  dataKey="completed"
                  fill="#22c55e"
                  name="Completed"
                  radius={[4, 4, 0, 0]}
                />
                <ReferenceLine
                  y={data.averageVelocity}
                  stroke="#3b82f6"
                  strokeDasharray="5 5"
                  label={{ value: "Avg", fontSize: 10, fill: "#3b82f6" }}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </>
      )}
    </div>
  );
}
