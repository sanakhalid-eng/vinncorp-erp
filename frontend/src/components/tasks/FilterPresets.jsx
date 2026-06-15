import { useState, useEffect, useCallback } from "react";
import { BookmarkPlus, Bookmark, X, ChevronDown } from "lucide-react";
import { toast } from "sonner";
const STORAGE_KEY = (pid) => `task-filter-presets-${pid}`;
function loadPresets(projectId) {
  try {
    const stored = localStorage.getItem(STORAGE_KEY(projectId));
    return stored ? JSON.parse(stored) : [];
  } catch {
    return [];
  }
}
function savePresets(projectId, presets) {
  try {
    localStorage.setItem(STORAGE_KEY(projectId), JSON.stringify(presets));
  } catch {
    /* localStorage full — ignore */
  }
}
export default function FilterPresets({ projectId, currentFilters, onApply }) {
  const [presets, setPresets] = useState([]);
  const [open, setOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [name, setName] = useState("");
  useEffect(() => {
    setPresets(loadPresets(projectId));
  }, [projectId]);
  const handleSave = useCallback(() => {
    const trimmed = name.trim();
    if (!trimmed) return;
    const existing = loadPresets(projectId);
    const next = [
      ...existing,
      { name: trimmed, filters: { ...currentFilters, page: 0 } },
    ];
    savePresets(projectId, next);
    setPresets(next);
    setName("");
    setSaving(false);
    toast.success(`Filter "${trimmed}" saved`);
  }, [name, currentFilters, projectId]);
  const handleDelete = useCallback(
    (index) => {
      const next = presets.filter((_, i) => i !== index);
      savePresets(projectId, next);
      setPresets(next);
      toast.success("Filter preset deleted");
    },
    [presets, projectId],
  );
  const handleApply = useCallback(
    (preset) => {
      onApply(preset.filters);
      setOpen(false);
    },
    [onApply],
  );
  if (!projectId) return null;
  return (
    <div className="relative">
       
      <button
        onClick={() => setOpen(!open)}
        className="flex items-center gap-1.5 rounded-xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-800 px-3 py-2 text-xs font-semibold text-surface-600 dark:text-surface-400 hover:bg-surface-50 dark:hover:bg-surface-700 transition-colors"
      >
         
        <Bookmark className="h-3.5 w-3.5" /> Presets 
        {presets.length > 0 && (
          <span className="ml-0.5 rounded-full bg-primary-100 dark:bg-primary-900/40 px-1.5 py-0.5 text-[10px] font-bold text-primary-700 dark:text-primary-400">
             
            {presets.length} 
          </span>
        )} 
        <ChevronDown className="h-3 w-3 text-surface-400" /> 
      </button> 
      {open && (
        <>
           
          <div
            className="fixed inset-0 z-40"
            onClick={() => setOpen(false)}
          /> 
          <div className="absolute left-0 top-full z-50 mt-1 min-w-[220px] rounded-xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-800 py-2 shadow-lg">
             
            <div className="px-3 pb-1 text-[10px] font-bold uppercase tracking-wider text-surface-500 dark:text-surface-400">
               
              Saved Filters 
            </div> 
            {presets.length === 0 && (
              <div className="px-3 py-3 text-center text-xs text-surface-400">
                 
                No saved filters yet 
              </div>
            )} 
            {presets.map((preset, index) => (
              <div
                key={index}
                className="group flex items-center justify-between px-3 py-1.5 hover:bg-surface-50 dark:hover:bg-surface-700"
              >
                 
                <button
                  onClick={() => handleApply(preset)}
                  className="flex-1 text-left text-sm text-surface-700 dark:text-surface-300"
                >
                   
                  {preset.name} 
                </button> 
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDelete(index);
                  }}
                  className="opacity-0 group-hover:opacity-100 rounded-md p-1 text-surface-400 hover:text-danger-500 transition-all"
                  aria-label={`Delete preset ${preset.name}`}
                >
                   
                  <X className="h-3 w-3" /> 
                </button> 
              </div>
            ))} 
            <div className="border-t border-surface-200 dark:border-surface-700 mt-2 pt-2 px-3">
               
              {saving ? (
                <div className="flex items-center gap-1.5">
                   
                  <input
                    autoFocus
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") handleSave();
                      if (e.key === "Escape") setSaving(false);
                    }}
                    placeholder="Preset name..."
                    className="flex-1 rounded-lg border border-surface-200 bg-surface-50 px-2 py-1 text-xs focus:outline-none focus:ring-1 focus:ring-primary-500"
                  /> 
                  <button
                    onClick={handleSave}
                    disabled={!name.trim()}
                    className="rounded-lg bg-primary-600 px-2 py-1 text-[10px] font-bold text-white disabled:opacity-50 hover:bg-primary-700"
                  >
                     
                    Save 
                  </button> 
                  <button
                    onClick={() => {
                      setSaving(false);
                      setName("");
                    }}
                    className="rounded-lg px-2 py-1 text-[10px] text-surface-500 hover:bg-surface-100"
                  >
                     
                    X 
                  </button> 
                </div>
              ) : (
                <button
                  onClick={() => setSaving(true)}
                  className="flex w-full items-center gap-1.5 rounded-lg px-2 py-1.5 text-xs text-primary-600 dark:text-primary-400 hover:bg-primary-50 dark:hover:bg-primary-900/20 transition-colors"
                >
                   
                  <BookmarkPlus className="h-3.5 w-3.5" /> Save current
                  filter 
                </button>
              )} 
            </div> 
          </div> 
        </>
      )} 
    </div>
  );
}
