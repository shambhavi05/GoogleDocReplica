import { api, toApiError } from './api';
import type { Permission, Share } from '../types/share';

export async function listShares(documentId: string): Promise<Share[]> {
  try {
    const { data } = await api.get<Share[]>(`/documents/${documentId}/shares`);
    return data;
  } catch (error) {
    throw toApiError(error);
  }
}

export async function createShare(
  documentId: string,
  email: string,
  permission: Permission
): Promise<Share> {
  try {
    const { data } = await api.post<Share>(`/documents/${documentId}/shares`, {
      email,
      permission,
    });
    return data;
  } catch (error) {
    throw toApiError(error);
  }
}

export async function revokeShare(documentId: string, shareId: string): Promise<void> {
  try {
    await api.delete(`/documents/${documentId}/shares/${shareId}`);
  } catch (error) {
    throw toApiError(error);
  }
}
