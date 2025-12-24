import { api } from './api';

export interface FormFieldRequest {
  fieldName: string;
}

export const formsService = {
  async createGoogleForm(): Promise<{ formId: string; url: string; message: string }> {
    const response = await api.post('/forms/create');
    return response.data;
  },

  async generateFormFields(): Promise<Record<string, string>> {
    const response = await api.get('/forms/generate');
    return response.data;
  },

  async addFormField(fieldName: string): Promise<void> {
    await api.post('/forms/addField', { fieldName });
  },

  async addFormFields(fields: FormFieldRequest[]): Promise<void> {
    await api.post('/forms/addFields', fields);
  },

  async getAllFormFields(): Promise<FormFieldRequest[]> {
    const response = await api.get('/forms/fields');
    return response.data;
  },
};
