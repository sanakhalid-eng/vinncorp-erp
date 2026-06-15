import { useCallback, useRef } from "react";
const pendingMutations = new Map();
let mutationId = 0;
function generateId() {
  return `opt-${++mutationId}-${Date.now()}`;
}
export function optimisticUpdate({
  mutationFn,
  updateCache,
  rollbackCache,
  onSuccess,
  onError,
}) {
  const id = generateId();
  const snapshot = updateCache();
  pendingMutations.set(id, { snapshot, rollbackCache });
  return mutationFn()
    .then((result) => {
      pendingMutations.delete(id);
      onSuccess?.(result);
      return result;
    })
    .catch((error) => {
      const mutation = pendingMutations.get(id);
      if (mutation) {
        mutation.rollbackCache(mutation.snapshot);
        pendingMutations.delete(id);
      }
      onError?.(error);
      throw error;
    });
}
export function useOptimisticMutation(options) {
  const optionsRef = useRef(options);
  optionsRef.current = options;
  return useCallback(async (...args) => {
    return optimisticUpdate({
      ...optionsRef.current,
      mutationFn: () => optionsRef.current.mutationFn(...args),
    });
  }, []);
}
export function createOptimisticTask(tasks, newTask) {
  return {
    id: `temp-${Date.now()}`,
    ...newTask,
    status: newTask.status || "TODO",
    priority: newTask.priority || "MEDIUM",
    _optimistic: true,
  };
}
export function applyOptimisticTask(tasks, newTask) {
  return [createOptimisticTask(tasks, newTask), ...tasks];
}
export function removeOptimisticTask(tasks, tempId) {
  return tasks.filter((t) => t.id !== tempId);
}
export function replaceOptimisticTask(tasks, tempId, realTask) {
  return tasks.map((t) =>
    t.id === tempId ? { ...realTask, _optimistic: false } : t,
  );
}
