import { api } from './api';
import { Application, ApiApplication } from '../types';
import { authService } from './auth.service';

export const applicationsService = {
  // Получить все приложения текущего разработчика
  async getMyApplications(): Promise<Application[]> {
    try {
      const developerId = authService.getDeveloperId();

      if (!developerId) {
        console.error('❌ Не удалось определить developerId');
        return [];
      }

      console.log(`🔄 Получение приложений для developerId: ${developerId}`);
      const response = await api.get<ApiApplication[]>(`/applications/developer/${developerId}`);

      console.log(`✅ Получено ${response.data.length} приложений`);
      return response.data.map(app => ({
        ...app,
        status: app.status,
        type: app.type,
      }));
    } catch (error: any) {
      console.error('❌ Ошибка получения приложений:', {
        message: error.message,
        status: error.response?.status,
        data: error.response?.data,
      });

      // Для отладки: если есть ошибка 404, значит эндпоинта нет
      if (error.response?.status === 404) {
        console.warn('⚠️ Эндпоинт /applications/developer/{id} не найден');
        console.warn('Проверьте наличие метода в ApplicationController');
      }

      return [];
    }
  },

  // Получить все приложения (для админов и обычных пользователей)
  async getAllApplications(): Promise<Application[]> {
    try {
      console.log('🔄 Получение всех приложений');
      const response = await api.get<ApiApplication[]>('/applications');

      console.log(`✅ Получено ${response.data.length} приложений`);
      return response.data.map(app => ({
        ...app,
        status: app.status,
        type: app.type,
      }));
    } catch (error: any) {
      console.error('❌ Ошибка получения всех приложений:', error);

      // Если эндпоинта нет, возвращаем пустой массив
      if (error.response?.status === 404) {
        console.warn('⚠️ Эндпоинт /applications не найден');
      }

      return [];
    }
  },

  // Получить приложения по developerId (для админов)
  async getApplicationsByDeveloper(developerId: number): Promise<Application[]> {
    try {
      const response = await api.get<ApiApplication[]>(`/applications/developer/${developerId}`);
      return response.data.map(app => ({
        ...app,
        status: app.status,
        type: app.type,
      }));
    } catch (error: any) {
      console.error(`❌ Ошибка получения приложений developer ${developerId}:`, error);
      throw new Error(error.response?.data?.message || 'Failed to fetch applications');
    }
  },

  async createApplication(data: Omit<Application, 'id'>): Promise<Application> {
    try {
      const response = await api.post<ApiApplication>('/applications', data);
      return {
        ...response.data,
        status: response.data.status,
        type: response.data.type,
      };
    } catch (error: any) {
      console.error('❌ Ошибка создания приложения:', error);
      throw new Error(error.response?.data?.message || 'Failed to create application');
    }
  },

  async deleteApplication(id: number): Promise<void> {
    try {
      await api.delete(`/applications/${id}`);
    } catch (error: any) {
      console.error(`❌ Ошибка удаления приложения ${id}:`, error);
      throw new Error(error.response?.data?.message || 'Failed to delete application');
    }
  },

  async getApplicationById(id: number): Promise<Application> {
    try {
      const response = await api.get<ApiApplication>(`/applications/${id}`);
      return {
        ...response.data,
        status: response.data.status,
        type: response.data.type,
      };
    } catch (error: any) {
      console.error(`❌ Ошибка получения приложения ${id}:`, error);
      throw new Error(error.response?.data?.message || 'Failed to fetch application');
    }
  },
};
