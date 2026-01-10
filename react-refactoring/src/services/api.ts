import axios from 'axios';

const API_BASE_URL = 'http://localhost:727/api';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

const requestCache = new Map<string, { data: any; timestamp: number }>();
const CACHE_DURATION = 30000;

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('auth_token');

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export const cachedApi = {
  get: async (url: string, config?: any): Promise<any> => {
    const cacheKey = JSON.stringify({ url, config });
    const cached = requestCache.get(cacheKey);

    if (cached && Date.now() - cached.timestamp < CACHE_DURATION) {
      console.log('📦 Using cached data for:', url);
      return Promise.resolve({ data: cached.data });
    }

    const response = await api.get(url, config);
    requestCache.set(cacheKey, { data: response.data, timestamp: Date.now() });
    return response;
  },

  post: api.post,
  put: api.put,
  delete: api.delete,

  clearCache: (urlPattern: string) => {
    for (const key of requestCache.keys()) {
      const { url } = JSON.parse(key);
      if (url.includes(urlPattern)) {
        requestCache.delete(key);
      }
    }
  },

  clearAllCache: () => {
    requestCache.clear();
  },
};
