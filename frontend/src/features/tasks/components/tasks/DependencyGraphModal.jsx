import { useState, useEffect } from "react";
import {
  X,
  AlertTriangle,
  ArrowRight,
  Link2,
  Layers,
  GitFork,
  Copy,
  Loader2,
} from "lucide-react";
import { getTaskDependencyGraph } from "../../api/taskDependencyApi";
const TYPE_COLORS = {
  BLOCKED_BY: { stroke: "#ef4444", label: "Blocked By" },
  BLOCKS: { stroke: "#f97316", label: "Blocks" },
  RELATES_TO: { stroke: "#3b82f6", label: "Relates To" },
  DUPLICATES: { stroke: "#a855f7", label: "Duplicates" },
  CAUSED_BY: { stroke: "#06b6d4", label: "Caused By" },
};
export default function DependencyGraphModal({ taskId, onClose }) {
  const [graph, setGraph] = useState(null);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    if (!taskId) return;
    setLoading(true);
    getTaskDependencyGraph(taskId)
      .then((data) => setGraph(data))
      .catch(() => setGraph({ nodes: [], edges: [] }))
      .finally(() => setLoading(false));
  }, [taskId]);
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
       
      <div className="fixed inset-0 bg-black/50" onClick={onClose} /> 
      <div className="relative w-full max-w-2xl mx-4 max-h-[80vh] bg-white dark:bg-gray-800 rounded-2xl shadow-2xl flex flex-col">
         
        <div className="flex items-center justify-between border-b border-gray-200 dark:border-gray-700 px-6 py-4">
           
          <h2 className="text-lg font-bold text-gray-800 dark:text-gray-100">
            Dependency Graph
          </h2> 
          <button
            onClick={onClose}
            className="rounded-lg p-2 hover:bg-gray-100 dark:hover:bg-gray-700"
          >
             
            <X className="h-5 w-5" /> 
          </button> 
        </div> 
        <div className="flex-1 overflow-y-auto p-6">
           
          {loading ? (
            <div className="flex items-center justify-center py-16">
               
              <Loader2 className="h-8 w-8 animate-spin text-indigo-600" /> 
            </div>
          ) : (
            <div className="space-y-6">
               
              {graph?.nodes?.length > 0 && (
                <div>
                   
                  <h3 className="text-sm font-semibold text-gray-500 dark:text-gray-400 mb-3 uppercase tracking-wider">
                     
                    Tasks ({graph.nodes.length}) 
                  </h3> 
                  <div className="space-y-2">
                     
                    {graph.nodes.map((node) => (
                      <div
                        key={node.id}
                        className="flex items-center justify-between rounded-lg border border-gray-100 dark:border-gray-700 p-3"
                      >
                         
                        <div>
                           
                          <p className="text-sm font-medium text-gray-800 dark:text-gray-100">
                             
                            #{node.id} {node.title} 
                          </p> 
                          {node.status && (
                            <span className="text-xs text-gray-500">
                              {node.status}
                            </span>
                          )} 
                        </div> 
                        <div className="flex items-center gap-2">
                           
                          {node.priority && (
                            <span className="text-xs px-2 py-0.5 rounded-full bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300">
                               
                              {node.priority} 
                            </span>
                          )} 
                          {node.assigneeName && (
                            <span className="text-xs text-gray-500">
                              {node.assigneeName}
                            </span>
                          )} 
                        </div> 
                      </div>
                    ))} 
                  </div> 
                </div>
              )} 
              {graph?.edges?.length > 0 && (
                <div>
                   
                  <h3 className="text-sm font-semibold text-gray-500 dark:text-gray-400 mb-3 uppercase tracking-wider">
                     
                    Connections ({graph.edges.length}) 
                  </h3> 
                  <div className="space-y-2">
                     
                    {graph.edges.map((edge) => {
                      const color =
                        TYPE_COLORS[edge.dependencyType] ||
                        TYPE_COLORS.BLOCKED_BY;
                      const source = graph.nodes.find(
                        (n) => n.id === edge.sourceId,
                      );
                      const target = graph.nodes.find(
                        (n) => n.id === edge.targetId,
                      );
                      return (
                        <div
                          key={edge.id}
                          className="flex items-center gap-3 rounded-lg border border-gray-100 dark:border-gray-700 p-3"
                        >
                           
                          <span
                            className="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium"
                            style={{
                              backgroundColor: color.stroke + "20",
                              color: color.stroke,
                            }}
                          >
                             
                            {edge.dependencyType} 
                          </span> 
                          <span className="text-sm font-medium text-gray-700 dark:text-gray-200 truncate">
                             
                            {source?.title || `#${edge.sourceId}`} 
                          </span> 
                          <ArrowRight className="h-4 w-4 text-gray-400 shrink-0" /> 
                          <span className="text-sm font-medium text-gray-700 dark:text-gray-200 truncate">
                             
                            {target?.title || `#${edge.targetId}`} 
                          </span> 
                          {edge.description && (
                            <span className="text-xs text-gray-500 ml-auto hidden sm:block">
                               
                              {edge.description} 
                            </span>
                          )} 
                        </div>
                      );
                    })} 
                  </div> 
                </div>
              )} 
              {(!graph?.nodes || graph.nodes.length === 0) && (
                <div className="text-center py-12">
                   
                  <Link2 className="w-12 h-12 mx-auto text-gray-300 dark:text-gray-600 mb-4" /> 
                  <p className="text-gray-500 dark:text-gray-400">
                    No dependencies found for this task
                  </p> 
                </div>
              )} 
            </div>
          )} 
        </div> 
        <div className="border-t border-gray-200 dark:border-gray-700 px-6 py-4">
           
          <div className="flex flex-wrap gap-3 text-xs">
             
            {Object.entries(TYPE_COLORS).map(([type, info]) => (
              <span key={type} className="inline-flex items-center gap-1">
                 
                <span
                  className="w-2.5 h-2.5 rounded-full"
                  style={{ backgroundColor: info.stroke }}
                /> 
                {info.label} 
              </span>
            ))} 
          </div> 
        </div> 
      </div> 
    </div>
  );
}
