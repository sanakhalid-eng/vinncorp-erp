import { useState, useEffect } from "react";
import { getEditHistory } from "../../api/commentApi";
export default function EditHistoryModal({ commentId, isOpen, onClose }) {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(false);
  useEffect(() => {
    if (isOpen && commentId) {
      setLoading(true);
      getEditHistory(commentId)
        .then(setHistory)
        .catch(() => setHistory([]))
        .finally(() => setLoading(false));
    }
  }, [isOpen, commentId]);
  if (!isOpen) return null;
  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      onClick={onClose}
    >
       
      <div
        className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
         
        <div className="mb-4 flex items-center justify-between">
           
          <h3 className="text-lg font-bold text-slate-900">
            Edit History
          </h3> 
          <button
            onClick={onClose}
            className="rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
          >
             
            <svg
              className="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
               
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M6 18L18 6M6 6l12 12"
              /> 
            </svg> 
          </button> 
        </div> 
        {loading ? (
          <div className="py-8 text-center text-sm text-gray-400">
            Loading history...
          </div>
        ) : history.length === 0 ? (
          <div className="py-8 text-center text-sm text-gray-400">
            No edit history available
          </div>
        ) : (
          <div className="space-y-4 max-h-96 overflow-y-auto">
             
            {history.map((entry) => (
              <div
                key={entry.id}
                className="rounded-xl border border-gray-100 bg-gray-50 p-4"
              >
                 
                <div className="mb-2 flex items-center justify-between">
                   
                  <span className="text-xs font-medium text-gray-500">
                     
                    Edited by {entry.editedBy?.name ?? "Unknown"} 
                  </span> 
                  <span className="text-xs text-gray-400">
                     
                    {new Date(entry.editedAt).toLocaleString()} 
                  </span> 
                </div> 
                <p className="whitespace-pre-wrap text-sm text-gray-700">
                  {entry.oldContent}
                </p> 
              </div>
            ))} 
          </div>
        )} 
      </div> 
    </div>
  );
}
