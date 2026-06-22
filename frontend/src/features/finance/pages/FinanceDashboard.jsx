import { useEffect, useState } from "react";
import { DollarSign, CreditCard, TrendingUp, FileText, AlertTriangle, Wallet } from "lucide-react";
import { toast } from "sonner";
import { getFinanceDashboard } from "../api/financeApi";
import { DashboardHeader, DashboardGrid, StatCard } from "../../analytics/components/dashboard";
import { PageSkeleton } from "../../../components/LoadingSkeleton";

export default function FinanceDashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    setLoading(true);
    try {
      const res = await getFinanceDashboard();
      setData(res);
    } catch (e) {
      toast.error("Failed to load finance dashboard");
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <PageSkeleton />;

  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      <DashboardHeader title="Finance Dashboard" subtitle="Overview of your financial metrics" />

      <DashboardGrid cols={5}>
        <StatCard
          icon={TrendingUp}
          label="Total Revenue"
          value={data?.totalRevenue != null ? `$${Number(data.totalRevenue).toLocaleString()}` : "$0"}
          color="emerald"
        />
        <StatCard
          icon={Wallet}
          label="Expenses"
          value={data?.expenses != null ? `$${Number(data.expenses).toLocaleString()}` : "$0"}
          color="rose"
        />
        <StatCard
          icon={DollarSign}
          label="Profit"
          value={data?.profit != null ? `$${Number(data.profit).toLocaleString()}` : "$0"}
          color="blue"
        />
        <StatCard
          icon={FileText}
          label="Outstanding Invoices"
          value={data?.outstandingInvoices ?? 0}
          color="amber"
        />
        <StatCard
          icon={AlertTriangle}
          label="Overdue Invoices"
          value={data?.overdueInvoices ?? 0}
          color="orange"
        />
      </DashboardGrid>
    </div>
  );
}

