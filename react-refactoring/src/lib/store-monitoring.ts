import { useAuthStore, useApplicationsStore, useDownloadsStore } from '../store';
import * as Sentry from '@sentry/react';

export const setupStoreMonitoring = () => {
  console.log('🔍 Setting up store monitoring...');

  let authUpdateCount = 0;
  let appsUpdateCount = 0;
  let downloadsUpdateCount = 0;

  const unsubscribeAuth = useAuthStore.subscribe(
    (state) => state,
    (state, prevState) => {
      authUpdateCount++;
      
      if (state.user !== prevState.user) {
        Sentry.setUser({
          id: state.user?.userId?.toString(),
          username: state.user?.username,
          email: state.user?.email,
          role: state.user?.role,
        });
        
        Sentry.addBreadcrumb({
          category: 'auth',
          message: `User changed: ${state.user?.username || 'logged out'}`,
          level: 'info',
          data: {
            previousUser: prevState.user?.username,
            newUser: state.user?.username,
            role: state.user?.role
          }
        });
      }
      
      if (authUpdateCount % 10 === 0) {
        Sentry.metrics.gauge('auth_store_updates', authUpdateCount);
      }
    }
  );

  let lastFetchTime = 0;
  const unsubscribeApps = useApplicationsStore.subscribe(
    (state) => state.isLoading,
    (isLoading) => {
      appsUpdateCount++;
      
      if (isLoading) {
        lastFetchTime = Date.now();
      } else {
        const fetchDuration = Date.now() - lastFetchTime;
        Sentry.metrics.distribution('applications_store_fetch_duration', fetchDuration);
        
        if (fetchDuration > 2000) {
          console.warn(`⚠️ Applications store fetch took ${fetchDuration}ms`);
          
          Sentry.captureMessage(`Slow applications fetch: ${fetchDuration}ms`, {
            level: 'warning',
            tags: { store: 'applications' }
          });
        }
      }
      
      if (appsUpdateCount % 10 === 0) {
        Sentry.metrics.gauge('applications_store_updates', appsUpdateCount);
      }
    }
  );

  const unsubscribeDownloads = useDownloadsStore.subscribe(
    (state) => state.downloadedApps.length,
    (downloadCount, prevCount) => {
      downloadsUpdateCount++;
      
      if (downloadCount !== prevCount) {
        Sentry.addBreadcrumb({
          category: 'downloads',
          message: `Downloads changed: ${prevCount} → ${downloadCount}`,
          level: 'info',
          data: { count: downloadCount }
        });
      }
      
      Sentry.metrics.gauge('downloads_count', downloadCount);
      
      if (downloadsUpdateCount % 10 === 0) {
        Sentry.metrics.gauge('downloads_store_updates', downloadsUpdateCount);
      }
    }
  );

  return () => {
    unsubscribeAuth();
    unsubscribeApps();
    unsubscribeDownloads();
  };
};