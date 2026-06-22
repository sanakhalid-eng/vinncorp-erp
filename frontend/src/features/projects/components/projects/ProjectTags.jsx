import { Tag } from "lucide-react";
const parseTags = (tags) => {
  if (!tags) return [];
  if (Array.isArray(tags)) return tags;
  return tags
    .split(",")
    .map((t) => t.trim())
    .filter(Boolean);
};
export default function ProjectTags({ tags, maxShow = 4 }) {
  const tagList = parseTags(tags);
  if (tagList.length === 0) return null;
  return (
    <div className="flex flex-wrap gap-2">
       
      {tagList.slice(0, maxShow).map((tag, idx) => (
        <span
          key={idx}
          className="inline-flex items-center gap-1 rounded-xl bg-slate-100 dark:bg-slate-800 px-2.5 py-1 text-xs text-slate-600 dark:text-slate-300"
        >
           
          <Tag className="w-3 h-3" /> {tag} 
        </span>
      ))} 
    </div>
  );
}
