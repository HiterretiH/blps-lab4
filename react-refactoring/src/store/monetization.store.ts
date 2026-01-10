import { create } from 'zustand';
import { MonetizedApplication, InAppPurchase, InAppAdd, ApplicationStats } from '../types';
import { monetizationService } from '../services/monetization.service';
import { purchasesService } from '../services/purchases.service';
import { adsService } from '../services/ads.service';
import { statsService } from '../services/stats.service';

interface CreateMonetizedAppData {
  developerId: number;
  applicationId: number;
  currentBalance?: number;
  revenue?: number;
  downloadRevenue?: number;
  adsRevenue?: number;
  purchasesRevenue?: number;
}

interface CreatePurchaseData {
  [key: string]: unknown;
}

interface CreateAdData {
  monetizedApplicationId: number;
  title: string;
  description: string;
  price: number;
}

interface CreateStatsData {
  [key: string]: unknown;
}

interface UpdateStatsData {
  [key: string]: unknown;
}

interface MonetizationState {
  monetizedApps: MonetizedApplication[];
  purchases: InAppPurchase[];
  ads: InAppAdd[];
  stats: ApplicationStats[];
  selectedMonetizedApp: MonetizedApplication | null;
  isLoading: boolean;
  error: string | null;

  fetchMonetizedApp: (id: number) => Promise<MonetizedApplication | null>;
  createMonetizedApp: (data: CreateMonetizedAppData) => Promise<MonetizedApplication>;

  fetchPurchases: () => Promise<InAppPurchase[]>;
  createPurchases: (data: CreatePurchaseData) => Promise<InAppPurchase[]>;
  linkPurchasesToApp: (appId: number) => Promise<InAppPurchase[]>;

  fetchAds: () => Promise<InAppAdd[]>;
  createAd: (data: CreateAdData) => Promise<InAppAdd>;
  createBulkAds: (ads: CreateAdData[]) => Promise<InAppAdd[]>;
  fetchAdsByApp: (appId: number) => Promise<InAppAdd[]>;

  fetchStats: () => Promise<ApplicationStats[]>;
  createStats: (data: CreateStatsData) => Promise<ApplicationStats>;
  updateStats: (id: number, data: UpdateStatsData) => Promise<ApplicationStats>;

  setSelectedMonetizedApp: (app: MonetizedApplication | null) => void;
  clearError: () => void;
}

export const useMonetizationStore = create<MonetizationState>(set => ({
  monetizedApps: [],
  purchases: [],
  ads: [],
  stats: [],
  selectedMonetizedApp: null,
  isLoading: false,
  error: null,

  fetchMonetizedApp: async id => {
    set({ isLoading: true, error: null });
    try {
      const app = await monetizationService.getMonetizedApplication(id);
      set({ isLoading: false });
      return app;
    } catch (error: unknown) {
      console.error('Failed to fetch monetized application:', error);
      set({ error: 'Failed to fetch monetized application', isLoading: false });
      return null;
    }
  },

  createMonetizedApp: async data => {
    set({ isLoading: true, error: null });
    try {
      const app = await monetizationService.createMonetizedApplication(data);
      set(state => ({
        monetizedApps: [...state.monetizedApps, app],
        isLoading: false,
      }));
      return app;
    } catch (error: unknown) {
      console.error('Failed to create monetized application:', error);
      set({ error: 'Failed to create monetized application', isLoading: false });
      throw error;
    }
  },

  fetchPurchases: async () => {
    set({ isLoading: true, error: null });
    try {
      const purchases = await purchasesService.getAllPurchases();
      set({ purchases, isLoading: false });
      return purchases;
    } catch (error: unknown) {
      console.error('Failed to fetch purchases:', error);
      set({ error: 'Failed to fetch purchases', isLoading: false });
      return [];
    }
  },

  createPurchases: async data => {
    set({ isLoading: true, error: null });
    try {
      const purchases = await purchasesService.createPurchases(data);
      set(state => ({
        purchases: [...state.purchases, ...purchases],
        isLoading: false,
      }));
      return purchases;
    } catch (error: unknown) {
      console.error('Failed to create purchases:', error);
      set({ error: 'Failed to create purchases', isLoading: false });
      throw error;
    }
  },

  linkPurchasesToApp: async appId => {
    set({ isLoading: true, error: null });
    try {
      const purchases = await purchasesService.linkToMonetizedApp(appId);
      set({ isLoading: false });
      return purchases;
    } catch (error: unknown) {
      console.error('Failed to link purchases:', error);
      set({ error: 'Failed to link purchases', isLoading: false });
      throw error;
    }
  },

  fetchAds: async () => {
    set({ isLoading: true, error: null });
    try {
      const ads = await adsService.getAllAds();
      set({ ads, isLoading: false });
      return ads;
    } catch (error: unknown) {
      console.error('Failed to fetch ads:', error);
      set({ error: 'Failed to fetch ads', isLoading: false });
      return [];
    }
  },

  createAd: async data => {
    set({ isLoading: true, error: null });
    try {
      const ad = await adsService.createAd(data);
      set(state => ({
        ads: [...state.ads, ad],
        isLoading: false,
      }));
      return ad;
    } catch (error: unknown) {
      console.error('Failed to create ad:', error);
      set({ error: 'Failed to create ad', isLoading: false });
      throw error;
    }
  },

  createBulkAds: async adsData => {
    set({ isLoading: true, error: null });
    try {
      const ads = await adsService.createBulkAds(adsData);
      set(state => ({
        ads: [...state.ads, ...ads],
        isLoading: false,
      }));
      return ads;
    } catch (error: unknown) {
      console.error('Failed to create bulk ads:', error);
      set({ error: 'Failed to create bulk ads', isLoading: false });
      throw error;
    }
  },

  fetchAdsByApp: async appId => {
    set({ isLoading: true, error: null });
    try {
      const ads = await adsService.getAdsByMonetizedApp(appId);
      set({ isLoading: false });
      return ads;
    } catch (error: unknown) {
      console.error('Failed to fetch ads by app:', error);
      set({ error: 'Failed to fetch ads by app', isLoading: false });
      return [];
    }
  },

  fetchStats: async () => {
    set({ isLoading: true, error: null });
    try {
      const stats = await statsService.getAllStats();
      set({ stats, isLoading: false });
      return stats;
    } catch (error: unknown) {
      console.error('Failed to fetch stats:', error);
      set({ error: 'Failed to fetch stats', isLoading: false });
      return [];
    }
  },

  createStats: async data => {
    set({ isLoading: true, error: null });
    try {
      const stats = await statsService.createStats(data);
      set(state => ({
        stats: [...state.stats, stats],
        isLoading: false,
      }));
      return stats;
    } catch (error: unknown) {
      console.error('Failed to create stats:', error);
      set({ error: 'Failed to create stats', isLoading: false });
      throw error;
    }
  },

  updateStats: async (id, data) => {
    set({ isLoading: true, error: null });
    try {
      const stats = await statsService.updateStats(id, data);
      set(state => ({
        stats: state.stats.map(s => (s.id === id ? stats : s)),
        isLoading: false,
      }));
      return stats;
    } catch (error: unknown) {
      console.error('Failed to update stats:', error);
      set({ error: 'Failed to update stats', isLoading: false });
      throw error;
    }
  },

  setSelectedMonetizedApp: app => {
    set({ selectedMonetizedApp: app });
  },

  clearError: () => {
    set({ error: null });
  },
}));
