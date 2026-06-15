import { Fragment, useState, useEffect } from "react";
import { Dialog, Transition } from "@headlessui/react";
import { X, ArrowRight, Plus, Shield, Loader2 } from "lucide-react";
import { createTransition, validateTransition } from "../../api/statusApi.js";
import StatusBadge from "../tasks/StatusBadge.jsx";
import RuleChip from "./RuleChip.jsx";
import { toast } from "sonner";

const TransitionFormModal = ({
  isOpen,
  onClose,
  statuses = [],
  projectId,
  onSuccess,
}) => {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    fromStatusId: "",
    toStatusId: "",
    rules: [],
  });
  const [validation, setValidation] = useState({ valid: true, message: "" });
  const [newRule, setNewRule] = useState({
    type: "role",
    field: "priority",
    operator: "=",
    value: "",
  });

  useEffect(() => {
    if (isOpen) {
      setFormData({ fromStatusId: "", toStatusId: "", rules: [] });
      setValidation({ valid: true, message: "" });
      setNewRule({ type: "role", field: "priority", operator: "=", value: "" });
    }
  }, [isOpen]);

  const handleFromChange = async (e) => {
    const value = e.target.value;
    setFormData({ ...formData, fromStatusId: value });
    if (value && formData.toStatusId) {
      await checkValidation(value, formData.toStatusId);
    }
  };

  const handleToChange = async (e) => {
    const value = e.target.value;
    setFormData({ ...formData, toStatusId: value });
    if (formData.fromStatusId && value) {
      await checkValidation(formData.fromStatusId, value);
    }
  };

  const checkValidation = async (fromId, toId) => {
    const result = await validateTransition(projectId, fromId, toId);
    setValidation(result);
  };

  const addRule = () => {
    if (!newRule.value.trim()) return;
    const rule = { ...newRule };
    setFormData((prev) => ({ ...prev, rules: [...prev.rules, rule] }));
    setNewRule({ type: "role", field: "priority", operator: "=", value: "" });
  };

  const removeRule = (index) => {
    setFormData((prev) => ({
      ...prev,
      rules: prev.rules.filter((_, i) => i !== index),
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.fromStatusId || !formData.toStatusId || !validation.valid) return;
    setLoading(true);
    try {
      await createTransition(projectId, formData);
      toast.success("Transition created");
      onSuccess();
      onClose();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to create transition");
    } finally {
      setLoading(false);
    }
  };

  const fromStatus = statuses.find((s) => s.id === formData.fromStatusId);
  const toStatus = statuses.find((s) => s.id === formData.toStatusId);

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
              <Dialog.Panel className="w-full max-w-lg transform overflow-hidden rounded-2xl bg-white dark:bg-surface-900 shadow-soft-lg border border-surface-200/50 dark:border-surface-800/50 transition-all">
                <div className="flex items-center justify-between px-6 py-4 border-b border-surface-200 dark:border-surface-800">
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded-lg bg-primary-100 dark:bg-primary-900/30">
                      <ArrowRight className="h-5 w-5 text-primary-600 dark:text-primary-400" />
                    </div>
                    <div>
                      <Dialog.Title className="text-lg font-semibold text-surface-900 dark:text-surface-100">
                        New Transition
                      </Dialog.Title>
                      <Dialog.Description className="text-sm text-surface-500 dark:text-surface-400">
                        Define allowed task movement between statuses
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

                {!validation.valid && (
                  <div className="mx-6 mt-4 p-3 rounded-xl bg-danger-50 dark:bg-danger-900/20 border border-danger-200 dark:border-danger-800/50">
                    <p className="text-sm font-medium text-danger-800 dark:text-danger-300">
                      {validation.message}
                    </p>
                  </div>
                )}

                <form id="transition-form" onSubmit={handleSubmit} className="p-6 space-y-5">
                  <div>
                    <label className="mb-1.5 text-sm font-medium text-surface-700 dark:text-surface-300">
                      From Status <span className="text-danger-500">*</span>
                    </label>
                    <select
                      value={formData.fromStatusId}
                      onChange={handleFromChange}
                      className="input-field"
                      disabled={loading}
                      required
                    >
                      <option value="">Select from status...</option>
                      {statuses.map((status) => (
                        <option key={status.id} value={status.id}>
                          {status.name}
                        </option>
                      ))}
                    </select>
                    {fromStatus && (
                      <div className="mt-2">
                        <StatusBadge
                          status={fromStatus.name}
                          color={fromStatus.color}
                        />
                      </div>
                    )}
                  </div>

                  <div className="flex items-center gap-3 p-4 rounded-xl bg-surface-50 dark:bg-surface-800/50 border border-surface-200 dark:border-surface-700">
                    <div className="flex-1">
                      <label className="mb-1.5 text-sm font-medium text-surface-700 dark:text-surface-300">
                        To Status <span className="text-danger-500">*</span>
                      </label>
                      <select
                        value={formData.toStatusId}
                        onChange={handleToChange}
                        className="input-field"
                        disabled={loading}
                        required
                      >
                        <option value="">Select to status...</option>
                        {statuses.map((status) => (
                          <option key={status.id} value={status.id}>
                            {status.name}
                          </option>
                        ))}
                      </select>
                    </div>
                    <ArrowRight className="h-6 w-6 text-surface-400 shrink-0 mt-6" />
                    {toStatus && (
                      <div className="shrink-0 mt-6">
                        <StatusBadge
                          status={toStatus.name}
                          color={toStatus.color}
                        />
                      </div>
                    )}
                  </div>

                  <div>
                    <label className="mb-1.5 flex items-center gap-1.5 text-sm font-medium text-surface-700 dark:text-surface-300">
                      <Shield className="h-4 w-4" /> Access Rules <span className="text-xs text-surface-500">(AND conditions)</span>
                    </label>
                    <div className="space-y-3">
                      {formData.rules.length > 0 && (
                        <div className="flex flex-wrap gap-2 p-3 rounded-xl bg-surface-50 dark:bg-surface-800/50 border border-surface-200 dark:border-surface-700">
                          {formData.rules.map((rule, index) => (
                            <RuleChip
                              key={index}
                              rule={rule}
                              onRemove={() => removeRule(index)}
                            />
                          ))}
                        </div>
                      )}

                      <div className="p-4 rounded-xl bg-primary-50/50 dark:bg-primary-900/10 border border-primary-200 dark:border-primary-800/50 space-y-3">
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-3 items-end">
                          <div>
                            <label className="text-xs font-medium text-surface-600 dark:text-surface-400 mb-1 block">
                              Type
                            </label>
                            <select
                              value={newRule.type}
                              onChange={(e) =>
                                setNewRule({ ...newRule, type: e.target.value })
                              }
                              className="input-field text-sm"
                              disabled={loading}
                            >
                              <option value="role">Role (e.g. MANAGER)</option>
                              <option value="assignee">Assignee Only</option>
                              <option value="field">Field Value</option>
                            </select>
                          </div>

                          {newRule.type === "field" && (
                            <div>
                              <label className="text-xs font-medium text-surface-600 dark:text-surface-400 mb-1 block">
                                Field
                              </label>
                              <select
                                value={newRule.field || ""}
                                onChange={(e) =>
                                  setNewRule({ ...newRule, field: e.target.value })
                                }
                                className="input-field text-sm"
                                disabled={loading}
                              >
                                <option value="priority">Priority</option>
                              </select>
                            </div>
                          )}

                          <div>
                            <label className="text-xs font-medium text-surface-600 dark:text-surface-400 mb-1 block">
                              Value
                            </label>
                            <input
                              type="text"
                              value={newRule.value}
                              onChange={(e) =>
                                setNewRule({ ...newRule, value: e.target.value })
                              }
                              placeholder={
                                newRule.type === "role"
                                  ? "PROJECT_MANAGER"
                                  : newRule.type === "assignee"
                                    ? "true"
                                    : "HIGH"
                              }
                              className="input-field text-sm"
                              disabled={loading}
                            />
                          </div>
                        </div>

                        <button
                          type="button"
                          onClick={addRule}
                          disabled={!newRule.value.trim() || loading}
                          className="btn-primary btn-sm w-full"
                        >
                          <Plus className="h-4 w-4" />
                          Add Condition
                        </button>
                      </div>

                      {formData.rules.length === 0 && (
                        <p className="text-xs text-surface-500 dark:text-surface-400 text-center py-3 italic">
                          No restrictions — everyone can use this transition
                        </p>
                      )}
                    </div>
                  </div>
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
                    form="transition-form"
                    disabled={loading || !formData.fromStatusId || !formData.toStatusId || !validation.valid}
                    className="btn-primary"
                  >
                    {loading ? (
                      <>
                        <Loader2 className="h-4 w-4 animate-spin" />
                        Saving...
                      </>
                    ) : (
                      "Create Transition"
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

export default TransitionFormModal;
