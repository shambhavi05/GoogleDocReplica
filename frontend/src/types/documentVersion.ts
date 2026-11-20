import type { User } from './user';

export interface DocumentVersionSummary {
  id: string;
  title: string;
  version: number;
  savedBy: User;
  createdAt: string;
}

export interface DocumentVersionDetail extends DocumentVersionSummary {
  content: Record<string, unknown>;
}
