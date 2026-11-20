import { api, toApiError } from './api';
import type { DocumentDetail } from '../types/document';

export const MAX_IMPORT_SIZE_BYTES = 2 * 1024 * 1024;
export const ALLOWED_IMPORT_EXTENSIONS = ['.txt', '.md', '.docx'];

export async function importFile(file: File): Promise<DocumentDetail> {
  try {
    const formData = new FormData();
    formData.append('file', file);
    const { data } = await api.post<DocumentDetail>('/files/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return data;
  } catch (error) {
    throw toApiError(error);
  }
}
