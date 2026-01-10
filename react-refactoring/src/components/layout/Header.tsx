import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/auth.store';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { Home, Package, BarChart3, Shield, LogOut, User, DollarSign, Download } from 'lucide-react';

export const Header: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, isAuthenticated, logout } = useAuthStore();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path: string) => location.pathname === path;

  const getRoleColor = (role: string) => {
    switch (role) {
      case 'DEVELOPER':
        return 'primary';
      case 'PRIVACY_POLICY':
        return 'danger';
      default:
        return 'info';
    }
  };

  return (
    <header className="sticky top-0 z-50 border-b border-gray-200 bg-white/95 shadow-sm backdrop-blur supports-[backdrop-filter]:bg-white/60">
      <div className="container mx-auto px-4">
        <div className="flex h-16 items-center justify-between">
          {/* Logo and Project Info */}
          <div className="flex items-center space-x-4">
            <Link to="/" className="flex items-center space-x-2">
              <div className="rounded-lg bg-gradient-to-br from-primary-500 to-primary-600 p-2">
                <Package className="h-5 w-5 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-bold text-gray-900">BLPS Platform</h1>
                <p className="text-xs text-gray-600">Monetization System</p>
              </div>
            </Link>
          </div>

          {/* Navigation */}
          {isAuthenticated && user && (
            <nav className="hidden items-center space-x-1 md:flex">
              <Link to="/applications">
                <Button
                  variant={isActive('/applications') ? 'primary' : 'outline'}
                  size="sm"
                  className="flex items-center space-x-2"
                >
                  <Package className="h-4 w-4" />
                  <span>Applications</span>
                </Button>
              </Link>
              {user.role === 'USER' && (
                <Link to="/my-downloads">
                  <Button
                    variant={isActive('/my-downloads') ? 'primary' : 'outline'}
                    size="sm"
                    className="flex items-center space-x-2"
                  >
                    <Download className="h-4 w-4" />
                    <span>My Downloads</span>
                  </Button>
                </Link>
              )}
              ,
              {user.role === 'DEVELOPER' && (
                <>
                  <Link to="/developer/profile">
                    <Button
                      variant={isActive('/developer/profile') ? 'primary' : 'outline'}
                      size="sm"
                      className="flex items-center space-x-2"
                    >
                      <DollarSign className="h-4 w-4" />
                      <span>Developer</span>
                    </Button>
                  </Link>
                </>
              )}
              {user.role === 'PRIVACY_POLICY' && (
                <Link to="/admin">
                  <Button
                    variant={isActive('/admin') ? 'primary' : 'outline'}
                    size="sm"
                    className="flex items-center space-x-2"
                  >
                    <Shield className="h-4 w-4" />
                    <span>Admin</span>
                  </Button>
                </Link>
              )}
            </nav>
          )}

          {/* User Actions */}
          <div className="flex items-center space-x-3">
            {isAuthenticated && user ? (
              <>
                <div className="flex items-center space-x-2">
                  <div className="rounded-lg bg-gray-100 p-2">
                    <User className="h-4 w-4 text-gray-600" />
                  </div>
                  <div className="hidden sm:block">
                    <p className="text-sm font-medium text-gray-900">{user.username}</p>
                    <div className="flex items-center space-x-1">
                      <Badge variant={getRoleColor(user.role)} className="text-xs">
                        {user.role?.replace('_', ' ')}
                      </Badge>
                    </div>
                  </div>
                </div>

                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleLogout}
                  className="flex items-center space-x-2"
                >
                  <LogOut className="h-4 w-4" />
                  <span className="hidden sm:inline">Logout</span>
                </Button>
              </>
            ) : (
              <>
                <Link to="/login">
                  <Button variant="outline" size="sm">
                    Login
                  </Button>
                </Link>
                <Link to="/register">
                  <Button variant="primary" size="sm">
                    Register
                  </Button>
                </Link>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Mobile Navigation */}
      {isAuthenticated && user && (
        <div className="border-t border-gray-200 md:hidden">
          <div className="container mx-auto px-4 py-2">
            <div className="flex items-center justify-around">
              <Link to="/dashboard" className="flex flex-col items-center">
                <Home
                  className={`h-5 w-5 ${isActive('/dashboard') ? 'text-primary-600' : 'text-gray-500'}`}
                />
                <span
                  className={`mt-1 text-xs ${isActive('/dashboard') ? 'font-medium text-primary-600' : 'text-gray-600'}`}
                >
                  Home
                </span>
              </Link>

              <Link to="/applications" className="flex flex-col items-center">
                <Package
                  className={`h-5 w-5 ${isActive('/applications') ? 'text-primary-600' : 'text-gray-500'}`}
                />
                <span
                  className={`mt-1 text-xs ${isActive('/applications') ? 'font-medium text-primary-600' : 'text-gray-600'}`}
                >
                  Apps
                </span>
              </Link>

              {/* Добавляем в мобильное меню */}
              <Link to="/my-downloads" className="flex flex-col items-center">
                <Download
                  className={`h-5 w-5 ${isActive('/my-downloads') ? 'text-primary-600' : 'text-gray-500'}`}
                />
                <span
                  className={`mt-1 text-xs ${isActive('/my-downloads') ? 'font-medium text-primary-600' : 'text-gray-600'}`}
                >
                  Downloads
                </span>
              </Link>

              <Link to="/analytics" className="flex flex-col items-center">
                <BarChart3
                  className={`h-5 w-5 ${isActive('/analytics') ? 'text-primary-600' : 'text-gray-500'}`}
                />
                <span
                  className={`mt-1 text-xs ${isActive('/analytics') ? 'font-medium text-primary-600' : 'text-gray-600'}`}
                >
                  Analytics
                </span>
              </Link>

              {user.role === 'DEVELOPER' && (
                <Link to="/developer/profile" className="flex flex-col items-center">
                  <DollarSign
                    className={`h-5 w-5 ${isActive('/developer/profile') ? 'text-primary-600' : 'text-gray-500'}`}
                  />
                  <span
                    className={`mt-1 text-xs ${isActive('/developer/profile') ? 'font-medium text-primary-600' : 'text-gray-600'}`}
                  >
                    Dev
                  </span>
                </Link>
              )}
            </div>
          </div>
        </div>
      )}
    </header>
  );
};
