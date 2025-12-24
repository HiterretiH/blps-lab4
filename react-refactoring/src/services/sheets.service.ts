import { api } from './api';

export const sheetsService = {
  async createRevenueSheet(): Promise<{ sheetId: string; url: string; message: string }> {
    const response = await api.post('/sheets/create-revenue-sheet');
    return response.data;
  },

  async addAppSheets(appId: number, sheetNames?: string[]): Promise<void> {
    await api.post(`/sheets/${appId}/add-sheets`, sheetNames || []);
  },

  async updateAppsTop(): Promise<void> {
    await api.post('/sheets/update-top');
  },

  async getRevenueSheetUrl(userId: number): Promise<string> {
    const response = await api.get(`/sheets/revenue/${userId}`);
    return response.data.url;
  },
};
