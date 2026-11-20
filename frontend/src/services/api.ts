import axios, { AxiosError } from 'axios';

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8081';

export const api = axios.create({
  baseURL: `${API_BASE_URL}/api`,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

export interface ApiErrorBody {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  details?: unknown;
}

export class ApiError extends Error {
  status: number;
  details?: unknown;

  constructor(body: ApiErrorBody) {
    super(body.message);
    this.status = body.status;
    this.details = body.details;
  }
}

export function toApiError(error: unknown): ApiError {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<ApiErrorBody>;
    if (axiosError.response?.data?.message) {
      return new ApiError(axiosError.response.data);
    }
    if (axiosError.response) {
      return new ApiError({
        timestamp: new Date().toISOString(),
        status: axiosError.response.status,
        error: axiosError.response.statusText,
        message: axiosError.response.statusText || 'Request failed',
        path: axiosError.config?.url ?? '',
      });
    }
    return new ApiError({
      timestamp: new Date().toISOString(),
      status: 0,
      error: 'Network Error',
      message: 'Could not reach the server. Check your connection and try again.',
      path: axiosError.config?.url ?? '',
    });
  }
  return new ApiError({
    timestamp: new Date().toISOString(),
    status: 500,
    error: 'Unknown Error',
    message: 'An unexpected error occurred',
    path: '',
  });
}
