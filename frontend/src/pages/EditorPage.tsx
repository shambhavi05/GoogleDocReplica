import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import { ArrowLeft, Share2, Trash2, Users, AlertTriangle, MessageSquare, History, Download } from 'lucide-react';
import { API_BASE_URL } from '../services/api';
import { deleteDocument, getDocument, updateDocument } from '../services/documents';
import { ApiError } from '../services/api';
import type { DocumentDetail } from '../types/document';
import { useAuth } from '../hooks/useAuth';
import { useAutosave } from '../hooks/useAutosave';
import { usePresence } from '../hooks/usePresence';
import EditorToolbar from '../components/EditorToolbar';
import SaveStatusIndicator from '../components/SaveStatusIndicator';
import ShareModal from '../components/ShareModal';
import ConfirmDialog from '../components/ConfirmDialog';
import PresenceIndicator from '../components/PresenceIndicator';
import CommentsPanel from '../components/CommentsPanel';
import VersionHistoryPanel from '../components/VersionHistoryPanel';

export default function EditorPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [doc, setDoc] = useState<DocumentDetail | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [title, setTitle] = useState('');
  const [showShare, setShowShare] = useState(false);
  const [showComments, setShowComments] = useState(false);
  const [showHistory, setShowHistory] = useState(false);
  const [showExportMenu, setShowExportMenu] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const versionRef = useRef(0);
  const activeUsers = usePresence(doc ? id : undefined);

  const canEdit = doc?.role === 'OWNER' || doc?.role === 'EDITOR';
  const canManageSharing = doc?.role === 'OWNER';
  const readyToEditRef = useRef(false);

  const { status, errorMessage, schedule, saveNow } = useAutosave<{
    title?: string;
    content?: Record<string, unknown>;
  }>({
    delayMs: 1000,
    onSave: async (payload) => {
      if (!id) throw new Error('Missing document id');
      const updated = await updateDocument(id, { ...payload, version: versionRef.current });
      versionRef.current = updated.version;
      setDoc(updated);
      return { version: updated.version };
    },
  });

  const editor = useEditor({
    extensions: [StarterKit],
    content: '',
    editable: false,
    onUpdate: ({ editor }) => {
      if (!readyToEditRef.current) return;
      schedule({ content: editor.getJSON() });
    },
  });

  useEffect(() => {
    if (!id) return;
    let cancelled = false;
    readyToEditRef.current = false;

    getDocument(id)
      .then((data) => {
        if (cancelled) return;
        setDoc(data);
        setTitle(data.title);
        versionRef.current = data.version;
        editor?.commands.setContent(data.content, { emitUpdate: false });
        editor?.setEditable(data.role === 'OWNER' || data.role === 'EDITOR');
        readyToEditRef.current = data.role === 'OWNER' || data.role === 'EDITOR';
      })
      .catch((err) => {
        if (cancelled) return;
        setLoadError(err instanceof ApiError ? err.message : 'Could not load this document');
      });

    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, editor]);

  const handleTitleBlur = useCallback(() => {
    if (!doc || !canEdit) return;
    const trimmed = title.trim();
    if (!trimmed || trimmed === doc.title) {
      setTitle(doc.title);
      return;
    }
    saveNow({ title: trimmed }).catch(() => {
      setTitle(doc.title);
    });
  }, [doc, title, canEdit, saveNow]);

  async function handleConfirmDelete() {
    if (!doc) return;
    setDeleting(true);
    setDeleteError(null);
    try {
      await deleteDocument(doc.id);
      navigate('/', { replace: true });
    } catch (err) {
      setDeleteError(err instanceof ApiError ? err.message : 'Could not delete document');
    } finally {
      setDeleting(false);
    }
  }

  if (loadError) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-3 bg-neutral-50 px-4 text-center">
        <AlertTriangle className="text-red-500" size={28} />
        <p className="text-sm text-neutral-600">{loadError}</p>
        <button
          type="button"
          onClick={() => navigate('/')}
          className="rounded-lg bg-brand-600 px-4 py-2 text-sm font-medium text-white hover:bg-brand-700"
        >
          Back to dashboard
        </button>
      </div>
    );
  }

  if (!doc) {
    return (
      <div className="flex min-h-screen items-center justify-center text-sm text-neutral-400">
        Loading document…
      </div>
    );
  }

  return (
    <div className="flex min-h-screen flex-col bg-neutral-50">
      <header className="flex flex-col gap-3 border-b border-neutral-200 bg-white px-6 py-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex min-w-0 items-center gap-4">
          <button
            type="button"
            onClick={() => navigate('/')}
            className="flex flex-shrink-0 items-center gap-1.5 text-sm font-medium text-neutral-600 hover:text-neutral-900"
          >
            <ArrowLeft size={16} />
            Back
          </button>
          <input
            value={title}
            disabled={!canEdit}
            onChange={(e) => setTitle(e.target.value)}
            onBlur={handleTitleBlur}
            className="min-w-0 flex-1 truncate rounded-md border border-transparent bg-transparent px-2 py-1 text-base font-semibold text-neutral-900 outline-none transition hover:border-neutral-200 focus:border-brand-300 focus:bg-white disabled:hover:border-transparent"
          />
        </div>

        <div className="flex items-center gap-3">
          <PresenceIndicator users={activeUsers} />
          {canEdit && <SaveStatusIndicator status={status} />}
          {doc.role === 'VIEWER' && (
            <span className="rounded-full border border-neutral-200 bg-neutral-100 px-2.5 py-1 text-xs font-medium text-neutral-500">
              View only
            </span>
          )}
          <button
            type="button"
            onClick={() => {
              setShowHistory(false);
              setShowComments((prev) => !prev);
            }}
            className="flex items-center gap-1.5 rounded-lg border border-neutral-200 bg-white px-3 py-1.5 text-sm font-medium text-neutral-700 transition hover:border-neutral-300 hover:bg-neutral-50"
          >
            <MessageSquare size={15} />
            Comments
          </button>
          <button
            type="button"
            onClick={() => {
              setShowComments(false);
              setShowHistory((prev) => !prev);
            }}
            className="flex items-center gap-1.5 rounded-lg border border-neutral-200 bg-white px-3 py-1.5 text-sm font-medium text-neutral-700 transition hover:border-neutral-300 hover:bg-neutral-50"
          >
            <History size={15} />
            History
          </button>
          <div className="relative">
            <button
              type="button"
              onClick={() => setShowExportMenu((prev) => !prev)}
              className="flex items-center gap-1.5 rounded-lg border border-neutral-200 bg-white px-3 py-1.5 text-sm font-medium text-neutral-700 transition hover:border-neutral-300 hover:bg-neutral-50"
            >
              <Download size={15} />
              Export
            </button>
            {showExportMenu && (
              <div className="absolute right-0 top-full z-10 mt-1 w-40 rounded-lg border border-neutral-200 bg-white py-1 shadow-lg">
                <a
                  href={`${API_BASE_URL}/api/documents/${doc.id}/export?format=pdf`}
                  onClick={() => setShowExportMenu(false)}
                  className="block px-3 py-2 text-sm text-neutral-700 hover:bg-neutral-50"
                >
                  Export as PDF
                </a>
                <a
                  href={`${API_BASE_URL}/api/documents/${doc.id}/export?format=markdown`}
                  onClick={() => setShowExportMenu(false)}
                  className="block px-3 py-2 text-sm text-neutral-700 hover:bg-neutral-50"
                >
                  Export as Markdown
                </a>
              </div>
            )}
          </div>
          {canManageSharing && (
            <button
              type="button"
              onClick={() => setShowShare(true)}
              className="flex items-center gap-1.5 rounded-lg border border-neutral-200 bg-white px-3 py-1.5 text-sm font-medium text-neutral-700 transition hover:border-neutral-300 hover:bg-neutral-50"
            >
              <Share2 size={15} />
              Share
            </button>
          )}
          {canManageSharing && (
            <button
              type="button"
              onClick={() => setShowDeleteConfirm(true)}
              title="Delete document"
              aria-label="Delete document"
              className="flex items-center gap-1.5 rounded-lg border border-neutral-200 bg-white px-2.5 py-1.5 text-sm font-medium text-neutral-500 transition hover:border-red-200 hover:bg-red-50 hover:text-red-600"
            >
              <Trash2 size={15} />
            </button>
          )}
        </div>
      </header>

      {errorMessage && (
        <div className="flex items-center gap-2 border-b border-amber-200 bg-amber-50 px-6 py-2 text-xs text-amber-800">
          <AlertTriangle size={13} />
          {errorMessage}
        </div>
      )}

      {canEdit && <EditorToolbar editor={editor} />}

      <main className="flex flex-1 justify-center px-4 py-8">
        <div className="w-full max-w-3xl rounded-xl border border-neutral-200 bg-white px-10 py-8 shadow-sm">
          <EditorContent editor={editor} className="docspace-prose" />
        </div>
      </main>

      {!canManageSharing && doc.role !== 'VIEWER' && (
        <div className="border-t border-neutral-200 bg-white px-6 py-2 text-xs text-neutral-400">
          <span className="inline-flex items-center gap-1">
            <Users size={12} /> Shared by {doc.owner.name}
          </span>
        </div>
      )}

      {showShare && <ShareModal documentId={doc.id} onClose={() => setShowShare(false)} />}

      {showComments && user && (
        <CommentsPanel
          documentId={doc.id}
          currentUserId={user.id}
          isOwner={doc.role === 'OWNER'}
          onClose={() => setShowComments(false)}
        />
      )}

      {showHistory && (
        <VersionHistoryPanel documentId={doc.id} onClose={() => setShowHistory(false)} />
      )}

      {showDeleteConfirm && (
        <ConfirmDialog
          title="Delete document?"
          description={`"${doc.title}" will be permanently deleted. This cannot be undone.`}
          isSubmitting={deleting}
          errorMessage={deleteError}
          onConfirm={handleConfirmDelete}
          onCancel={() => {
            setShowDeleteConfirm(false);
            setDeleteError(null);
          }}
        />
      )}
    </div>
  );
}
