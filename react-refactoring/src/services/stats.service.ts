import { api } from './api';
import { ApplicationStats } from '../types';
import { authService } from './auth.service';

export interface ApplicationStatsJson {
  applicationId: number;
  downloads: number;
  rating?: number;
}

export const statsService = {
  async createStats(data: ApplicationStatsJson): Promise<ApplicationStats> {
    const response = await api.post<ApplicationStats>('/application-stats', data);
    return response.data;
  },

  async updateStats(id: number, data: ApplicationStatsJson): Promise<ApplicationStats> {
    const response = await api.put<ApplicationStats>(`/application-stats/${id}`, data);
    return response.data;
  },

  async getStatsById(id: number): Promise<ApplicationStats> {
    const response = await api.get<ApplicationStats>(`/application-stats/${id}`);
    return response.data;
  },

  async getAllStats(): Promise<ApplicationStats[]> {
    const response = await api.get<ApplicationStats[]>('/application-stats');
    return response.data;
  },

  async getStatsByApplication(applicationId: number): Promise<ApplicationStats[]> {
    const response = await api.get<ApplicationStats[]>(
      `/application-stats/application/${applicationId}`
    );
    return response.data;
  },

  async deleteStats(id: number): Promise<void> {
    await api.delete(`/application-stats/${id}`);
  },

  async getStatsByDeveloper(developerId: number): Promise<ApplicationStats[]> {
    try {
      const appsResponse = await api.get(`/applications/developer/${developerId}`);
      const apps = appsResponse.data;

      const statsResponse = await api.get('/application-stats');
      const allStats = statsResponse.data;

      const developerStats = allStats.filter((stat: any) => {
        return apps.some((app: any) => app.id === stat.application.id);
      });

      return developerStats;
    } catch (error) {
      console.error('Error fetching developer stats:', error);
      return [];
    }
  },

  async getTotalDownloadsByDeveloper(developerId: number): Promise<number> {
    try {
      const stats = await this.getStatsByDeveloper(developerId);
      return stats.reduce((total, stat) => total + (stat.downloads || 0), 0);
    } catch (error) {
      console.error('Error calculating total downloads:', error);
      return 0;
    }
  },

  async getUserStats(): Promise<ApplicationStats[]> {
    try {
      const user = authService.getCurrentUser();
      if (!user || !user.userId) {
        return [];
      }

      const appsResponse = await api.get(`/applications/user/${user.userId}`);
      const apps = appsResponse.data;

      if (apps.length === 0) return [];

      const statsResponse = await api.get('/application-stats');
      const allStats = statsResponse.data;

      return allStats.filter((stat: any) =>
        apps.some((app: any) => app.id === stat.application.id)
      );
    } catch (error) {
      console.error('Error fetching user stats:', error);
      return [];
    }
  },
};
