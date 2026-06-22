import { useEffect, useState, useRef } from "react";
import { BookOpen, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import {
  createKnowledgeArticle,
  deleteKnowledgeArticle,
  listPublishedArticles,
} from "../api/notesApi";
import { CardSkeleton } from "../../../components/LoadingSkeleton";
import EmptyState from "../../../components/EmptyState";
import ErrorState from "../../../components/ErrorState";
import ConfirmationDialog from "../../projects/components/members/ConfirmationDialog";

export default function KnowledgeHub() {
  const [articles, setArticles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [articleToDelete, setArticleToDelete] = useState(null);
  const titleInputRef = useRef(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await listPublishedArticles(0, 50);
      setArticles(res.data.data?.content || []);
    } catch (e) {
      setError(e);
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

  const handleDelete = (id) => {
    setArticleToDelete(id);
    setShowConfirmDialog(true);
  };

  const confirmDelete = async () => {
    if (!articleToDelete) return;
    try {
      await deleteKnowledgeArticle(articleToDelete);
      toast.success("Article removed");
      load();
    } catch {
      toast.error("Could not delete article");
    } finally {
      setShowConfirmDialog(false);
      setArticleToDelete(null);
    }
  };

  const focusCreateForm = () => {
    titleInputRef.current?.focus();
    titleInputRef.current?.scrollIntoView({ behavior: "smooth", block: "center" });
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
          ref={titleInputRef}
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
        <div className="space-y-3">
          {[1, 2, 3].map((i) => <CardSkeleton key={i} />)}
        </div>
      ) : error ? (
        <ErrorState error={error} onRetry={load} />
      ) : articles.length === 0 ? (
        <EmptyState
          icon={BookOpen}
          title="No articles yet"
          description="Publish knowledge base articles for your team to reference."
          action={{ label: "Write Article", icon: Plus, onClick: focusCreateForm }}
        />
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
        </ul>
      )}

      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={() => { setShowConfirmDialog(false); setArticleToDelete(null); }}
        onConfirm={confirmDelete}
        title="Delete article?"
        message="This article will be permanently removed from the knowledge hub."
        confirmText="Delete"
      />
    </div>
  );
}
