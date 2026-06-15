import { useEffect, useState } from "react";
import { BookOpen, Loader2, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import {
  createKnowledgeArticle,
  deleteKnowledgeArticle,
  listPublishedArticles,
} from "../api/notesApi";

export default function KnowledgeHub() {
  const [articles, setArticles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");

  const load = async () => {
    setLoading(true);
    try {
      const res = await listPublishedArticles(0, 50);
      setArticles(res.data.data?.content || []);
    } catch {
      toast.error("Failed to load articles");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!title.trim()) return;
    try {
      await createKnowledgeArticle({
        title,
        markdownContent: content,
        published: true,
      });
      setTitle("");
      setContent("");
      toast.success("Article published");
      load();
    } catch {
      toast.error("Could not create article");
    }
  };

  const handleDelete = async (id) => {
    try {
      await deleteKnowledgeArticle(id);
      toast.success("Article removed");
      load();
    } catch {
      toast.error("Could not delete article");
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 flex items-center gap-2">
          <BookOpen className="h-7 w-7 text-indigo-600" />
          Knowledge Hub
        </h1>
        <p className="text-slate-500 text-sm mt-1">
          Workspace markdown articles
        </p>
      </div>

      <form
        onSubmit={handleCreate}
        className="rounded-xl border border-slate-200 bg-white p-4 space-y-3"
      >
        <input
          className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
          placeholder="Article title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
        <textarea
          className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm min-h-[120px]"
          placeholder="Markdown content"
          value={content}
          onChange={(e) => setContent(e.target.value)}
        />
        <button
          type="submit"
          className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm text-white"
        >
          <Plus className="h-4 w-4" />
          Publish
        </button>
      </form>

      {loading ? (
        <div className="flex justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
        </div>
      ) : (
        <ul className="space-y-3">
          {articles.map((a) => (
            <li
              key={a.id}
              className="rounded-xl border border-slate-200 bg-white p-4 flex justify-between gap-4"
            >
              <div>
                <h2 className="font-semibold text-slate-900">{a.title}</h2>
                <p className="text-xs text-slate-500 mt-1">/{a.slug}</p>
                {a.markdownContent && (
                  <p className="text-sm text-slate-600 mt-2 line-clamp-3 whitespace-pre-wrap">
                    {a.markdownContent}
                  </p>
                )}
              </div>
              <button
                type="button"
                onClick={() => handleDelete(a.id)}
                className="text-red-500 hover:text-red-700"
              >
                <Trash2 className="h-4 w-4" />
              </button>
            </li>
          ))}
          {articles.length === 0 && (
            <p className="text-slate-500 text-sm">No published articles yet.</p>
          )}
        </ul>
      )}
    </div>
  );
}
