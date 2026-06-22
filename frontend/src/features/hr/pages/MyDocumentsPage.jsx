import { useEffect, useState } from 'react';
import {
  FileText,
  Download,
  Upload,
  CheckCircle2,
  Clock,
} from 'lucide-react';
import { toast } from 'sonner';
import { getMyDocuments } from '../api/hrApi';
import { CardSkeleton } from '../../../components/LoadingSkeleton';
import { EmptyState } from '../../../components/EmptyStates';
import ErrorState from '../../../components/ErrorState';

const DOC_STATUS_ICON = {
  Available: <CheckCircle2 className="w-4 h-4 text-emerald-600" />,
  'Pending Upload': <Clock className="w-4 h-4 text-amber-600" />,
};

const DOC_STATUS_BADGE = {
  Available: 'bg-emerald-100 text-emerald-700',
  'Pending Upload': 'bg-amber-100 text-amber-700',
};

export default function MyDocumentsPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadDocuments();
  }, []);

  const loadDocuments = async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await getMyDocuments();
      setData(result);
    } catch (e) {
      setError(e.message || 'Failed to load documents');
      toast.error(e.message || 'Failed to load documents');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="p-4 sm:p-6">
        <div className="mb-8">
          <div className="h-10 bg-gray-200 rounded w-1/3 mb-2 animate-pulse" />
          <div className="h-4 bg-gray-200 rounded w-1/4 animate-pulse" />
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {[1, 2, 3].map((i) => <CardSkeleton key={i} />)}
        </div>
      </div>
    );
  }

  if (error) {
    return <ErrorState title="Failed to load documents" message={error} onRetry={loadDocuments} />;
  }

  const documents = data?.documents || [];

  return (
    <div className="p-4 sm:p-6">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
          <FileText className="w-7 h-7 text-indigo-600" /> My Documents
        </h1>
        <p className="text-slate-500 mt-1">View and manage your employee documents</p>
      </div>

      {documents.length === 0 ? (
        <EmptyState title="No documents" message="No documents are available yet." />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
          {documents.map((doc, idx) => (
            <div key={idx} className="bg-white rounded-2xl border border-slate-200 p-6 hover:shadow-lg transition-shadow">
              <div className="flex items-start justify-between mb-4">
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-indigo-50">
                  <FileText className="w-6 h-6 text-indigo-600" />
                </div>
                <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${DOC_STATUS_BADGE[doc.status] || 'bg-slate-100 text-slate-600'}`}>
                  {DOC_STATUS_ICON[doc.status]}
                  {doc.status}
                </span>
              </div>
              <h3 className="text-lg font-semibold text-slate-900 mb-2">{doc.name}</h3>
              <p className="text-sm text-slate-500 mb-4">{doc.type?.replace(/_/g, ' ')}</p>
              <div className="flex gap-2">
                {doc.status === 'Available' && (
                  <button className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-indigo-50 text-indigo-700 text-xs font-medium hover:bg-indigo-100">
                    <Download className="w-3 h-3" /> Download
                  </button>
                )}
                {doc.status === 'Pending Upload' && (
                  <button className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-amber-50 text-amber-700 text-xs font-medium hover:bg-amber-100">
                    <Upload className="w-3 h-3" /> Upload
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
