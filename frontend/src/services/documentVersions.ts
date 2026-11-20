import { api, toApiError } from './api';
import type { DocumentVersionDetail, DocumentVersionSummary } from '../types/documentVersion';

export async function listDocumentVersions(documentId: string): Promise<DocumentVersionSummary[]> {
  try {
    const { data } = await api.get<DocumentVersionSummary[]>(`/documents/${documentId}/versions`);
    return data;
  } catch (error) {
    throw toApiError(error);
  }
}

export async function getDocumentVersion(
  documentId: string,
  versionId: string
): Promise<DocumentVersionDetail> {
  try {
    const { data } = await api.get<DocumentVersionDetail>(
      `/documents/${documentId}/versions/${versionId}`
    );
    return data;
  } catch (error) {
    throw toApiError(error);
  }
}
