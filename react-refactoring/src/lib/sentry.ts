import * as Sentry from '@sentry/react';
import { BrowserTracing } from '@sentry/tracing';
import { getCurrentHub } from '@sentry/react';

const initSentry = () => {
  const dsn = import.meta.env.VITE_SENTRY_DSN;
  
  if (!dsn) {
    console.warn('Sentry DSN не найден. Отключен в development режиме.');
    return;
  }

  Sentry.init({
    dsn,
    environment: import.meta.env.MODE || 'development',
    release: `app@${import.meta.env.VITE_APP_VERSION || '1.0.0'}`,
    
    integrations: [
      Sentry.browserTracingIntegration(),
    ],
    
    tracesSampleRate: import.meta.env.MODE === 'production' ? 0.2 : 1.0,
    
    maxBreadcrumbs: 50,
    attachStacktrace: true,
    
    beforeSend(event) {
      if (event.request) {
        delete event.request.cookies;
        delete event.request.headers?.['Authorization'];
        delete event.request.headers?.['Cookie'];
      }
      
      if (event.exception?.values?.[0]?.value?.includes('ResizeObserver')) {
        return null;
      }
      
      return event;
    },
    
    beforeBreadcrumb(breadcrumb) {
      if (breadcrumb.category === 'console') {
        return null;
      }
      return breadcrumb;
    },
  });
};

export const setSentryUser = (user: { id?: string; username?: string; email?: string; role?: string }) => {
  Sentry.setUser({
    id: user.id?.toString(),
    username: user.username,
    email: user.email,
    role: user.role,
  });
};

export const clearSentryUser = () => {
  Sentry.setUser(null);
};

export const captureApiError = (error: any, context: Record<string, any> = {}) => {
  Sentry.captureException(error, {
    tags: {
      type: 'api_error',
      ...context.tags,
    },
    extra: {
      ...context.extra,
      timestamp: new Date().toISOString(),
    },
  });
};

export const captureComponentError = (error: Error, componentName: string, props?: any) => {
  Sentry.captureException(error, {
    tags: {
      component: componentName,
      type: 'component_error',
    },
    extra: {
      props: props ? JSON.stringify(props) : undefined,
      location: window.location.href,
    },
  });
};

export const startPerformanceSpan = (
  name: string,
  op: string = 'ui.render'
) => {
  return Sentry.startSpan(
    {
      name,
      op,
    },
    span => span
  );
};

initSentry();

export { Sentry };
export const SentryErrorBoundary = Sentry.ErrorBoundary;