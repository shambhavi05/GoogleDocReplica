import { useEffect, useState } from 'react';
import { X, UserPlus, Trash2 } from 'lucide-react';
import type { Permission, Share } from '../types/share';
import { createShare, listShares, revokeShare } from '../services/shares';
import { ApiError } from '../services/api';

interface ShareModalProps {
  documentId: string;
  onClose: () => void;
}

export default function ShareModal({ documentId, onClose }: ShareModalProps) {
  const [shares, setShares] = useState<Share[]>([]);
  const [loading, setLoading] = useState(true);
  const [email, setEmail] = useState('');
  const [permission, setPermission] = useState<Permission>('VIEWER');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    refresh();
  }, [documentId]);

  async function refresh() {
    setLoading(true);
    try {
      const data = await listShares(documentId);
      setShares(data);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not load collaborators');
    } finally {
      setLoading(false);
    }
  }

  async function handleShare(e: React.FormEvent) {
    e.preventDefault();
    if (!email.trim()) return;
    setSubmitting(true);
    setError(null);
    try {
      await createShare(documentId, email.trim(), permission);
      setEmail('');
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not share document');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleRevoke(shareId: string) {
    setError(null);
    try {
      await revokeShare(documentId, shareId);
      setShares((prev) => prev.filter((s) => s.id !== shareId));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not revoke access');
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-base font-semibold text-neutral-900">Share document</h2>
          <button
            type="button"
            onClick={onClose}
            className="rounded-md p-1 text-neutral-400 hover:bg-neutral-100 hover:text-neutral-600"
          >
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleShare} className="flex gap-2">
          <input
            type="email"
            required
            placeholder="person@docspace.test"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="flex-1 rounded-lg border border-neutral-200 px-3 py-2 text-sm outline-none focus:border-brand-400 focus:ring-2 focus:ring-brand-100"
          />
          <select
            value={permission}
            onChange={(e) => setPermission(e.target.value as Permission)}
            className="rounded-lg border border-neutral-200 px-2 py-2 text-sm outline-none focus:border-brand-400"
          >
            <option value="VIEWER">Viewer</option>
            <option value="EDITOR">Editor</option>
          </select>
          <button
            type="submit"
            disabled={submitting}
            className="flex items-center gap-1 rounded-lg bg-brand-600 px-3 py-2 text-sm font-medium text-white transition hover:bg-brand-700 disabled:opacity-60"
          >
            <UserPlus size={15} />
          </button>
        </form>

        {error && (
          <div className="mt-3 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
          </div>
        )}

        <div className="mt-5">
          <p className="mb-2 text-xs font-medium uppercase tracking-wide text-neutral-400">
            People with access
          </p>
          {loading ? (
            <p className="text-sm text-neutral-500">Loading…</p>
          ) : shares.length === 0 ? (
            <p className="text-sm text-neutral-500">Only you have access right now.</p>
          ) : (
            <ul className="flex flex-col gap-2">
              {shares.map((share) => (
                <li
                  key={share.id}
                  className="flex items-center justify-between rounded-lg border border-neutral-100 px-3 py-2"
                >
                  <div>
                    <p className="text-sm font-medium text-neutral-800">{share.user.name}</p>
                    <p className="text-xs text-neutral-500">{share.user.email}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <span
                      className={`rounded-full border px-2 py-0.5 text-xs font-medium ${
                        share.permission === 'EDITOR'
                          ? 'border-blue-200 bg-blue-50 text-blue-700'
                          : 'border-neutral-200 bg-neutral-100 text-neutral-600'
                      }`}
                    >
                      {share.permission === 'EDITOR' ? 'Editor' : 'Viewer'}
                    </span>
                    <button
                      type="button"
                      onClick={() => handleRevoke(share.id)}
                      title="Revoke access"
                      className="rounded-md p-1 text-neutral-400 hover:bg-red-50 hover:text-red-600"
                    >
                      <Trash2 size={14} />
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
