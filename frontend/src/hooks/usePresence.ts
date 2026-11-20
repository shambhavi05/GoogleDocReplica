import { useEffect, useState } from 'react';
import type { User } from '../types/user';
import { listActiveUsers, sendHeartbeat } from '../services/presence';

const HEARTBEAT_INTERVAL_MS = 5000;

/**
 * Polling-based presence: sends a heartbeat and refreshes the active-user list every
 * few seconds. Not WebSocket push and no live cursors — just "who else has this open."
 */
export function usePresence(documentId: string | undefined): User[] {
  const [activeUsers, setActiveUsers] = useState<User[]>([]);

  useEffect(() => {
    if (!documentId) return;
    let cancelled = false;

    async function tick() {
      try {
        await sendHeartbeat(documentId!);
        const users = await listActiveUsers(documentId!);
        if (!cancelled) setActiveUsers(users);
      } catch {
        // presence is best-effort; silently skip a failed tick
      }
    }

    tick();
    const interval = setInterval(tick, HEARTBEAT_INTERVAL_MS);

    return () => {
      cancelled = true;
      clearInterval(interval);
    };
  }, [documentId]);

  return activeUsers;
}
