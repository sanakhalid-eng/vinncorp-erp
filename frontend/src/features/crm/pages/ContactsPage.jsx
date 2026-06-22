import { useEffect, useMemo, useState } from "react";
import {
  Plus,
  Search,
  Edit2,
  Trash2,
  Phone,
  Mail,
} from "lucide-react";
import { toast } from "sonner";
import { listContacts, deleteContact } from "../api/crmApi";
import ContactFormModal from "../components/ContactFormModal";
import { TableRowSkeleton } from "../../../components/LoadingSkeleton";
import EmptyState from "../../../components/EmptyState";
import ErrorState from "../../../components/ErrorState";
import ConfirmationDialog from "../../projects/components/members/ConfirmationDialog";

export default function ContactsPage() {
  const [contacts, setContacts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [confirmDeleteId, setConfirmDeleteId] = useState(null);

  useEffect(() => {
    loadContacts();
  }, []);

  const loadContacts = async () => {
    setLoading(true);
    try {
      const res = await listContacts(0, 100);
      setContacts(Array.isArray(res) ? res : res?.content || []);
      setError(null);
    } catch (e) {
      setError(e);
    } finally {
      setLoading(false);
    }
  };

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return contacts.filter((c) => {
      if (!q) return true;
      return (
        c.firstName?.toLowerCase().includes(q) ||
        c.lastName?.toLowerCase().includes(q) ||
        c.email?.toLowerCase().includes(q) ||
        c.company?.toLowerCase().includes(q)
      );
    });
  }, [contacts, search]);

  const handleDelete = async (id) => {
    setConfirmDeleteId(id);
    setShowConfirmDialog(true);
  };

  const confirmDelete = async () => {
    if (!confirmDeleteId) return;
    try {
      await deleteContact(confirmDeleteId);
      toast.success("Contact deleted");
      loadContacts();
    } catch (e) {
      toast.error(e.message || "Failed to delete contact");
    } finally {
      setShowConfirmDialog(false);
      setConfirmDeleteId(null);
    }
  };

  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Contacts</h1>
          <p className="text-slate-500">{filtered.length} contacts</p>
        </div>
        <button
          onClick={() => { setEditing(null); setShowModal(true); }}
          className="flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-indigo-700"
        >
          <Plus className="h-4 w-4" /> New Contact
        </button>
      </div>

      <div className="mb-6">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search contacts..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-10 pr-4 text-sm focus:border-indigo-500 focus:outline-none"
          />
        </div>
      </div>

      {loading ? (
        <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-slate-200 bg-slate-50">
              <tr>
                {["Name", "Email", "Phone", "Company", "Job Title", "Actions"].map((h) => (
                  <th key={h} className="px-4 py-3 font-medium text-slate-600">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {Array.from({ length: 5 }).map((_, i) => <TableRowSkeleton key={i} columns={6} />)}
            </tbody>
          </table>
        </div>
      ) : error ? (
        <ErrorState error={error} onRetry={loadContacts} />
      ) : filtered.length === 0 ? (
        <EmptyState
          icon={Phone}
          title="No contacts yet"
          description="Add contacts to manage relationships with your customers and leads."
          action={{ label: "New Contact", icon: Plus, onClick: () => { setEditing(null); setShowModal(true); } }}
        />
      ) : (
        <div className="overflow-x-auto overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-slate-200 bg-slate-50">
              <tr>
                <th className="px-4 py-3 font-medium text-slate-600">Name</th>
                <th className="px-4 py-3 font-medium text-slate-600">Email</th>
                <th className="px-4 py-3 font-medium text-slate-600">Phone</th>
                <th className="px-4 py-3 font-medium text-slate-600">Company</th>
                <th className="px-4 py-3 font-medium text-slate-600">Job Title</th>
                <th className="px-4 py-3 font-medium text-slate-600">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map((contact) => (
                <tr key={contact.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium text-slate-800">
                    {contact.firstName} {contact.lastName}
                  </td>
                  <td className="px-4 py-3 text-slate-600">{contact.email || "-"}</td>
                  <td className="px-4 py-3 text-slate-600">{contact.phone || "-"}</td>
                  <td className="px-4 py-3 text-slate-600">{contact.company || "-"}</td>
                  <td className="px-4 py-3 text-slate-600">{contact.jobTitle || "-"}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => { setEditing(contact); setShowModal(true); }}
                        className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                      >
                        <Edit2 className="h-4 w-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(contact.id)}
                        className="rounded-lg p-1.5 text-slate-400 hover:bg-rose-50 hover:text-rose-600"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <ContactFormModal
          contact={editing}
          onClose={() => { setShowModal(false); setEditing(null); }}
          onSaved={() => { setShowModal(false); setEditing(null); loadContacts(); }}
        />
      )}

      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={() => { setShowConfirmDialog(false); setConfirmDeleteId(null); }}
        onConfirm={confirmDelete}
        title="Delete Contact"
        message="Are you sure you want to delete this contact?"
        confirmText="Delete"
      />
    </div>
  );
}
