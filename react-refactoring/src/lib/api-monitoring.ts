import axios, { AxiosError, AxiosResponse, AxiosInstance } from 'axios';
import { captureApiError } from './sentry';
import { useApiMetricsStore } from '@/store/metrics.store';

interface ApiMetrics {
  url: string;
  method: string;
  duration: number;
  status: number;
  success: boolean;
  timestamp: Date;
}

const CLEANUP_INTERVAL = 5 * 60 * 1000;

let apiMetricsStore: ReturnType<typeof useApiMetricsStore.getState>;

let broadcastChannel: BroadcastChannel | null = null;

export const setupApiMonitoring = (axiosInstance?: AxiosInstance) => {
  console.log('🔧 Setting up API monitoring with Zustand store...');
  
  if (!apiMetricsStore) {
    apiMetricsStore = useApiMetricsStore.getState();
    
    setInterval(() => {
      apiMetricsStore.cleanupOldMetrics();
      console.log('🧹 Auto-cleanup: Removed old API metrics');
    }, CLEANUP_INTERVAL);
  }

  if ('BroadcastChannel' in window) {
    try {
      broadcastChannel = new BroadcastChannel('api-metrics-channel');
    } catch (error) {
      console.warn('BroadcastChannel не поддерживается:', error);
    }
  }
  
  const instance = axiosInstance || axios;
  
  instance.interceptors.request.use((config) => {
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    (config as any).metadata = { 
      startTime: performance.now(),
      url: config.url,
      method: config.method
    };
    
    console.log(`📤 API Request: ${config.method?.toUpperCase()} ${config.url}`);
    return config;
  }, (error) => {
    console.error('❌ API Request error:', error);
    return Promise.reject(error);
  });

  instance.interceptors.response.use(
    (response: AxiosResponse) => {
      const endTime = performance.now();
      const startTime = (response.config as any).metadata?.startTime;
      const duration = startTime ? Math.round(endTime - startTime) : 0;
      
      const url = response.config.url || '';
      const method = response.config.method?.toUpperCase() || 'GET';

      const metric = {
      url,
      method,
      duration,
      status: response.status,
      success: response.status >= 200 && response.status < 300,
    };

      apiMetricsStore.addMetric(metric);

      if (broadcastChannel) {
        try {
          broadcastChannel.postMessage({
            type: 'METRICS_UPDATED',
            timestamp: new Date().toISOString(),
            metric: metric
          });
        } catch (error) {
          console.warn('Не удалось отправить сообщение через BroadcastChannel:', error);
        }
      }
      
      if (duration > 1000) {
        console.warn(`🐌 Slow API: ${method} ${url} took ${duration}ms`);
      }
      
      console.log(`📥 API Response: ${method} ${url} - ${duration}ms - ${response.status}`);
      
      return response;
    },
    (error: AxiosError) => {
      const endTime = performance.now();
      const startTime = (error.config as any)?.metadata?.startTime;
      const duration = startTime ? Math.round(endTime - startTime) : 0;
      
      const url = error.config?.url || '';
      const method = error.config?.method?.toUpperCase() || 'GET';
      const status = error.response?.status || 0;

      const errorInfo = {
        url,
        method,
        status,
        duration,
        message: error.message,
        responseData: error.response?.data,
      };

      const metric: ApiMetrics = {
        url,
        method,
        duration,
        status,
        success: false,
        timestamp: new Date(),
      };
      
      apiMetricsStore.addMetric(metric);

      if (broadcastChannel) {
        try {
          broadcastChannel.postMessage({
            type: 'METRICS_UPDATED',
            timestamp: new Date().toISOString(),
            metric: metric
          });
        } catch (error) {
          console.warn('Не удалось отправить сообщение через BroadcastChannel:', error);
        }
      }

      console.error(`❌ API Error: ${method} ${url} - ${duration}ms - ${status}`, error.message);

      if (status === 401) {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user');
        window.location.href = '/login';
      }

      if (status >= 500) {
        captureApiError(error, {
          tags: {
            api_endpoint: url,
            http_status: status.toString(),
          },
          extra: errorInfo,
        });
      }

      return Promise.reject(error);
    }
  );
  
  console.log('✅ API monitoring setup complete');
};

export const getApiStats = (limit?: number) => {
  if (!apiMetricsStore) {
    apiMetricsStore = useApiMetricsStore.getState();
  }
  return apiMetricsStore.getStats(limit);
};

export const clearApiMetrics = () => {
  if (!apiMetricsStore) {
    apiMetricsStore = useApiMetricsStore.getState();
  }
  apiMetricsStore.clearAllMetrics();
  console.log('🧹 API metrics cleared from Zustand store');
};

export const getAllApiMetrics = () => {
  if (!apiMetricsStore) {
    apiMetricsStore = useApiMetricsStore.getState();
  }
  return apiMetricsStore.metrics;
};

export { useApiMetricsStore };