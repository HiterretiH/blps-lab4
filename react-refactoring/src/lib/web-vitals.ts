import { onCLS, onINP, onLCP, onFCP, onTTFB } from 'web-vitals';
import * as Sentry from '@sentry/react';

interface Metric {
  name: string;
  value: number;
  rating: 'good' | 'needs-improvement' | 'poor';
}

export const trackWebVitals = () => {
  console.log('🚀 Initializing Web Vitals tracking...');

  const captureMetric = (metric: Metric) => {
    const { name, value, rating } = metric;
    
    const key = 'web-vitals-history';
    let history = JSON.parse(localStorage.getItem(key) || '{}');
    
    const normalizedName = name === 'INP' ? 'FID' : name;
    
    if (!history[normalizedName]) {
      history[normalizedName] = [];
    }
    
    history[normalizedName].push({
      value,
      rating,
      timestamp: Date.now(),
      url: window.location.href,
      userAgent: navigator.userAgent
    });
    
    if (history[normalizedName].length > 100) {
      history[normalizedName] = history[normalizedName].slice(-100);
    }
    
    localStorage.setItem(key, JSON.stringify(history));
    
    window.dispatchEvent(new CustomEvent('web-vitals-update', {
      detail: { name: normalizedName, value }
    }));
    
    Sentry.metrics.distribution(`web_vital_${normalizedName.toLowerCase()}`, value, {
      tags: { rating }
    });
    
    console.log(`📊 Web Vitals - ${normalizedName}: ${value.toFixed(2)} (${rating})`);
    
    const thresholds: Record<string, { warning: number; critical: number }> = {
      CLS: { warning: 0.1, critical: 0.25 },
      FID: { warning: 100, critical: 300 },
      LCP: { warning: 2500, critical: 4000 },
      FCP: { warning: 1800, critical: 3000 },
      TTFB: { warning: 800, critical: 1800 },
      INP: { warning: 200, critical: 500 }
    };
    
    const threshold = thresholds[normalizedName];
    if (threshold) {
      if (value >= threshold.critical) {
        console.error(`❌ Critical Web Vital: ${normalizedName} = ${value.toFixed(2)}`);
        Sentry.captureMessage(`Critical Web Vital: ${normalizedName} = ${value.toFixed(2)}`, {
          level: 'error',
          tags: { metric: normalizedName }
        });
      } else if (value >= threshold.warning) {
        console.warn(`⚠️  Warning Web Vital: ${normalizedName} = ${value.toFixed(2)}`);
        Sentry.captureMessage(`Warning Web Vital: ${normalizedName} = ${value.toFixed(2)}`, {
          level: 'warning',
          tags: { metric: normalizedName }
        });
      }
    }
  };

  onCLS(captureMetric);
  onINP(captureMetric);
  onLCP(captureMetric);
  onFCP(captureMetric);
  onTTFB(captureMetric);

  if ('PerformanceObserver' in window) {
    try {
      const observer = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          if (entry.duration > 50) {
            console.warn(`⏱️ Long task detected: ${entry.duration.toFixed(2)}ms`);
            
            Sentry.metrics.distribution('long_task_duration', entry.duration);
            
            localStorage.setItem(
              'last-long-task',
              JSON.stringify({
                duration: entry.duration,
                timestamp: Date.now(),
                url: window.location.href
              })
            );
          }
        }
      });
      
      observer.observe({ entryTypes: ['longtask'] });
    } catch (e) {
      console.log('Long Task API not supported');
    }

    try {
      const layoutShiftObserver = new PerformanceObserver((list) => {
        for (const entry of list.getEntries()) {
          const shiftEntry = entry as LayoutShift;
          if (shiftEntry.hadRecentInput) return;
          
          console.log(`📐 Layout shift: ${shiftEntry.value.toFixed(3)}`);
        }
      });
      
      layoutShiftObserver.observe({ entryTypes: ['layout-shift'] });
    } catch (e) {
      console.log('Layout Shift API not supported');
    }
  }
};

export const getWebVitalsHistory = () => {
  try {
    const key = 'web-vitals-history';
    const history = JSON.parse(localStorage.getItem(key) || '{}');
    return history;
  } catch (error) {
    console.error('Error getting Web Vitals history:', error);
    return {};
  }
};

export const clearWebVitalsHistory = () => {
  localStorage.removeItem('web-vitals-history');
  localStorage.removeItem('web-vitals');
  localStorage.removeItem('web_vitals_history');
  console.log('🧹 Web Vitals history cleared');
};