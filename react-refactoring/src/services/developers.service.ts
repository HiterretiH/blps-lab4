import { api } from './api';
import { Developer } from '../types';

export const developersService = {
  async createDeveloper(data: { name: string; description: string }): Promise<Developer> {
    const response = await api.post<Developer>('/developers', data);
    return response.data;
  },

  async getDeveloper(id: number): Promise<Developer> {
    const response = await api.get<Developer>(`/developers/${id}`);
    return response.data;
  },

  async updateDeveloper(
    id: number,
    data: { name: string; description: string }
  ): Promise<Developer> {
    const response = await api.put<Developer>(`/developers/${id}`, null, {
      params: data,
    });
    return response.data;
  },

  async deleteDeveloper(id: number): Promise<void> {
    await api.delete(`/developers/${id}`);
  },

  async getDeveloperByUserId(userId: number): Promise<Developer> {
    const response = await api.get<Developer>(`/developers/user/${userId}`);
    return response.data;
  },
};
