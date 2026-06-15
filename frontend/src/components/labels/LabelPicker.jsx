import { useState, useRef, useEffect } from "react";
import { Plus, Tag, X } from "lucide-react";
import {
  getProjectLabels,
  createLabel,
  assignLabelsToTask,
} from "../../api/labelApi";
import notify from "../../lib/toast";
const PRESET_COLORS = [
  "#EF4444",
  "#F97316",
  "#F59E0B",
  "#84CC16",
  "#22C55E",
  "#06B6D4",
  "#3B82F6",
  "#6366F1",
  "#8B5CF6",
  "#EC4899",
  "#F43F5E",
  "#78716C",
  "#64748B",
  "#14B8A6",
  "#A855F7",
];
export default function LabelPicker({
  projectId,
  taskId,
  currentLabels,
  onLabelsChange,
}) {
  const [isOpen, setIsOpen] = useState(false);
  const [labels, setLabels] = useState([]);
  const [search, setSearch] = useState("");
  const [showCreate, setShowCreate] = useState(false);
  const [newName, setNewName] = useState("");
  const [newColor, setNewColor] = useState(PRESET_COLORS[0]);
  const [loading, setLoading] = useState(false);
  const dropdownRef = useRef(null);
  useEffect(() => {
    if (isOpen) fetchLabels();
  }, [isOpen]);
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
        setShowCreate(false);
        setSearch("");
      }
    };
    if (isOpen) document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [isOpen]);
  const fetchLabels = async () => {
    try {
      const res = await getProjectLabels(projectId);
      setLabels(res.data || []);
    } catch {
      setLabels([]);
    }
  };
  const handleToggle = async (label) => {
    const isSelected = currentLabels?.some((l) => l.id === label.id);
    if (isSelected) return;
    setLoading(true);
    try {
      await assignLabelsToTask(taskId, [label.id]);
      onLabelsChange?.([...(currentLabels || []), label]);
      notify.success(`Label "${label.name}" added`);
    } catch {
      notify.error("Failed to assign label");
    } finally {
      setLoading(false);
    }
  };
  const handleCreate = async () => {
    if (!newName.trim()) return;
    setLoading(true);
    try {
      const res = await createLabel(projectId, {
        name: newName.trim(),
        color: newColor,
      });
      const newLabel = res.data;
      setLabels((prev) => [...prev, newLabel]);
      await assignLabelsToTask(taskId, [newLabel.id]);
      onLabelsChange?.([...(currentLabels || []), newLabel]);
      setNewName("");
      setShowCreate(false);
      notify.success(`Label "${newLabel.name}" created and added`);
    } catch (err) {
      notify.error(err.response?.data?.message || "Failed to create label");
    } finally {
      setLoading(false);
    }
  };
  const assignedIds = new Set(currentLabels?.map((l) => l.id) || []);
  const filtered = labels.filter((l) =>
    l.name.toLowerCase().includes(search.toLowerCase()),
  );
  return (
    <div className="relative" ref={dropdownRef}>
       
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-1.5 rounded-md border border-gray-200 px-2.5 py-1 text-xs text-gray-500 hover:border-gray-300 hover:text-gray-700"
      >
         
        <Tag className="h-3.5 w-3.5" /> Labels 
      </button> 
      {isOpen && (
        <div className="absolute right-0 top-full z-50 mt-1 w-64 rounded-xl border border-gray-200 bg-white p-3 shadow-xl">
           
          {!showCreate && (
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search labels..."
              className="mb-2 w-full rounded-lg border border-gray-100 px-2.5 py-1.5 text-sm outline-none focus:border-blue-400"
              autoFocus
            />
          )} 
          {showCreate ? (
            <div className="space-y-2">
               
              <input
                type="text"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleCreate()}
                placeholder="Label name..."
                className="w-full rounded-lg border border-gray-100 px-2.5 py-1.5 text-sm outline-none focus:border-blue-400"
                autoFocus
              /> 
              <div className="flex flex-wrap gap-1.5">
                 
                {PRESET_COLORS.map((color) => (
                  <button
                    key={color}
                    onClick={() => setNewColor(color)}
                    className={`h-6 w-6 rounded-full transition-all ${newColor === color ? "ring-2 ring-offset-1 ring-gray-400 scale-110" : "hover:scale-110"}`}
                    style={{ backgroundColor: color }}
                  />
                ))} 
              </div> 
              <div className="flex gap-2">
                 
                <button
                  onClick={() => {
                    setShowCreate(false);
                    setNewName("");
                  }}
                  className="flex-1 rounded-lg border border-gray-200 px-3 py-1.5 text-xs text-gray-500 hover:bg-gray-50"
                >
                   
                  Cancel 
                </button> 
                <button
                  onClick={handleCreate}
                  disabled={loading || !newName.trim()}
                  className="flex-1 rounded-lg bg-blue-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-blue-700 disabled:opacity-50"
                >
                   
                  Create 
                </button> 
              </div> 
            </div>
          ) : (
            <>
               
              <div className="max-h-48 space-y-0.5 overflow-y-auto">
                 
                {filtered.length === 0 && !search && (
                  <p className="py-4 text-center text-xs text-gray-400">
                    No labels yet
                  </p>
                )} 
                {filtered.length === 0 && search && (
                  <p className="py-4 text-center text-xs text-gray-400">
                    No matching labels
                  </p>
                )} 
                {filtered.map((label) => {
                  const isAssigned = assignedIds.has(label.id);
                  return (
                    <button
                      key={label.id}
                      onClick={() => !isAssigned && handleToggle(label)}
                      disabled={isAssigned || loading}
                      className={`flex w-full items-center gap-2 rounded-md px-2 py-1.5 text-sm transition-colors ${isAssigned ? "cursor-default bg-gray-50 opacity-50" : "hover:bg-gray-50"}`}
                    >
                       
                      <span
                        className="h-3.5 w-3.5 rounded-full"
                        style={{ backgroundColor: label.color }}
                      /> 
                      <span className="flex-1 text-left text-gray-700">
                        {label.name}
                      </span> 
                      {isAssigned ? (
                        <span className="text-xs text-gray-400">Added</span>
                      ) : (
                        <span className="text-[10px] text-gray-400">
                          {label.usageCount || 0} tasks
                        </span>
                      )} 
                    </button>
                  );
                })} 
              </div> 
              <div className="mt-2 border-t pt-2">
                 
                <button
                  onClick={() => setShowCreate(true)}
                  className="flex w-full items-center gap-1.5 rounded-md px-2 py-1.5 text-xs font-medium text-blue-600 hover:bg-blue-50"
                >
                   
                  <Plus className="h-3.5 w-3.5" /> Create new label 
                </button> 
              </div> 
            </>
          )} 
        </div>
      )} 
    </div>
  );
}
