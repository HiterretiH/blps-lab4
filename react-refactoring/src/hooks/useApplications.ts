import { useState, useCallback } from 'react';
import { applicationsService } from '../services/applications.service';
import { authService } from '../services/auth.service';
import { Application } from '../types';

interface ApiError {
  message?: string;
  response?: {
    data?: {
      message?: string;
    };
  };
}

export const useApplications = () => {
  const [applications, setApplications] = useState<Application[]>([]);
  const [selectedApp, setSelectedApp] = useState<Application | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchMyApplications = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      console.log('🔄 Загрузка моих приложений...');

      const currentUser = authService.getCurrentUser();
      const isDeveloper = currentUser?.role === 'DEVELOPER';

      let data: Application[];

      if (isDeveloper) {
        data = await applicationsService.getMyApplications();
        console.log(`👨‍💻 Разработчик, получено моих приложений: ${data.length}`);
      } else {
        data = await applicationsService.getAllApplications();
        console.log(`👤 Не разработчик, получено всех приложений: ${data.length}`);
      }

      setApplications(data);
      return data;
    } catch (err) {
      const apiError = err as ApiError;
      const errorMsg = apiError.response?.data?.message || 'Не удалось загрузить приложения';
      setError(errorMsg);
      console.error('❌ Ошибка загрузки приложений:', err);

      setApplications([]);
      return [];
    } finally {
      setIsLoading(false);
    }
  }, []);

  const fetchAllApplications = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await applicationsService.getAllApplications();
      setApplications(data);
      return data;
    } catch (err) {
      const apiError = err as ApiError;
      setError(apiError.response?.data?.message || 'Failed to fetch applications');
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const createApplication = useCallback(async (appData: Omit<Application, 'id'>) => {
    setIsLoading(true);
    setError(null);
    try {
      const developerId = authService.getDeveloperId();

      if (!developerId) {
        throw new Error('Не удалось определить ID разработчика');
      }

      const dataToSend = {
        ...appData,
        developerId: developerId,
      };

      console.log('📤 Отправка данных приложения:', dataToSend);

      const newApp = await applicationsService.createApplication(dataToSend);
      setApplications(prev => [...prev, newApp]);
      return newApp;
    } catch (err) {
      const apiError = err as ApiError;
      const errorMsg = apiError.response?.data?.message || 'Не удалось создать приложение';
      setError(errorMsg);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const deleteApplication = useCallback(async (id: number) => {
    setIsLoading(true);
    setError(null);
    try {
      await applicationsService.deleteApplication(id);
      setApplications(prev => prev.filter(app => app.id !== id));
    } catch (err) {
      const apiError = err as ApiError;
      const errorMsg = apiError.response?.data?.message || 'Не удалось удалить приложение';
      setError(errorMsg);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  return {
    applications,
    selectedApp,
    setSelectedApp,
    isLoading,
    error,
    fetchMyApplications, // Для разработчиков - их приложения
    fetchAllApplications, // Для всех - все приложения
    createApplication,
    deleteApplication,
  };
};
