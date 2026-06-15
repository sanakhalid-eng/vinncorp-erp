import ReactFlow, {
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
} from "reactflow";
import "reactflow/dist/style.css";
import { useEffect, useCallback, useState } from "react";
import {
  getProjectStatuses,
  getProjectTransitions,
} from "../../api/statusApi.js";
import { ArrowRight } from "lucide-react";
import { cn } from "../../utils/cn.js";
import { toast } from "sonner";

const nodeTypes = {
  statusNode: ({ data, selected }) => (
    <div
      className={cn(
        "shadow-lg rounded-2xl p-4 border-2 w-48 h-24 flex flex-col items-center justify-center cursor-grab active:cursor-grabbing transition-all",
        selected
          ? "ring-4 ring-primary-300/50 shadow-2xl border-primary-400"
          : "border-surface-200 dark:border-surface-700 hover:border-primary-300 dark:hover:border-primary-600 hover:shadow-xl",
        data.color
          ? "bg-gradient-to-b from-white to-primary-50 dark:from-surface-800 dark:to-primary-900/30"
          : "bg-gradient-to-b from-white to-surface-50 dark:from-surface-800 dark:to-surface-800",
      )}
    >
      <div
        className="w-12 h-12 rounded-xl shadow-md flex items-center justify-center mb-2 flex-shrink-0"
        style={{ backgroundColor: data.color || "#94a3b8" }}
      />
      <div className="text-center text-sm font-bold text-surface-900 dark:text-surface-100 truncate w-full px-1">
        {data.label}
      </div>
      <div className="text-xs text-surface-500 mt-1 font-mono">
        {data.orderIndex ?? ""}
      </div>
    </div>
  ),
};

const edgeTypes = {
  transitionEdge: ({ selected }) => (
    <ArrowRight
      className={cn(
        "w-6 h-6 mx-2 text-primary-500 absolute -top-3 left-1/2 transform -translate-x-1/2 shadow-lg",
        selected && "ring-2 ring-primary-400 shadow-2xl animate-pulse",
      )}
    />
  ),
};

const WorkflowGraph = ({
  projectId,
  statuses: initialStatuses = [],
  transitions: initialTransitions = [],
  height = 500,
  onTransitionsChange,
}) => {
  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadWorkflow();
  }, [projectId]);

  const loadWorkflow = async () => {
    setLoading(true);
    try {
      const statuses = initialStatuses.length
        ? initialStatuses
        : await getProjectStatuses(projectId);
      const transitions = initialTransitions.length
        ? initialTransitions
        : await getProjectTransitions(projectId);

      const workflowNodes = statuses.map((status, index) => ({
        id: String(status.id),
        type: "statusNode",
        position: { x: index * 220, y: 100 },
        data: {
          label: status.name,
          color: status.color,
          orderIndex: status.orderIndex,
        },
        draggable: false,
      }));

      const workflowEdges = transitions
        .map((transition) => {
          const fromNode = workflowNodes.find(
            (n) => n.id === transition.fromStatusId,
          );
          const toNode = workflowNodes.find(
            (n) => n.id === transition.toStatusId,
          );

          if (fromNode && toNode) {
            return {
              id: String(transition.id),
              source: String(transition.fromStatusId),
              target: String(transition.toStatusId),
              type: "smoothstep",
              style: { stroke: "#3b82f6", strokeWidth: 3 },
              animated: true,
            };
          }
          return null;
        })
        .filter(Boolean);

      setNodes(workflowNodes);
      setEdges(workflowEdges);

      onTransitionsChange?.(transitions);
    } catch {
      toast.error("Failed to load workflow graph");
    } finally {
      setLoading(false);
    }
  };

  const onConnect = useCallback(() => {
    toast.info("Drag-drop transitions coming soon!");
  }, []);

  if (loading) {
    return (
      <div className="card flex items-center justify-center" style={{ height: `${height}px` }}>
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-primary-200 dark:border-primary-800 border-t-primary-500 rounded-full animate-spin mx-auto mb-4" />
          <p className="text-lg font-semibold text-surface-700 dark:text-surface-300">
            Loading workflow...
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="card overflow-hidden" style={{ height }}>
      <div className="px-6 py-4 border-b border-surface-200 dark:border-surface-700 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <ArrowRight className="w-6 h-6 text-primary-500" />
          <div>
            <h3 className="text-xl font-bold text-surface-900 dark:text-surface-100">
              Workflow Graph
            </h3>
            <p className="text-sm text-surface-500 mt-0.5">
              {nodes.length} stages &middot; {edges.length} transitions
            </p>
          </div>
        </div>
      </div>

      <div className="h-full w-full" style={{ height: `calc(${height}px - 73px)` }}>
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={onConnect}
          nodeTypes={nodeTypes}
          edgeTypes={edgeTypes}
          fitView
          maxZoom={1.5}
          minZoom={0.3}
        >
          <Background />
          <Controls />
          <MiniMap />
        </ReactFlow>
      </div>

      {edges.length === 0 && (
        <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
          <div className="text-center text-surface-400">
            <ArrowRight className="w-16 h-16 mx-auto mb-4 opacity-30" />
            <p className="text-lg font-semibold">No transitions</p>
            <p className="text-sm">Add transitions to see flow visualization</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default WorkflowGraph;
