import type { User } from './user';

export type Role = 'OWNER' | 'EDITOR' | 'VIEWER';

export interface DocumentSummary {
  id: string;
  title: string;
  owner: User;
  role: Role;
  createdAt: string;
  updatedAt: string;
}

export interface DocumentDetail {
  id: string;
  title: string;
  content: Record<string, unknown>;
  owner: User;
  version: number;
  role: Role;
  createdAt: string;
  updatedAt: string;
}

export interface DocumentListResponse {
  owned: DocumentSummary[];
  shared: DocumentSummary[];
}
