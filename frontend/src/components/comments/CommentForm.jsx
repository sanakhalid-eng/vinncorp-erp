import { useState, useRef, useEffect } from "react";
export default function CommentForm({
  onSubmit,
  onCancel,
  placeholder = "Write a comment...",
  initialValue = "",
  autoFocus = false,
}) {
  const [content, setContent] = useState(initialValue);
  const [isFocused, setIsFocused] = useState(false);
  const textareaRef = useRef(null);
  useEffect(() => {
    if (autoFocus && textareaRef.current) {
      textareaRef.current.focus();
    }
  }, [autoFocus]);
  const handleSubmit = () => {
    const trimmed = content.trim();
    if (!trimmed) return;
    onSubmit(trimmed);
    setContent("");
  };
  const handleKeyDown = (e) => {
    if (e.key === "Enter" && (e.ctrlKey || e.metaKey)) {
      e.preventDefault();
      handleSubmit();
    }
    if (e.key === "Escape" && onCancel) {
      onCancel();
    }
  };
  return (
    <div
      className={`rounded-xl border transition-colors ${isFocused ? "border-blue-300 ring-2 ring-blue-100" : "border-gray-200"}`}
    >
       
      <textarea
        ref={textareaRef}
        value={content}
        onChange={(e) => setContent(e.target.value)}
        onFocus={() => setIsFocused(true)}
        onBlur={() => setIsFocused(false)}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        rows={initialValue ? 3 : 2}
        className="w-full resize-none rounded-t-xl border-none bg-transparent p-3 text-sm text-gray-800 placeholder-gray-400 focus:outline-none focus:ring-0"
      /> 
      <div className="flex items-center justify-between border-t border-gray-100 px-3 py-2">
         
        <span className="text-[10px] text-gray-400">
           
          {content.length > 0
            ? `${content.length}/5000`
            : "Ctrl+Enter to submit"} 
        </span> 
        <div className="flex gap-2">
           
          {onCancel && (
            <button
              onClick={onCancel}
              className="rounded-lg px-3 py-1.5 text-xs font-medium text-gray-500 transition hover:bg-gray-100"
            >
               
              Cancel 
            </button>
          )} 
          <button
            onClick={handleSubmit}
            disabled={!content.trim()}
            className="rounded-lg bg-blue-600 px-3 py-1.5 text-xs font-medium text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-gray-300"
          >
             
            {initialValue ? "Save" : "Comment"} 
          </button> 
        </div> 
      </div> 
    </div>
  );
}
