import { act, renderHook, waitFor } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { useAutosave } from './useAutosave';
import { ApiError } from '../services/api';

describe('useAutosave', () => {
  it('debounces schedule() calls into a single save after the delay', async () => {
    vi.useFakeTimers();
    const onSave = vi.fn().mockResolvedValue({ version: 1 });

    const { result } = renderHook(() =>
      useAutosave<{ content: string }>({ delayMs: 1000, onSave })
    );

    act(() => {
      result.current.schedule({ content: 'a' });
      result.current.schedule({ content: 'ab' });
      result.current.schedule({ content: 'abc' });
    });

    expect(onSave).not.toHaveBeenCalled();

    await act(async () => {
      vi.advanceTimersByTime(1000);
    });

    expect(onSave).toHaveBeenCalledTimes(1);
    expect(onSave).toHaveBeenCalledWith({ content: 'abc' });

    vi.useRealTimers();
  });

  it('reports a conflict status on a 409 ApiError', async () => {
    const conflictError = new ApiError({
      timestamp: new Date().toISOString(),
      status: 409,
      error: 'Conflict',
      message: 'Document was modified elsewhere',
      path: '/api/documents/1',
    });
    const onSave = vi.fn().mockRejectedValue(conflictError);

    const { result } = renderHook(() => useAutosave<{ content: string }>({ onSave }));

    await act(async () => {
      await result.current.saveNow({ content: 'x' }).catch(() => {});
    });

    await waitFor(() => {
      expect(result.current.status).toBe('conflict');
    });
  });

  it('reports an error status when the save fails for a non-conflict reason', async () => {
    const onSave = vi.fn().mockRejectedValue(new Error('network down'));

    const { result } = renderHook(() => useAutosave<{ content: string }>({ onSave }));

    await act(async () => {
      await result.current.saveNow({ content: 'x' }).catch(() => {});
    });

    await waitFor(() => {
      expect(result.current.status).toBe('error');
    });
  });
});
