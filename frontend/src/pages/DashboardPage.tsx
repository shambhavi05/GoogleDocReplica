import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FilePlus2, FolderOpen, Search, Share2, UploadCloud } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { useDocuments } from '../hooks/useDocuments';
import { createDocument, deleteDocument } from '../services/documents';
import { ApiError } from '../services/api';
import type { DocumentSummary } from '../types/document';
import DocumentCard from '../components/DocumentCard';
import EmptyState from '../components/EmptyState';
import FileUploadModal from '../components/FileUploadModal';
import ConfirmDialog from '../components/ConfirmDialog';

export default function DashboardPage() {
  const { user } = useAuth();
  const { data, loading, error, refresh } = useDocuments();
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const [creating, setCreating] = useState(false);
  const [showUpload, setShowUpload] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);
  const [pendingDelete, setPendingDelete] = useState<DocumentSummary | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const filteredOwned = useMemo(
    () => filterBySearch(data?.owned ?? [], search),
    [data?.owned, search]
  );
  const filteredShared = useMemo(
    () => filterBySearch(data?.shared ?? [], search),
    [data?.shared, search]
  );

  const recent = useMemo(() => {
    const all = [...(data?.owned ?? []), ...(data?.shared ?? [])];
    return [...all]
      .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
      .slice(0, 4);
  }, [data]);

  async function handleCreate() {
    setCreating(true);
    setCreateError(null);
    try {
      const created = await createDocument('Untitled Document');
      navigate(`/documents/${created.id}`);
    } catch (err) {
      setCreateError(err instanceof ApiError ? err.message : 'Could not create document');
    } finally {
      setCreating(false);
    }
  }

  async function handleConfirmDelete() {
    if (!pendingDelete) return;
    setDeleting(true);
    setDeleteError(null);
    try {
      await deleteDocument(pendingDelete.id);
      setPendingDelete(null);
      await refresh();
    } catch (err) {
      setDeleteError(err instanceof ApiError ? err.message : 'Could not delete document');
    } finally {
      setDeleting(false);
    }
  }

  return (
    <div className="flex flex-col gap-8">
      <section className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-neutral-900">
            Welcome back, {user?.name}
          </h1>
          <p className="mt-1 text-sm text-neutral-500">
            Here's what's happening across your documents.
          </p>
        </div>
        <div className="flex gap-2">
          <button
            type="button"
            onClick={() => setShowUpload(true)}
            className="flex items-center gap-1.5 rounded-lg border border-neutral-200 bg-white px-3.5 py-2 text-sm font-medium text-neutral-700 transition hover:border-neutral-300 hover:bg-neutral-50"
          >
            <UploadCloud size={16} />
            Upload File
          </button>
          <button
            type="button"
            disabled={creating}
            onClick={handleCreate}
            className="flex items-center gap-1.5 rounded-lg bg-brand-600 px-3.5 py-2 text-sm font-medium text-white transition hover:bg-brand-700 disabled:opacity-60"
          >
            <FilePlus2 size={16} />
            {creating ? 'Creating…' : 'New Document'}
          </button>
        </div>
      </section>

      {createError && (
        <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {createError}
        </div>
      )}

      <section className="relative">
        <Search
          size={16}
          className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-neutral-400"
        />
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search documents…"
          className="w-full max-w-sm rounded-lg border border-neutral-200 bg-white py-2 pl-9 pr-3 text-sm text-neutral-700 outline-none transition focus:border-brand-400 focus:ring-2 focus:ring-brand-100"
        />
      </section>

      {error && (
        <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}{' '}
          <button type="button" onClick={refresh} className="font-medium underline">
            Retry
          </button>
        </div>
      )}

      {loading ? (
        <p className="text-sm text-neutral-500">Loading your documents…</p>
      ) : (
        <>
          {!search && recent.length > 0 && (
            <section>
              <h2 className="mb-3 text-sm font-semibold text-neutral-700">Recent documents</h2>
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
                {recent.map((doc) => (
                  <DocumentCard key={doc.id} document={doc} showOwner onDelete={setPendingDelete} />
                ))}
              </div>
            </section>
          )}

          <section>
            <h2 className="mb-3 flex items-center gap-2 text-sm font-semibold text-neutral-700">
              <FolderOpen size={15} /> Owned Documents
            </h2>
            {filteredOwned.length === 0 ? (
              <EmptyState
                icon={FolderOpen}
                title={search ? 'No owned documents match your search' : 'No documents yet'}
                description={
                  search
                    ? 'Try a different search term.'
                    : 'Create your first document or import a file to get started.'
                }
              />
            ) : (
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
                {filteredOwned.map((doc) => (
                  <DocumentCard key={doc.id} document={doc} onDelete={setPendingDelete} />
                ))}
              </div>
            )}
          </section>

          <section>
            <h2 className="mb-3 flex items-center gap-2 text-sm font-semibold text-neutral-700">
              <Share2 size={15} /> Shared With Me
            </h2>
            {filteredShared.length === 0 ? (
              <EmptyState
                icon={Share2}
                title={search ? 'No shared documents match your search' : 'Nothing shared with you yet'}
                description="Documents other people share with you will show up here."
              />
            ) : (
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
                {filteredShared.map((doc) => (
                  <DocumentCard key={doc.id} document={doc} showOwner />
                ))}
              </div>
            )}
          </section>
        </>
      )}

      {showUpload && (
        <FileUploadModal
          onClose={() => setShowUpload(false)}
          onImported={(id) => navigate(`/documents/${id}`)}
        />
      )}

      {pendingDelete && (
        <ConfirmDialog
          title="Delete document?"
          description={`"${pendingDelete.title}" will be permanently deleted. This cannot be undone.`}
          isSubmitting={deleting}
          errorMessage={deleteError}
          onConfirm={handleConfirmDelete}
          onCancel={() => {
            setPendingDelete(null);
            setDeleteError(null);
          }}
        />
      )}
    </div>
  );
}

function filterBySearch<T extends { title: string }>(items: T[], search: string): T[] {
  if (!search.trim()) return items;
  const lower = search.toLowerCase();
  return items.filter((item) => item.title.toLowerCase().includes(lower));
}
