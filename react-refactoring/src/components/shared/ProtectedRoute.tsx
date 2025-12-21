import React, { useEffect, useState } from 'react';
import { Navigate } from 'react-router-dom';
import { authService } from '../../services/auth.service';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRoles?: string[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRoles = [] }) => {
  const [isChecking, setIsChecking] = useState(true);
  const [shouldRedirect, setShouldRedirect] = useState(false);

  useEffect(() => {
    const checkAuth = async () => {
      const isAuthenticated = authService.isAuthenticated();
      const currentUser = authService.getCurrentUser();

      console.log('🔐 ProtectedRoute проверка:', {
        isAuthenticated,
        currentUser,
        requiredRoles,
      });

      if (!isAuthenticated) {
        console.log('❌ Не авторизован, редирект на /login');
        setShouldRedirect(true);
      } else if (requiredRoles.length > 0 && currentUser) {
        const hasRequiredRole = requiredRoles.includes(currentUser.role);
        console.log('👤 Проверка роли:', {
          userRole: currentUser.role,
          requiredRoles,
          hasRequiredRole,
        });

        if (!hasRequiredRole) {
          console.log('🚫 Нет прав, редирект на /dashboard');
          setShouldRedirect(true);
        }
      }

      setIsChecking(false);
    };

    checkAuth();
  }, [requiredRoles]);

  if (isChecking) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (shouldRedirect) {
    const redirectTo = authService.isAuthenticated() ? '/dashboard' : '/login';
    console.log(`🔄 Редирект на: ${redirectTo}`);
    return <Navigate to={redirectTo} replace />;
  }

  console.log('✅ Доступ разрешен');
  return <>{children}</>;
};
