import { useState, useEffect } from "react";
import { X, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { createOpportunity, updateOpportunity } from "../api/crmApi";

export default function OpportunityFormModal({ opportunity, pipelines, stages, onClose, onSaved }) {
  const [form, setForm] = useState({
    title: "",
    value: "",
    stageId: "",
    contactName: "",
    customerName: "",
    notes: "",
  });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (opportunity) {
      setForm({
        title: opportunity.title || "",
        value: opportunity.value ?? "",
        stageId: opportunity.stage?.id || opportunity.stageId || "",
        contactName: opportunity.contactName || "",
        customerName: opportunity.customerName || "",
        notes: opportunity.notes || "",
      });
    } else if (stages.length > 0) {
      setForm((prev) => ({ ...prev, stageId: stages[0].id }));
    }
  }, [opportunity, stages]);

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.title.trim()) {
      toast.error("Title is required");
      return;
    }
    if (!form.stageId) {
      toast.error("Pipeline stage is required");
      return;
    }
    setSaving(true);
    try {
      const payload = {
        ...form,
        value: form.value ? Number(form.value) : null,
        stage: { id: form.stageId },
      };
      if (opportunity) {
        await updateOpportunity(opportunity.id, payload);
        toast.success("Opportunity updated");
      } else {
        await createOpportunity(payload);
        toast.success("Opportunity created");
      }
      onSaved();
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to save opportunity");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-lg rounded-2xl bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
          <h2 className="text-lg font-semibold text-slate-800">
            {opportunity ? "Edit Opportunity" : "New Opportunity"}
          </h2>
          <button onClick={onClose} className="rounded-lg p-1 text-slate-400 hover:bg-slate-100">
            <X className="h-5 w-5" />
          </button>
        </div>
        <form onSubmit={handleSubmit} className="px-6 py-4 space-y-4 max-h-[70vh] overflow-y-auto">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Title *</label>
            <input
              value={form.title}
              onChange={set("title")}
              className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none"
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Value ($)</label>
              <input
                type="number"
                value={form.value}
                onChange={set("value")}
                className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Stage *</label>
              <select value={form.stageId} onChange={set("stageId")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none">
                <option value="">Select stage</option>
                {stages.map((s) => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Contact Name</label>
              <input
                value={form.contactName}
                onChange={set("contactName")}
                className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Customer Name</label>
              <input
                value={form.customerName}
                onChange={set("customerName")}
                className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none"
              />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Notes</label>
            <textarea
              value={form.notes}
              onChange={set("notes")}
              rows={3}
              className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none"
            />
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
              {opportunity ? "Update" : "Create"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
