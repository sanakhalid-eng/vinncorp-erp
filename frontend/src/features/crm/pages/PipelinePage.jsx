import { useEffect, useState } from "react";
import {
  Plus,
  GripVertical,
  Edit2,
  Trash2,
  ArrowRight,
  CheckCircle2,
  XCircle,
} from "lucide-react";
import { toast } from "sonner";
import {
  listPipelines,
  listPipelineStages,
  deletePipeline,
  deletePipelineStage,
  changeOpportunityStage,
  listOpportunities,
  markOpportunityWon,
  markOpportunityLost,
} from "../api/crmApi";
import { PageSkeleton } from "../../../components/LoadingSkeleton";
import ErrorState from "../../../components/ErrorState";

const STAGE_COLORS = [
  "from-blue-500 to-indigo-600",
  "from-cyan-500 to-teal-600",
  "from-amber-500 to-orange-600",
  "from-violet-500 to-purple-600",
  "from-emerald-500 to-green-600",
  "from-rose-500 to-red-600",
];

export default function PipelinePage() {
  const [pipelines, setPipelines] = useState([]);
  const [selectedPipeline, setSelectedPipeline] = useState(null);
  const [stages, setStages] = useState([]);
  const [opportunities, setOpportunities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [draggingOpp, setDraggingOpp] = useState(null);

  useEffect(() => {
    loadPipelines();
  }, []);

  const loadPipelines = async () => {
    setLoading(true);
    try {
      const pipes = await listPipelines();
      setPipelines(pipes);
      if (pipes.length > 0) {
        await selectPipeline(pipes[0]);
      }
    } catch (e) {
      setError(e.message || "Failed to load pipelines");
    } finally {
      setLoading(false);
    }
  };

  const selectPipeline = async (pipeline) => {
    setSelectedPipeline(pipeline);
    try {
      const [stgs, opps] = await Promise.all([
        listPipelineStages(pipeline.id),
        listOpportunities(0, 200),
      ]);
      setStages(stgs);
      const oppList = Array.isArray(opps) ? opps : opps?.content || [];
      setOpportunities(oppList);
    } catch (e) {
      toast.error(e.message || "Failed to load pipeline data");
    }
  };

  const getOppsForStage = (stageId) => {
    return opportunities.filter(
      (o) => o.stage?.id === stageId || o.stageId === stageId
    );
  };

  const handleDragStart = (e, opp) => {
    setDraggingOpp(opp);
    e.dataTransfer.effectAllowed = "move";
  };

  const handleDrop = async (e, stageId) => {
    e.preventDefault();
    if (!draggingOpp) return;
    try {
      await changeOpportunityStage(draggingOpp.id, stageId);
      toast.success("Opportunity stage updated");
      if (selectedPipeline) await selectPipeline(selectedPipeline);
    } catch (err) {
      toast.error(err.message || "Failed to move opportunity");
    } finally {
      setDraggingOpp(null);
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = "move";
  };

  if (loading) return <PageSkeleton />;

  if (error) {
    return (
      <div className="mx-auto max-w-full px-4 py-8">
        <ErrorState title="Failed to load pipeline" message={error} onRetry={loadPipelines} />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-full px-4 py-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Sales Pipeline</h1>
          <p className="text-slate-500">Drag & drop opportunities across stages</p>
        </div>
      </div>

      {/* Pipeline Selector */}
      <div className="mb-6 flex gap-2 overflow-x-auto pb-2">
        {pipelines.map((p) => (
          <button
            key={p.id}
            onClick={() => selectPipeline(p)}
            className={`whitespace-nowrap rounded-xl px-4 py-2 text-sm font-medium transition ${
              selectedPipeline?.id === p.id
                ? "bg-indigo-600 text-white"
                : "bg-white border border-slate-200 text-slate-600 hover:bg-slate-50"
            }`}
          >
            {p.name}
          </button>
        ))}
      </div>

      {/* Kanban Board */}
      {stages.length === 0 ? (
        <div className="py-20 text-center text-slate-400">No stages configured</div>
      ) : (
        <div className="flex gap-4 overflow-x-auto pb-4">
          {stages.map((stage, idx) => {
            const stageOpps = getOppsForStage(stage.id);
            return (
              <div
                key={stage.id}
                className="min-w-[300px] flex-1"
                onDragOver={handleDragOver}
                onDrop={(e) => handleDrop(e, stage.id)}
              >
                <div className="mb-3 flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className={`h-3 w-3 rounded-full bg-gradient-to-br ${STAGE_COLORS[idx % STAGE_COLORS.length]}`} />
                    <h3 className="font-semibold text-slate-700">{stage.name}</h3>
                    <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-500">
                      {stageOpps.length}
                    </span>
                  </div>
                </div>
                <div className="min-h-[200px] space-y-3 rounded-2xl bg-slate-50 p-3">
                  {stageOpps.map((opp) => (
                    <div
                      key={opp.id}
                      draggable
                      onDragStart={(e) => handleDragStart(e, opp)}
                      className="cursor-grab rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition hover:shadow-md active:cursor-grabbing"
                    >
                      <div className="mb-2 flex items-start justify-between">
                        <p className="font-medium text-slate-800">{opp.title}</p>
                        <GripVertical className="h-4 w-4 text-slate-300 shrink-0" />
                      </div>
                      {opp.value != null && (
                        <p className="mb-2 text-sm font-semibold text-indigo-600">
                          ${Number(opp.value).toLocaleString()}
                        </p>
                      )}
                      {opp.contactName && (
                        <p className="text-xs text-slate-400">{opp.contactName}</p>
                      )}
                      <div className="mt-2 flex items-center gap-1">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            markOpportunityWon(opp.id).then(() => {
                              toast.success("Deal won!");
                              if (selectedPipeline) selectPipeline(selectedPipeline);
                            }).catch(() => toast.error("Failed"));
                          }}
                          className="rounded p-1 text-emerald-500 hover:bg-emerald-50"
                          title="Mark Won"
                        >
                          <CheckCircle2 className="h-3.5 w-3.5" />
                        </button>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            markOpportunityLost(opp.id, "Lost").then(() => {
                              toast.success("Deal lost");
                              if (selectedPipeline) selectPipeline(selectedPipeline);
                            }).catch(() => toast.error("Failed"));
                          }}
                          className="rounded p-1 text-rose-500 hover:bg-rose-50"
                          title="Mark Lost"
                        >
                          <XCircle className="h-3.5 w-3.5" />
                        </button>
                      </div>
                    </div>
                  ))}
                  {stageOpps.length === 0 && (
                    <p className="py-8 text-center text-sm text-slate-400">
                      Drop opportunities here
                    </p>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
