import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface ApiMetric {
  url: string;
  method: string;
  duration: number;
  status: number;
  success: boolean;
  timestamp: string;
}

interface ApiMetricsStore {
  metrics: ApiMetric[];
  lastCleanup: string;
  maxMetrics: number;
  statsLimit: number;
  
  addMetric: (metric: Omit<ApiMetric, 'timestamp'> & { timestamp?: Date }) => void;
  addMetrics: (metrics: ApiMetric[]) => void;
  cleanupOldMetrics: () => void;
  clearAllMetrics: () => void;
  setStatsLimit: (limit: number) => void;
  getStats: (limit?: number) => {
    totalCalls: number;
    avgDuration: number;
    successRate: number;
    recentErrors: number;
    slowCalls: number;
    recentMetrics: any[];
  };
}

export const CLEANUP_INTERVAL = 5 * 60 * 1000;
export const MAX_METRICS = 1000;
export const MAX_METRICS_AGE = 60 * 60 * 1000;
export const DEFAULT_STATS_LIMIT = 100;

export const useApiMetricsStore = create<ApiMetricsStore>()(
  persist(
    (set, get) => ({
      metrics: [],
      lastCleanup: new Date().toISOString(),
      maxMetrics: MAX_METRICS,
      statsLimit: DEFAULT_STATS_LIMIT,
      
      addMetric: (metric) => {
        set((state) => {
          const timestamp = (metric.timestamp || new Date()).toISOString();
          const newMetric: ApiMetric = {
            ...metric,
            timestamp,
          };
          
          const newMetrics = [...state.metrics, newMetric];
          
          if (newMetrics.length > state.maxMetrics) {
            newMetrics.splice(0, newMetrics.length - state.maxMetrics);
          }
          
          const now = new Date();
          const lastCleanupTime = new Date(state.lastCleanup).getTime();
          
          if (now.getTime() - lastCleanupTime > CLEANUP_INTERVAL) {
            const oneHourAgo = new Date(now.getTime() - MAX_METRICS_AGE);
            const filteredMetrics = newMetrics.filter(m => {
              const metricTime = new Date(m.timestamp).getTime();
              return metricTime > oneHourAgo.getTime();
            });
            
            return {
              metrics: filteredMetrics,
              lastCleanup: now.toISOString(),
            };
          }
          
          return { metrics: newMetrics };
        });
      },
      
      addMetrics: (metrics) => {
        set((state) => {
          const newMetrics = [...state.metrics, ...metrics];
          
          if (newMetrics.length > state.maxMetrics) {
            newMetrics.splice(0, newMetrics.length - state.maxMetrics);
          }
          
          return { metrics: newMetrics };
        });
      },
      
      cleanupOldMetrics: () => {
        const now = new Date();
        const oneHourAgo = new Date(now.getTime() - MAX_METRICS_AGE);
        
        set((state) => {
          const filteredMetrics = state.metrics.filter(m => {
            const metricTime = new Date(m.timestamp).getTime();
            return metricTime > oneHourAgo.getTime();
          });
          
          return {
            metrics: filteredMetrics,
            lastCleanup: now.toISOString(),
          };
        });
      },
      
      clearAllMetrics: () => {
        set({
          metrics: [],
          lastCleanup: new Date().toISOString(),
        });
      },
      
      setStatsLimit: (limit) => {
        set({ statsLimit: Math.max(1, Math.min(1000, limit)) });
      },
      
      getStats: (customLimit?: number) => {
        const state = get();
        const limit = customLimit || state.statsLimit || DEFAULT_STATS_LIMIT;
        const recentMetrics = state.metrics.slice(-limit);
        const totalCalls = recentMetrics.length;
        
        const avgDuration = totalCalls > 0 
          ? Math.round(recentMetrics.reduce((sum, m) => sum + m.duration, 0) / totalCalls)
          : 0;
        
        const successCount = recentMetrics.filter(m => m.success).length;
        const successRate = totalCalls > 0
          ? Math.round((successCount / totalCalls) * 100)
          : 100;

        return {
          totalCalls,
          avgDuration,
          successRate,
          recentErrors: recentMetrics.filter(m => !m.success).length,
          slowCalls: recentMetrics.filter(m => m.duration > 1000).length,
          recentMetrics: recentMetrics
        };
      },
    }),
    {
      name: 'api-metrics-storage',
      partialize: (state) => {
        const oneHourAgo = new Date(Date.now() - MAX_METRICS_AGE);
        const recentMetrics = state.metrics.filter(m => {
          const metricTime = new Date(m.timestamp).getTime();
          return metricTime > oneHourAgo.getTime();
        });
        
        return {
          metrics: recentMetrics.slice(-500),
          lastCleanup: state.lastCleanup,
          statsLimit: state.statsLimit,
        };
      },
    }
  )
);