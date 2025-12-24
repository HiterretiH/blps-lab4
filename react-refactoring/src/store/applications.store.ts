import { create } from 'zustand';
import { Application } from '../types';
import { applicationsService } from '../services/applications.service';

interface ApplicationsState {
  applications: Application[];
  selectedApp: Application | null;
  isLoading: boolean;
  error: string | null;
  lastFetchTime: number;
  fetchMyApplications: () => Promise<Application[]>;
  fetchAllApplications: () => Promise<Application[]>;
  createApplication: (data: Omit<Application, 'id'>) => Promise<Application>;
  updateApplication: (id: number, data: Partial<Application>) => Promise<Application>;
  deleteApplication: (id: number) => Promise<void>;
  selectApplication: (app: Application | null) => void;
  clearError: () => void;
}

const FETCH_THROTTLE_MS = 5000;

export const useApplicationsStore = create<ApplicationsState>((set, get) => ({
  applications: [],
  selectedApp: null,
  isLoading: false,
  error: null,
  lastFetchTime: 0,

  fetchMyApplications: async () => {
    const now = Date.now();
    const { lastFetchTime, applications } = get();
    
    // Throttle запросы
    if (now - lastFetchTime < FETCH_THROTTLE_MS && applications.length > 0) {
      console.log('⏱️ Fetch throttled, returning cached data');
      return applications;
    }

    set({ isLoading: true, error: null });
    
    try {
      console.log('📥 Fetching applications...');
      const apps = await applicationsService.getMyApplications();
      set({ 
        applications: apps, 
        isLoading: false,
        lastFetchTime: now 
      });
      console.log(`✅ Loaded ${apps.length} applications`);
      return apps;
    } catch (error) {
      console.error('❌ Fetch error:', error);
      set({ error: 'Failed to fetch applications', isLoading: false });
      return [];
    }
  },

  fetchAllApplications: async () => {
    const now = Date.now();
    const { lastFetchTime } = get();
    
    // Throttle запросы
    if (now - lastFetchTime < FETCH_THROTTLE_MS) {
      console.log('⏱️ Fetch throttled');
      return get().applications;
    }

    set({ isLoading: true, error: null });
    
    try {
      console.log('📥 Fetching all applications...');
      const apps = await applicationsService.getAllApplications();
      set({ 
        applications: apps, 
        isLoading: false,
        lastFetchTime: now 
      });
      console.log(`✅ Loaded ${apps.length} applications`);
      return apps;
    } catch (error) {
      console.error('❌ Fetch error:', error);
      set({ error: 'Failed to fetch applications', isLoading: false });
      return [];
    }
  },

  createApplication: async data => {
    set({ isLoading: true, error: null });
    try {
      const newApp = await applicationsService.createApplication(data);
      set(state => ({
        applications: [...state.applications, newApp],
        isLoading: false,
      }));
      return newApp;
    } catch (error) {
      set({ error: 'Failed to create application', isLoading: false });
      throw error;
    }
  },

  updateApplication: async (id, data) => {
    set({ isLoading: true, error: null });
    try {
      const updatedApp = { ...get().applications.find(app => app.id === id)!, ...data };
      set(state => ({
        applications: state.applications.map(app => (app.id === id ? updatedApp : app)),
        selectedApp: state.selectedApp?.id === id ? updatedApp : state.selectedApp,
        isLoading: false,
      }));
      return updatedApp;
    } catch (error) {
      set({ error: 'Failed to update application', isLoading: false });
      throw error;
    }
  },

  deleteApplication: async id => {
    set({ isLoading: true, error: null });
    try {
      await applicationsService.deleteApplication(id);
      set(state => ({
        applications: state.applications.filter(app => app.id !== id),
        selectedApp: state.selectedApp?.id === id ? null : state.selectedApp,
        isLoading: false,
      }));
    } catch (error) {
      set({ error: 'Failed to delete application', isLoading: false });
      throw error;
    }
  },

  selectApplication: app => {
    set({ selectedApp: app });
  },

  clearError: () => {
    set({ error: null });
  },
}));
