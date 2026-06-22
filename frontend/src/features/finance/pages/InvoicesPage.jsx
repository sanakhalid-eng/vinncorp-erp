import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { Plus, Search, Edit2, Trash2, Send, CheckCircle, FileText } from "lucide-react";
import { toast } from "sonner";
import {
  listInvoices, createInvoice, updateInvoice, deleteInvoice, sendInvoice, markInvoicePaid,
} from "../api/financeApi";
import DataTable from "../../../components/table/DataTable";
import Modal from "../../../components/ui/Modal";
import { TableRowSkeleton } from "../../../components/LoadingSkeleton";
import ErrorState from "../../../components/ErrorState";
import ConfirmationDialog from "../../projects/components/members/ConfirmationDialog";
import { parseApiError } from "../../../utils/apiError";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

const STATUS_BADGE = {
  DRAFT: "bg-slate-100 text-slate-700",
  SENT: "bg-blue-100 text-blue-700",
  PARTIALLY_PAID: "bg-amber-100 text-amber-700",
  PAID: "bg-emerald-100 text-emerald-700",
  OVERDUE: "bg-rose-100 text-rose-700",
  CANCELLED: "bg-slate-200 text-slate-500",
};

const invoiceSchema = z.object({
  invoiceNumber: z.string().min(1, "Required"),
  customerId: z.coerce.number().min(1, "Required"),
  projectId: z.coerce.number().optional(),
  opportunityId: z.coerce.number().optional(),
  issueDate: z.string().min(1, "Required"),
  dueDate: z.string().min(1, "Required"),
  discountAmount: z.coerce.number().min(0).optional(),
  taxAmount: z.coerce.number().min(0).optional(),
  notes: z.string().optional(),
  items: z.array(z.object({
    description: z.string().min(1, "Required"),
    quantity: z.coerce.number().min(1, "Min 1"),
    unitPrice: z.coerce.number().min(0, "Min 0"),
  })).min(1, "At least one item required"),
});

