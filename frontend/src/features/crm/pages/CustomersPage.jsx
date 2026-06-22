import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Plus,
  Search,
  Edit2,
  Trash2,
  Users,
  ExternalLink,
} from "lucide-react";
import { toast } from "sonner";
import { listCustomers, deleteCustomer } from "../api/crmApi";
import CustomerFormModal from "../components/CustomerFormModal";
import { CardSkeleton } from "../../../components/LoadingSkeleton";
import EmptyState from "../../../components/EmptyState";
import ErrorState from "../../../components/ErrorState";
import ConfirmationDialog from "../../projects/components/members/ConfirmationDialog";

export default function CustomersPage() {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [confirmDeleteId, setConfirmDeleteId] = useState(null);

  // Extract workspaceSlug from URL
  const workspaceSlug = window.location.pathname.split("/")[2] || "workspace";

  useEffect(() => {
    loadCustomers();
  }, []);

  const loadCustomers = async () => {
    setLoading(true);
    try {
      const res = await listCustomers(0, 100);
      setCustomers(Array.isArray(res) ? res : res?.content || []);
      setError(null);
    } catch (e) {
      setError(e);
    } finally {
      setLoading(false);
    }
  };

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return customers.filter((c) => {
      if (!q) return true;
      return (
        c.name?.toLowerCase().includes(q) ||
        c.email?.toLowerCase().includes(q) ||
        c.phone?.toLowerCase().includes(q)
      );
    });
  }, [customers, search]);

  const handleDelete = async (id) => {
    setConfirmDeleteId(id);
    setShowConfirmDialog(true);
  };

  const confirmDelete = async () => {
    if (!confirmDeleteId) return;
    try {
      await deleteCustomer(confirmDeleteId);
      toast.success("Customer deleted");
      loadCustomers();
    } catch (e) {
      toast.error(e.message || "Failed to delete customer");
    } finally {
      setShowConfirmDialog(false);
      setConfirmDeleteId(null);
    }
  };

  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Customers</h1>
          <p className="text-slate-500">{filtered.length} customers</p>
        </div>
        <button
          onClick={() => { setEditing(null); setShowModal(true); }}
          className="flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-indigo-700"
        >
          <Plus className="h-4 w-4" /> New Customer
        </button>
      </div>

      <div className="mb-6">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search customers..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-10 pr-4 text-sm focus:border-indigo-500 focus:outline-none"
          />
        </div>
      </div>

      {loading ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => <CardSkeleton key={i} />)}
        </div>
      ) : error ? (
        <ErrorState error={error} onRetry={loadCustomers} />
      ) : filtered.length === 0 ? (
        <EmptyState
          icon={Users}
          title="No customers yet"
          description="Add customers to track accounts, contacts, and opportunities."
          action={{ label: "New Customer", icon: Plus, onClick: () => { setEditing(null); setShowModal(true); } }}
        />
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {filtered.map((customer) => (
            <div
              key={customer.id}
              onClick={() => navigate(`/w/${workspaceSlug}/crm/customers/${customer.id}`)}
              className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:shadow-md cursor-pointer"
            >
              <div className="mb-3 flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br from-indigo-500 to-fuchsia-500 text-sm font-bold text-white">
                  {customer.name?.charAt(0)?.toUpperCase() || "C"}
                </div>
                <div className="min-w-0 flex-1">
                  <p className="truncate font-semibold text-slate-800">{customer.name}</p>
                  {customer.industry && (
                    <p className="text-xs text-slate-400">{customer.industry}</p>
                  )}
                </div>
                <ExternalLink className="h-4 w-4 text-slate-400" />
              </div>
              {customer.email && (
                <p className="mb-1 text-sm text-slate-500 truncate">{customer.email}</p>
              )}
              {customer.phone && (
                <p className="mb-3 text-sm text-slate-500">{customer.phone}</p>
              )}
              <div className="flex items-center gap-2 border-t border-slate-100 pt-3">
                <button
                  onClick={(e) => { e.stopPropagation(); setEditing(customer); setShowModal(true); }}
                  className="flex items-center gap-1 rounded-lg px-2 py-1 text-xs font-medium text-slate-500 hover:bg-slate-100"
                >
                  <Edit2 className="h-3 w-3" /> Edit
                </button>
                <button
                  onClick={(e) => { e.stopPropagation(); handleDelete(customer.id); }}
                  className="flex items-center gap-1 rounded-lg px-2 py-1 text-xs font-medium text-rose-500 hover:bg-rose-50"
                >
                  <Trash2 className="h-3 w-3" /> Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <CustomerFormModal
          customer={editing}
          onClose={() => { setShowModal(false); setEditing(null); }}
          onSaved={() => { setShowModal(false); setEditing(null); loadCustomers(); }}
        />
      )}

      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={() => { setShowConfirmDialog(false); setConfirmDeleteId(null); }}
        onConfirm={confirmDelete}
        title="Delete Customer"
        message="Are you sure you want to delete this customer?"
        confirmText="Delete"
      />
    </div>
  );
}
