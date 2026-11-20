import { useNavigate } from 'react-router-dom';
import { FileText, Pencil, Eye, Trash2 } from 'lucide-react';
import type { DocumentSummary } from '../types/document';
import { formatRelativeTime } from '../utils/formatDate';

interface DocumentCardProps {
  document: DocumentSummary;
  showOwner?: boolean;
  onDelete?: (document: DocumentSummary) => void;
}

const roleBadge: Record<DocumentSummary['role'], { label: string; className: string }> = {
  OWNER: { label: 'Owner', className: 'bg-brand-50 text-brand-700 border-brand-200' },
  EDITOR: { label: 'Editor', className: 'bg-blue-50 text-blue-700 border-blue-200' },
  VIEWER: { label: 'Viewer', className: 'bg-neutral-100 text-neutral-600 border-neutral-200' },
};

export default function DocumentCard({ document, showOwner = false, onDelete }: DocumentCardProps) {
  const navigate = useNavigate();
  const badge = roleBadge[document.role];
  const canDelete = document.role === 'OWNER' && onDelete;

  return (
    <div
      role="button"
      tabIndex={0}
      onClick={() => navigate(`/documents/${document.id}`)}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          navigate(`/documents/${document.id}`);
        }
      }}
      className="group relative flex w-full cursor-pointer flex-col gap-3 rounded-xl border border-neutral-200 bg-white p-4 text-left transition hover:border-brand-300 hover:shadow-sm"
    >
      <div className="flex items-start justify-between gap-2">
        <span className="flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg bg-neutral-100 text-neutral-500">
          <FileText size={16} />
        </span>
        <span className="flex items-center gap-1">
          <span
            className={`flex items-center gap-1 rounded-full border px-2 py-0.5 text-xs font-medium ${badge.className}`}
          >
            {document.role === 'VIEWER' ? <Eye size={11} /> : <Pencil size={11} />}
            {badge.label}
          </span>
          {canDelete && (
            <button
              type="button"
              title="Delete document"
              aria-label="Delete document"
              onClick={(e) => {
                e.stopPropagation();
                onDelete(document);
              }}
              className="rounded-md p-1 text-neutral-300 opacity-0 transition hover:bg-red-50 hover:text-red-600 group-hover:opacity-100"
            >
              <Trash2 size={14} />
            </button>
          )}
        </span>
      </div>

      <div>
        <p className="truncate text-sm font-medium text-neutral-900">{document.title}</p>
        <p className="mt-0.5 text-xs text-neutral-500">
          Updated {formatRelativeTime(document.updatedAt)}
          {showOwner && document.role !== 'OWNER' && <> · by {document.owner.name}</>}
        </p>
      </div>
    </div>
  );
}
