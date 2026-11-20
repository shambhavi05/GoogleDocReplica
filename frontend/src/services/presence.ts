import { api, toApiError } from './api';
import type { User } from '../types/user';

export async function sendHeartbeat(documentId: string): Promise<void> {
  try {
    await api.post(`/documents/${documentId}/presence/heartbeat`);
  } catch (error) {
    throw toApiError(error);
  }
}

export async function listActiveUsers(documentId: string): Promise<User[]> {
  try {
    const { data } = await api.get<User[]>(`/documents/${documentId}/presence`);
    return data;
  } catch (error) {
    throw toApiError(error);
  }
}
