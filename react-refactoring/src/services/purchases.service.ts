import { api } from './api';
import { InAppPurchase } from '../types';

export interface InAppPurchasesJson {
  titles: string[];
  descriptions: string[];
  prices: number[];
}

export const purchasesService = {
  async createPurchases(data: InAppPurchasesJson): Promise<InAppPurchase[]> {
    const response = await api.post<InAppPurchase[]>('/in-app-purchases/create', data);
    return response.data;
  },

  async getAllPurchases(): Promise<InAppPurchase[]> {
    try {
      const response = await api.get<InAppPurchase[]>('/in-app-purchases/all');
      return response.data;
    } catch (error) {
      console.error('Error fetching all purchases:', error);
      return [];
    }
  },

  async getPurchaseById(id: number): Promise<InAppPurchase> {
    const response = await api.get<InAppPurchase>(`/in-app-purchases/${id}`);
    return response.data;
  },

  async linkToMonetizedApp(monetizedApplicationId: number): Promise<InAppPurchase[]> {
    const response = await api.post<InAppPurchase[]>(
      `/in-app-purchases/link-to-monetized-app/${monetizedApplicationId}`
    );
    return response.data;
  },

  async getPurchasesByApp(applicationId: number): Promise<InAppPurchase[]> {
    try {
      const response = await api.get<InAppPurchase[]>(
        `/in-app-purchases/application/${applicationId}`
      );
      return response.data;
    } catch (error) {
      console.error('Error fetching purchases by app:', error);
      return [];
    }
  },

  async getPurchasesByMonetizedApp(monetizedApplicationId: number): Promise<InAppPurchase[]> {
    try {
      const allPurchases = await this.getAllPurchases();
      return allPurchases.filter(
        purchase => purchase.monetizedApplicationId === monetizedApplicationId
      );
    } catch (error) {
      console.error('Error filtering purchases:', error);
      return [];
    }
  },
};
