import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardHeader, CardContent, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Alert } from '../components/ui/Alert';
import { authService } from '../services/auth.service';
import { applicationsService } from '../services/applications.service';
import { api } from '../services/api';

interface AuthState {
  tokenExists: boolean;
  token: string;
  user: string | Record<string, unknown>;
  isAuthenticated: boolean;
  currentUser: { username: string; role: string } | null;
  developerId?: number | null;
}

interface ApiTestResult {
  success: boolean;
  data?: unknown;
  error?: string;
  response?: unknown;
  status?: number | undefined;
  message: string;
}

interface ApiError {
  message?: string;
  response?: {
    data?: {
      message?: string;
    };
    status?: number;
  };
}

// Вспомогательная функция для безопасного рендеринга
const renderJsonSafe = (data: unknown): string => {
  if (data === null || data === undefined) {
    return '';
  }
  try {
    return JSON.stringify(data, null, 2);
  } catch {
    return String(data);
  }
};

export const DebugAuthPage: React.FC = () => {
  const navigate = useNavigate();
  const [authState, setAuthState] = useState<AuthState>({
    tokenExists: false,
    token: '',
    user: '',
    isAuthenticated: false,
    currentUser: null,
    developerId: null,
  });
  const [apiTestResult, setApiTestResult] = useState<ApiTestResult | null>(null);
  const [isTesting, setIsTesting] = useState(false);

  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem('auth_token');
      const user = localStorage.getItem('user');

      setAuthState({
        tokenExists: !!token,
        token: token ? `${token.substring(0, 20)}...` : 'Нет токена',
        user: user ? JSON.parse(user) : 'Нет пользователя',
        isAuthenticated: authService.isAuthenticated(),
        currentUser: authService.getCurrentUser(),
        developerId: authService.getDeveloperId?.(),
      });
    };

    checkAuth();
  }, []);

  const testApiCall = async () => {
    setIsTesting(true);
    try {
      console.group('🧪 Тест API запроса');
      console.log('Делаем запрос к /applications...');

      const response = await applicationsService.getAllApplications();

      setApiTestResult({
        success: true,
        data: response,
        message: 'API запрос успешен!',
      });

      console.log('✅ Успех:', response);
      console.groupEnd();
    } catch (error: unknown) {
      console.error('❌ Ошибка:', error);

      const apiError = error as ApiError;

      setApiTestResult({
        success: false,
        error: apiError.message || 'Unknown error',
        response: apiError.response?.data,
        status: apiError.response?.status,
        message: 'API запрос провален',
      });

      console.groupEnd();
    } finally {
      setIsTesting(false);
    }
  };

  const clearAuth = () => {
    console.log('🧹 Очистка авторизации...');
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user');
    setAuthState({
      tokenExists: false,
      token: '',
      user: '',
      isAuthenticated: false,
      currentUser: null,
      developerId: null,
    });
    setApiTestResult(null);
    console.log('✅ Очищено');
  };

  const simulateLogin = async (username: string, password: string) => {
    console.log(`🔑 Симуляция входа: ${username}`);

    try {
      const response = await api.post<{ token: string; role: string }>('/auth/login', {
        username,
        password,
      });
      console.log('✅ Логин успешен:', response.data);

      localStorage.setItem('auth_token', response.data.token);
      localStorage.setItem(
        'user',
        JSON.stringify({
          username: username,
          role: response.data.role,
        })
      );

      window.location.reload();
    } catch (error: unknown) {
      const apiError = error as ApiError;

      console.error('❌ Ошибка логина:', apiError.response?.data || apiError.message);
      alert(`Ошибка: ${apiError.response?.data?.message || apiError.message}`);
    }
  };

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <div>
        <h1 className="mb-2 text-3xl font-bold text-gray-900">Отладка авторизации</h1>
        <p className="text-gray-600">Диагностика проблем с авторизацией и API</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Текущее состояние авторизации</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <pre className="overflow-auto rounded-lg bg-gray-50 p-4 text-sm">
              {JSON.stringify(authState, null, 2)}
            </pre>

            <div className="flex gap-3">
              <Button onClick={() => window.location.reload()}>Обновить состояние</Button>
              <Button variant="outline" onClick={clearAuth}>
                Очистить авторизацию
              </Button>
              <Button variant="outline" onClick={() => navigate('/applications')}>
                Перейти на Applications
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Тест API запросов</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <Button onClick={testApiCall} isLoading={isTesting} disabled={!authState.tokenExists}>
              Тест запроса /applications
            </Button>

            {!authState.tokenExists && (
              <Alert variant="warning">Токен не найден. Сначала выполните вход.</Alert>
            )}

            {apiTestResult && (
              <div className="space-y-2">
                <Alert variant={apiTestResult.success ? 'success' : 'danger'}>
                  {apiTestResult.message}
                </Alert>

                {apiTestResult.error && (
                  <div className="space-y-2">
                    <pre className="overflow-auto rounded-lg bg-gray-50 p-4 text-sm">
                      Ошибка: {String(apiTestResult.error)}
                      {apiTestResult.status !== undefined && `\nСтатус: ${apiTestResult.status}`}
                    </pre>

                    {apiTestResult.response !== undefined && apiTestResult.response !== null && (
                      <pre className="overflow-auto rounded-lg bg-gray-50 p-4 text-sm">
                        Ответ: {renderJsonSafe(apiTestResult.response)}
                      </pre>
                    )}
                  </div>
                )}

                {apiTestResult.success &&
                  apiTestResult.data !== undefined &&
                  apiTestResult.data !== null && (
                    <div>
                      <p className="mb-2 font-medium text-gray-900">Полученные данные:</p>
                      <pre className="overflow-auto rounded-lg bg-gray-50 p-4 text-sm">
                        {renderJsonSafe(apiTestResult.data)}
                      </pre>
                    </div>
                  )}
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Быстрый вход для теста</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
            <Button
              variant="outline"
              onClick={() => simulateLogin('321321', '12345')}
              className="flex-col"
            >
              <span className="font-semibold">321321</span>
              <span className="text-sm text-gray-600">DEVELOPER</span>
            </Button>

            <Button
              variant="outline"
              onClick={() => simulateLogin('dev', '12345')}
              className="flex-col"
            >
              <span className="font-semibold">dev</span>
              <span className="text-sm text-gray-600">DEVELOPER</span>
            </Button>

            <Button
              variant="outline"
              onClick={() => simulateLogin('user', '12345')}
              className="flex-col"
            >
              <span className="font-semibold">user</span>
              <span className="text-sm text-gray-600">USER</span>
            </Button>
          </div>

          <div className="mt-4 rounded-lg bg-yellow-50 p-3">
            <p className="text-sm text-yellow-800">
              <strong>Пароли:</strong> у всех тестовых пользователей пароль <code>12345</code>
            </p>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Инструкция по отладке</CardTitle>
        </CardHeader>
        <CardContent>
          <ol className="list-decimal space-y-2 pl-5 text-gray-700">
            <li>
              Откройте <strong>Developer Tools (F12)</strong>
            </li>
            <li>
              Перейдите на вкладку <strong>Console</strong>
            </li>
            <li>Выполните вход через кнопки выше</li>
            <li>Нажмите "Тест запроса /applications"</li>
            <li>Смотрите логи запросов в консоли</li>
            <li>Если есть ошибки - скопируйте их</li>
          </ol>

          <div className="mt-4 rounded-lg bg-blue-50 p-3">
            <p className="text-sm text-blue-800">
              <strong>Важно:</strong> Убедитесь что бэкенд запущен на{' '}
              <code>http://localhost:727</code>
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
