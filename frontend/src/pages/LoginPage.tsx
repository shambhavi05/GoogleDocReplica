import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FileText, LogIn } from 'lucide-react';
import { useAuth } from '../hooks/useAuth';
import { DEMO_USERS } from '../utils/demoUsers';
import { ApiError } from '../services/api';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [pendingEmail, setPendingEmail] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function handleLogin(email: string) {
    setPendingEmail(email);
    setError(null);
    try {
      await login(email);
      navigate('/', { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not sign in. Please try again.');
    } finally {
      setPendingEmail(null);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-neutral-50 px-4">
      <div className="w-full max-w-md rounded-2xl border border-neutral-200 bg-white p-8 shadow-sm">
        <div className="mb-6 flex flex-col items-center text-center">
          <span className="mb-3 flex h-11 w-11 items-center justify-center rounded-xl bg-brand-600 text-white">
            <FileText size={22} />
          </span>
          <h1 className="text-xl font-semibold text-neutral-900">Welcome to DocSpace</h1>
          <p className="mt-1 text-sm text-neutral-500">
            Pick a demo account to sign in. No password needed.
          </p>
        </div>

        {error && (
          <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
          </div>
        )}

        <div className="flex flex-col gap-2">
          {DEMO_USERS.map((demoUser) => (
            <button
              key={demoUser.email}
              type="button"
              disabled={pendingEmail !== null}
              onClick={() => handleLogin(demoUser.email)}
              className="group flex items-center justify-between rounded-xl border border-neutral-200 px-4 py-3 text-left transition hover:border-brand-400 hover:bg-brand-50 disabled:cursor-not-allowed disabled:opacity-60"
            >
              <span>
                <span className="block text-sm font-medium text-neutral-900">{demoUser.name}</span>
                <span className="block text-xs text-neutral-500">{demoUser.description}</span>
              </span>
              <LogIn
                size={16}
                className="text-neutral-400 transition group-hover:text-brand-600"
              />
            </button>
          ))}
        </div>

        <p className="mt-6 text-center text-xs text-neutral-400">
          Switch accounts anytime from the dashboard header to see how sharing and permissions
          look from each role.
        </p>
      </div>
    </div>
  );
}
