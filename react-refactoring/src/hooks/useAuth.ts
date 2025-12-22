import { useState, useCallback } from 'react';
import { authService } from '../services/auth.service';
import { LoginCredentials, RegisterCredentials } from '../types';

interface ApiError {
  response?: {
    data?: {
      message?: string;
    };
  };
  message?: string;
}

export const useAuth = () => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const login = useCallback(async (credentials: LoginCredentials) => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await authService.login(credentials);
      localStorage.setItem('auth_token', response.token);
      localStorage.setItem(
        'user',
        JSON.stringify({
          username: credentials.username,
          role: response.role,
        })
      );
      return response;
    } catch (err) {
      const error = err as ApiError;
      setError(error.response?.data?.message || 'Login failed');
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const register = useCallback(async (credentials: RegisterCredentials, isDeveloper = false) => {
    setIsLoading(true);
    setError(null);
    try {
      const response = isDeveloper
        ? await authService.registerDeveloper(credentials)
        : await authService.register(credentials);

      localStorage.setItem('auth_token', response.token);
      localStorage.setItem(
        'user',
        JSON.stringify({
          username: credentials.username,
          role: response.role,
        })
      );
      return response;
    } catch (err) {
      const error = err as ApiError;
      setError(error.response?.data?.message || 'Registration failed');
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(() => {
    authService.logout();
  }, []);

  const getCurrentUser = useCallback(() => {
    return authService.getCurrentUser();
  }, []);

  const isAuthenticated = useCallback(() => {
    return authService.isAuthenticated();
  }, []);

  const connectGoogle = useCallback(async () => {
    setIsLoading(true);
    try {
      const response = await authService.connectGoogle();
      window.open(response.authUrl, '_blank', 'width=600,height=600');
      return response;
    } catch (err) {
      const error = err as ApiError;
      setError(error.response?.data?.message || 'Google connection failed');
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  return {
    login,
    register,
    logout,
    getCurrentUser,
    isAuthenticated,
    connectGoogle,
    isLoading,
    error,
  };
};
