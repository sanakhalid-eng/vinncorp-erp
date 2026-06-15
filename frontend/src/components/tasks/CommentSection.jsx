import { useEffect, useState, useRef } from "react";
import { MessageSquare, Send, Edit2, Trash2, Smile, MoreHorizontal, Reply, X } from "lucide-react";
import { toast } from "sonner";
import {
  getComments,
  createComment,
  updateComment,
  deleteComment as deleteCommentApi,
  toggleReaction,
} from "../../api/commentApi";

const EMOJIS = ["👍", "❤️", "😄", "🎉", "😕", "👀"];

export default function CommentSection({ taskId }) {
  const [comments, setComments] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [loading, setLoading] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [editContent, setEditContent] = useState("");
  const [replyTo, setReplyTo] = useState(null);
  const [showEmojiPicker, setShowEmojiPicker] = useState(null);
  const inputRef = useRef(null);

  useEffect(() => {
    if (!taskId) return;
    loadComments();
  }, [taskId]);

  const loadComments = async () => {
    try {
      const data = await getComments(taskId);
      setComments(Array.isArray(data) ? data : []);
    } catch {
      setComments([]);
    }
  };

  const handleSubmit = async () => {
    if (!newComment.trim()) return;
    setLoading(true);
    try {
      await createComment(taskId, newComment.trim(), replyTo?.id ?? null);
      setNewComment("");
      setReplyTo(null);
      await loadComments();
    } catch {
      toast.error("Failed to add comment");
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = async (commentId) => {
    if (!editContent.trim()) return;
    try {
      await updateComment(commentId, editContent.trim());
      setEditingId(null);
      await loadComments();
    } catch {
      toast.error("Failed to update comment");
    }
  };

  const handleDelete = async (commentId) => {
    try {
      await deleteCommentApi(commentId);
      await loadComments();
    } catch {
      toast.error("Failed to delete comment");
    }
  };

  const handleReaction = async (commentId, emoji) => {
    try {
      await toggleReaction(commentId, emoji);
      await loadComments();
    } catch {
      // ignore
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return "";
    const d = new Date(dateStr);
    const now = new Date();
    const diff = now - d;
    const mins = Math.floor(diff / 60000);
    if (mins < 1) return "just now";
    if (mins < 60) return `${mins}m ago`;
    const hours = Math.floor(mins / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    if (days < 7) return `${days}d ago`;
    return d.toLocaleDateString();
  };

  const renderComment = (comment, isReply = false) => {
    const isEditing = editingId === comment.id;
    return (
      <div
        key={comment.id}
        className={`group ${isReply ? "ml-10 mt-3" : "mt-4 first:mt-0"}`}
      >
        <div className="flex items-start gap-3">
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-400 to-purple-500 flex items-center justify-center text-white text-xs font-bold flex-shrink-0">
            {(comment.author?.name || comment.author?.username || "?").charAt(0).toUpperCase()}
          </div>
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 mb-1">
              <span className="text-sm font-semibold text-gray-900">
                {comment.author?.name || comment.author?.username || "Unknown"}
              </span>
              <span className="text-xs text-gray-400">{formatDate(comment.createdAt)}</span>
              {comment.isEdited && (
                <span className="text-xs text-gray-400 italic">(edited)</span>
              )}
            </div>

            {isEditing ? (
              <div className="flex items-start gap-2">
                <textarea
                  value={editContent}
                  onChange={(e) => setEditContent(e.target.value)}
                  className="flex-1 text-sm rounded-lg border border-gray-200 px-3 py-2 resize-none focus:ring-2 focus:ring-indigo-500 min-h-[60px]"
                  autoFocus
                />
                <div className="flex gap-1">
                  <button
                    type="button"
                    onClick={() => handleEdit(comment.id)}
                    className="p-1.5 rounded-lg bg-indigo-500 text-white hover:bg-indigo-600"
                  >
                    <Send className="h-3.5 w-3.5" />
                  </button>
                  <button
                    type="button"
                    onClick={() => setEditingId(null)}
                    className="p-1.5 rounded-lg bg-gray-200 text-gray-600 hover:bg-gray-300"
                  >
                    <X className="h-3.5 w-3.5" />
                  </button>
                </div>
              </div>
            ) : (
              <p className="text-sm text-gray-700 whitespace-pre-wrap">{comment.content}</p>
            )}

            <div className="flex items-center gap-2 mt-1.5">
              {EMOJIS.map((emoji) => (
                <button
                  key={emoji}
                  type="button"
                  onClick={() => handleReaction(comment.id, emoji)}
                  className={`text-xs px-1.5 py-0.5 rounded-full border hover:bg-gray-50 transition-colors ${
                    comment.reactions?.some((r) => r.emoji === emoji || r.reactionType === emoji)
                      ? "bg-indigo-50 border-indigo-200 text-indigo-600"
                      : "border-gray-200 text-gray-500"
                  }`}
                >
                  {emoji}
                  {comment.reactions?.filter((r) => r.emoji === emoji || r.reactionType === emoji).length > 0 && (
                    <span className="ml-0.5 font-medium">
                      {comment.reactions.filter((r) => r.emoji === emoji || r.reactionType === emoji).length}
                    </span>
                  )}
                </button>
              ))}

              <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity ml-2">
                <button
                  type="button"
                  onClick={() => {
                    setReplyTo(comment);
                    setEditingId(null);
                    inputRef.current?.focus();
                  }}
                  className="p-1 rounded hover:bg-gray-100 text-gray-400 hover:text-gray-600"
                  title="Reply"
                >
                  <Reply className="h-3.5 w-3.5" />
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setEditingId(comment.id);
                    setEditContent(comment.content);
                    setReplyTo(null);
                  }}
                  className="p-1 rounded hover:bg-gray-100 text-gray-400 hover:text-gray-600"
                  title="Edit"
                >
                  <Edit2 className="h-3.5 w-3.5" />
                </button>
                <button
                  type="button"
                  onClick={() => handleDelete(comment.id)}
                  className="p-1 rounded hover:bg-red-50 text-gray-400 hover:text-red-500"
                  title="Delete"
                >
                  <Trash2 className="h-3.5 w-3.5" />
                </button>
              </div>
            </div>

            {comment.replies?.length > 0 && (
              <div className="mt-3 space-y-1">
                {comment.replies.map((reply) => renderComment(reply, true))}
              </div>
            )}
          </div>
        </div>
      </div>
    );
  };

  return (
    <div>
      <div className="flex items-center gap-2 mb-4">
        <MessageSquare className="h-5 w-5 text-indigo-500" />
        <h3 className="font-semibold text-gray-900">Comments</h3>
        {comments.length > 0 && (
          <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-700">
            {comments.length}
          </span>
        )}
      </div>

      {/* New comment form */}
      <div className="flex items-start gap-3 mb-6">
        <div className="flex-1">
          {replyTo && (
            <div className="flex items-center gap-2 mb-2 px-3 py-1.5 bg-gray-50 rounded-lg text-sm text-gray-500">
              <Reply className="h-3.5 w-3.5" />
              <span>
                Replying to <strong>{replyTo.author?.name || replyTo.author?.username || "Unknown"}</strong>
              </span>
              <button
                type="button"
                onClick={() => setReplyTo(null)}
                className="ml-auto p-0.5 hover:bg-gray-200 rounded"
              >
                <X className="h-3.5 w-3.5" />
              </button>
            </div>
          )}
          <textarea
            ref={inputRef}
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            placeholder="Write a comment..."
            className="w-full text-sm rounded-xl border border-gray-200 px-4 py-3 resize-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent min-h-[80px]"
            onKeyDown={(e) => {
              if (e.key === "Enter" && !e.shiftKey) {
                e.preventDefault();
                handleSubmit();
              }
            }}
          />
        </div>
        <button
          type="button"
          onClick={handleSubmit}
          disabled={loading || !newComment.trim()}
          className="shrink-0 p-3 rounded-xl bg-indigo-500 text-white hover:bg-indigo-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors self-end"
        >
          <Send className="h-4 w-4" />
        </button>
      </div>

      {/* Comment list */}
      <div className="space-y-1 max-h-[500px] overflow-y-auto">
        {comments
          .filter((c) => !c.parentCommentId)
          .map((comment) => renderComment(comment))}
        {comments.length === 0 && (
          <p className="text-sm text-gray-400 text-center py-6">
            No comments yet. Be the first to comment!
          </p>
        )}
      </div>
    </div>
  );
}
