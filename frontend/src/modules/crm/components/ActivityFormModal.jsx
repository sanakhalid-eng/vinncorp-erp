import { useState, useEffect } from "react";
import { X, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { createActivity, updateActivity } from "../api/crmApi";

const INITIAL = {
  type: "CALL",
  subject: "",
  description: "",
  contactId: "",
  customerId: "",
  leadId: "",
  opportunityId: "",
  scheduledAt: "",
};

const TYPES = ["CALL", "EMAIL", "MEETING", "TASK", "NOTE"];

export default function ActivityFormModal({ activity, onClose, onSaved }) {
  const [form, setForm] = useState(INITIAL);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (activity) {
      setForm({
        type: activity.type || "CALL",
        subject: activity.subject || "",
        description: activity.description || "",
        contactId: activity.contactId || "",
        customerId: activity.customerId || "",
        leadId: activity.leadId || "",
        opportunityId: activity.opportunityId || "",
        scheduledAt: activity.scheduledAt || "",
      });
    }
  }, [activity]);

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.subject.trim()) {
      toast.error("Subject is required");
      return;
    }
    setSaving(true);
    try {
      if (activity) {
        await updateActivity(activity.id, form);
        toast.success("Activity updated");
      } else {
        await createActivity(form);
        toast.success("Activity created");
      }
      onSaved();
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to save activity");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
          <h2 className="text-lg font-semibold text-slate-800">
            {activity ? "Edit Activity" : "New Activity"}
          </h2>
          <button onClick={onClose} className="rounded-lg p-1 text-slate-400 hover:bg-slate-100">
            <X className="h-5 w-5" />
          </button>
        </div>
        <form onSubmit={handleSubmit} className="px-6 py-4 space-y-4 max-h-[70vh] overflow-y-auto">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Type</label>
              <select value={form.type} onChange={set("type")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none">
                {TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Scheduled At</label>
              <input
                type="datetime-local"
                value={form.scheduledAt}
                onChange={set("scheduledAt")}
                className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none"
              />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Subject *</label>
            <input
              value={form.subject}
              onChange={set("subject")}
              className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
            <textarea
              value={form.description}
              onChange={set("description")}
              rows={3}
              className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none"
            />
          </div>
          <p className="text-xs text-slate-400">Link to at least one entity (contact, customer, lead, or opportunity)</p>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Contact ID</label>
              <input
                value={form.contactId}
                onChange={set("contactId")}
                placeholder="Optional"
                className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Customer ID</label>
              <input
                value={form.customerId}
                onChange={set("customerId")}
                placeholder="Optional"
                className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none"
              />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Lead ID</label>
              <input
                value={form.leadId}
                onChange={set("leadId")}
                placeholder="Optional"
                className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Opportunity ID</label>
              <input
                value={form.opportunityId}
                onChange={set("opportunityId")}
                placeholder="Optional"
                className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none"
              />
            </div>
          </div>
          <div className="flex justify-end gap-3 border-t border-slate-200 pt-4">
            <button type="button" onClick={onClose} className="rounded-xl px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100">
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
            >
              {saving && <Loader2 className="h-4 w-4 animate-spin" />}
              {activity ? "Update" : "Create"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