export default function InvoicesPage() {
  const navigate = useNavigate();
  const [invoices, setInvoices] = useState([]);
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
    resolver: zodResolver(invoiceSchema),
    defaultValues: {
      invoiceNumber: "", customerId: "", projectId: "", opportunityId: "",
      issueDate: "", dueDate: "", discountAmount: 0, taxAmount: 0,
      notes: "", items: [{ description: "", quantity: 1, unitPrice: 0 }],
    },
  });

  const loadInvoices = useCallback(async (p = page) => {
    setLoading(true);
    try {
      const params = { page: p, size: pageSize };
      if (search) params.search = search;
      if (statusFilter) params.status = statusFilter;
      const res = await listInvoices(params);
      setInvoices(res?.content || []);
      setTotalPages(res?.totalPages || 0);
      setTotalElements(res?.totalElements || 0);
    } catch (e) {
      setError(e);
    } finally {
      setLoading(false);
    }
  }, [search, statusFilter, page]);

  useEffect(() => { loadInvoices(); }, [loadInvoices]);

  const handleSubmit = async (data) => {
    setSubmitting(true);
    try {
      const payload = {
        ...data,
        issueDate: new Date(data.issueDate).toISOString(),
        dueDate: new Date(data.dueDate).toISOString(),
        discountAmount: data.discountAmount || 0,
        taxAmount: data.taxAmount || 0,
        items: data.items.map((item) => ({
          description: item.description,
          quantity: Number(item.quantity),
          unitPrice: Number(item.unitPrice),
        })),
      };
      if (editing) {
        await updateInvoice(editing.id, payload);
        toast.success("Invoice updated");
      } else {
        await createInvoice(payload);
        toast.success("Invoice created");
      }
      setShowModal(false);
      setEditing(null);
      form.reset();
      loadInvoices(0);
    } catch (e) {
      toast.error(e.response?.data?.message || "Failed to save invoice");
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (inv) => {
    setEditing(inv);
    form.reset({
      invoiceNumber: inv.invoiceNumber,
      customerId: inv.customerId,
      projectId: inv.projectId || "",
      opportunityId: inv.opportunityId || "",
      issueDate: inv.issueDate?.split("T")[0] || "",
      dueDate: inv.dueDate?.split("T")[0] || "",
      discountAmount: inv.discountAmount || 0,
      taxAmount: inv.taxAmount || 0,
      notes: inv.notes || "",
      items: inv.items?.length > 0 ? inv.items.map((i) => ({
        description: i.description,
        quantity: i.quantity,
        unitPrice: i.unitPrice,
      })) : [{ description: "", quantity: 1, unitPrice: 0 }],
    });
    setShowModal(true);
  };

  const handleDelete = (id) => {
    setConfirmAction(() => async () => {
      try {
        await deleteInvoice(id);
        toast.success("Invoice deleted");
        loadInvoices();
      } catch (e) {
        toast.error("Failed to delete invoice");
      }
      setShowConfirmDialog(false);
    });
    setShowConfirmDialog(true);
  };

  const handleSend = async (id) => {
    try {
      await sendInvoice(id);
      toast.success("Invoice sent");
      loadInvoices();
    } catch (e) {
      toast.error(e.response?.data?.message || "Failed to send invoice");
    }
  };

  const handleMarkPaid = async (id) => {
    try {
      await markInvoicePaid(id);
      toast.success("Invoice marked as paid");
      loadInvoices();
    } catch (e) {
      toast.error(e.response?.data?.message || "Failed to mark paid");
    }
  };

  const columns = [
    {
      header: "Invoice #", accessor: "invoiceNumber", width: "150px",
      render: (row) => (
        <button onClick={() => navigate(`/w/${workspaceSlug}/finance/invoices/${row.id}`)}
          className="text-indigo-600 hover:text-indigo-800 font-medium">
          {row.invoiceNumber}
        </button>
      ),
    },
    {
      header: "Customer", accessor: "customerId", width: "120px",
      render: (row) => `#${row.customerId}`,
    },
    {
      header: "Amount", accessor: "totalAmount", width: "130px",
      render: (row) => `$${Number(row.totalAmount || 0).toLocaleString()}`,
    },
    {
      header: "Paid", accessor: "amountPaid", width: "110px",
      render: (row) => `$${Number(row.amountPaid || 0).toLocaleString()}`,
    },
    {
      header: "Balance", accessor: "balanceDue", width: "110px",
      render: (row) => `$${Number(row.balanceDue || 0).toLocaleString()}`,
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
      header: "Due Date", accessor: "dueDate", width: "130px",
      render: (row) => row.dueDate ? new Date(row.dueDate).toLocaleDateString() : "-",
    },
    {
      header: "Actions", width: "200px",
      render: (row) => (
        <div className="flex items-center gap-1">
          <button onClick={() => navigate(`/w/${workspaceSlug}/finance/invoices/${row.id}`)}
            className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-500 hover:text-indigo-600" title="View">
            <FileText className="h-4 w-4" />
          </button>
          <button onClick={() => handleEdit(row)}
            className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-500 hover:text-blue-600" title="Edit">
            <Edit2 className="h-4 w-4" />
          </button>
          {row.status === "DRAFT" && (
            <button onClick={() => handleSend(row.id)}
              className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-500 hover:text-emerald-600" title="Send">
              <Send className="h-4 w-4" />
            </button>
          )}
          {(row.status === "SENT" || row.status === "PARTIALLY_PAID" || row.status === "OVERDUE") && (
            <button onClick={() => handleMarkPaid(row.id)}
              className="p-1.5 rounded-lg hover:bg-slate-100 text-slate-500 hover:text-emerald-600" title="Mark Paid">
              <CheckCircle className="h-4 w-4" />
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

  const workspaceSlug = (() => {
    try {
      const parts = window.location.pathname.split("/");
      const idx = parts.indexOf("w");
      return idx >= 0 && idx + 1 < parts.length ? parts[idx + 1] : "";
    } catch { return ""; }
  })();

  if (error) return <ErrorState error={error} onRetry={() => { setError(null); loadInvoices(); }} />;

  if (loading && invoices.length === 0) {
    return (
      <div className="mx-auto max-w-7xl p-4 sm:p-6">
        <div className="mb-6 h-8 bg-slate-200 rounded w-1/4 animate-pulse"></div>
        <div className="mb-6 h-10 bg-slate-200 rounded w-full animate-pulse"></div>
        <table className="w-full">
          <thead>
            <tr>
              {["Invoice #", "Customer", "Amount", "Paid", "Balance", "Status", "Due Date", "Actions"].map((h) => (
                <th key={h} className="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {Array(6).fill(0).map((_, i) => <TableRowSkeleton key={i} columns={8} />)}
          </tbody>
        </table>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl p-4 sm:p-6">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Invoices</h1>
          <p className="text-slate-500">{totalElements} invoices</p>
        </div>
        <button onClick={() => { setEditing(null); form.reset(); setShowModal(true); }}
          className="flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-indigo-700">
          <Plus className="h-4 w-4" /> New Invoice
        </button>
      </div>

      <div className="mb-6 flex flex-col gap-3 sm:flex-row">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input type="text" placeholder="Search invoices..." value={search}
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
        data={invoices}
        loading={loading}
        searchable={false}
        emptyMessage="No invoices found"
        emptyDescription="Create your first invoice to start billing customers."
        emptyIcon={FileText}
        emptyAction={{ label: "New Invoice", icon: Plus, onClick: () => { setEditing(null); form.reset(); setShowModal(true); } }}
        serverSide
        totalPages={totalPages}
        currentPage={page}
        onPageChange={setPage}
        totalRecords={totalElements}
        pageSize={pageSize}
        striped
      />

      <Modal open={showModal} onClose={() => { setShowModal(false); setEditing(null); }}
        title={editing ? "Edit Invoice" : "Create Invoice"} size="lg">
        <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Invoice Number *</label>
              <input {...form.register("invoiceNumber")}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
              {form.formState.errors.invoiceNumber && <p className="text-red-500 text-xs mt-1">{form.formState.errors.invoiceNumber.message}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Customer ID *</label>
              <input type="number" {...form.register("customerId")}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
              {form.formState.errors.customerId && <p className="text-red-500 text-xs mt-1">{form.formState.errors.customerId.message}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Issue Date *</label>
              <input type="date" {...form.register("issueDate")}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Due Date *</label>
              <input type="date" {...form.register("dueDate")}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Discount</label>
              <input type="number" step="0.01" {...form.register("discountAmount")}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Tax</label>
              <input type="number" step="0.01" {...form.register("taxAmount")}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Notes</label>
            <textarea {...form.register("notes")} rows={2}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
          </div>

          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="block text-sm font-medium text-slate-700">Items</label>
              <button type="button" onClick={() => {
                const items = form.getValues("items") || [];
                form.setValue("items", [...items, { description: "", quantity: 1, unitPrice: 0 }]);
              }} className="text-xs text-indigo-600 hover:text-indigo-800">+ Add Item</button>
            </div>
            {form.watch("items")?.map((_, idx) => (
              <div key={idx} className="flex gap-2 mb-2 items-start">
                <div className="flex-1">
                  <input placeholder="Description" {...form.register(`items.${idx}.description`)}
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
                </div>
                <div className="w-20">
                  <input type="number" placeholder="Qty" {...form.register(`items.${idx}.quantity`)}
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
                </div>
                <div className="w-24">
                  <input type="number" step="0.01" placeholder="Price" {...form.register(`items.${idx}.unitPrice`)}
                    className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm focus:border-indigo-500 focus:outline-none" />
                </div>
                <button type="button" onClick={() => {
                  const items = form.getValues("items").filter((_, i) => i !== idx);
                  form.setValue("items", items);
                }} className="p-2 text-slate-400 hover:text-rose-500 mt-1">
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            ))}
            {form.formState.errors.items && <p className="text-red-500 text-xs">{form.formState.errors.items.message || form.formState.errors.items.root?.message}</p>}
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
        title="Delete Invoice"
        message="Are you sure you want to delete this invoice? This action cannot be undone."
        confirmText="Delete"
      />
    </div>
  );
}

