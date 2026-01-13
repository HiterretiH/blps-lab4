export const env = {
  NODE_ENV: process.env.NODE_ENV || 'development',
  SENTRY_DSN: process.env.REACT_APP_SENTRY_DSN || '',
  VERSION: process.env.REACT_APP_VERSION || '1.0.0',
  
  ENABLE_PERFORMANCE_METRICS: process.env.REACT_APP_ENABLE_PERFORMANCE_METRICS === 'true',
};