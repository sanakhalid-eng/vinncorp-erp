import { useState, useEffect } from "react";
import {
  Trophy,
  LayoutGrid,
  Megaphone,
  Map,
  Loader2,
  Check,
  Sparkles,
} from "lucide-react";
import { getProjectTemplates } from "../../settings/api/workspaceApi";
import Button from "../../../components/Button";
const TEMPLATE_ICONS = { Trophy, LayoutGrid, Megaphone, Map };
const DEFAULT_ICON = Sparkles;
export default function TemplateSelector({ onSelect, onClose }) {
  const [templates, setTemplates] = useState([]);
  const [selected, setSelected] = useState(null);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    loadTemplates();
  }, []);
  const loadTemplates = async () => {
    try {
      const res = await getProjectTemplates();
      setTemplates(res.data.data || []);
    } catch {
      setTemplates([]);
    } finally {
      setLoading(false);
    }
  };
  const handleConfirm = () => {
    if (selected) onSelect?.(selected);
    else onSelect?.(null);
  };
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/45 backdrop-blur-sm p-4">
       
      <div className="w-full max-w-2xl rounded-2xl bg-white p-6 shadow-2xl">
         
        <div className="flex items-center justify-between mb-6">
           
          <div>
             
            <h3 className="text-xl font-bold text-slate-900">
              Project Templates
            </h3> 
            <p className="text-sm text-slate-500">
              Start faster with a pre-configured template
            </p> 
          </div> 
          <button
            onClick={onClose}
            className="rounded-xl p-2 text-slate-400 hover:bg-slate-100"
          >
             
            Γ£ò 
          </button> 
        </div> 
        {loading ? (
          <div className="flex justify-center py-12">
             
            <Loader2 className="w-8 h-8 animate-spin text-indigo-600" /> 
          </div>
        ) : (
          <div className="grid sm:grid-cols-2 gap-4 mb-6">
             
            <button
              onClick={() => setSelected(null)}
              className={`rounded-2xl border-2 p-5 text-left transition hover:shadow-md ${selected === null ? "border-indigo-500 bg-indigo-50" : "border-slate-200 hover:border-slate-300"}`}
            >
               
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-slate-100 mb-3">
                 
                <Sparkles className="h-6 w-6 text-slate-500" /> 
              </div> 
              <h4 className="font-semibold text-slate-900 mb-1">
                Blank Project
              </h4> 
              <p className="text-xs text-slate-400">
                Start from scratch with default workflow
              </p> 
            </button> 
            {templates.map((t) => {
              const Icon = TEMPLATE_ICONS[t.icon] || DEFAULT_ICON;
              const isSelected = selected?.id === t.id;
              return (
                <button
                  key={t.id}
                  onClick={() => setSelected(t)}
                  className={`rounded-2xl border-2 p-5 text-left transition hover:shadow-md ${isSelected ? "border-indigo-500 bg-indigo-50" : "border-slate-200 hover:border-slate-300"}`}
                >
                   
                  <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-cyan-400 to-indigo-500 mb-3">
                     
                    <Icon className="h-6 w-6 text-white" /> 
                  </div> 
                  <h4 className="font-semibold text-slate-900 mb-1">
                    {t.name}
                  </h4> 
                  <p className="text-xs text-slate-400 mb-2">{t.description}</p> 
                  <div className="flex flex-wrap gap-1.5">
                     
                    {t.defaultColumns?.slice(0, 3).map((col) => (
                      <span
                        key={col}
                        className="rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-500"
                      >
                         
                        {col} 
                      </span>
                    ))} 
                    {t.defaultColumns?.length > 3 && (
                      <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs text-slate-400">
                         
                        +{t.defaultColumns.length - 3} 
                      </span>
                    )} 
                  </div> 
                </button>
              );
            })} 
          </div>
        )} 
        <div className="flex justify-end gap-3">
           
          <Button
            onClick={onClose}
            variant="outline"
            className="rounded-xl border-slate-200 text-slate-600"
          >
             
            Cancel 
          </Button> 
          <Button
            onClick={handleConfirm}
            className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
          >
             
            <Check className="w-4 h-4 mr-1.5 inline" /> 
            {selected ? `Use ${selected.name}` : "Start Blank"} 
          </Button> 
        </div> 
      </div> 
    </div>
  );
}
