import { useEffect } from 'react';
import { useLocation, useNavigationType } from 'react-router-dom';
import * as Sentry from '@sentry/react';

export const useNavigationMonitoring = () => {
  const location = useLocation();
  const navType = useNavigationType();

  useEffect(() => {
    const transaction = Sentry.startTransaction({
      name: `route_${location.pathname}`,
      op: 'navigation',
    });

    const pageLoadStart = performance.now();
    let isPageLoaded = false;

    const handleLoad = () => {
      if (!isPageLoaded) {
        isPageLoaded = true;
        const loadTime = performance.now() - pageLoadStart;
        
        transaction.setMeasurement('page_load_time', loadTime, 'millisecond');
        transaction.setTag('route', location.pathname);
        transaction.setTag('navigation_type', navType);
        transaction.finish();
        
        Sentry.metrics.distribution('page_load_time', loadTime, {
          tags: { route: location.pathname }
        });
        
        console.log(`📈 Page load time for ${location.pathname}: ${loadTime.toFixed(2)}ms`);
        
        const key = 'page-load-history';
        let history = JSON.parse(localStorage.getItem(key) || '[]');
        history.push({
          route: location.pathname,
          loadTime,
          timestamp: Date.now(),
          navigationType: navType
        });
        
        if (history.length > 50) history.shift();
        localStorage.setItem(key, JSON.stringify(history));
      }
    };

    window.addEventListener('load', handleLoad, { once: true });

    const timeoutId = setTimeout(() => {
      if (!isPageLoaded) {
        handleLoad();
      }
    }, 5000);

    return () => {
      window.removeEventListener('load', handleLoad);
      clearTimeout(timeoutId);
      if (!isPageLoaded) {
        transaction.finish();
      }
    };
  }, [location.pathname, navType]);

  useEffect(() => {
    console.log(`📍 Navigation: ${navType} to ${location.pathname}`);
    
    Sentry.addBreadcrumb({
      category: 'navigation',
      message: `Navigated to ${location.pathname}`,
      level: 'info',
      data: {
        path: location.pathname,
        search: location.search,
        navigationType: navType,
        timestamp: new Date().toISOString()
      }
    });

    Sentry.metrics.increment('navigation_count', 1, {
      tags: { route: location.pathname }
    });

  }, [location, navType]);
};