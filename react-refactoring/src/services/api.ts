import axios from 'axios';

const API_BASE_URL = 'http://localhost:727/api';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

let requestCounter = 0;
console.log(requestCounter);

// Флаг для отладки
const DEBUG_MODE = true;

// Request interceptor
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('auth_token');
    requestCounter++;

    if (DEBUG_MODE) {
      console.group('📤 API Request');
      console.log('URL:', config.url);
      console.log('Method:', config.method?.toUpperCase());
      console.log('Token exists:', !!token);
      console.log('Headers:', config.headers);
      console.groupEnd();
    }

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  error => {
    if (DEBUG_MODE) {
      console.error('❌ API Request Error:', error);
    }
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  response => {
    if (DEBUG_MODE) {
      console.group('📥 API Response Success');
      console.log('URL:', response.config.url);
      console.log('Status:', response.status);
      console.log('Data:', response.data);
      console.groupEnd();
    }
    return response;
  },
  error => {
    if (DEBUG_MODE) {
      requestCounter--;
      console.group('❌ API Response Error');
      console.log('URL:', error.config?.url);
      console.log('Method:', error.config?.method?.toUpperCase());
      console.log('Status:', error.response?.status);
      console.log('Status Text:', error.response?.statusText);
      console.log('Data:', error.response?.data);
      console.log('Headers:', error.response?.headers);
      console.log('Token in localStorage:', localStorage.getItem('auth_token'));
      console.groupEnd();
    }

    // НЕ делаем автоматический редирект при 401
    // Пусть компоненты сами решают что делать
    return Promise.reject(error);
  }
);
