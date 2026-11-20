import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import LoginPage from './LoginPage';
import { AuthProvider } from '../hooks/useAuth';
import * as authService from '../services/auth';

vi.mock('../services/auth');

describe('LoginPage', () => {
  beforeEach(() => {
    vi.mocked(authService.fetchCurrentUser).mockResolvedValue(null);
    // useAuth's dev-mode auto-login effect calls login() once on mount with a
    // fixed demo email; stub that call separately so it doesn't consume the
    // mockRejectedValueOnce/mockResolvedValue queued below for the real button click.
    vi.mocked(authService.login).mockResolvedValue({
      id: '0',
      name: 'Auto Login',
      email: 'alice@docspace.test',
    });
  });

  it('logs in the selected demo user and shows an error if login fails', async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </MemoryRouter>
    );

    // Let the mount-time auto-login effect resolve against the beforeEach default
    // before queuing the rejection for the deliberate button click below.
    const shambhaviButton = await screen.findByRole('button', { name: /^Shambhavi/ });
    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledTimes(1);
    });

    vi.mocked(authService.login).mockRejectedValueOnce(
      new (await import('../services/api')).ApiError({
        timestamp: new Date().toISOString(),
        status: 400,
        error: 'Bad Request',
        message: 'No demo account found',
        path: '/api/auth/login',
      })
    );

    await user.click(shambhaviButton);

    await waitFor(() => {
      expect(screen.getByText('No demo account found')).toBeInTheDocument();
    });

    expect(authService.login).toHaveBeenCalledWith('shambhavi@ajaia.com');
  });

  it('calls login with each demo user email', async () => {
    vi.mocked(authService.login).mockResolvedValue({
      id: '1',
      name: 'Bob',
      email: 'bob@agij.com',
    });

    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </MemoryRouter>
    );

    const bobButton = await screen.findByRole('button', { name: /Bob/i });
    await user.click(bobButton);

    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledWith('bob@agij.com');
    });
  });
});
