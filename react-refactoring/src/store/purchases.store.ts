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
    const response = await api.get<InAppPurchase[]>('/in-app-purchases/all');
    return response.data;
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
    const response = await api.get<InAppPurchase[]>(
      `/in-app-purchases/application/${applicationId}`
    );
    return response.data;
  },
};
