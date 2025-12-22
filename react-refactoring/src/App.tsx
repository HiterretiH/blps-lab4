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
import { UiKitDemoPage } from './pages/UiKitDemoPage'; // Добавляем импорт
import { DebugAuthPage } from './pages/DebugAuthPage';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <Header />
        <main className="container mx-auto px-4 py-8">
          <Routes>
            {/* Public routes */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

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
              path="/analytics"
              element={
                <ProtectedRoute>
                  <AnalyticsPage />
                </ProtectedRoute>
              }
            />

            {/* Developer-only routes */}
            <Route
              path="/developer"
              element={
                <ProtectedRoute requiredRoles={['DEVELOPER']}>
                  <DeveloperDashboardPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/developer/apps"
              element={
                <ProtectedRoute requiredRoles={['DEVELOPER']}>
                  <DeveloperDashboardPage />
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
            <Route path="/debug-auth" element={<DebugAuthPage />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
