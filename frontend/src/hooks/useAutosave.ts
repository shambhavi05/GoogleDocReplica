import { useCallback, useEffect, useRef, useState } from 'react';
import { ApiError } from '../services/api';

export type SaveStatus = 'idle' | 'saving' | 'saved' | 'error' | 'conflict';

interface UseAutosaveOptions<T> {
  delayMs?: number;
  onSave: (payload: T) => Promise<{ version: number }>;
}

interface UseAutosaveResult<T> {
  status: SaveStatus;
  errorMessage: string | null;
  schedule: (payload: T) => void;
  saveNow: (payload: T) => Promise<void>;
}

/**
 * Debounces saves and tracks status, but every save in flight is diffed against the
 * latest schedule() call when it resolves: an older in-flight request finishing after
 * a newer edit was scheduled must not downgrade "saving" back to "saved" for content
 * that's already stale, so we track a monotonically increasing request id.
 */
export function useAutosave<T>({ delayMs = 1000, onSave }: UseAutosaveOptions<T>): UseAutosaveResult<T> {
  const [status, setStatus] = useState<SaveStatus>('idle');
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const latestRequestId = useRef(0);
  const onSaveRef = useRef(onSave);
  onSaveRef.current = onSave;

  const saveNow = useCallback(async (payload: T) => {
    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
    const requestId = ++latestRequestId.current;
    setStatus('saving');
    setErrorMessage(null);
    try {
      await onSaveRef.current(payload);
      if (latestRequestId.current === requestId) {
        setStatus('saved');
      }
    } catch (err) {
      if (latestRequestId.current === requestId) {
        if (err instanceof ApiError && err.status === 409) {
          setStatus('conflict');
          setErrorMessage('This document was changed elsewhere. Reload to see the latest version.');
        } else {
          setStatus('error');
          setErrorMessage(err instanceof Error ? err.message : 'Save failed');
        }
      }
      throw err;
    }
  }, []);

  const schedule = useCallback(
    (payload: T) => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
      timerRef.current = setTimeout(() => {
        saveNow(payload).catch(() => {
          // status already reflects the failure; nothing further to do here
        });
      }, delayMs);
    },
    [delayMs, saveNow]
  );

  useEffect(() => {
    return () => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
    };
  }, []);

  return { status, errorMessage, schedule, saveNow };
}
