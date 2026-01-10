import { api } from './api';
import { InAppAdd } from '../types';
import { monetizationService } from './monetization.service';

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
    try {
      const response = await api.get<InAppAdd[]>('/in-app-ads/list');
      return response.data;
    } catch (error) {
      console.error('Error fetching all ads:', error);
      return [];
    }
  },

  async getAdById(id: number): Promise<InAppAdd> {
    const response = await api.get<InAppAdd>(`/in-app-ads/get/${id}`);
    return response.data;
  },

  async getAdsByMonetizedApp(monetizedApplicationId: number): Promise<InAppAdd[]> {
    try {
      console.log(`Fetching ads for monetized app ID: ${monetizedApplicationId}`);
      const response = await api.get<InAppAdd[]>(`/in-app-ads/monetized/${monetizedApplicationId}`);
      console.log(`Found ${response.data.length} ads for monetized app ${monetizedApplicationId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching ads for monetized app ${monetizedApplicationId}:`, error);

      try {
        const allAds = await this.getAllAds();
        const filteredAds = allAds.filter(
          ad => ad.monetizedApplicationId === monetizedApplicationId
        );
        console.log(`Fallback: Found ${filteredAds.length} ads after filtering`);
        return filteredAds;
      } catch (fallbackError) {
        console.error('Fallback also failed:', fallbackError);
        return [];
      }
    }
  },

  async deleteAd(id: number): Promise<void> {
    try {
      await api.delete(`/in-app-ads/${id}`);
    } catch (error) {
      console.error(`Error deleting ad ${id}:`, error);
      throw error;
    }
  },

  async getAdsByApplication(applicationId: number): Promise<InAppAdd[]> {
    try {
      const monetization = await monetizationService.getMonetizationInfo(applicationId);
      if (!monetization) {
        return [];
      }

      return await this.getAdsByMonetizedApp(monetization.id);
    } catch (error) {
      console.error(`Error fetching ads for application ${applicationId}:`, error);
      return [];
    }
  },
};
