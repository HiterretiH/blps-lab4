import { api } from './api';
import { ApplicationStats } from '../types';

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
};
