import { api } from './api';
import { InAppAdd } from '../types';

export interface InAppAddJson {
  monetizedApplicationId: number;
  title: string;
  description: string;
  price: number;
}

export const adsService = {
  async createAd(data: InAppAddJson): Promise<InAppAdd> {
    const response = await api.post<InAppAdd>('/in-app-ads/create', data);
    return response.data;
  },

  async createBulkAds(ads: InAppAddJson[]): Promise<InAppAdd[]> {
    const response = await api.post<InAppAdd[]>('/in-app-ads/bulk', ads);
    return response.data;
  },

  async getAllAds(): Promise<InAppAdd[]> {
    const response = await api.get<InAppAdd[]>('/in-app-ads/list');
    return response.data;
  },

  async getAdById(id: number): Promise<InAppAdd> {
    const response = await api.get<InAppAdd>(`/in-app-ads/get/${id}`);
    return response.data;
  },

  async getAdsByMonetizedApp(monetizedApplicationId: number): Promise<InAppAdd[]> {
    const response = await api.get<InAppAdd[]>(`/in-app-ads/monetized/${monetizedApplicationId}`);
    return response.data;
  },

  async deleteAd(id: number): Promise<void> {
    await api.delete(`/in-app-ads/${id}`);
  },
};
