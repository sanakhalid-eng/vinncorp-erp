export function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  window.URL.revokeObjectURL(url);
}
export function tasksToCsv(tasks) {
  if (!tasks || tasks.length === 0) return "";
  const headers = [
    "ID",
    "Title",
    "Description",
    "Status",
    "Priority",
    "Assignee",
    "Due Date",
    "Created At",
  ];
  const rows = tasks.map((t) => [
    t.id,
    `"${(t.title || "").replace(/"/g, '""')}"`,
    `"${(t.description || "").replace(/"/g, '""')}"`,
    t.status || "",
    t.priority || "",
    t.assignee?.name || "",
    t.dueDate ? new Date(t.dueDate).toLocaleDateString() : "",
    t.createdAt ? new Date(t.createdAt).toLocaleDateString() : "",
  ]);
  return [headers.join(","), ...rows.map((r) => r.join(","))].join("\n");
}
export function downloadTasksCsv(tasks, filename = "tasks.csv") {
  const csv = tasksToCsv(tasks);
  if (!csv) return;
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  downloadBlob(blob, filename);
}
