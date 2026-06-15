import { useState, useEffect, useCallback } from "react";
import {
  getComments,
  createComment,
  updateComment,
  deleteComment,
  toggleReaction,
} from "../../api/commentApi";
import CommentItem from "./CommentItem";
import CommentForm from "./CommentForm";
export default function CommentsList({ taskId, currentUser }) {
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const fetchComments = useCallback(async () => {
    try {
      setLoading(true);
      const data = await getComments(taskId);
      setComments(data);
      setError(null);
    } catch (err) {
      console.error("Failed to fetch comments:", err);
      setError("Failed to load comments");
    } finally {
      setLoading(false);
    }
  }, [taskId]);
  useEffect(() => {
    if (taskId) {
      fetchComments();
    }
  }, [taskId, fetchComments]);
  const handleCreate = async (content) => {
    try {
      await createComment(taskId, content);
      fetchComments();
    } catch (err) {
      console.error("Failed to create comment:", err);
    }
  };
  const handleReply = async (parentCommentId, content) => {
    try {
      await createComment(taskId, content, parentCommentId);
      fetchComments();
    } catch (err) {
      console.error("Failed to reply:", err);
    }
  };
  const handleEdit = async (commentId, content) => {
    try {
      await updateComment(commentId, content);
      fetchComments();
    } catch (err) {
      console.error("Failed to edit comment:", err);
    }
  };
  const handleDelete = async (commentId) => {
    if (!window.confirm("Delete this comment?")) return;
    try {
      await deleteComment(commentId);
      fetchComments();
    } catch (err) {
      console.error("Failed to delete comment:", err);
    }
  };
  const handleToggleReaction = async (commentId, reactionType) => {
    try {
      await toggleReaction(commentId, reactionType);
      fetchComments();
    } catch (err) {
      console.error("Failed to toggle reaction:", err);
    }
  };
  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
         
        <div className="animate-spin h-6 w-6 border-2 border-gray-300 border-t-blue-500 rounded-full" /> 
        <span className="ml-3 text-sm text-gray-500">
          Loading comments...
        </span> 
      </div>
    );
  }
  if (error) {
    return <div className="text-center py-8 text-red-500 text-sm">{error}</div>;
  }
  return (
    <div className="space-y-4">
       
      <CommentForm
        onSubmit={handleCreate}
        placeholder="Add a comment... Use @name to mention someone"
      /> 
      {comments.length === 0 ? (
        <div className="text-center py-10 text-gray-400">
           
          <svg
            className="mx-auto h-12 w-12 text-gray-300"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
             
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={1.5}
              d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"
            /> 
          </svg> 
          <p className="mt-2 text-sm">
            No comments yet. Start the discussion!
          </p> 
        </div>
      ) : (
        <div className="space-y-4">
           
          {comments.map((comment) => (
            <CommentItem
              key={comment.id}
              comment={comment}
              currentUser={currentUser}
              onReply={handleReply}
              onEdit={handleEdit}
              onDelete={handleDelete}
              onToggleReaction={handleToggleReaction}
            />
          ))} 
        </div>
      )} 
    </div>
  );
}
