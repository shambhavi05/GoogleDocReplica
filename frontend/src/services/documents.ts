import { api, toApiError } from './api';
import type { DocumentDetail, DocumentListResponse } from '../types/document';

export async function listDocuments(): Promise<DocumentListResponse> {
  try {
    const { data } = await api.get<DocumentListResponse>('/documents');
    return data;
  } catch (error) {
    throw toApiError(error);
  }
}

export async function getDocument(id: string): Promise<DocumentDetail> {
  try {
    const { data } = await api.get<DocumentDetail>(`/documents/${id}`);
    return data;
  } catch (error) {
    throw toApiError(error);
  }
}

export async function createDocument(title: string): Promise<DocumentDetail> {
  try {
    const { data } = await api.post<DocumentDetail>('/documents', { title });
    return data;
  } catch (error) {
    throw toApiError(error);
  }
}

export interface UpdateDocumentPayload {
  title?: string;
  content?: Record<string, unknown>;
  version: number;
}

export async function updateDocument(
  id: string,
  payload: UpdateDocumentPayload
): Promise<DocumentDetail> {
  try {
    const { data } = await api.put<DocumentDetail>(`/documents/${id}`, payload);
    return data;
  } catch (error) {
    throw toApiError(error);
  }
}

export async function deleteDocument(id: string): Promise<void> {
  try {
    await api.delete(`/documents/${id}`);
  } catch (error) {
    throw toApiError(error);
  }
}
