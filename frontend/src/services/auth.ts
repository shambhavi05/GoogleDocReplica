import { api, toApiError } from './api';
import type { User } from '../types/user';

export async function login(email: string): Promise<User> {
  try {
    const { data } = await api.post<User>('/auth/login', { email });
    return data;
  } catch (error) {
    throw toApiError(error);
  }
}

export async function logout(): Promise<void> {
  try {
    await api.post('/auth/logout');
  } catch (error) {
    throw toApiError(error);
  }
}

export async function fetchCurrentUser(): Promise<User | null> {
  try {
    const { data } = await api.get<User>('/auth/me');
    return data;
  } catch (error) {
    const apiError = toApiError(error);
    if (apiError.status === 401) {
      return null;
    }
    throw apiError;
  }
}
