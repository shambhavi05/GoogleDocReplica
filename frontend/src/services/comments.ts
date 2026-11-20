import { api, toApiError } from './api';
import type { Comment } from '../types/comment';

export async function listComments(documentId: string): Promise<Comment[]> {
  try {
    const { data } = await api.get<Comment[]>(`/documents/${documentId}/comments`);
    return data;
  } catch (error) {
    throw toApiError(error);
  }
}

export async function createComment(documentId: string, body: string): Promise<Comment> {
  try {
    const { data } = await api.post<Comment>(`/documents/${documentId}/comments`, { body });
    return data;
  } catch (error) {
    throw toApiError(error);
  }
}

export async function resolveComment(documentId: string, commentId: string): Promise<Comment> {
  try {
    const { data } = await api.patch<Comment>(`/documents/${documentId}/comments/${commentId}/resolve`);
    return data;
  } catch (error) {
    throw toApiError(error);
  }
}

export async function deleteComment(documentId: string, commentId: string): Promise<void> {
  try {
    await api.delete(`/documents/${documentId}/comments/${commentId}`);
  } catch (error) {
    throw toApiError(error);
  }
}
