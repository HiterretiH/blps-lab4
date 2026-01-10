import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../../store/auth.store';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRoles?: string[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRoles = [] }) => {
  const { isAuthenticated, user, checkAuth } = useAuthStore();
  const [isChecking, setIsChecking] = useState(true);
  const [shouldRedirect, setShouldRedirect] = useState(false);

  useEffect(() => {
    const checkAccess = async () => {
      console.log('🔐 ProtectedRoute проверка:', {
        isAuthenticated,
        user,
        requiredRoles,
      });

      if (!isAuthenticated || !user) {
        console.log('⚠️ Store не авторизован, проверяем localStorage...');

        const token = localStorage.getItem('auth_token');
        const userStr = localStorage.getItem('user');

        if (token && userStr) {
          try {
            console.log('🔄 Найдены данные в localStorage, синхронизируем store...');
            await checkAuth();
          } catch (error) {
            console.error('Ошибка синхронизации:', error);
          }
        } else {
          console.log('❌ Нет данных в localStorage, редирект на /login');
          setShouldRedirect(true);
        }
      } else {
        if (requiredRoles.length > 0 && user) {
          const hasRequiredRole = requiredRoles.includes(user.role);
          console.log('👤 Проверка роли:', {
            userRole: user.role,
            requiredRoles,
            hasRequiredRole,
          });

          if (!hasRequiredRole) {
            console.log('🚫 Нет прав, редирект на /dashboard');
            setShouldRedirect(true);
          }
        }
      }

      setIsChecking(false);
    };

    checkAccess();
  }, [isAuthenticated, user, requiredRoles, checkAuth]);

  if (isChecking) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (shouldRedirect) {
    const redirectTo = isAuthenticated ? '/dashboard' : '/login';
    console.log(`🔄 Редирект на: ${redirectTo}`);
    return <Navigate to={redirectTo} replace />;
  }

  console.log('✅ Доступ разрешен');
  return <>{children}</>;
};
