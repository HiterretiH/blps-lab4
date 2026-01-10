import { api } from './api';
import { LoginCredentials, RegisterCredentials, Token, Developer } from '../types';
import { useAuthStore } from '../store/auth.store';

export const authService = {
  async login(credentials: LoginCredentials): Promise<Token> {
    const response = await api.post<Token>('/auth/login', credentials);
    return response.data;
  },

  async register(credentials: RegisterCredentials): Promise<Token> {
    const response = await api.post<Token>('/auth/register', credentials);
    return response.data;
  },

  async registerDeveloper(credentials: RegisterCredentials): Promise<Token> {
    const response = await api.post<Token>('/auth/developer/register', credentials);
    return response.data;
  },

  async connectGoogle(): Promise<{ authUrl: string; state: string; message: string }> {
    const response = await api.post('/auth/google/connect');
    return response.data;
  },

  logout(): void {
    useAuthStore.getState().logout();
  },

  getCurrentUser() {
    const state = useAuthStore.getState();
    if (state.user) {
      return state.user;
    }

    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  async getCurrentDeveloper(): Promise<Developer | null> {
    try {
      const state = useAuthStore.getState();
      if (!state.user?.userId) return null;

      const response = await api.get<Developer>(`/developers/by-user/${state.user.userId}`, {
        headers: {
          Authorization: `Bearer ${state.token}`,
        },
      });

      localStorage.setItem('developer', JSON.stringify(response.data));

      useAuthStore.getState().setDeveloperId(response.data.id);

      return response.data;
    } catch (error) {
      console.error('Error fetching developer:', error);
      return null;
    }
  },

  getDeveloperIdSync(): number | null {
    try {
      const developerStr = localStorage.getItem('developer');
      if (developerStr) {
        const developer = JSON.parse(developerStr);
        return developer.id;
      }

      const state = useAuthStore.getState();
      if (!state.user?.username) return null;

      const developerMap: Record<string, number> = {
        '321321': 2,
        dev: 1,
        developer: 3,
        console: 4,
        '1231233': 5,
      };

      return developerMap[state.user.username] || null;
    } catch {
      return null;
    }
  },

  async getDeveloperId(): Promise<number | null> {
    try {
      const cachedId = this.getDeveloperIdSync();
      if (cachedId) return cachedId;

      const developer = await this.getCurrentDeveloper();
      if (developer) {
        return developer.id;
      }

      return null;
    } catch (error) {
      console.error('Error getting developer ID:', error);
      return null;
    }
  },

  getDeveloperIdSyncCompatible(): number | null {
    return this.getDeveloperIdSync();
  },

  isAuthenticated(): boolean {
    return useAuthStore.getState().isAuthenticated;
  },
};
