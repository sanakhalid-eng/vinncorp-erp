import { useEffect, useState, useRef } from "react";
import { Paperclip, Upload, Trash2, FileText, Image, Download, File, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { uploadAttachment, getAttachments, deleteAttachment } from "../../api/attachmentApi";

export default function AttachmentSection({ taskId }) {
  const [attachments, setAttachments] = useState([]);
  const [uploading, setUploading] = useState(false);
  const [dragging, setDragging] = useState(false);
  const inputRef = useRef(null);

  useEffect(() => {
    if (!taskId) return;
    loadAttachments();
  }, [taskId]);

  const loadAttachments = async () => {
    try {
      const data = await getAttachments(taskId);
      setAttachments(Array.isArray(data) ? data : []);
    } catch {
      setAttachments([]);
    }
  };

  const handleFile = async (file) => {
    if (uploading) return;
    setUploading(true);
    try {
      await uploadAttachment(taskId, file);
      toast.success("File uploaded");
      await loadAttachments();
    } catch {
      toast.error("Upload failed");
    } finally {
      setUploading(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragging(false);
    const file = e.dataTransfer?.files?.[0];
    if (file) handleFile(file);
  };

  const handleDelete = async (attachmentId) => {
    try {
      await deleteAttachment(attachmentId);
      await loadAttachments();
    } catch {
      toast.error("Failed to delete file");
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return "";
    return new Date(dateStr).toLocaleDateString();
  };

  const getFileIcon = (mimeType) => {
    if (mimeType?.startsWith("image/")) return <Image className="h-5 w-5" />;
    return <FileText className="h-5 w-5" />;
  };

  return (
    <div>
      <div className="flex items-center gap-2 mb-4">
        <Paperclip className="h-5 w-5 text-indigo-500" />
        <h3 className="font-semibold text-gray-900">Attachments</h3>
        {attachments.length > 0 && (
          <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-700">
            {attachments.length}
          </span>
        )}
      </div>

      {/* Drop zone */}
      <div
        onDragOver={(e) => {
          e.preventDefault();
          setDragging(true);
        }}
        onDragLeave={() => setDragging(false)}
        onDrop={handleDrop}
        onClick={() => inputRef.current?.click()}
        className={`relative border-2 border-dashed rounded-xl p-6 text-center cursor-pointer transition-colors mb-4 ${
          dragging
            ? "border-indigo-400 bg-indigo-50"
            : "border-gray-200 hover:border-gray-300 hover:bg-gray-50"
        }`}
      >
        <input
          ref={inputRef}
          type="file"
          className="hidden"
          onChange={(e) => {
            const file = e.target.files?.[0];
            if (file) handleFile(file);
            e.target.value = "";
          }}
        />
        {uploading ? (
          <div className="flex flex-col items-center gap-2">
            <Loader2 className="h-8 w-8 text-indigo-500 animate-spin" />
            <p className="text-sm text-gray-500">Uploading...</p>
          </div>
        ) : (
          <div className="flex flex-col items-center gap-2">
            <Upload className="h-8 w-8 text-gray-400" />
            <p className="text-sm text-gray-600">
              <span className="font-semibold text-indigo-600">Click to upload</span> or drag and drop
            </p>
            <p className="text-xs text-gray-400">Any file type supported</p>
          </div>
        )}
      </div>

      {/* File list */}
      <div className="space-y-2 max-h-[400px] overflow-y-auto">
        {attachments.map((att) => (
          <div
            key={att.id}
            className="flex items-center gap-3 p-3 rounded-xl border border-gray-100 hover:border-gray-200 hover:bg-gray-50 transition-colors group"
          >
            <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-indigo-100 to-purple-100 flex items-center justify-center text-indigo-600 flex-shrink-0">
              {getFileIcon(att.mimeType || att.fileType)}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-gray-900 truncate">{att.fileName || att.name}</p>
              <p className="text-xs text-gray-400">
                {att.fileSizeFormatted || att.fileSize}
                {att.uploadedBy && ` ΓÇó by ${att.uploadedBy.name || att.uploadedBy.username}`}
                {att.createdAt && ` ΓÇó ${formatDate(att.createdAt)}`}
              </p>
            </div>
            <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
              {att.url && (
                <a
                  href={att.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="p-1.5 rounded-lg hover:bg-gray-200 text-gray-500 hover:text-gray-700"
                  title="Download"
                >
                  <Download className="h-4 w-4" />
                </a>
              )}
              <button
                type="button"
                onClick={() => handleDelete(att.id)}
                className="p-1.5 rounded-lg hover:bg-red-50 text-gray-500 hover:text-red-500"
                title="Delete"
              >
                <Trash2 className="h-4 w-4" />
              </button>
            </div>
          </div>
        ))}
        {attachments.length === 0 && !uploading && (
          <p className="text-sm text-gray-400 text-center py-4">No attachments yet.</p>
        )}
      </div>
    </div>
  );
}
