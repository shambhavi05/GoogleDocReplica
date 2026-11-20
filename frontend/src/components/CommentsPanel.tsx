import { useEffect, useState } from 'react';
import { X, Send, Check, Trash2, MessageSquare } from 'lucide-react';
import type { Comment } from '../types/comment';
import { createComment, deleteComment, listComments, resolveComment } from '../services/comments';
import { ApiError } from '../services/api';

interface CommentsPanelProps {
  documentId: string;
  currentUserId: string;
  isOwner: boolean;
  onClose: () => void;
}

export default function CommentsPanel({ documentId, currentUserId, isOwner, onClose }: CommentsPanelProps) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [loading, setLoading] = useState(true);
  const [body, setBody] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [documentId]);

  async function refresh() {
    setLoading(true);
    try {
      setComments(await listComments(documentId));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not load comments');
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!body.trim()) return;
    setSubmitting(true);
    setError(null);
    try {
      await createComment(documentId, body.trim());
      setBody('');
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not add comment');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleResolve(commentId: string) {
    setError(null);
    try {
      await resolveComment(documentId, commentId);
      await refresh();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not resolve comment');
    }
  }

  async function handleDelete(commentId: string) {
    setError(null);
    try {
      await deleteComment(documentId, commentId);
      setComments((prev) => prev.filter((c) => c.id !== commentId));
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not delete comment');
    }
  }

  return (
    <div className="fixed inset-y-0 right-0 z-40 flex w-full max-w-sm flex-col border-l border-neutral-200 bg-white shadow-xl">
      <div className="flex items-center justify-between border-b border-neutral-200 px-4 py-3">
        <h2 className="flex items-center gap-2 text-sm font-semibold text-neutral-900">
          <MessageSquare size={16} /> Comments
        </h2>
        <button
          type="button"
          onClick={onClose}
          className="rounded-md p-1 text-neutral-400 hover:bg-neutral-100 hover:text-neutral-600"
        >
          <X size={18} />
        </button>
      </div>

      <div className="flex-1 overflow-y-auto px-4 py-3">
        {loading ? (
          <p className="text-sm text-neutral-500">Loading comments…</p>
        ) : comments.length === 0 ? (
          <p className="text-sm text-neutral-500">No comments yet. Start the discussion below.</p>
        ) : (
          <ul className="flex flex-col gap-3">
            {comments.map((comment) => {
              const canManage = comment.author.id === currentUserId || isOwner;
              return (
                <li
                  key={comment.id}
                  className={`rounded-lg border px-3 py-2 ${
                    comment.resolved ? 'border-neutral-100 bg-neutral-50' : 'border-neutral-200 bg-white'
                  }`}
                >
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <p className="text-xs font-medium text-neutral-700">{comment.author.name}</p>
                      <p className={`mt-0.5 text-sm ${comment.resolved ? 'text-neutral-400 line-through' : 'text-neutral-800'}`}>
                        {comment.body}
                      </p>
                    </div>
                    {canManage && (
                      <div className="flex flex-shrink-0 items-center gap-1">
                        {!comment.resolved && (
                          <button
                            type="button"
                            title="Mark resolved"
                            onClick={() => handleResolve(comment.id)}
                            className="rounded-md p-1 text-neutral-400 hover:bg-brand-50 hover:text-brand-600"
                          >
                            <Check size={14} />
                          </button>
                        )}
                        <button
                          type="button"
                          title="Delete comment"
                          onClick={() => handleDelete(comment.id)}
                          className="rounded-md p-1 text-neutral-400 hover:bg-red-50 hover:text-red-600"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    )}
                  </div>
                </li>
              );
            })}
          </ul>
        )}
      </div>

      {error && (
        <div className="mx-4 mb-2 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="flex items-center gap-2 border-t border-neutral-200 px-4 py-3">
        <input
          value={body}
          onChange={(e) => setBody(e.target.value)}
          placeholder="Add a comment…"
          className="flex-1 rounded-lg border border-neutral-200 px-3 py-2 text-sm outline-none focus:border-brand-400 focus:ring-2 focus:ring-brand-100"
        />
        <button
          type="submit"
          disabled={submitting || !body.trim()}
          className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg bg-brand-600 text-white transition hover:bg-brand-700 disabled:opacity-60"
        >
          <Send size={15} />
        </button>
      </form>
    </div>
  );
}
