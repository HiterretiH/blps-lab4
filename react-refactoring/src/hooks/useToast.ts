import { useState, useCallback } from 'react';

interface Toast {
  id: string;
  title: string;
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
}

export const useToast = () => {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const toast = useCallback((type: Toast['type'], title: string, message?: string) => {
    const id = Date.now().toString();
    setToasts(prev => [...prev, { id, title, message: message || title, type }]);
    
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 5000);
  }, []);

  const removeToast = useCallback((id: string) => {
    setToasts(prev => prev.filter(t => t.id !== id));
  }, []);

  return {
    toasts,
    toast: {
      success: (title: string, message?: string) => toast('success', title, message),
      error: (title: string, message?: string) => toast('error', title, message),
      info: (title: string, message?: string) => toast('info', title, message),
      warning: (title: string, message?: string) => toast('warning', title, message),
    },
    removeToast,
  };
};