import { useEffect, useRef, useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { createProject, updateProject, getProjects } from "../api/projectApi";
import Button from "../../../components/Button.jsx";
import { toast } from "sonner";
import {
  Calendar,
  Flag,
  Tag,
  DollarSign,
  FileText,
  Users,
  Globe,
} from "lucide-react";
import { usePermission } from "../../../context/usePermission";

const EMPTY_DATA = {};

const projectSchema = z.object({
  name: z.string().min(1, "Name is required").max(100),
  description: z.string().max(2000).optional(),
  priority: z.enum(["LOW", "MEDIUM", "HIGH", "CRITICAL"]).optional(),
  tags: z.string().max(500).optional(),
  category: z.string().max(100).optional(),
  objectives: z.string().max(1000).optional(),
  budget: z.number().min(0).optional(),
  currency: z.string().max(10).optional(),
  startDate: z.string().optional(),
  endDate: z.string().optional(),
  isActive: z.boolean().optional(),
  isPublic: z.boolean().optional(),
  projectManagerId: z.string().optional(),
});

const priorityOptions = [
  {
    value: "LOW",
    label: "Low",
    color: "text-emerald-600",
    bg: "bg-emerald-50",
  },
  {
    value: "MEDIUM",
    label: "Medium",
    color: "text-amber-600",
    bg: "bg-amber-50",
  },
  {
    value: "HIGH",
    label: "High",
    color: "text-orange-600",
    bg: "bg-orange-50",
  },
  {
    value: "CRITICAL",
    label: "Critical",
    color: "text-red-600",
    bg: "bg-red-50",
  },
];

const categoryOptions = [
  "Web Development",
  "Mobile App",
  "Desktop Application",
  "Data Science",
  "Machine Learning",
  "DevOps",
  "UI/UX Design",
  "Marketing",
  "Research",
  "Other",
];

const ProjectFormModal = ({
  isOpen,
  onClose,
  onSuccess,
  projectId,
  initialData = EMPTY_DATA,
}) => {
  const [loading, setLoading] = useState(false);
  const [mode, setMode] = useState(projectId ? "edit" : "create");
  const { canCreateProject, canEditProject } = usePermission();
  const prevProjectIdRef = useRef(projectId);
  const prevInitialDataRef = useRef(initialData);

  const form = useForm({
    resolver: zodResolver(projectSchema),
    defaultValues: {
      name: initialData.name || "",
      description: initialData.description || "",
      priority: initialData.priority || "MEDIUM",
      tags: initialData.tags || "",
      category: initialData.category || "",
      objectives: initialData.objectives || "",
      budget: initialData.budget || "",
      currency: initialData.currency || "USD",
      startDate: initialData.startDate
        ? new Date(initialData.startDate).toISOString().split("T")[0]
        : "",
      endDate: initialData.endDate
        ? new Date(initialData.endDate).toISOString().split("T")[0]
        : "",
      isActive: initialData.isActive ?? true,
      isPublic: initialData.isPublic ?? false,
    },
  });

  useEffect(() => {
    const projectIdChanged = prevProjectIdRef.current !== projectId;
    const initialDataChanged = prevInitialDataRef.current !== initialData;

    if (!projectIdChanged && !initialDataChanged) return;

    prevProjectIdRef.current = projectId;
    prevInitialDataRef.current = initialData;

    const nextMode = projectId ? "edit" : "create";
    setMode(nextMode);
    form.reset({
      name: initialData.name || "",
      description: initialData.description || "",
      priority: initialData.priority || "MEDIUM",
      tags: initialData.tags || "",
      category: initialData.category || "",
      objectives: initialData.objectives || "",
      budget: initialData.budget || "",
      currency: initialData.currency || "USD",
      startDate: initialData.startDate
        ? new Date(initialData.startDate).toISOString().split("T")[0]
        : "",
      endDate: initialData.endDate
        ? new Date(initialData.endDate).toISOString().split("T")[0]
        : "",
      isActive: initialData.isActive ?? true,
      isPublic: initialData.isPublic ?? false,
    });
  }, [form, initialData, projectId]);

  const onSubmit = async (data) => {
    try {
      setLoading(true);

      // Convert form data
      const payload = {
        ...data,
        budget: data.budget ?? null,
        startDate: data.startDate || null,
        endDate: data.endDate || null,
      };
      delete payload.startDate;
      delete payload.endDate;

      if (mode === "create") {
        if (!canCreateProject()) {
          toast.error("You don't have permission to create projects");
          return;
        }
        await createProject(payload);
        toast.success("Project created successfully!");
      } else {
        if (!canEditProject()) {
          toast.error("You don't have permission to edit projects");
          return;
        }
        if (!projectId) return;
        await updateProject(projectId, payload);
        toast.success("Project updated successfully!");
      }

      form.reset();
      onSuccess?.();
    } catch (error) {
      console.error(error);
      toast.error("Failed to save project");
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    form.reset();
    onClose?.();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white/95 backdrop-blur-xl rounded-3xl shadow-2xl border border-white/50 w-full max-w-3xl max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="sticky top-0 z-10 bg-white/90 backdrop-blur-xl border-b border-gray-100 p-6 rounded-t-3xl">
          <div className="flex items-center justify-between">
            <h2 className="text-2xl font-bold text-gray-900">
              {mode === "create" ? "Create New Project" : "Edit Project"}
            </h2>
            <button
              onClick={handleClose}
              className="p-2 hover:bg-gray-100 rounded-xl transition-colors text-gray-500 hover:text-gray-900"
            >
              Γ£ò
            </button>
          </div>
        </div>

        {/* Form */}
        <form onSubmit={form.handleSubmit(onSubmit)} className="p-6 space-y-6">
          {/* Name */}
          <div>
            <label
              htmlFor="name"
              className="block text-sm font-semibold text-gray-700 mb-1"
            >
              Project Name *
            </label>
            <input
              id="name"
              {...form.register("name")}
              className="w-full px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              placeholder="Enter project name..."
            />
            {form.formState.errors.name && (
              <p className="text-red-500 text-sm mt-1">
                {form.formState.errors.name.message}
              </p>
            )}
          </div>

          {/* Description */}
          <div>
            <label
              htmlFor="description"
              className="block text-sm font-semibold text-gray-700 mb-1"
            >
              <span className="inline-flex items-center gap-2">
                <FileText className="w-4 h-4" />
                Description (optional)
              </span>
            </label>
            <textarea
              id="description"
              {...form.register("description")}
              rows={4}
              className="w-full px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-vertical"
              placeholder="Describe the project objectives, scope, and key deliverables..."
            />
          </div>

          {/* Priority & Category Row */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Priority */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">
                <span className="inline-flex items-center gap-2">
                  <Flag className="w-4 h-4" />
                  Priority
                </span>
              </label>
              <div className="grid grid-cols-4 gap-2">
                {priorityOptions.map((p) => (
                  <button
                    key={p.value}
                    type="button"
                    onClick={() => form.setValue("priority", p.value)}
                    className={`p-3 rounded-xl border-2 transition-all text-sm font-medium ${
                      form.watch("priority") === p.value
                        ? `border-indigo-500 ${p.bg} ${p.color}`
                        : "border-gray-200 hover:border-gray-300 text-gray-600"
                    }`}
                  >
                    {p.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Category */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">
                <span className="inline-flex items-center gap-2">
                  <Tag className="w-4 h-4" />
                  Category
                </span>
              </label>
              <select
                {...form.register("category")}
                className="w-full px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent bg-white"
              >
                <option value="">Select category...</option>
                {categoryOptions.map((cat) => (
                  <option key={cat} value={cat}>
                    {cat}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Tags */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">
              <span className="inline-flex items-center gap-2">
                <Tag className="w-4 h-4" />
                Tags (comma-separated)
              </span>
            </label>
            <input
              id="tags"
              {...form.register("tags")}
              className="w-full px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              placeholder="e.g., frontend, backend, urgent, client-A"
            />
            <p className="text-xs text-gray-500 mt-1">
              Separate tags with commas. These help with project discovery.
            </p>
          </div>

          {/* Objectives */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">
              Objectives & Goals (optional)
            </label>
            <textarea
              id="objectives"
              {...form.register("objectives")}
              rows={3}
              className="w-full px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-vertical"
              placeholder="Define the key objectives and success criteria for this project..."
            />
          </div>

          {/* Dates Row */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">
                <span className="inline-flex items-center gap-2">
                  <Calendar className="w-4 h-4" />
                  Start Date
                </span>
              </label>
              <input
                type="date"
                {...form.register("startDate")}
                className="w-full px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">
                End Date
              </label>
              <input
                type="date"
                {...form.register("endDate")}
                className="w-full px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              />
            </div>
          </div>

          {/* Budget */}
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">
              <span className="inline-flex items-center gap-2">
                <DollarSign className="w-4 h-4" />
                Budget (optional)
              </span>
            </label>
            <div className="flex gap-2">
              <select
                {...form.register("currency")}
                className="px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 bg-white"
              >
                <option value="USD">USD ($)</option>
                <option value="EUR">EUR (Γé¼)</option>
                <option value="GBP">GBP (┬ú)</option>
                <option value="PKR">PKR (Γé¿)</option>
              </select>
              <input
                type="number"
                step="0.01"
                min="0"
                {...form.register("budget", {
                  setValueAs: (v) => (v === "" ? undefined : parseFloat(v)),
                })}
                className="flex-1 px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                placeholder="0.00"
              />
            </div>
            {form.formState.errors.budget && (
              <p className="text-red-500 text-sm mt-1">
                {form.formState.errors.budget.message}
              </p>
            )}
          </div>

          {/* Toggle Options */}
          <div className="space-y-3 pt-4 border-t border-gray-100">
            <label className="flex items-center gap-3 cursor-pointer">
              <input
                type="checkbox"
                {...form.register("isActive")}
                className="w-5 h-5 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
              />
              <span className="text-sm font-medium text-gray-700">
                Project is active
              </span>
            </label>

            <label className="flex items-center gap-3 cursor-pointer">
              <input
                type="checkbox"
                {...form.register("isPublic")}
                className="w-5 h-5 rounded border-gray-300 text-indigo-600 focus:ring-indigo-500"
              />
              <span className="inline-flex items-center gap-2 text-sm font-medium text-gray-700">
                <Globe className="w-4 h-4" />
                Public project (visible to all users)
              </span>
            </label>
          </div>

          {/* Buttons */}
          <div className="flex gap-3 pt-4 border-t border-gray-100">
            <Button
              type="submit"
              disabled={loading}
              className="flex-1 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700"
            >
              {loading
                ? "Saving..."
                : mode === "create"
                  ? "Create Project"
                  : "Update Project"}
            </Button>

            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={loading}
              className="flex-1"
            >
              Cancel
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ProjectFormModal;
