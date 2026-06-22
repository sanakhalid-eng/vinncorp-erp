import { useEffect, useState, useCallback } from "react";
import { Plus, Search, Edit2, Trash2, CheckCircle, XCircle, Wallet, DollarSign } from "lucide-react";
import { toast } from "sonner";
import {
  listExpenses, createExpense, updateExpense, deleteExpense,
  approveExpense, rejectExpense, reimburseExpense,
} from "../api/financeApi";
import DataTable from "../../../components/table/DataTable";
import Modal from "../../../components/ui/Modal";
import { TableRowSkeleton } from "../../../components/LoadingSkeleton";
import ErrorState from "../../../components/ErrorState";
import ConfirmationDialog from "../../projects/components/members/ConfirmationDialog";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

const STATUS_BADGE = {
  PENDING: "bg-amber-100 text-amber-700",
  APPROVED: "bg-emerald-100 text-emerald-700",
  REJECTED: "bg-rose-100 text-rose-700",
  REIMBURSED: "bg-blue-100 text-blue-700",
};

const expenseSchema = z.object({
  title: z.string().min(1, "Required"),
  description: z.string().optional(),
  category: z.string().min(1, "Required"),
  amount: z.coerce.number().positive("Must be positive"),
  expenseDate: z.string().min(1, "Required"),
  attachmentUrl: z.string().optional(),
});

