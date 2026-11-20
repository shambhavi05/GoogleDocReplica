import type { User } from './user';

export interface Comment {
  id: string;
  author: User;
  body: string;
  resolved: boolean;
  createdAt: string;
}
