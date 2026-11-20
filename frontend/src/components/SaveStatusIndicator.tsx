import { CheckCircle2, AlertTriangle, Loader2 } from 'lucide-react';
import type { SaveStatus } from '../hooks/useAutosave';

export default function SaveStatusIndicator({ status }: { status: SaveStatus }) {
  switch (status) {
    case 'saving':
      return (
        <span className="flex items-center gap-1.5 text-xs text-neutral-500">
          <Loader2 size={13} className="animate-spin" />
          Saving…
        </span>
      );
    case 'saved':
      return (
        <span className="flex items-center gap-1.5 text-xs text-brand-600">
          <CheckCircle2 size={13} />
          Saved
        </span>
      );
    case 'error':
      return (
        <span className="flex items-center gap-1.5 text-xs text-red-600">
          <AlertTriangle size={13} />
          Save failed
        </span>
      );
    case 'conflict':
      return (
        <span className="flex items-center gap-1.5 text-xs text-amber-600">
          <AlertTriangle size={13} />
          Changed elsewhere
        </span>
      );
    default:
      return null;
  }
}