export default function ExpensesPage() {
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 20;
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);

  const form = useForm({
    resolver: zodResolver(expenseSchema),
    defaultValues: {
      title: "", description: "", category: "", amount: "",
      expenseDate: new Date().toISOString().split("T")[0], attachmentUrl: "",
    },
  });

  const loadExpenses = useCallback(async (p = page) => {
    setLoading(true);
    try {
      const params = { page: p, size: pageSize };
      if (search) params.search = search;
      if (statusFilter) params.status = statusFilter;
      const res = await listExpenses(params);
      setExpenses(res?.content || []);
      setTotalPages(res?.totalPages || 0);
      setTotalElements(res?.totalElements || 0);
    } catch (e) {
      setError(e.message || "Failed to load expenses");
    } finally {
      setLoading(false);
    }
  }, [search, statusFilter, page]);

  useEffect(() => { loadExpenses(); }, [loadExpenses]);

  const handleSubmit = async (data) => {
    setSubmitting(true);
    try {
      const payload = {
        ...data,
        expenseDate: new Date(data.expenseDate).toISOString(),
        amount: Number(data.amount),
        description: data.description || undefined,
        attachmentUrl: data.attachmentUrl || undefined,
      };
      if (editing) {
        await updateExpense(editing.id, payload);
        toast.success("Expense updated");
      } else {
        await createExpense(payload);
        toast.success("Expense created");
      }
      setShowModal(false);
      setEditing(null);
      form.reset();
      loadExpenses(0);
    } catch (e) {
      toast.error(e.response?.data?.message || "Failed to save expense");
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (exp) => {
    setEditing(exp);
    form.reset({
      title: exp.title,
      description: exp.description || "",
      category: exp.category,
      amount: exp.amount,
      expenseDate: exp.expenseDate?.split("T")[0] || "",
      attachmentUrl: exp.attachmentUrl || "",
    });
    setShowModal(true);
  };

  const handleDelete = (id) => {
    setConfirmAction(() => async () => {
      try {
        await deleteExpense(id);
        toast.success("Expense deleted");
        loadExpenses();
      } catch (e) {
        toast.error("Failed to delete expense");
      }
      setShowConfirmDialog(false);
    });
    setShowConfirmDialog(true);
  };

  const handleApprove = async (id) => {
    try {
      await approveExpense(id);
      toast.success("Expense approved");
      loadExpenses();
    } catch (e) {
      toast.error(e.response?.data?.message || "Failed to approve");
    }
  };

  const handleReject = async (id) => {
    try {
      await rejectExpense(id);
      toast.success("Expense rejected");
      loadExpenses();
    } catch (e) {
      toast.error(e.response?.data?.message || "Failed to reject");
    }
  };

  const handleReimburse = async (id) => {
    try {
      await reimburseExpense(id);
      toast.success("Expense reimbursed");
      loadExpenses();
    } catch (e) {
      toast.error(e.response?.data?.message || "Failed to reimburse");
    }
  };

  const columns = [
    { header: "Title", accessor: "title", width: "200px" },
    { header: "Category", accessor: "category", width: "120px" },
    {
      header: "Amount", accessor: "amount", width: "120px",
      render: (row) => `$${Number(row.amount || 0).toLocaleString()}`,
    },
    {
      header: "Date", accessor: "expenseDate", width: "120px",
      render: (row) => row.expenseDate ? new Date(row.expenseDate).toLocaleDateString() : "-",
    },
    {
      header: "Status", accessor: "status", width: "120px",
      render: (row) => (
        <span className={`inline-block px-2.5 py-1 rounded-full text-xs font-semibold ${STATUS_BADGE[row.status] || "bg-slate-100 text-slate-600"}`}>
          {row.status?.replace("_", " ")}
        </span>
      ),
    },
    {
      header: "Actions", width: "200px",
      render: (row) => (
        <div className="flex items-center gap-1">
          {row.status === "PENDING" && (
            <>
              <button onClick={() => handleEdit(row)}
                className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-500 hover:text-blue-600" title="Edit">
                <Edit2 className="h-4 w-4" />
              </button>
              <button onClick={() => handleApprove(row.id)}
                className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-500 hover:text-emerald-600" title="Approve">
                <CheckCircle className="h-4 w-4" />
              </button>
              <button onClick={() => handleReject(row.id)}
                className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-500 hover:text-rose-600" title="Reject">
                <XCircle className="h-4 w-4" />
              </button>
            </>
          )}
          {row.status === "APPROVED" && (
            <button onClick={() => handleReimburse(row.id)}
              className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-500 hover:text-blue-600" title="Reimburse">
              <DollarSign className="h-4 w-4" />
            </button>
          )}
          <button onClick={() => handleDelete(row.id)}
            className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-500 hover:text-rose-600" title="Delete">
            <Trash2 className="h-4 w-4" />
          </button>
        </div>
      ),
    },
  ];

  if (error) return <ErrorState title="Failed to load expenses" message={error} onRetry={() => { setError(null); loadExpenses(); }} />;

  if (loading && expenses.length === 0) {
    return (
      <div className="mx-auto max-w-7xl p-4 sm:p-6">
        <div className="mb-6 h-8 bg-slate-200 rounded w-1/4 animate-pulse"></div>
        <div className="mb-6 h-10 bg-slate-200 rounded w-full animate-pulse"></div>
        <table className="w-full">
          <thead>
            <tr>
              {["Title", "Category", "Amount", "Date", "Status", "Actions"].map((h) => (
                <th key={h} className="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {Array(6).fill(0).map((_, i) => <TableRowSkeleton key={i} columns={6} />)}
          </tbody>
        </table>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl p-4 sm:p-6">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Expenses</h1>
          <p className="text-slate-500">{totalElements} expenses</p>
        </div>
        <button onClick={() => { setEditing(null); form.reset(); setShowModal(true); }}
          className="flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-indigo-700">
          <Plus className="h-4 w-4" /> New Expense
        </button>
      </div>

      <div className="mb-6 flex flex-col gap-3 sm:flex-row">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input type="text" placeholder="Search expenses..." value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-10 pr-4 text-sm focus:border-indigo-500 focus:outline-none" />
        </div>
        <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}
          className="rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none">
          <option value="">All Statuses</option>
          {Object.keys(STATUS_BADGE).map((s) => (
            <option key={s} value={s}>{s.replace("_", " ")}</option>
          ))}
        </select>
      </div>

      <DataTable
        columns={columns}
        data={expenses}
        loading={loading}
        searchable={false}
        emptyMessage="No expenses found"
        emptyIcon={Wallet}
        serverSide
        totalPages={totalPages}
        currentPage={page}
        onPageChange={setPage}
        totalRecords={totalElements}
        pageSize={pageSize}
        striped
      />

      <Modal open={showModal} onClose={() => { setShowModal(false); setEditing(null); }}
        title={editing ? "Edit Expense" : "Create Expense"}>
        <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Title *</label>
            <input {...form.register("title")}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
            {form.formState.errors.title && <p className="text-red-500 text-xs mt-1">{form.formState.errors.title.message}</p>}
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
            <textarea {...form.register("description")} rows={2}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Category *</label>
              <input {...form.register("category")}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
              {form.formState.errors.category && <p className="text-red-500 text-xs mt-1">{form.formState.errors.category.message}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Amount *</label>
              <input type="number" step="0.01" {...form.register("amount")}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
              {form.formState.errors.amount && <p className="text-red-500 text-xs mt-1">{form.formState.errors.amount.message}</p>}
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Expense Date *</label>
            <input type="date" {...form.register("expenseDate")}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Attachment URL</label>
            <input {...form.register("attachmentUrl")}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
          </div>
          <div className="flex justify-end gap-3 pt-4 border-t">
            <button type="button" onClick={() => { setShowModal(false); setEditing(null); }}
              className="px-4 py-2 text-sm rounded-lg border border-slate-200 text-slate-600 hover:bg-slate-50">
              Cancel
            </button>
            <button type="submit" disabled={submitting}
              className="px-4 py-2 text-sm rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 disabled:opacity-50">
              {submitting ? "Saving..." : editing ? "Update" : "Create"}
            </button>
          </div>
        </form>
      </Modal>

      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={() => setShowConfirmDialog(false)}
        onConfirm={confirmAction}
        title="Delete Expense"
        message="Are you sure you want to delete this expense? This action cannot be undone."
        confirmText="Delete"
      />
    </div>
  );
}

