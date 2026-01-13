import { startPerformanceSpan } from './sentry';

export const measureComponentRender = (componentName: string) => {
  const startTime = performance.now();
  
  return {
    end: () => {
      const duration = performance.now() - startTime;
      
      if (duration > 100) {
        console.warn(`Slow component render: ${componentName} took ${duration.toFixed(2)}ms`);
      }
      
      return duration;
    },
  };
};

export const withPerformanceMonitor = <P extends object>(
  WrappedComponent: React.ComponentType<P>,
  componentName: string
) => {
  return function WithPerformanceMonitor(props: P) {
    const span = startPerformanceSpan(
      `${componentName}_render`,
      'ui.render'
    );
    
    const renderTime = measureComponentRender(componentName);
    
    try {
      return <WrappedComponent {...props} />;
    } finally {
      const duration = renderTime.end();
      span.setAttribute('duration', duration);
      span.end();
    }
  };
};

export const measureDataFetch = (operationName: string) => {
  const span = startPerformanceSpan(
    `${operationName}_fetch`,
    'data.fetch'
  );

  return {
    success: () => {
      span.setAttribute('status', 'ok');
      span.end();
    },
    error: (error: any) => {
      span.setAttribute('status', 'error');
      span.setAttribute('error.message', error?.message);
      span.end();
    },
  };
};