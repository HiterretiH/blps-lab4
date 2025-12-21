import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppCard } from '../components/apps/AppCard';
import { CreateAppForm } from '../components/apps/CreateAppForm';
import { Button } from '../components/ui/Button';
import { Alert } from '../components/ui/Alert';
import { Card, CardContent } from '../components/ui/Card';
import { useApplications } from '../hooks/useApplications';
import { authService } from '../services/auth.service';
import { Application } from '../types';
import { Loader2, Package, User, Filter } from 'lucide-react';

export const ApplicationsPage: React.FC = () => {
  const navigate = useNavigate();
  const {
    applications,
    isLoading,
    error,
    fetchMyApplications,
    createApplication,
    deleteApplication,
  } = useApplications();

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const currentUser = authService.getCurrentUser();
  const isDeveloper = currentUser?.role === 'DEVELOPER';
  const developerId = authService.getDeveloperId();

  useEffect(() => {
    fetchMyApplications().catch(err => {
      console.error('Failed to fetch applications:', err);
    });
  }, [fetchMyApplications]);

  const handleCreateApp = async (appData: any) => {
    try {
      await createApplication(appData);
      await fetchMyApplications(); // Обновляем список
    } catch (err: any) {
      console.error('Error creating app:', err);
      alert(err.message || 'Ошибка при создании приложения');
    }
  };

  const handleDeleteApp = async (id: number) => {
    if (window.confirm('Вы уверены, что хотите удалить это приложение?')) {
      try {
        await deleteApplication(id);
        await fetchMyApplications(); // Обновляем список
      } catch (err: any) {
        console.error('Error deleting app:', err);
        alert(err.message || 'Ошибка при удалении приложения');
      }
    }
  };

  // Если пользователь не авторизован, перенаправляем
  if (!currentUser) {
    navigate('/login');
    return null;
  }

  return (
    <div className="space-y-6">
      {/* Заголовок и информация */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {isDeveloper ? 'Мои приложения' : 'Каталог приложений'}
          </h1>
          <p className="mt-1 text-gray-600">
            {isDeveloper
              ? 'Управляйте своими приложениями и отслеживайте статистику'
              : 'Найдите и скачайте интересные приложения'}
          </p>
        </div>

        {isDeveloper && (
          <Button onClick={() => setIsCreateModalOpen(true)} className="flex items-center gap-2">
            <Package className="h-4 w-4" />
            Создать приложение
          </Button>
        )}
      </div>

      {/* Информационная карточка */}
      <Card className="bg-gradient-to-r from-primary-50 to-blue-50">
        <CardContent className="p-6">
          <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
            <div className="flex items-center gap-3">
              <div className="rounded-lg bg-primary-100 p-2">
                {isDeveloper ? (
                  <Package className="h-6 w-6 text-primary-600" />
                ) : (
                  <User className="h-6 w-6 text-primary-600" />
                )}
              </div>
              <div>
                <h3 className="font-medium text-gray-900">
                  {isDeveloper ? 'Статистика разработчика' : 'Статистика пользователя'}
                </h3>
                <div className="mt-1 flex items-center gap-4">
                  <span className="text-sm text-gray-600">
                    Имя: <span className="font-medium">{currentUser.username}</span>
                  </span>
                  <span className="text-sm text-gray-600">
                    Роль: <span className="font-medium">{currentUser.role}</span>
                  </span>
                  {developerId && (
                    <span className="text-sm text-gray-600">
                      ID разработчика: <span className="font-medium">{developerId}</span>
                    </span>
                  )}
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2 rounded-lg border bg-white px-3 py-2">
              <Filter className="h-4 w-4 text-gray-500" />
              <span className="text-sm text-gray-700">
                Приложений: <span className="font-semibold">{applications.length}</span>
              </span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Сообщения об ошибках */}
      {error && (
        <Alert variant="danger" title="Ошибка">
          {error}
          {error.includes('404') && (
            <div className="mt-2 text-sm">
              <p>Возможно, эндпоинт API недоступен. Проверьте:</p>
              <ul className="mt-1 list-disc pl-5">
                <li>Запущен ли бэкенд на порту 727</li>
                <li>Доступен ли эндпоинт /api/applications/developer/{developerId}</li>
                <li>Правильные ли права доступа у вашего токена</li>
              </ul>
            </div>
          )}
        </Alert>
      )}

      {/* Загрузка */}
      {isLoading ? (
        <div className="flex flex-col items-center justify-center py-12">
          <Loader2 className="mb-4 h-12 w-12 animate-spin text-primary-600" />
          <p className="text-gray-600">Загрузка приложений...</p>
          <p className="mt-1 text-sm text-gray-500">
            Запрос к: /api/applications/developer/{developerId}
          </p>
        </div>
      ) : applications.length === 0 ? (
        /* Пустой список */
        <Card>
          <CardContent className="py-12">
            <div className="mx-auto max-w-md text-center">
              <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-gray-100">
                {isDeveloper ? (
                  <Package className="h-8 w-8 text-gray-400" />
                ) : (
                  <User className="h-8 w-8 text-gray-400" />
                )}
              </div>

              <h3 className="mb-2 text-lg font-medium text-gray-900">
                {isDeveloper ? 'У вас еще нет приложений' : 'Приложения не найдены'}
              </h3>

              <p className="mb-6 text-gray-600">
                {isDeveloper
                  ? 'Создайте свое первое приложение, чтобы начать монетизацию!'
                  : 'В системе пока нет доступных приложений. Попробуйте позже.'}
              </p>

              {isDeveloper ? (
                <Button onClick={() => setIsCreateModalOpen(true)}>
                  Создать первое приложение
                </Button>
              ) : (
                <div className="space-y-3">
                  <p className="text-sm text-gray-500">Хотите стать разработчиком?</p>
                  <Button variant="outline" onClick={() => navigate('/register')}>
                    Зарегистрироваться как разработчик
                  </Button>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      ) : (
        /* Список приложений */
        <>
          <div className="mb-4 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Package className="h-5 w-5 text-gray-500" />
              <span className="text-sm font-medium text-gray-700">
                {isDeveloper ? 'Ваши приложения' : 'Доступные приложения'}
              </span>
              <span className="rounded-full bg-gray-100 px-2 py-1 text-xs text-gray-600">
                {applications.length}
              </span>
            </div>

            {isDeveloper && (
              <div className="text-sm text-gray-600">
                Сортировка: <span className="font-medium">по дате создания</span>
              </div>
            )}
          </div>

          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
            {applications.map(app => (
              <AppCard
                key={app.id}
                application={app}
                onDelete={isDeveloper ? handleDeleteApp : undefined}
                currentDeveloperId={developerId || undefined}
              />
            ))}
          </div>
        </>
      )}

      {/* Модальное окно создания приложения */}
      <CreateAppForm
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSubmit={handleCreateApp}
        developerId={developerId}
      />
    </div>
  );
};
