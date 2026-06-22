import { useState, useCallback } from "react";
import {
  Paperclip,
  Trash2,
  Download,
  Upload,
  FileText,
  Image,
  File,
} from "lucide-react";
import {
  uploadAttachment,
  getAttachments,
  deleteAttachment,
} from "../../api/attachmentApi";
import { toast } from "sonner";
import { usePermission } from "../../../../context/usePermission.js";
function getFileIcon(fileType) {
  if (fileType?.startsWith("image/")) return Image;
  if (fileType === "application/pdf") return FileText;
  return File;
}
function formatFileSize(bytes) {
  if (!bytes) return "0 B";
  const units = ["B", "KB", "MB", "GB"];
  let i = 0;
  let size = bytes;
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024;
    i++;
  }
  return `${size.toFixed(i === 0 ? 0 : 1)} ${units[i]}`;
}
export default function AttachmentList({ taskId, currentUser, canUpload }) {
  const [attachments, setAttachments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [isDragging, setIsDragging] = useState(false);
  const [error, setError] = useState(null);
  const fetchAttachments = useCallback(async () => {
    try {
      setLoading(true);
      const data = await getAttachments(taskId);
      setAttachments(data);
      setError(null);
    } catch {
      setError("Failed to load attachments");
    } finally {
      setLoading(false);
    }
  }, [taskId]);
  const handleUpload = async (files) => {
    const fileArray = Array.from(files);
    if (fileArray.length === 0) return;
    if (fileArray.length > 5) {
      toast.error("Maximum 5 files at a time");
      return;
    }
    setUploading(true);
    try {
      for (const file of fileArray) {
        await uploadAttachment(taskId, file);
        toast.success(`Uploaded: ${file.name}`);
      }
      fetchAttachments();
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to upload file");
    } finally {
      setUploading(false);
    }
  };
  const handleDelete = async (attachmentId, fileName) => {
    if (!window.confirm(`Delete "${fileName}"?`)) return;
    try {
      await deleteAttachment(attachmentId);
      toast.success("Attachment deleted");
      fetchAttachments();
    } catch {
      toast.error("Failed to delete attachment");
    }
  };
  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (canUpload) setIsDragging(true);
  };
  const handleDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  };
  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
    if (e.dataTransfer.files && canUpload) {
      handleUpload(e.dataTransfer.files);
    }
  };
  const handleFileSelect = (e) => {
    if (e.target.files && canUpload) {
      handleUpload(e.target.files);
      e.target.value = "";
    }
  };
  if (loading) {
    return (
      <div className="flex items-center justify-center py-6">
         
        <div className="h-5 w-5 animate-spin rounded-full border-2 border-gray-300 border-t-blue-500" /> 
      </div>
    );
  }
  if (error) {
    return <div className="py-4 text-center text-sm text-red-500">{error}</div>;
  }
  const { isAdmin, isProjectManager } = usePermission();
  const isUploader = (attachment) =>
    currentUser?.name === attachment.uploadedBy?.name;
  const isPM = isAdmin() || isProjectManager();
  return (
    <div
      className="space-y-4"
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
    >
       
      {canUpload && (
        <div
          className={`relative rounded-xl border-2 border-dashed p-6 text-center transition-all ${isDragging ? "border-blue-400 bg-blue-50" : "border-gray-200 hover:border-gray-300 hover:bg-gray-50"} ${uploading ? "pointer-events-none opacity-60" : ""}`}
        >
           
          <input
            type="file"
            multiple
            accept="image/*,.pdf,.doc,.docx,.xls,.xlsx,.txt,.csv,.zip,.rar"
            onChange={handleFileSelect}
            className="absolute inset-0 cursor-pointer opacity-0"
            disabled={uploading}
          /> 
          {uploading ? (
            <div className="flex flex-col items-center gap-2">
               
              <div className="h-5 w-5 animate-spin rounded-full border-2 border-blue-500 border-t-transparent" /> 
              <span className="text-sm text-blue-600">Uploading...</span> 
            </div>
          ) : (
            <div className="flex flex-col items-center gap-2">
               
              <Upload className="h-8 w-8 text-gray-400" /> 
              <p className="text-sm text-gray-500">
                 
                <span className="font-semibold text-blue-600">
                  Click to upload
                </span> 
                or drag & drop 
              </p> 
              <p className="text-xs text-gray-400">
                PNG, JPG, PDF, DOC, XLS, ZIP up to 10MB (max 5 files)
              </p> 
            </div>
          )} 
        </div>
      )} 
      {attachments.length === 0 ? (
        <div className="py-8 text-center text-gray-400">
           
          <Paperclip className="mx-auto h-10 w-10 text-gray-300" /> 
          <p className="mt-2 text-sm">No attachments yet</p> 
        </div>
      ) : (
        <div className="space-y-2">
           
          {attachments.map((attachment) => {
            const FileIcon = getFileIcon(attachment.fileType);
            const canDelete = isUploader(attachment) || isPM;
            return (
              <div
                key={attachment.id}
                className="group flex items-center gap-3 rounded-lg border border-gray-100 bg-white p-3 transition-colors hover:border-gray-200 hover:bg-gray-50"
              >
                 
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-gray-100 text-gray-500">
                   
                  <FileIcon className="h-5 w-5" /> 
                </div> 
                <div className="min-w-0 flex-1">
                   
                  <a
                    href={attachment.fileUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="truncate text-sm font-medium text-gray-800 hover:text-blue-600"
                  >
                     
                    {attachment.fileName} 
                  </a> 
                  <div className="flex items-center gap-2 text-xs text-gray-400">
                     
                    <span>{attachment.fileSizeFormatted}</span> <span>ΓÇó</span> 
                    <span>
                      {new Date(attachment.createdAt).toLocaleDateString()}
                    </span> 
                    <span>ΓÇó</span> 
                    <span>{attachment.uploadedBy?.name}</span> 
                  </div> 
                </div> 
                <div className="flex items-center gap-1 opacity-100 md:opacity-0 md:transition-opacity md:group-hover:opacity-100">
                   
                  <a
                    href={attachment.fileUrl}
                    download={attachment.fileName}
                    className="rounded p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
                    title="Download"
                  >
                     
                    <Download className="h-4 w-4" /> 
                  </a> 
                  {canDelete && (
                    <button
                      onClick={() =>
                        handleDelete(attachment.id, attachment.fileName)
                      }
                      className="rounded p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-500"
                      title="Delete"
                    >
                       
                      <Trash2 className="h-4 w-4" /> 
                    </button>
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
