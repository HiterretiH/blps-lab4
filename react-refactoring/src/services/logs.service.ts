import { api } from './api';
import { VerificationLog, PayoutLog, GoogleOperationResult } from '../types';

export const logsService = {
  async createVerificationLog(data: {
    securityCheckPassed: boolean;
    policyCheckPassed: boolean;
    adsCheckPassed: boolean;
    logMessage: string;
  }): Promise<VerificationLog> {
    const response = await api.post<VerificationLog>('/verification-logs', null, {
      params: data,
    });
    return response.data;
  },

  async getVerificationLog(id: number): Promise<VerificationLog> {
    const response = await api.get<VerificationLog>(`/verification-logs/${id}`);
    return response.data;
  },

  async createPayoutLog(data: PayoutLog): Promise<PayoutLog> {
    const response = await api.post<PayoutLog>('/payout-logs', data);
    return response.data;
  },

  async getPayoutLog(id: number): Promise<PayoutLog> {
    const response = await api.get<PayoutLog>(`/payout-logs/${id}`);
    return response.data;
  },

  async getAllPayoutLogs(): Promise<PayoutLog[]> {
    const response = await api.get<PayoutLog[]>('/payout-logs');
    return response.data;
  },

  async getMyGoogleResults(): Promise<GoogleOperationResult[]> {
    const response = await api.get<GoogleOperationResult[]>('/google-results/my-results');
    return response.data;
  },

  async getGoogleResultsByUser(userId: number): Promise<GoogleOperationResult[]> {
    const response = await api.get<GoogleOperationResult[]>(`/google-results/user/${userId}`);
    return response.data;
  },

  async getGoogleResultsByOperation(operation: string): Promise<GoogleOperationResult[]> {
    const response = await api.get<GoogleOperationResult[]>(
      `/google-results/operation/${operation}`
    );
    return response.data;
  },

  async getMyLatestGoogleResults(limit: number = 10): Promise<GoogleOperationResult[]> {
    const response = await api.get<GoogleOperationResult[]>(
      `/google-results/my-results/latest/${limit}`
    );
    return response.data;
  },

  async getGoogleResultsWithErrors(): Promise<GoogleOperationResult[]> {
    const response = await api.get<GoogleOperationResult[]>('/google-results/errors');
    return response.data;
  },

  async getSuccessfulGoogleOperations(): Promise<GoogleOperationResult[]> {
    const response = await api.get<GoogleOperationResult[]>('/google-results/successful');
    return response.data;
  },
};
