import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authService } from '../services/auth.service';
import { setSentryUser, clearSentryUser } from '../lib/sentry';

interface AuthState {
  user: {
    username: string;
    role: string;
    email?: string;
    userId?: number;
  } | null;
  token: string | null;
  isAuthenticated: boolean;
  developerId: number | null;
  login: (
    username: string,
    token: string,
    role: string,
    email?: string,
    userId?: number
  ) => Promise<void>;
  logout: () => void;
  setUser: (user: { username: string; role: string; email?: string; userId?: number }) => void;
  setDeveloperId: (id: number | null) => void;
  checkAuth: () => Promise<void>;
  initialize: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      developerId: null,

      login: async (username, token, role, email, userId) => {
        set({
          user: { username, role, email, userId },
          token,
          isAuthenticated: true,
          developerId: null,
        });

        setSentryUser({
          id: userId?.toString(),
          username,
          email,
          role,
        });

        localStorage.setItem('auth_token', token);
        localStorage.setItem(
          'user',
          JSON.stringify({
            username,
            role,
            email,
            userId,
          })
        );

        await get().initialize();
      },

      logout: () => {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user');
        localStorage.removeItem('developer');

        clearSentryUser();

        set({
          user: null,
          token: null,
          isAuthenticated: false,
          developerId: null,
        });
      },

      setUser: user => set({ user }),

      setDeveloperId: developerId => set({ developerId }),

      checkAuth: async () => {
        const token = localStorage.getItem('auth_token');
        const userStr = localStorage.getItem('user');

        if (token && userStr) {
          try {
            const user = JSON.parse(userStr);
            set({
              user,
              token,
              isAuthenticated: true,
              developerId: null,
            });

            await get().initialize();
          } catch {
            get().logout();
          }
        } else {
          get().logout();
        }
      },

      initialize: async () => {
        const state = get();
        if (!state.isAuthenticated || !state.user) return;

        try {
          const developerStr = localStorage.getItem('developer');
          if (developerStr) {
            const developer = JSON.parse(developerStr);
            set({ developerId: developer.id });
            return;
          }

          const developerMap: Record<string, number> = {
            '321321': 2,
            dev: 1,
            developer: 3,
            console: 4,
            '1231233': 5,
          };

          const mappedId = developerMap[state.user.username];
          if (mappedId) {
            set({ developerId: mappedId });
            return;
          }

          if (state.user.role === 'DEVELOPER') {
            const developer = await authService.getCurrentDeveloper();
            if (developer) {
              set({ developerId: developer.id });
            }
          }
        } catch (error) {
          console.error('Error initializing auth store:', error);
        }
      },
    }),
    {
      name: 'auth-storage',
    }
  )
);
