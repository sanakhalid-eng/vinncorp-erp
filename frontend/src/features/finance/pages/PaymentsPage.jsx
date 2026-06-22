import { useEffect, useState, useCallback } from "react";
import { Plus, Search, Edit2, Trash2, DollarSign } from "lucide-react";
import { toast } from "sonner";
import { listPayments, createPayment, updatePayment, deletePayment } from "../api/financeApi";
import DataTable from "../../../components/table/DataTable";
import Modal from "../../../components/ui/Modal";
import { TableRowSkeleton } from "../../../components/LoadingSkeleton";
import ErrorState from "../../../components/ErrorState";
import ConfirmationDialog from "../../projects/components/members/ConfirmationDialog";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

const PAYMENT_METHODS = ["CASH", "BANK_TRANSFER", "CARD", "CHEQUE", "OTHER"];

const paymentSchema = z.object({
  invoiceId: z.coerce.number().min(1, "Required"),
  amount: z.coerce.number().positive("Must be positive"),
  paymentDate: z.string().min(1, "Required"),
  paymentMethod: z.string().min(1, "Required"),
  referenceNumber: z.string().optional(),
  notes: z.string().optional(),
});

export default function PaymentsPage() {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 20;
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);

  const form = useForm({
    resolver: zodResolver(paymentSchema),
    defaultValues: {
      invoiceId: "", amount: "", paymentDate: new Date().toISOString().split("T")[0],
      paymentMethod: "BANK_TRANSFER", referenceNumber: "", notes: "",
    },
  });

  const loadPayments = useCallback(async (p = page) => {
    setLoading(true);
    try {
      const res = await listPayments({ page: p, size: pageSize });
      setPayments(res?.content || []);
      setTotalPages(res?.totalPages || 0);
      setTotalElements(res?.totalElements || 0);
    } catch (e) {
      setError(e.message || "Failed to load payments");
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => { loadPayments(); }, [loadPayments]);

  const handleSubmit = async (data) => {
    setSubmitting(true);
    try {
      const payload = {
        ...data,
        paymentDate: new Date(data.paymentDate).toISOString(),
        amount: Number(data.amount),
      };
      if (editing) {
        await updatePayment(editing.id, payload);
        toast.success("Payment updated");
      } else {
        await createPayment(payload);
        toast.success("Payment created");
      }
      setShowModal(false);
      setEditing(null);
      form.reset();
      loadPayments(0);
    } catch (e) {
      toast.error(e.response?.data?.message || "Failed to save payment");
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (p) => {
    setEditing(p);
    form.reset({
      invoiceId: p.invoiceId,
      amount: p.amount,
      paymentDate: p.paymentDate?.split("T")[0] || "",
      paymentMethod: p.paymentMethod,
      referenceNumber: p.referenceNumber || "",
      notes: p.notes || "",
    });
    setShowModal(true);
  };

  const handleDelete = (id) => {
    setConfirmAction(() => async () => {
      try {
        await deletePayment(id);
        toast.success("Payment deleted");
        loadPayments();
      } catch (e) {
        toast.error("Failed to delete payment");
      }
      setShowConfirmDialog(false);
    });
    setShowConfirmDialog(true);
  };

  const columns = [
    { header: "Invoice", accessor: "invoiceNumber", width: "130px" },
    {
      header: "Amount", accessor: "amount", width: "120px",
      render: (row) => `$${Number(row.amount || 0).toLocaleString()}`,
    },
    {
      header: "Method", accessor: "paymentMethod", width: "130px",
      render: (row) => row.paymentMethod?.replace("_", " ") || "-",
    },
    {
      header: "Date", accessor: "paymentDate", width: "130px",
      render: (row) => row.paymentDate ? new Date(row.paymentDate).toLocaleDateString() : "-",
    },
    { header: "Reference", accessor: "referenceNumber", width: "140px" },
    {
      header: "Actions", width: "100px",
      render: (row) => (
        <div className="flex items-center gap-1">
          <button onClick={() => handleEdit(row)}
            className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-500 hover:text-blue-600" title="Edit">
            <Edit2 className="h-4 w-4" />
          </button>
          <button onClick={() => handleDelete(row.id)}
            className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-500 hover:text-rose-600" title="Delete">
            <Trash2 className="h-4 w-4" />
          </button>
        </div>
      ),
    },
  ];

  if (error) return <ErrorState title="Failed to load payments" message={error} onRetry={() => { setError(null); loadPayments(); }} />;

  if (loading && payments.length === 0) {
    return (
      <div className="mx-auto max-w-7xl p-4 sm:p-6">
        <div className="mb-6 h-8 bg-slate-200 rounded w-1/4 animate-pulse"></div>
        <table className="w-full">
          <thead>
            <tr>
              {["Invoice", "Amount", "Method", "Date", "Reference", "Actions"].map((h) => (
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
          <h1 className="text-2xl font-bold text-slate-800">Payments</h1>
          <p className="text-slate-500">{totalElements} payments</p>
        </div>
        <button onClick={() => { setEditing(null); form.reset(); setShowModal(true); }}
          className="flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-indigo-700">
          <Plus className="h-4 w-4" /> New Payment
        </button>
      </div>

      <DataTable
        columns={columns}
        data={payments}
        loading={loading}
        searchable={false}
        emptyMessage="No payments found"
        emptyIcon={DollarSign}
        serverSide
        totalPages={totalPages}
        currentPage={page}
        onPageChange={setPage}
        totalRecords={totalElements}
        pageSize={pageSize}
        striped
      />

      <Modal open={showModal} onClose={() => { setShowModal(false); setEditing(null); }}
        title={editing ? "Edit Payment" : "Create Payment"}>
        <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Invoice ID *</label>
            <input type="number" {...form.register("invoiceId")}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
            {form.formState.errors.invoiceId && <p className="text-red-500 text-xs mt-1">{form.formState.errors.invoiceId.message}</p>}
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Amount *</label>
            <input type="number" step="0.01" {...form.register("amount")}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
            {form.formState.errors.amount && <p className="text-red-500 text-xs mt-1">{form.formState.errors.amount.message}</p>}
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Payment Date *</label>
            <input type="date" {...form.register("paymentDate")}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Method *</label>
            <select {...form.register("paymentMethod")}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none">
              {PAYMENT_METHODS.map((m) => <option key={m} value={m}>{m.replace("_", " ")}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Reference Number</label>
            <input {...form.register("referenceNumber")}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Notes</label>
            <textarea {...form.register("notes")} rows={2}
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
        title="Delete Payment"
        message="Are you sure you want to delete this payment? This action cannot be undone."
        confirmText="Delete"
      />
    </div>
  );
}

