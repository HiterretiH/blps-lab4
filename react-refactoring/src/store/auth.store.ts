import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  user: { username: string; role: string; email?: string } | null;
  token: string | null;
  isAuthenticated: boolean;
  developerId: number | null;
  login: (username: string, token: string, role: string, email?: string) => void;
  logout: () => void;
  setUser: (user: { username: string; role: string; email?: string }) => void;
  setDeveloperId: (id: number | null) => void;
  checkAuth: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      developerId: null,

      login: (username, token, role, email) => {
        localStorage.setItem('auth_token', token);
        localStorage.setItem('user', JSON.stringify({ username, role, email }));

        const developerMap: Record<string, number> = {
          '321321': 2,
          dev: 1,
          developer: 3,
          console: 4,
        };

        set({
          user: { username, role, email },
          token,
          isAuthenticated: true,
          developerId: developerMap[username] || null,
        });
      },

      logout: () => {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user');
        set({ user: null, token: null, isAuthenticated: false, developerId: null });
      },

      setUser: user => set({ user }),

      setDeveloperId: developerId => set({ developerId }),

      checkAuth: () => {
        const token = localStorage.getItem('auth_token');
        const userStr = localStorage.getItem('user');

        if (token && userStr) {
          try {
            const user = JSON.parse(userStr);
            const developerMap: Record<string, number> = {
              '321321': 2,
              dev: 1,
              developer: 3,
              console: 4,
            };

            set({
              user,
              token,
              isAuthenticated: true,
              developerId: developerMap[user.username] || null,
            });
          } catch {
            get().logout();
          }
        }
      },
    }),
    {
      name: 'auth-storage',
    }
  )
);
