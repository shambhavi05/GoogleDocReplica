import type { LucideIcon } from 'lucide-react';

interface EmptyStateProps {
  icon: LucideIcon;
  title: string;
  description: string;
}

export default function EmptyState({ icon: Icon, title, description }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-neutral-200 bg-white px-6 py-10 text-center">
      <span className="mb-3 flex h-10 w-10 items-center justify-center rounded-full bg-neutral-100 text-neutral-400">
        <Icon size={20} />
      </span>
      <p className="text-sm font-medium text-neutral-700">{title}</p>
      <p className="mt-1 max-w-xs text-xs text-neutral-500">{description}</p>
    </div>
  );
}
