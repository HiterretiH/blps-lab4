import { api } from './api';
import { MonetizedApplication, PaymentRequest, Application } from '../types';
import { authService } from './auth.service';

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

  async getAllMonetizedAppsByDeveloper(developerId: number): Promise<MonetizedApplication[]> {
    try {
      const response = await api.get<MonetizedApplication[]>(
        `/monetized-applications/developer/${developerId}`
      );
      return response.data;
    } catch (error) {
      console.error('Error fetching monetized apps:', error);
      return [];
    }
  },

  async getAppStats(applicationId: number): Promise<any> {
    try {
      const response = await api.get(`/application-stats/application/${applicationId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching app stats:', error);
      return null;
    }
  },

  async getDeveloperTotalStats(developerId: number): Promise<{
    totalRevenue: number;
    totalDownloads: number;
    totalAdsRevenue: number;
    totalPurchaseRevenue: number;
    totalDownloadRevenue: number;
  }> {
    try {
      const monetizedApps = await this.getAllMonetizedAppsByDeveloper(developerId);

      let totalRevenue = 0;
      let totalAdsRevenue = 0;
      let totalPurchaseRevenue = 0;
      let totalDownloadRevenue = 0;

      monetizedApps.forEach(app => {
        totalRevenue += app.revenue || 0;
        totalAdsRevenue += app.adsRevenue || 0;
        totalPurchaseRevenue += app.purchasesRevenue || 0;
        totalDownloadRevenue += app.downloadRevenue || 0;
      });

      const statsResponse = await api.get('/application-stats');
      const allStats = statsResponse.data;

      const developerStats = allStats.filter((stat: any) => {
        return monetizedApps.some(app => app.application.id === stat.application.id);
      });

      const totalDownloads = developerStats.reduce(
        (sum: number, stat: any) => sum + (stat.downloads || 0),
        0
      );

      return {
        totalRevenue,
        totalDownloads,
        totalAdsRevenue,
        totalPurchaseRevenue,
        totalDownloadRevenue,
      };
    } catch (error) {
      console.error('Error fetching developer total stats:', error);
      return {
        totalRevenue: 0,
        totalDownloads: 0,
        totalAdsRevenue: 0,
        totalPurchaseRevenue: 0,
        totalDownloadRevenue: 0,
      };
    }
  },

  async getUserMonetizedApps(): Promise<MonetizedApplication[]> {
    try {
      const user = authService.getCurrentUser();
      if (!user || !user.userId) {
        console.error('❌ User ID not found');
        return [];
      }

      const developerResponse = await api.get(`/developers/by-user/${user.userId}`);
      const developer = developerResponse.data;

      if (!developer || !developer.id) {
        console.error('❌ Developer not found for user');
        return [];
      }

      const response = await api.get<MonetizedApplication[]>(
        `/monetized-applications/developer/${developer.id}`
      );
      return response.data;
    } catch (error) {
      console.error('Error fetching user monetized apps:', error);
      return [];
    }
  },

  async getUserTotalStats(): Promise<{
    totalRevenue: number;
    totalDownloads: number;
    totalAdsRevenue: number;
    totalPurchaseRevenue: number;
    totalDownloadRevenue: number;
  }> {
    try {
      const user = authService.getCurrentUser();
      if (!user || !user.userId) {
        return {
          totalRevenue: 0,
          totalDownloads: 0,
          totalAdsRevenue: 0,
          totalPurchaseRevenue: 0,
          totalDownloadRevenue: 0,
        };
      }

      const appsResponse = await api.get(`/applications/user/${user.userId}`);
      const apps = appsResponse.data;

      if (apps.length === 0) {
        return {
          totalRevenue: 0,
          totalDownloads: 0,
          totalAdsRevenue: 0,
          totalPurchaseRevenue: 0,
          totalDownloadRevenue: 0,
        };
      }

      const statsResponse = await api.get('/application-stats');
      const allStats = statsResponse.data;

      const monetizedResponse = await api.get('/monetized-applications');
      const allMonetized = monetizedResponse.data;

      let totalRevenue = 0;
      let totalAdsRevenue = 0;
      let totalPurchaseRevenue = 0;
      let totalDownloadRevenue = 0;
      let totalDownloads = 0;

      apps.forEach((app: any) => {
        const monetization = allMonetized.find((m: any) => m.application.id === app.id);
        if (monetization) {
          totalRevenue += monetization.revenue || 0;
          totalAdsRevenue += monetization.adsRevenue || 0;
          totalPurchaseRevenue += monetization.purchasesRevenue || 0;
          totalDownloadRevenue += monetization.downloadRevenue || 0;
        }

        const stat = allStats.find((s: any) => s.application.id === app.id);
        if (stat) {
          totalDownloads += stat.downloads || 0;
        }
      });

      return {
        totalRevenue,
        totalDownloads,
        totalAdsRevenue,
        totalPurchaseRevenue,
        totalDownloadRevenue,
      };
    } catch (error) {
      console.error('Error fetching user total stats:', error);
      return {
        totalRevenue: 0,
        totalDownloads: 0,
        totalAdsRevenue: 0,
        totalPurchaseRevenue: 0,
        totalDownloadRevenue: 0,
      };
    }
  },

  async monetizeApplication(applicationId: number, developerId: number): Promise<any> {
    try {
      const response = await fetch(`http://localhost:727/api/monetized-applications`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`,
        },
        body: JSON.stringify({
          developerId,
          applicationId,
          currentBalance: 0,
          revenue: 0,
          downloadRevenue: 0,
          adsRevenue: 0,
          purchasesRevenue: 0,
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to monetize application');
      }

      return await response.json();
    } catch (error) {
      console.error('Monetization error:', error);
      throw error;
    }
  },
};
