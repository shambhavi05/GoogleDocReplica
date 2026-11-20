import { useEffect, useState } from 'react';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import { X, History, ArrowLeft } from 'lucide-react';
import type { DocumentVersionDetail, DocumentVersionSummary } from '../types/documentVersion';
import { getDocumentVersion, listDocumentVersions } from '../services/documentVersions';
import { ApiError } from '../services/api';
import { formatRelativeTime } from '../utils/formatDate';

interface VersionHistoryPanelProps {
  documentId: string;
  onClose: () => void;
}

export default function VersionHistoryPanel({ documentId, onClose }: VersionHistoryPanelProps) {
  const [versions, setVersions] = useState<DocumentVersionSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<DocumentVersionDetail | null>(null);

  const previewEditor = useEditor({
    extensions: [StarterKit],
    content: '',
    editable: false,
  });

  useEffect(() => {
    listDocumentVersions(documentId)
      .then(setVersions)
      .catch((err) => setError(err instanceof ApiError ? err.message : 'Could not load version history'))
      .finally(() => setLoading(false));
  }, [documentId]);

  async function openVersion(versionId: string) {
    setError(null);
    try {
      const detail = await getDocumentVersion(documentId, versionId);
      setSelected(detail);
      previewEditor?.commands.setContent(detail.content, { emitUpdate: false });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not load this version');
    }
  }

  return (
    <div className="fixed inset-y-0 right-0 z-40 flex w-full max-w-md flex-col border-l border-neutral-200 bg-white shadow-xl">
      <div className="flex items-center justify-between border-b border-neutral-200 px-4 py-3">
        <h2 className="flex items-center gap-2 text-sm font-semibold text-neutral-900">
          {selected ? (
            <button
              type="button"
              onClick={() => setSelected(null)}
              className="flex items-center gap-1.5 text-neutral-600 hover:text-neutral-900"
            >
              <ArrowLeft size={15} /> History
            </button>
          ) : (
            <>
              <History size={16} /> Version history
            </>
          )}
        </h2>
        <button
          type="button"
          onClick={onClose}
          className="rounded-md p-1 text-neutral-400 hover:bg-neutral-100 hover:text-neutral-600"
        >
          <X size={18} />
        </button>
      </div>

      {error && (
        <div className="mx-4 mt-3 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </div>
      )}

      <div className="flex-1 overflow-y-auto px-4 py-3">
        {selected ? (
          <div className="rounded-lg border border-neutral-200 p-4">
            <p className="mb-2 text-xs text-neutral-500">
              Saved by {selected.savedBy.name} · {formatRelativeTime(selected.createdAt)} · read-only
            </p>
            <EditorContent editor={previewEditor} className="docspace-prose" />
          </div>
        ) : loading ? (
          <p className="text-sm text-neutral-500">Loading history…</p>
        ) : versions.length === 0 ? (
          <p className="text-sm text-neutral-500">
            No past versions yet. Every save after this one will appear here.
          </p>
        ) : (
          <ul className="flex flex-col gap-2">
            {versions.map((v) => (
              <li key={v.id}>
                <button
                  type="button"
                  onClick={() => openVersion(v.id)}
                  className="w-full rounded-lg border border-neutral-200 px-3 py-2 text-left transition hover:border-brand-300 hover:bg-brand-50"
                >
                  <p className="text-sm font-medium text-neutral-800">{v.title}</p>
                  <p className="text-xs text-neutral-500">
                    v{v.version} · saved by {v.savedBy.name} · {formatRelativeTime(v.createdAt)}
                  </p>
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
