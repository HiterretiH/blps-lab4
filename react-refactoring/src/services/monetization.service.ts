import { api } from './api';
import { MonetizedApplication, PaymentRequest } from '../types';

export const monetizationService = {
  async getMonetizationInfo(applicationId: number): Promise<MonetizedApplication | null> {
    try {
      const response = await api.get<MonetizedApplication>(`/monetization/info/${applicationId}`);
      return response.data;
    } catch {
      return null;
    }
  },

  async sendForm(applicationId: number, amount: number): Promise<PaymentRequest> {
    const response = await api.post<PaymentRequest>(
      `/monetization/sendForm/${applicationId}`,
      null,
      { params: { amount } }
    );
    return response.data;
  },

  async makePayout(paymentRequest: PaymentRequest): Promise<string> {
    const response = await api.post<string>('/monetization/payout', paymentRequest);
    return response.data;
  },

  async createMonetizedApplication(data: {
    developerId: number;
    applicationId: number;
    currentBalance?: number;
    revenue?: number;
    downloadRevenue?: number;
    adsRevenue?: number;
    purchasesRevenue?: number;
  }): Promise<MonetizedApplication> {
    const response = await api.post<MonetizedApplication>('/monetized-applications', data);
    return response.data;
  },

  async getMonetizedApplication(id: number): Promise<MonetizedApplication> {
    const response = await api.get<MonetizedApplication>(`/monetized-applications/${id}`);
    return response.data;
  },
};
