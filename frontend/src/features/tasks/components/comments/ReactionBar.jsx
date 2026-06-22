import { useState } from "react";
const REACTIONS = ["≡ƒæì", "Γ¥ñ∩╕Å", "≡ƒÿé", "≡ƒÿ«", "≡ƒÿó"];
export default function ReactionBar({
  reactions,
  currentUser,
  onToggle,
  compact = false,
}) {
  const [showPicker, setShowPicker] = useState(false);
  const reactionCounts = {};
  const userReacted = {};
  (reactions ?? []).forEach((reaction) => {
    reactionCounts[reaction.type] = reaction.count;
    if (reaction.users?.includes(currentUser?.name)) {
      userReacted[reaction.type] = true;
    }
  });
  const activeTypes = Object.keys(reactionCounts);
  if (activeTypes.length === 0 && !showPicker) {
    return (
      <button
        onClick={() => setShowPicker(true)}
        className={`inline-flex items-center gap-1 rounded-full border border-gray-200 px-2 py-0.5 text-xs text-gray-400 transition hover:border-gray-300 hover:text-gray-600 ${compact ? "px-1.5 text-[10px]" : ""}`}
      >
         
        <span>+ React</span> 
      </button>
    );
  }
  return (
    <div className="relative inline-flex flex-wrap items-center gap-1">
       
      {activeTypes.map((type) => (
        <button
          key={type}
          onClick={() => onToggle(type)}
          className={`inline-flex items-center gap-1 rounded-full border px-2 py-0.5 text-xs transition-all ${userReacted[type] ? "border-blue-300 bg-blue-50 text-blue-700" : "border-gray-200 bg-white text-gray-600 hover:border-gray-300"} ${compact ? "px-1.5 text-[10px]" : ""}`}
        >
           
          <span>{type}</span> 
          <span className="font-medium">{reactionCounts[type]}</span> 
        </button>
      ))} 
      <button
        onClick={() => setShowPicker(!showPicker)}
        className={`inline-flex items-center rounded-full border border-gray-200 px-2 py-0.5 text-xs text-gray-400 transition hover:border-gray-300 hover:text-gray-600 ${compact ? "px-1.5" : ""}`}
      >
         
        + 
      </button> 
      {showPicker && (
        <div className="absolute bottom-full left-0 mb-2 rounded-lg border border-gray-200 bg-white p-2 shadow-lg">
           
          <div className="flex gap-1">
             
            {REACTIONS.map((emoji) => (
              <button
                key={emoji}
                onClick={() => {
                  onToggle(emoji);
                  setShowPicker(false);
                }}
                className="rounded p-1 text-lg transition hover:bg-gray-100"
              >
                 
                {emoji} 
              </button>
            ))} 
          </div> 
        </div>
      )} 
    </div>
  );
}
