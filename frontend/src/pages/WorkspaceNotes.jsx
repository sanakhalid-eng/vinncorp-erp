import { useEffect, useState } from "react";
import { Loader2, Plus, StickyNote, Trash2, Edit } from "lucide-react";
import { toast } from "sonner";
import {
  createWorkspaceNote,
  deleteWorkspaceNote,
  updateWorkspaceNote,
  listWorkspaceNotes,
} from "../api/notesApi";
export default function WorkspaceNotes() {
  const [notes, setNotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [editingNoteId, setEditingNoteId] = useState(null);
  const [editTitle, setEditTitle] = useState("");
  const [editContent, setEditContent] = useState("");
  const [editPinned, setEditPinned] = useState(false);
  const load = async () => {
    setLoading(true);
    try {
      const res = await listWorkspaceNotes({ page: 0, size: 50 });
      setNotes(res.data.data?.content || []);
    } catch {
      toast.error("Failed to load notes");
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
  const handleDelete = async (id) => {
    try {
      await deleteWorkspaceNote(id);
      load();
    } catch {
      toast.error("Could not delete note");
    }
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
        <div className="flex justify-center py-12">
           
          <Loader2 className="h-8 w-8 animate-spin text-indigo-600" /> 
        </div>
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
          {notes.length === 0 && (
            <p className="text-slate-500 text-sm">No notes yet.</p>
          )} 
        </ul>
      )} 
    </div>
  );
}
