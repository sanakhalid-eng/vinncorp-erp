import { Fragment, useState, useEffect } from "react";
import { Dialog, Transition } from "@headlessui/react";
import { X, Palette, ListOrdered, Settings, Loader2 } from "lucide-react";
import { createStatus, updateStatus } from "../../api/statusApi.js";
import { toast } from "sonner";

const StatusFormModal = ({ isOpen, onClose, status, projectId, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: "",
    color: "#f59e0b",
    orderIndex: 0,
  });

  useEffect(() => {
    if (status) {
      setFormData({
        name: status.name || "",
        color: status.color || "#f59e0b",
        orderIndex: status.orderIndex || 0,
      });
    } else {
      setFormData((prev) => ({ ...prev, orderIndex: 999 }));
    }
  }, [status]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.name.trim() || !projectId) return;
    setLoading(true);
    try {
      if (status) {
        await updateStatus(projectId, status.id, formData);
        toast.success("Status updated");
      } else {
        await createStatus(projectId, formData);
        toast.success("Status created");
      }
      onSuccess();
      onClose();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to save status");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Transition appear show={isOpen} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={loading ? () => {} : onClose}>
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-black/50 backdrop-blur-sm" />
        </Transition.Child>

        <div className="fixed inset-0 overflow-y-auto">
          <div className="flex min-h-full items-center justify-center p-4">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 scale-95"
              enterTo="opacity-100 scale-100"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 scale-100"
              leaveTo="opacity-0 scale-95"
            >
              <Dialog.Panel className="w-full max-w-md transform overflow-hidden rounded-2xl bg-white dark:bg-surface-900 shadow-soft-lg border border-surface-200/50 dark:border-surface-800/50 transition-all">
                <div className="flex items-center justify-between px-6 py-4 border-b border-surface-200 dark:border-surface-800">
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded-lg bg-primary-100 dark:bg-primary-900/30">
                      <Settings className="h-5 w-5 text-primary-600 dark:text-primary-400" />
                    </div>
                    <div>
                      <Dialog.Title className="text-lg font-semibold text-surface-900 dark:text-surface-100">
                        {status ? "Edit Status" : "New Status"}
                      </Dialog.Title>
                      <Dialog.Description className="text-sm text-surface-500 dark:text-surface-400">
                        {status ? "Update workflow stage" : "Add a new workflow stage"}
                      </Dialog.Description>
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={onClose}
                    className="rounded-lg p-1.5 text-surface-400 hover:text-surface-600 hover:bg-surface-100 dark:hover:text-surface-200 dark:hover:bg-surface-800 transition-colors"
                    disabled={loading}
                  >
                    <X className="h-5 w-5" />
                  </button>
                </div>

                <form id="status-form" onSubmit={handleSubmit} className="p-6 space-y-5">
                  <div>
                    <label className="mb-1.5 flex items-center gap-1.5 text-sm font-medium text-surface-700 dark:text-surface-300">
                      <ListOrdered className="h-4 w-4" /> Status Name <span className="text-danger-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={formData.name}
                      onChange={(e) =>
                        setFormData({ ...formData, name: e.target.value })
                      }
                      className="input-field"
                      placeholder="e.g. In Review"
                      maxLength={20}
                      required
                      disabled={loading}
                    />
                  </div>

                  <div>
                    <label className="mb-1.5 flex items-center gap-1.5 text-sm font-medium text-surface-700 dark:text-surface-300">
                      <Palette className="h-4 w-4" /> Column Color
                    </label>
                    <div className="relative">
                      <div
                        className="input-field flex items-center gap-3 cursor-pointer"
                        style={{
                          backgroundColor: formData.color + "20",
                          borderColor: formData.color,
                        }}
                      >
                        <div
                          className="w-8 h-8 rounded-lg shadow-sm shrink-0"
                          style={{ backgroundColor: formData.color }}
                        />
                        <span className="font-mono text-sm text-surface-700 dark:text-surface-300">
                          {formData.color.toUpperCase()}
                        </span>
                      </div>
                      <input
                        type="color"
                        value={formData.color}
                        onChange={(e) =>
                          setFormData({ ...formData, color: e.target.value })
                        }
                        className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                        disabled={loading}
                      />
                    </div>
                  </div>

                  <input type="hidden" value={formData.orderIndex} />
                </form>

                <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-surface-200 dark:border-surface-800 bg-surface-50/50 dark:bg-surface-900/50">
                  <button
                    type="button"
                    onClick={onClose}
                    className="btn-secondary"
                    disabled={loading}
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    form="status-form"
                    disabled={loading || !formData.name.trim()}
                    className="btn-primary"
                  >
                    {loading ? (
                      <>
                        <Loader2 className="h-4 w-4 animate-spin" />
                        Saving...
                      </>
                    ) : status ? (
                      "Update Status"
                    ) : (
                      "Create Status"
                    )}
                  </button>
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition>
  );
};

export default StatusFormModal;
