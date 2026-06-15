import { useEffect, useRef } from "react";
const MODIFIER_KEYS = new Set(["Meta", "Control", "Alt", "Shift"]);
function normalizeKey(e) {
  if (e.key === " ") return "Space";
  if (e.key === "Escape") return "Esc";
  return e.key.length === 1 ? e.key.toUpperCase() : e.key;
}
function getShortcut(e) {
  const parts = [];
  if (e.metaKey) parts.push("Meta");
  if (e.ctrlKey) parts.push("Ctrl");
  if (e.altKey) parts.push("Alt");
  if (e.shiftKey) parts.push("Shift");
  const key = normalizeKey(e);
  if (!MODIFIER_KEYS.has(key)) parts.push(key);
  return parts.join("+");
}
function isInputTarget(e) {
  const tag = e.target?.tagName;
  return (
    tag === "INPUT" ||
    tag === "TEXTAREA" ||
    tag === "SELECT" ||
    e.target?.contentEditable === "true"
  );
}

export function useKeyboardShortcuts(shortcuts, enabled = true) {
  const shortcutsRef = useRef(shortcuts);
  shortcutsRef.current = shortcuts;
  useEffect(() => {
    if (!enabled) return;
    const handleKeyDown = (e) => {
      if (isInputTarget(e)) return;
      const shortcut = getShortcut(e);
      const handler = shortcutsRef.current[shortcut];
      if (handler && !e.defaultPrevented) {
        e.preventDefault();
        handler(e);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [enabled]);
}
export function useShortcut(shortcut, handler, enabled = true) {
  const handlerRef = useRef(handler);
  handlerRef.current = handler;
  useEffect(() => {
    if (!enabled) return;
    const handleKeyDown = (e) => {
      if (isInputTarget(e)) return;
      if (getShortcut(e) === shortcut && !e.defaultPrevented) {
        e.preventDefault();
        handlerRef.current(e);
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [shortcut, enabled]);
}
export const SHORTCUTS = {
  COMMAND_PALETTE: "Meta+K",
  COMMAND_PALETTE_WIN: "Ctrl+K",
  NEW_TASK: "N",
  NEW_PROJECT: "Meta+N",
  NEW_PROJECT_WIN: "Ctrl+N",
  GO_TO_TASKS: "G+T",
  GO_TO_PROJECTS: "G+P",
  GO_TO_DASHBOARD: "G+D",
  GO_TO_SPRINTS: "G+S",
  GO_TO_ANALYTICS: "G+A",
  TOGGLE_SIDEBAR: "[",
  LOGOUT: "Meta+Shift+Q",
  LOGOUT_WIN: "Ctrl+Shift+Q",
  HELP: "?",
};
export function getShortcutLabel(shortcut) {
  return shortcut
    .replace("Meta+", "⌘")
    .replace("Ctrl+", "^")
    .replace("Alt+", "⌥")
    .replace("Shift+", "⇧")
    .replace("Space", "␣");
}
