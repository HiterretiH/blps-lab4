import { api } from './api';

export const googleService = {
  // Google Forms
  async createGoogleForm(): Promise<{ formId: string; url: string }> {
    const response = await api.post('/forms/create');
    return response.data;
  },

  async generateFormFields(): Promise<{ fields: string[] }> {
    const response = await api.get('/forms/generate');
    return response.data;
  },

  async addFormFields(fields: { fieldName: string }[]): Promise<void> {
    await api.post('/forms/addFields', fields);
  },

  // Google Sheets
  async createRevenueSheet(applicationId: number): Promise<{ sheetId: string; url: string }> {
    const response = await api.post('/sheets/create-revenue-sheet', { applicationId });
    return response.data;
  },

  async addAppSheets(appId: number, sheetNames: string[]): Promise<void> {
    await api.post(`/sheets/${appId}/add-sheets`, sheetNames);
  },

  async updateTopApps(): Promise<void> {
    await api.post('/sheets/update-top');
  },
};
