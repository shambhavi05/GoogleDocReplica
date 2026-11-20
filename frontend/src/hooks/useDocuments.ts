import { useCallback, useEffect, useState } from 'react';
import type { DocumentListResponse } from '../types/document';
import { listDocuments } from '../services/documents';
import { toApiError } from '../services/api';

interface UseDocumentsResult {
  data: DocumentListResponse | null;
  loading: boolean;
  error: string | null;
  refresh: () => Promise<void>;
}

export function useDocuments(): UseDocumentsResult {
  const [data, setData] = useState<DocumentListResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await listDocuments();
      setData(result);
    } catch (err) {
      setError(toApiError(err).message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  return { data, loading, error, refresh };
}
