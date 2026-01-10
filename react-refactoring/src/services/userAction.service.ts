import { api } from './api';
import { Card } from '../types';

interface UserActivity {
  id: number;
  userId: number;
  action: string;
  timestamp: string;
  details?: Record<string, unknown>;
}

export const userActionsService = {
  async downloadApplication(applicationId: number, card: Card): Promise<string> {
    const response = await api.post<string>(`/user/download/${applicationId}`, card);
    return response.data;
  },

  async purchaseInAppItem(purchaseId: number, card: Card): Promise<string> {
    const response = await api.post<string>(`/user/purchase/${purchaseId}`, card);
    return response.data;
  },

  async viewAdvertisement(adId: number): Promise<string> {
    const response = await api.post<string>(`/user/view-ad/${adId}`);
    return response.data;
  },

  async getUserActivity(userId: number): Promise<UserActivity[]> {
    const response = await api.get<UserActivity[]>(`/user/activity/${userId}`);
    return response.data;
  },
};
