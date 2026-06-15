import { createContext, useContext, useState, useCallback } from "react";
import { useKeyboardShortcuts, SHORTCUTS } from "./useKeyboardShortcuts";
const KeyboardShortcutsContext = createContext(null);
export function useKeyboardShortcutsContext() {
  return useContext(KeyboardShortcutsContext);
}
const DEFAULT_SHORTCUTS = {
  [SHORTCUTS.COMMAND_PALETTE]: null,
  [SHORTCUTS.COMMAND_PALETTE_WIN]: null,
  [SHORTCUTS.NEW_TASK]: null,
  [SHORTCUTS.NEW_PROJECT]: null,
  [SHORTCUTS.NEW_PROJECT_WIN]: null,
  [SHORTCUTS.GO_TO_TASKS]: null,
  [SHORTCUTS.GO_TO_PROJECTS]: null,
  [SHORTCUTS.GO_TO_DASHBOARD]: null,
  [SHORTCUTS.GO_TO_SPRINTS]: null,
  [SHORTCUTS.GO_TO_ANALYTICS]: null,
  [SHORTCUTS.TOGGLE_SIDEBAR]: null,
  [SHORTCUTS.HELP]: null,
};
export function KeyboardShortcutsProvider({ children }) {
  const [handlers, setHandlers] = useState(DEFAULT_SHORTCUTS);
  const register = useCallback((shortcut, handler) => {
    setHandlers((prev) => ({ ...prev, [shortcut]: handler }));
    return () => {
      setHandlers((prev) => ({ ...prev, [shortcut]: null }));
    };
  }, []);
  const handleShortcut = useCallback(
    (e) => {
      const shortcut = e.shortcut;
      const handler = handlers[shortcut];
      if (handler) {
        handler(e);
      }
    },
    [handlers],
  );
  useKeyboardShortcuts(handlers);
  return (
    <KeyboardShortcutsContext.Provider value={{ register, handlers }}>
       
      {children} 
    </KeyboardShortcutsContext.Provider>
  );
}
