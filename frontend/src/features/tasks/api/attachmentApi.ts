import API from '../../../api/axios';

const unwrapData = (response: any) => response?.data?.data ?? response?.data ?? {};

const formatFileSize = (bytes: any) => {
  if (!bytes) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB'];
  let i = 0;
  let size = bytes;
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024;
    i++;
  }
  return `${size.toFixed(i === 0 ? 0 : 1)} ${units[i]}`;
};

const normalizeAttachment = (attachment: any) => ({
  ...attachment,
  uploadedBy: attachment?.uploadedBy ?? null,
  fileSizeFormatted: formatFileSize(attachment?.fileSize),
  createdAt: attachment?.createdAt ?? null,
});

export const uploadAttachment = async (taskId: any, file: any) => {
  try {
    const formData = new FormData();
    formData.append('file', file);
    const res = await API.post(`/tasks/${taskId}/attachments`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return normalizeAttachment(unwrapData(res));
  } catch (error) {
    console.error('Upload attachment error:', error);
    throw error;
  }
};

export const getAttachments = async (taskId: any) => {
  try {
    const res = await API.get(`/tasks/${taskId}/attachments`);
    const data = unwrapData(res);
    return Array.isArray(data) ? data.map(normalizeAttachment) : [];
  } catch (error) {
    console.error('Get attachments error:', error);
    throw error;
  }
};

export const deleteAttachment = async (attachmentId: any) => {
  try {
    await API.delete(`/attachments/${attachmentId}`);
  } catch (error) {
    console.error('Delete attachment error:', error);
    throw error;
  }
};
