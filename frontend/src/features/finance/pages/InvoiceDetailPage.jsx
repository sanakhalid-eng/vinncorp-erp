import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, DollarSign, Calendar, FileText, Send, CheckCircle } from "lucide-react";
import { toast } from "sonner";
import { getInvoice, sendInvoice, markInvoicePaid } from "../api/financeApi";
import { PageSkeleton } from "../../../components/LoadingSkeleton";
import ErrorState from "../../../components/ErrorState";

const STATUS_BADGE = {
  DRAFT: "bg-slate-100 text-slate-700",
  SENT: "bg-blue-100 text-blue-700",
  PARTIALLY_PAID: "bg-amber-100 text-amber-700",
  PAID: "bg-emerald-100 text-emerald-700",
  OVERDUE: "bg-rose-100 text-rose-700",
  CANCELLED: "bg-slate-200 text-slate-500",
};

export default function InvoiceDetailPage() {
  const { invoiceId } = useParams();
  const navigate = useNavigate();
  const [invoice, setInvoice] = useState(null);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState("summary");

  useEffect(() => {
    loadInvoice();
  }, [invoiceId]);

  const loadInvoice = async () => {
    setLoading(true);
    try {
      const res = await getInvoice(invoiceId);
      setInvoice(res);
    } catch (e) {
      toast.error("Failed to load invoice");
    } finally {
      setLoading(false);
    }
  };

  const handleSend = async () => {
    try {
      await sendInvoice(invoiceId);
      toast.success("Invoice sent");
      loadInvoice();
    } catch (e) {
      toast.error(e.response?.data?.message || "Failed to send invoice");
    }
  };

  const handleMarkPaid = async () => {
    try {
      await markInvoicePaid(invoiceId);
      toast.success("Invoice marked as paid");
      loadInvoice();
    } catch (e) {
      toast.error(e.response?.data?.message || "Failed to mark paid");
    }
  };

  const goBack = () => {
    const pathParts = window.location.pathname.split("/");
    const idx = pathParts.indexOf("finance");
    if (idx >= 0) {
      navigate(pathParts.slice(0, idx + 1).join("/") + "/invoices");
    } else {
      navigate(-1);
    }
  };

  if (loading) return <PageSkeleton />;
  if (!invoice) return <div className="p-8 text-center text-slate-500">Invoice not found</div>;

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <button onClick={goBack} className="flex items-center gap-2 text-slate-500 hover:text-slate-700 mb-6">
        <ArrowLeft className="h-4 w-4" /> Back to Invoices
      </button>

      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Invoice {invoice.invoiceNumber}</h1>
          <span className={`inline-block mt-1 px-3 py-1 rounded-full text-xs font-semibold ${STATUS_BADGE[invoice.status] || ""}`}>
            {invoice.status?.replace("_", " ")}
          </span>
        </div>
        <div className="flex items-center gap-2">
          {invoice.status === "DRAFT" && (
            <button onClick={handleSend}
              className="flex items-center gap-2 rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-emerald-700">
              <Send className="h-4 w-4" /> Send Invoice
            </button>
          )}
          {(invoice.status === "SENT" || invoice.status === "PARTIALLY_PAID" || invoice.status === "OVERDUE") && (
            <button onClick={handleMarkPaid}
              className="flex items-center gap-2 rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-emerald-700">
              <CheckCircle className="h-4 w-4" /> Mark Paid
            </button>
          )}
        </div>
      </div>

      <div className="mb-6 flex gap-4 border-b border-slate-200">
        {["summary", "items", "payments", "activity"].map((t) => (
          <button key={t} onClick={() => setTab(t)}
            className={`pb-3 text-sm font-medium capitalize border-b-2 transition ${tab === t ? "border-indigo-600 text-indigo-600" : "border-transparent text-slate-500 hover:text-slate-700"}`}>
            {t}
          </button>
        ))}
      </div>

      {tab === "summary" && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="rounded-xl border border-slate-200 bg-white p-6">
            <h3 className="text-sm font-semibold text-slate-500 uppercase tracking-wider mb-4">Details</h3>
            <dl className="space-y-3">
              <div className="flex justify-between"><dt className="text-slate-500">Customer ID</dt><dd className="font-medium">#{invoice.customerId}</dd></div>
              {invoice.projectId && <div className="flex justify-between"><dt className="text-slate-500">Project ID</dt><dd className="font-medium">#{invoice.projectId}</dd></div>}
              {invoice.opportunityId && <div className="flex justify-between"><dt className="text-slate-500">Opportunity ID</dt><dd className="font-medium">#{invoice.opportunityId}</dd></div>}
              <div className="flex justify-between"><dt className="text-slate-500">Issue Date</dt><dd className="font-medium">{new Date(invoice.issueDate).toLocaleDateString()}</dd></div>
              <div className="flex justify-between"><dt className="text-slate-500">Due Date</dt><dd className="font-medium">{new Date(invoice.dueDate).toLocaleDateString()}</dd></div>
              {invoice.sentAt && <div className="flex justify-between"><dt className="text-slate-500">Sent At</dt><dd className="font-medium">{new Date(invoice.sentAt).toLocaleString()}</dd></div>}
              {invoice.paidAt && <div className="flex justify-between"><dt className="text-slate-500">Paid At</dt><dd className="font-medium">{new Date(invoice.paidAt).toLocaleString()}</dd></div>}
              {invoice.notes && <div className="pt-2"><dt className="text-slate-500 mb-1">Notes</dt><dd className="text-sm">{invoice.notes}</dd></div>}
            </dl>
          </div>

          <div className="rounded-xl border border-slate-200 bg-white p-6">
            <h3 className="text-sm font-semibold text-slate-500 uppercase tracking-wider mb-4">Totals</h3>
            <dl className="space-y-3">
              <div className="flex justify-between"><dt className="text-slate-500">Subtotal</dt><dd className="font-medium">${Number(invoice.subtotal || 0).toLocaleString()}</dd></div>
              <div className="flex justify-between"><dt className="text-slate-500">Discount</dt><dd className="font-medium text-rose-600">-${Number(invoice.discountAmount || 0).toLocaleString()}</dd></div>
              <div className="flex justify-between"><dt className="text-slate-500">Tax</dt><dd className="font-medium">${Number(invoice.taxAmount || 0).toLocaleString()}</dd></div>
              <div className="border-t pt-2 flex justify-between"><dt className="font-semibold text-slate-700">Total</dt><dd className="font-bold text-lg">${Number(invoice.totalAmount || 0).toLocaleString()}</dd></div>
              <div className="flex justify-between"><dt className="text-slate-500">Amount Paid</dt><dd className="font-medium text-emerald-600">${Number(invoice.amountPaid || 0).toLocaleString()}</dd></div>
              <div className="flex justify-between"><dt className="text-slate-500">Balance Due</dt><dd className="font-bold text-rose-600">${Number(invoice.balanceDue || 0).toLocaleString()}</dd></div>
            </dl>
          </div>
        </div>
      )}

      {tab === "items" && (
        <div className="rounded-xl border border-slate-200 bg-white overflow-hidden">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200">
              <tr>
                <th className="px-4 py-3 font-medium text-slate-600">Description</th>
                <th className="px-4 py-3 font-medium text-slate-600 text-right">Qty</th>
                <th className="px-4 py-3 font-medium text-slate-600 text-right">Unit Price</th>
                <th className="px-4 py-3 font-medium text-slate-600 text-right">Total</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {invoice.items?.map((item, idx) => (
                <tr key={item.id || idx} className="hover:bg-slate-50">
                  <td className="px-4 py-3">{item.description}</td>
                  <td className="px-4 py-3 text-right">{item.quantity}</td>
                  <td className="px-4 py-3 text-right">${Number(item.unitPrice).toLocaleString()}</td>
                  <td className="px-4 py-3 text-right font-medium">${Number(item.totalPrice).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === "payments" && (
        <div className="rounded-xl border border-slate-200 bg-white overflow-hidden">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b border-slate-200">
              <tr>
                <th className="px-4 py-3 font-medium text-slate-600">Date</th>
                <th className="px-4 py-3 font-medium text-slate-600">Method</th>
                <th className="px-4 py-3 font-medium text-slate-600">Reference</th>
                <th className="px-4 py-3 font-medium text-slate-600 text-right">Amount</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {invoice.payments?.length > 0 ? invoice.payments.map((p, idx) => (
                <tr key={p.id || idx} className="hover:bg-slate-50">
                  <td className="px-4 py-3">{new Date(p.paymentDate).toLocaleDateString()}</td>
                  <td className="px-4 py-3">{p.paymentMethod?.replace("_", " ")}</td>
                  <td className="px-4 py-3">{p.referenceNumber || "-"}</td>
                  <td className="px-4 py-3 text-right font-medium">${Number(p.amount).toLocaleString()}</td>
                </tr>
              )) : (
                <tr><td colSpan={4} className="px-4 py-8 text-center text-slate-500">No payments recorded</td></tr>
              )}
            </tbody>
          </table>
        </div>
      )}

      {tab === "activity" && (
        <div className="rounded-xl border border-slate-200 bg-white p-6">
          <p className="text-center text-slate-500">Activity log coming soon</p>
        </div>
      )}
    </div>
  );
}

