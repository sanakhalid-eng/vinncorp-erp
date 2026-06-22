import API from '../../../api/axios';

const unwrapData = (response: any) => response?.data?.data ?? response?.data ?? {};

const normalizeComment = (comment: any) => ({
  ...comment,
  author: comment?.author ?? null,
  content: comment?.content ?? '',
  isEdited: comment?.isEdited ?? false,
  isDeleted: comment?.isDeleted ?? false,
  parentCommentId: comment?.parentCommentId ?? null,
  replies: Array.isArray(comment?.replies) ? comment.replies.map(normalizeComment) : [],
  reactions: Array.isArray(comment?.reactions) ? comment.reactions : [],
  mentions: Array.isArray(comment?.mentions) ? comment.mentions : [],
  createdAt: comment?.createdAt ?? null,
  updatedAt: comment?.updatedAt ?? null,
});

export const getComments = async (taskId: any) => {
  try {
    const res = await API.get(`/tasks/${taskId}/comments`);
    const data = unwrapData(res);
    return Array.isArray(data) ? data.map(normalizeComment) : [];
  } catch (error) {
    console.error('Get comments error:', error);
    throw error;
  }
};

export const createComment = async (taskId: any, content: any, parentCommentId: any= null) => {
  try {
    const res = await API.post(`/tasks/${taskId}/comments`, { content, parentCommentId });
    return normalizeComment(unwrapData(res));
  } catch (error) {
    console.error('Create comment error:', error);
    throw error;
  }
};

export const updateComment = async (commentId: any, content: any) => {
  try {
    const res = await API.put(`/comments/${commentId}`, { content });
    return normalizeComment(unwrapData(res));
  } catch (error) {
    console.error('Update comment error:', error);
    throw error;
  }
};

export const deleteComment = async (commentId: any) => {
  try {
    await API.delete(`/comments/${commentId}`);
  } catch (error) {
    console.error('Delete comment error:', error);
    throw error;
  }
};

export const toggleReaction = async (commentId: any, reactionType: any) => {
  try {
    const res = await API.post(`/comments/${commentId}/reactions`, { reactionType });
    return normalizeComment(unwrapData(res));
  } catch (error) {
    console.error('Toggle reaction error:', error);
    throw error;
  }
};

export const getEditHistory = async (commentId: any) => {
  try {
    const res = await API.get(`/comments/${commentId}/history`);
    const data = unwrapData(res);
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.error('Get edit history error:', error);
    throw error;
  }
};

export const getCommentCount = async (taskId: any) => {
  try {
    const res = await API.get(`/tasks/${taskId}/comments/count`);
    return unwrapData(res);
  } catch (error) {
    console.error('Get comment count error:', error);
    return 0;
  }
};
