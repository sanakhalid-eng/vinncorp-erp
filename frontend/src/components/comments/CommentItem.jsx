import { useState } from "react";
import ReactionBar from "./ReactionBar";
import CommentForm from "./CommentForm";
import EditHistoryModal from "./EditHistoryModal";
import { usePermission } from "../../context/usePermission.js";
function formatTimeAgo(dateString) {
  if (!dateString) return "";
  const date = new Date(dateString);
  const now = new Date();
  const seconds = Math.floor((now - date) / 1000);
  if (seconds < 60) return "just now";
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}h ago`;
  if (seconds < 604800) return `${Math.floor(seconds / 86400)}d ago`;
  return date.toLocaleDateString();
}
export default function CommentItem({
  comment,
  currentUser,
  onReply,
  onEdit,
  onDelete,
  onToggleReaction,
  maxDepth = 2,
  currentDepth = 0,
}) {
  const [showReply, setShowReply] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [showHistory, setShowHistory] = useState(false);
  const { isAdmin } = usePermission();
  const isAuthor = currentUser?.name === comment.author?.name;
  const canDelete = isAuthor || isAdmin();
  const canReply = currentDepth < maxDepth && !comment.isDeleted;
  const handleReply = (content) => {
    onReply(comment.id, content);
    setShowReply(false);
  };
  const handleEdit = (content) => {
    onEdit(comment.id, content);
    setIsEditing(false);
  };
  if (comment.isDeleted && (!comment.replies || comment.replies.length === 0)) {
    return (
      <div className="rounded-xl border border-gray-100 bg-gray-50/50 p-4">
         
        <div className="flex items-center gap-2 text-sm text-gray-400 italic">
           
          <svg
            className="h-4 w-4"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
             
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m0 0V5a2 2 0 012-2h10a2 2 0 012 2v2"
            /> 
          </svg> 
          This comment was deleted 
        </div> 
      </div>
    );
  }
  return (
    <div
      className={`group ${currentDepth > 0 ? "ml-8 mt-3 border-l-2 border-gray-100 pl-4" : ""}`}
    >
       
      <div
        className={`rounded-xl border p-4 transition-colors ${comment.isDeleted ? "border-gray-100 bg-gray-50/50" : "border-gray-100 bg-white hover:border-gray-200"}`}
      >
         
        <div className="mb-2 flex items-center justify-between">
           
          <div className="flex items-center gap-2">
             
            <div className="flex h-7 w-7 items-center justify-center rounded-full bg-gradient-to-br from-blue-500 to-indigo-600 text-xs font-bold text-white">
               
              {comment.author?.name?.charAt(0)?.toUpperCase() ?? "U"} 
            </div> 
            <div>
               
              <span className="text-sm font-semibold text-slate-800">
                {comment.author?.name ?? "Unknown"}
              </span> 
              <span className="ml-2 text-xs text-gray-400">
                {formatTimeAgo(comment.createdAt)}
              </span> 
              {comment.isEdited && (
                <button
                  onClick={() => setShowHistory(true)}
                  className="ml-1 text-[10px] italic text-gray-400 hover:text-gray-600"
                >
                   
                  (edited) 
                </button>
              )} 
            </div> 
          </div> 
          {!comment.isDeleted && (isAuthor || canDelete) && (
            <div className="flex items-center gap-1 opacity-100 md:opacity-0 md:transition-opacity md:group-hover:opacity-100">
               
              {isAuthor && (
                <button
                  onClick={() => setIsEditing(true)}
                  className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
                  title="Edit"
                >
                   
                  <svg
                    className="h-3.5 w-3.5"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                     
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M15.232 5.232l3.536 3.536m-2.036-5.021a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"
                    /> 
                  </svg> 
                </button>
              )} 
              {canDelete && (
                <button
                  onClick={() => onDelete(comment.id)}
                  className="rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-500"
                  title="Delete"
                >
                   
                  <svg
                    className="h-3.5 w-3.5"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                     
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m0 0V5a2 2 0 012-2h10a2 2 0 012 2v2"
                    /> 
                  </svg> 
                </button>
              )} 
            </div>
          )} 
        </div> 
        {comment.isDeleted ? (
          <div className="text-sm italic text-gray-400">
            This comment was deleted
          </div>
        ) : isEditing ? (
          <CommentForm
            initialValue={comment.content}
            onSubmit={handleEdit}
            onCancel={() => setIsEditing(false)}
            autoFocus
          />
        ) : (
          <div className="mb-3 whitespace-pre-wrap text-sm text-gray-700">
            {comment.content}
          </div>
        )} 
        {!comment.isDeleted && (
          <div className="flex items-center gap-3">
             
            <ReactionBar
              reactions={comment.reactions}
              currentUser={currentUser}
              onToggle={(type) => onToggleReaction(comment.id, type)}
            /> 
            {canReply && (
              <button
                onClick={() => setShowReply(!showReply)}
                className="text-xs text-gray-400 transition hover:text-blue-600"
              >
                 
                Reply 
              </button>
            )} 
          </div>
        )} 
      </div> 
      {showReply && (
        <div className="ml-8 mt-2">
           
          <CommentForm
            onSubmit={handleReply}
            onCancel={() => setShowReply(false)}
            placeholder={`Reply to ${comment.author?.name}...`}
            autoFocus
          /> 
        </div>
      )} 
      {comment.replies?.length > 0 && (
        <div className="mt-2">
           
          {comment.replies.map((reply) => (
            <CommentItem
              key={reply.id}
              comment={reply}
              currentUser={currentUser}
              onReply={onReply}
              onEdit={onEdit}
              onDelete={onDelete}
              onToggleReaction={onToggleReaction}
              maxDepth={maxDepth}
              currentDepth={currentDepth + 1}
            />
          ))} 
        </div>
      )} 
      <EditHistoryModal
        commentId={comment.id}
        isOpen={showHistory}
        onClose={() => setShowHistory(false)}
      /> 
    </div>
  );
}
