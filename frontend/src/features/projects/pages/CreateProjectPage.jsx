import { useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { createProject } from "../api/projectApi";
import { toast } from "sonner";
import { usePermission } from "../../../context/usePermission";
import { PageSkeleton } from "../../../components/LoadingSkeleton";
import Button from "../../../components/Button.jsx";
import {
  Plus,
  Settings,
  Calendar,
  Flag,
  Tag,
  DollarSign,
  FileText,
  Users,
  Globe,
} from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";

const CreateProjectPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { canCreateProject } = usePermission();

  const [loading, setLoading] = useState(true);
  const [submitLoading, setSubmitLoading] = useState(false);

  // Form schema (same as in ProjectFormModal)
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

  const form = useForm({
    resolver: zodResolver(projectSchema),
    defaultValues: {
      name: "",
      description: "",
      priority: "MEDIUM",
      tags: "",
      category: "",
      objectives: "",
      budget: "",
      currency: "USD",
      startDate: "",
      endDate: "",
      isActive: true,
      isPublic: false,
    },
  });

  const onSubmit = async (data) => {
    setSubmitLoading(true);
    try {
      // Convert form data (same logic as in ProjectFormModal)
      const payload = {
        ...data,
        budget: data.budget ?? null,
        startDate: data.startDate || null,
        endDate: data.endDate || null,
      };
      // Remove the date fields as they're handled in the payload conversion above
      delete payload.startDate;
      delete payload.endDate;

      await createProject(payload);
      toast.success("Project created successfully!");
      // Reset form
      form.reset({
        name: "",
        description: "",
        priority: "MEDIUM",
        tags: "",
        category: "",
        objectives: "",
        budget: "",
        currency: "USD",
        startDate: "",
        endDate: "",
        isActive: true,
        isPublic: false,
      });
      // Navigate back or to projects page
      navigate(-1); // Go back to previous page
    } catch (error) {
      console.error(error);
      toast.error("Failed to save project");
    } finally {
      setSubmitLoading(false);
    }
  };

  // Simulate loading state (in a real app, you might load user data or permissions)
  useEffect(() => {
    setLoading(false);
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen p-4">
        <PageSkeleton />
      </div>
    );
  }

  return (
    <div className="min-h-screen p-4 md:p-6">
      <div className="mb-6">
        <div className="flex items-center gap-3 mb-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-r from-primary-500 to-primary-600 shadow-lg">
            <Plus className="h-5 w-5 text-white" />
          </div>
          <h1 className="text-2xl font-bold text-surface-900 dark:text-surface-100">
            Create New Project
          </h1>
        </div>
        <p className="text-sm text-surface-500 dark:text-surface-400">
          Create a new project and define its details
        </p>
      </div>

      <div className="rounded-xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-800 p-6">
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
          {/* Name */}
          <div>
            <label
              htmlFor="name"
              className="block text-sm font-semibold text-surface-700 dark:text-surface-300 mb-1"
            >
              Project Name *
            </label>
            <input
              id="name"
              {...form.register("name")}
              className="w-full px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900"
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
              className="block text-sm font-semibold text-surface-700 dark:text-surface-300 mb-1"
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
              className="w-full px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900 resize-vertical"
              placeholder="Describe the project objectives, scope, and key deliverables..."
            />
          </div>

          {/* Priority & Category Row */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Priority */}
            <div>
              <label className="block text-sm font-semibold text-surface-700 dark:text-surface-300 mb-1">
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
                        ? `border-primary-500 ${p.bg} ${p.color}`
                        : "border-surface-200 dark:border-surface-700 hover:border-surface-300 text-surface-600 dark:text-surface-400"
                    }`}
                  >
                    {p.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Category */}
            <div>
              <label className="block text-sm font-semibold text-surface-700 dark:text-surface-300 mb-1">
                <span className="inline-flex items-center gap-2">
                  <Tag className="w-4 h-4" />
                  Category
                </span>
              </label>
              <select
                {...form.register("category")}
                className="w-full px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900"
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
            <label className="block text-sm font-semibold text-surface-700 dark:text-surface-300 mb-1">
              <span className="inline-flex items-center gap-2">
                <Tag className="w-4 h-4" />
                Tags (comma-separated)
              </span>
            </label>
            <input
              id="tags"
              {...form.register("tags")}
              className="w-full px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900"
              placeholder="e.g., frontend, backend, urgent, client-A"
            />
            <p className="text-xs text-surface-500 dark:text-surface-400 mt-1">
              Separate tags with commas. These help with project discovery.
            </p>
          </div>

          {/* Objectives */}
          <div>
            <label className="block text-sm font-semibold text-surface-700 dark:text-surface-300 mb-1">
              Objectives & Goals (optional)
            </label>
            <textarea
              id="objectives"
              {...form.register("objectives")}
              rows={3}
              className="w-full px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900 resize-vertical"
              placeholder="Define the key objectives and success criteria for this project..."
            />
          </div>

          {/* Dates Row */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-semibold text-surface-700 dark:text-surface-300 mb-1">
                <span className="inline-flex items-center gap-2">
                  <Calendar className="w-4 h-4" />
                  Start Date
                </span>
              </label>
              <input
                type="date"
                {...form.register("startDate")}
                className="w-full px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold text-surface-700 dark:text-surface-300 mb-1">
                End Date
              </label>
              <input
                type="date"
                {...form.register("endDate")}
                className="w-full px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900"
              />
            </div>
          </div>

          {/* Budget */}
          <div>
            <label className="block text-sm font-semibold text-surface-700 dark:text-surface-300 mb-1">
              <span className="inline-flex items-center gap-2">
                <DollarSign className="w-4 h-4" />
                Budget (optional)
              </span>
            </label>
            <div className="flex gap-2">
              <select
                {...form.register("currency")}
                className="px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900"
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
                className="flex-1 px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus-ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900"
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
          <div className="space-y-3 pt-4 border-t border-surface-200 dark:border-surface-700">
            <label className="flex items-center gap-3 cursor-pointer">
              <input
                type="checkbox"
                {...form.register("isActive")}
                className="w-5 h-5 rounded border-surface-300 dark:border-surface-600 text-primary-600 dark:text-primary-400 focus:ring-primary-500"
              />
              <span className="text-sm font-medium text-surface-600 dark:text-surface-400">
                Project is active
              </span>
            </label>

            <label className="flex items-center gap-3 cursor-pointer">
              <input
                type="checkbox"
                {...form.register("isPublic")}
                className="w-5 h-5 rounded border-surface-300 dark:border-surface-600 text-primary-600 dark:text-primary-400 focus:ring-primary-500"
              />
              <span className="inline-flex items-center gap-2 text-sm font-medium text-surface-600 dark:text-surface-400">
                <Globe className="w-4 h-4" />
                Public project (visible to all users)
              </span>
            </label>
          </div>

          {/* Buttons */}
          <div className="flex gap-3 pt-4 border-t border-surface-200 dark:border-surface-700">
            <Button
              type="submit"
              disabled={submitLoading}
              className="flex-1 bg-gradient-to-r from-primary-500 to-primary-600 hover:from-primary-600 hover:to-primary-700"
            >
              {submitLoading ? "Creating..." : "Create Project"}
            </Button>

            <Button
              type="button"
              variant="outline"
              onClick={() => navigate(-1)}
              disabled={submitLoading}
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

export default CreateProjectPage;
