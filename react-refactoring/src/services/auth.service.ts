import { api } from './api';
import { LoginCredentials, RegisterCredentials, Token } from '../types';

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
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user');
  },

  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  getDeveloperId(): number | null {
    const user = this.getCurrentUser();
    if (!user) return null;

    // Маппинг username -> developer_id из базы данных
    const developerMap: Record<string, number> = {
      '321321': 2,
      dev: 1,
      developer: 3,
      console: 4,
    };

    return developerMap[user.username] || null;
  },

  isAuthenticated(): boolean {
    return !!localStorage.getItem('auth_token');
  },
};
