import { useEffect, useState, useRef } from "react";
import { Plus, StickyNote, Trash2, Edit } from "lucide-react";
import { toast } from "sonner";
import {
  createWorkspaceNote,
  deleteWorkspaceNote,
  updateWorkspaceNote,
  listWorkspaceNotes,
} from "../api/notesApi";
import { CardSkeleton } from "../../../components/LoadingSkeleton";
import EmptyState from "../../../components/EmptyState";
import ErrorState from "../../../components/ErrorState";
import ConfirmationDialog from "../../projects/components/members/ConfirmationDialog";

export default function WorkspaceNotes() {
  const [notes, setNotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [editingNoteId, setEditingNoteId] = useState(null);
  const [editTitle, setEditTitle] = useState("");
  const [editContent, setEditContent] = useState("");
  const [editPinned, setEditPinned] = useState(false);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [noteToDelete, setNoteToDelete] = useState(null);
  const titleInputRef = useRef(null);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await listWorkspaceNotes({ page: 0, size: 50 });
      setNotes(res.data.data?.content || []);
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
      await createWorkspaceNote({ title, content, pinned: false });
      setTitle("");
      setContent("");
      toast.success("Note created");
      load();
    } catch {
      toast.error("Could not create note");
    }
  };

  const handleEdit = async (e) => {
    e.preventDefault();
    if (!editingNoteId) return;
    try {
      await updateWorkspaceNote(editingNoteId, {
        title: editTitle,
        content: editContent,
        pinned: editPinned,
      });
      setEditingNoteId(null);
      setEditTitle("");
      setEditContent("");
      setEditPinned(false);
      toast.success("Note updated");
      load();
    } catch {
      toast.error("Could not update note");
    }
  };

  const handleDelete = (id) => {
    setNoteToDelete(id);
    setShowConfirmDialog(true);
  };

  const confirmDelete = async () => {
    if (!noteToDelete) return;
    try {
      await deleteWorkspaceNote(noteToDelete);
      toast.success("Note deleted");
      load();
    } catch {
      toast.error("Could not delete note");
    } finally {
      setShowConfirmDialog(false);
      setNoteToDelete(null);
    }
  };

  const focusCreateForm = () => {
    titleInputRef.current?.focus();
    titleInputRef.current?.scrollIntoView({ behavior: "smooth", block: "center" });
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <header>
        <h1 className="text-2xl font-bold text-slate-900 flex items-center gap-2">
          <StickyNote className="h-7 w-7 text-amber-500" /> Workspace Notes
        </h1>
        <p className="text-slate-500 text-sm mt-1">
          Shared workspace and project notes
        </p>
      </header>

      <form
        onSubmit={handleCreate}
        className="rounded-xl border border-slate-200 bg-white p-4 space-y-3"
      >
        <input
          ref={titleInputRef}
          className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
          placeholder="Note title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
        <textarea
          className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm min-h-[80px]"
          placeholder="Content"
          value={content}
          onChange={(e) => setContent(e.target.value)}
        />
        <button
          type="submit"
          className="inline-flex items-center gap-2 rounded-lg bg-amber-500 px-4 py-2 text-sm text-white"
        >
          <Plus className="h-4 w-4" /> Add note
        </button>
      </form>

      {loading ? (
        <div className="space-y-3">
          {[1, 2, 3].map((i) => <CardSkeleton key={i} />)}
        </div>
      ) : error ? (
        <ErrorState error={error} onRetry={load} />
      ) : editingNoteId !== null ? (
        <div className="rounded-xl border border-slate-200 bg-white p-4">
          <h2 className="font-semibold text-slate-900 mb-2">Edit Note</h2>
          <form onSubmit={handleEdit} className="space-y-3">
            <input
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm"
              placeholder="Note title"
              value={editTitle}
              onChange={(e) => setEditTitle(e.target.value)}
            />
            <textarea
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm min-h-[80px]"
              placeholder="Content"
              value={editContent}
              onChange={(e) => setEditContent(e.target.value)}
            />
            <div className="flex items-center space-x-3">
              <label className="flex items-center space-x-2 text-slate-600 text-sm">
                <input
                  type="checkbox"
                  checked={editPinned}
                  onChange={(e) => setEditPinned(e.target.checked)}
                  className="h-4 w-4 text-amber-600"
                />
                Pin note
              </label>
            </div>
            <div className="flex justify-end space-x-2 mt-4">
              <button
                type="button"
                onClick={() => {
                  setEditingNoteId(null);
                  setEditTitle("");
                  setEditContent("");
                  setEditPinned(false);
                }}
                className="text-slate-500 hover:text-slate-700"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="inline-flex items-center gap-2 rounded-lg bg-amber-500 px-4 py-2 text-sm text-white"
              >
                <Plus className="h-4 w-4" /> Save changes
              </button>
            </div>
          </form>
        </div>
      ) : notes.length === 0 ? (
        <EmptyState
          icon={StickyNote}
          title="No notes yet"
          description="Capture ideas, meeting notes, and team updates in one place."
          action={{ label: "Add Note", icon: Plus, onClick: focusCreateForm }}
        />
      ) : (
        <ul className="space-y-3">
          {notes.map((n) => (
            <li
              key={n.id}
              className="rounded-xl border border-slate-200 bg-white p-4 flex justify-between"
            >
              <div>
                <h2 className="font-semibold text-slate-900">{n.title}</h2>
                {n.content && (
                  <p className="text-sm text-slate-600 mt-1 whitespace-pre-wrap">
                    {n.content}
                  </p>
                )}
              </div>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => {
                    setEditingNoteId(n.id);
                    setEditTitle(n.title);
                    setEditContent(n.content || "");
                    setEditPinned(n.pinned || false);
                  }}
                  className="text-amber-500 hover:text-amber-700"
                >
                  <Edit className="h-4 w-4" />
                </button>
                <button
                  type="button"
                  onClick={() => handleDelete(n.id)}
                  className="text-red-500"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}

      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={() => { setShowConfirmDialog(false); setNoteToDelete(null); }}
        onConfirm={confirmDelete}
        title="Delete note?"
        message="This note will be permanently removed."
        confirmText="Delete"
      />
    </div>
  );
}
