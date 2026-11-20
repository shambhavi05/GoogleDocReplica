import type { User } from './user';

export type Permission = 'VIEWER' | 'EDITOR';

export interface Share {
  id: string;
  user: User;
  permission: Permission;
  createdAt: string;
}
