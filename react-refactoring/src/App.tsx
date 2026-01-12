import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Header } from './components/layout/Header';
import { ProtectedRoute } from './components/shared/ProtectedRoute';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { DashboardPage } from './pages/DashboardPage';
import { ApplicationsPage } from './pages/ApplicationsPage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { DeveloperDashboardPage } from './pages/DeveloperDashboardPage';
import { AdminPanelPage } from './pages/AdminPanelPage';
import { UiKitDemoPage } from './pages/UiKitDemoPage';
import { DebugAuthPage } from './pages/DebugAuthPage';
import { DeveloperProfilePage } from './pages/DeveloperProfilePage.tsx';
import { MonetizationPage } from './pages/MonetizationPage';
import { useEffect } from 'react';
import { useAuthStore } from './store/auth.store';
import { UserDownloadsPage } from './pages/UserDownloadsPage.tsx';
import { DownloadAppPage } from './pages/DownloadAppPage.tsx';
import { SentryErrorBoundary } from './lib/sentry.ts';
import { ToastContainer } from './components/ui/ToastContainer.tsx';

function ErrorFallback() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center">
      <h1 className="text-2xl font-bold text-red-600">Something went wrong</h1>
      <p className="mt-2 text-gray-600">We are already working on it</p>
    </div>
  );
}

function App() {
  const { checkAuth } = useAuthStore();

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  return (
    <SentryErrorBoundary fallback={<ErrorFallback />}>
    <Router>
      <div className="min-h-screen bg-gray-50">
        <Header />
        <main className="container mx-auto px-4 py-8">
          <Routes>
            {/* Public routes */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/debug-auth" element={<DebugAuthPage />} />

            {/* Protected routes for all authenticated users */}
            <Route
              path="/"
              element={
                <ProtectedRoute>
                  <DashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <DashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/applications"
              element={
                <ProtectedRoute>
                  <ApplicationsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/applications/:id"
              element={
                <ProtectedRoute>
                  <ApplicationsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/analytics"
              element={
                <ProtectedRoute>
                  <AnalyticsPage />
                </ProtectedRoute>
              }
            />

            {/* Developer routes */}
            <Route
              path="/developer"
              element={
                <ProtectedRoute requiredRoles={['DEVELOPER']}>
                  <DeveloperDashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/developer/profile"
              element={
                <ProtectedRoute requiredRoles={['DEVELOPER']}>
                  <DeveloperProfilePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/developer/apps"
              element={
                <ProtectedRoute requiredRoles={['DEVELOPER']}>
                  <ApplicationsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/monetization/:id"
              element={
                <ProtectedRoute requiredRoles={['DEVELOPER', 'PRIVACY_POLICY']}>
                  <MonetizationPage />
                </ProtectedRoute>
              }
            />

            {/* Admin-only routes */}
            <Route
              path="/admin"
              element={
                <ProtectedRoute requiredRoles={['PRIVACY_POLICY']}>
                  <AdminPanelPage />
                </ProtectedRoute>
              }
            />

            {/* UI Kit Demo - доступно всем авторизованным */}
            <Route
              path="/ui-kit"
              element={
                <ProtectedRoute>
                  <UiKitDemoPage />
                </ProtectedRoute>
              }
            />

            <Route
              path="/my-downloads"
              element={
                <ProtectedRoute>
                  <UserDownloadsPage />
                </ProtectedRoute>
              }
            />

            <Route
              path="/download-app/:id"
              element={
                <ProtectedRoute>
                  <DownloadAppPage />
                </ProtectedRoute>
              }
            />

            {/* 404 route */}
            <Route
              path="*"
              element={
                <div className="flex min-h-[60vh] flex-col items-center justify-center">
                  <h1 className="text-4xl font-bold text-gray-900">404</h1>
                  <p className="mt-2 text-gray-600">Page not found</p>
                </div>
              }
            />
          </Routes>
        </main>
      </div>
    </Router>
    <ToastContainer />
    </SentryErrorBoundary>
  );
}

export default App;
