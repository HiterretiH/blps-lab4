import { api } from './api';
import { PaymentRequest } from '../types';

export const paymentService = {
  async createPaymentRequest(applicationId: number, amount: number): Promise<PaymentRequest> {
    const response = await api.post<PaymentRequest>('/payment-requests', null, {
      params: { applicationId, amount },
    });
    return response.data;
  },

  async getPaymentRequest(applicationId: number): Promise<PaymentRequest> {
    const response = await api.get<PaymentRequest>(`/payment-requests/${applicationId}`);
    return response.data;
  },

  async validateCard(applicationId: number): Promise<boolean> {
    try {
      const response = await api.get<string>(`/payment-requests/validate/${applicationId}`);
      return response.data === 'Card is valid';
    } catch {
      return false;
    }
  },

  async getPaymentHistory(developerId: number): Promise<PaymentRequest[]> {
    const response = await api.get<PaymentRequest[]>(`/payment-requests/developer/${developerId}`);
    return response.data;
  },
};
