import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface DownloadedApp {
  id: number;
  applicationId: number;
  name: string;
  type: string;
  price: number;
  description: string;
  downloadedAt: string;
  developerId: number;
}

interface DownloadsState {
  downloadedApps: DownloadedApp[];
  isLoading: boolean;
  error: string | null;

  addDownload: (app: {
    id: number;
    name: string;
    type: string;
    price: number;
    description: string;
    developerId: number;
  }) => Promise<void>;

  removeDownload: (applicationId: number) => void;

  isDownloaded: (applicationId: number) => boolean;

  getDownloadedApp: (applicationId: number) => DownloadedApp | undefined;

  getDownloadStats: () => {
    totalDownloads: number;
    totalSpent: number;
    last30Days: number;
    favoriteType: string;
  };

  clearError: () => void;
}

export const useDownloadsStore = create<DownloadsState>()(
  persist(
    (set, get) => ({
      downloadedApps: [],
      isLoading: false,
      error: null,

      addDownload: async app => {
        set({ isLoading: true, error: null });
        try {
          await new Promise(resolve => setTimeout(resolve, 500));

          const downloadedApp: DownloadedApp = {
            id: Date.now(),
            applicationId: app.id,
            name: app.name,
            type: app.type,
            price: app.price,
            description: app.description,
            downloadedAt: new Date().toISOString(),
            developerId: app.developerId,
          };

          set(state => ({
            downloadedApps: [...state.downloadedApps, downloadedApp],
            isLoading: false,
          }));
        } catch (error) {
          set({
            error: 'Failed to add download',
            isLoading: false,
          });
          throw error;
        }
      },

      removeDownload: (applicationId: number) => {
        set(state => ({
          downloadedApps: state.downloadedApps.filter(app => app.applicationId !== applicationId),
        }));
      },

      isDownloaded: (applicationId: number) => {
        return get().downloadedApps.some(app => app.applicationId === applicationId);
      },

      getDownloadedApp: (applicationId: number) => {
        return get().downloadedApps.find(app => app.applicationId === applicationId);
      },

      getDownloadStats: () => {
        const downloads = get().downloadedApps;
        const now = new Date();
        const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);

        const last30Days = downloads.filter(
          app => new Date(app.downloadedAt) >= thirtyDaysAgo
        ).length;

        const typeCounts: Record<string, number> = {};
        downloads.forEach(app => {
          typeCounts[app.type] = (typeCounts[app.type] || 0) + 1;
        });

        const favoriteType =
          Object.entries(typeCounts).sort(([, a], [, b]) => b - a)[0]?.[0] || 'N/A';

        return {
          totalDownloads: downloads.length,
          totalSpent: downloads.reduce((sum, app) => sum + app.price, 0),
          last30Days,
          favoriteType,
        };
      },

      clearError: () => set({ error: null }),
    }),
    {
      name: 'downloads-storage',
    }
  )
);
