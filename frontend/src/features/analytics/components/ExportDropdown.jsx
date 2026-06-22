import { useState } from "react";
import {
  Download,
  FileText,
  FileSpreadsheet,
  Loader2,
  CheckCircle,
} from "lucide-react";
import {
  exportTasks,
  exportSprintReport,
  exportAnalytics,
  exportCalendar,
  exportTimesheet,
} from "../api/exportApi";
import { toast, Toaster } from "sonner";
const ExportDropdown = ({
  projectId,
  sprintId,
  userId,
  showAnalytics = false,
  showCalendar = false,
  showTimesheet = false,
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(null);
  // tracks which export is loading
  const handleExport = async (type, format, exportFn, fileName) => {
    setLoading(type);
    try {
      const data = await exportFn();
      downloadFile(data, fileName);
      toast.success(`${type} exported successfully`);
    } catch (error) {
      toast.error(`Failed to export ${type.toLowerCase()}`);
      console.error("Export error:", error);
    } finally {
      setLoading(null);
      setIsOpen(false);
    }
  };
  const downloadFile = (data, fileName) => {
    const blob = new Blob([data], {
      type: fileName.endsWith(".csv") ? "text/csv" : "application/pdf",
    });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  };
  const exportOptions = [
    ...(projectId
      ? [
          {
            type: "Tasks",
            icon: FileSpreadsheet,
            formats: [
              {
                format: "CSV",
                action: () =>
                  handleExport(
                    "Tasks",
                    "csv",
                    () => exportTasks(projectId, "csv"),
                    `project-${projectId}-tasks.csv`,
                  ),
              },
              {
                format: "PDF",
                action: () =>
                  handleExport(
                    "Tasks",
                    "pdf",
                    () => exportTasks(projectId, "pdf"),
                    `project-${projectId}-tasks.pdf`,
                  ),
              },
            ],
          },
          ...(showCalendar
            ? [
                {
                  type: "Calendar",
                  icon: FileSpreadsheet,
                  formats: [
                    {
                      format: "CSV",
                      action: () =>
                        handleExport(
                          "Calendar",
                          "csv",
                          () => exportCalendar(projectId, "csv"),
                          `project-${projectId}-calendar.csv`,
                        ),
                    },
                  ],
                },
              ]
            : []),
          ...(showTimesheet
            ? [
                {
                  type: "Timesheet",
                  icon: FileSpreadsheet,
                  formats: [
                    {
                      format: "CSV",
                      action: () =>
                        handleExport(
                          "Timesheet",
                          "csv",
                          () => exportTimesheet(userId, "csv"),
                          `user-${userId}-timesheet.csv`,
                        ),
                    },
                    {
                      format: "PDF",
                      action: () =>
                        handleExport(
                          "Timesheet",
                          "pdf",
                          () => exportTimesheet(userId, "pdf"),
                          `user-${userId}-timesheet.pdf`,
                        ),
                    },
                  ],
                },
              ]
            : []),
          ...(showAnalytics
            ? [
                {
                  type: "Analytics",
                  icon: FileText,
                  formats: [
                    {
                      format: "PDF",
                      action: () =>
                        handleExport(
                          "Analytics",
                          "pdf",
                          () => exportAnalytics(projectId, "pdf"),
                          `project-${projectId}-analytics.pdf`,
                        ),
                    },
                  ],
                },
              ]
            : []),
        ]
      : []),
    ...(sprintId
      ? [
          {
            type: "Sprint Report",
            icon: FileText,
            formats: [
              {
                format: "PDF",
                action: () =>
                  handleExport(
                    "Sprint Report",
                    "pdf",
                    () => exportSprintReport(sprintId, "pdf"),
                    `sprint-${sprintId}-report.pdf`,
                  ),
              },
            ],
          },
        ]
      : []),
  ];
  if (exportOptions.length === 0) return null;
  return (
    <div className="relative">
       
      <Toaster position="top-right" /> 
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-2 px-3 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
        disabled={loading !== null}
      >
         
        {loading ? (
          <Loader2 size={16} className="animate-spin" />
        ) : (
          <Download size={16} />
        )} 
        Export 
      </button> 
      {isOpen && (
        <div className="absolute right-0 mt-2 w-56 bg-white rounded-lg shadow-lg border border-gray-200 py-2 z-50">
           
          {exportOptions.map((option, idx) => (
            <div key={idx}>
               
              <div className="px-4 py-2 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                 
                {option.type} 
              </div> 
              {option.formats.map((fmt, fidx) => (
                <button
                  key={fidx}
                  onClick={fmt.action}
                  disabled={loading !== null}
                  className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-2 disabled:opacity-50"
                >
                   
                  {loading === option.type ? (
                    <Loader2 size={14} className="animate-spin" />
                  ) : (
                    <option.icon size={14} />
                  )} 
                  Export as {fmt.format} 
                </button>
              ))} 
              {idx < exportOptions.length - 1 && (
                <div className="border-t border-gray-100 my-1"></div>
              )} 
            </div>
          ))} 
        </div>
      )} 
      {/* Overlay to close dropdown */} 
      {isOpen && (
        <div className="fixed inset-0 z-40" onClick={() => setIsOpen(false)} />
      )} 
    </div>
  );
};
export default ExportDropdown;
