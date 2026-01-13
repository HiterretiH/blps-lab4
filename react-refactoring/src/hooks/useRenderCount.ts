import { useEffect, useRef } from 'react';
import { captureApiError } from '../lib/sentry';

export const useRenderCount = (componentName: string) => {
  const renderCount = useRef(0);
  const lastRenderTime = useRef(Date.now());

  useEffect(() => {
    renderCount.current += 1;
    const now = Date.now();
    const timeSinceLastRender = now - lastRenderTime.current;
    lastRenderTime.current = now;

    const key = 'component-render-counts';
    let counts = JSON.parse(localStorage.getItem(key) || '{}');
    counts[componentName] = renderCount.current;
    localStorage.setItem(key, JSON.stringify(counts));

    if (timeSinceLastRender < 50 && renderCount.current > 10) {
      console.warn(`⚠️ Component "${componentName}" is re-rendering too frequently`);
      
      captureApiError(new Error('Excessive re-renders'), {
        tags: { component: componentName, type: 'performance' },
        extra: { 
          renderCount: renderCount.current, 
          timeSinceLastRender,
          timestamp: new Date().toISOString()
        }
      });
    }

    if (renderCount.current % 10 === 0) {
      import('../lib/sentry').then(({ Sentry }) => {
        Sentry.metrics.gauge(`${componentName}_render_count`, renderCount.current);
      });
    }

    return () => {
      if (counts[componentName]) {
        delete counts[componentName];
        localStorage.setItem(key, JSON.stringify(counts));
      }
    };
  });

  return renderCount.current;
};