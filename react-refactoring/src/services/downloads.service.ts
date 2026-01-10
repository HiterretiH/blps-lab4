import { api } from './api';

export interface DownloadedApp {
  id: number;
  applicationId: number;
  userId: number;
  downloadedAt: string;
  application: {
    id: number;
    name: string;
    type: string;
    price: number;
    description: string;
    developerId: number;
    status: number;
  };
}

export const downloadsService = {
  async getMyDownloads(): Promise<DownloadedApp[]> {
    try {
      const response = await api.get<DownloadedApp[]>('/user/downloads');
      return response.data;
    } catch (error) {
      console.error('Error fetching downloads:', error);
      return [];
    }
  },

  async addDownload(applicationId: number): Promise<DownloadedApp> {
    const response = await api.post<DownloadedApp>(`/user/downloads/${applicationId}`);
    return response.data;
  },

  async checkIfDownloaded(applicationId: number): Promise<boolean> {
    try {
      const downloads = await this.getMyDownloads();
      return downloads.some(download => download.application.id === applicationId);
    } catch (error) {
      console.error('Error checking download status:', error);
      return false;
    }
  },

  async removeDownload(applicationId: number): Promise<void> {
    await api.delete(`/user/downloads/${applicationId}`);
  },

  async getDownloadStats(): Promise<{
    totalDownloads: number;
    last30Days: number;
    favoriteType: string;
  }> {
    try {
      const response = await api.get('/user/downloads/stats');
      return response.data;
    } catch (error) {
      console.error('Error fetching download stats:', error);
      return {
        totalDownloads: 0,
        last30Days: 0,
        favoriteType: 'N/A',
      };
    }
  },
};
