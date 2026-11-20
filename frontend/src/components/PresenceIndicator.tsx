import type { User } from '../types/user';

function initialsOf(name: string): string {
  return name.trim().charAt(0).toUpperCase();
}

export default function PresenceIndicator({ users }: { users: User[] }) {
  if (users.length === 0) return null;

  return (
    <div className="flex items-center gap-1.5" title={users.map((u) => u.name).join(', ') + ' also viewing'}>
      <div className="flex -space-x-2">
        {users.slice(0, 3).map((user) => (
          <span
            key={user.id}
            className="flex h-7 w-7 items-center justify-center rounded-full border-2 border-white bg-brand-100 text-xs font-semibold text-brand-700"
          >
            {initialsOf(user.name)}
          </span>
        ))}
      </div>
      <span className="text-xs text-neutral-500">
        {users.length === 1 ? `${users[0].name} is also here` : `${users.length} others here`}
      </span>
    </div>
  );
}
