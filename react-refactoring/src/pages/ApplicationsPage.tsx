import React, { useEffect, useCallback, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppCard } from '../components/apps/AppCard';
import { CreateAppForm } from '../components/apps/CreateAppForm';
import { Button } from '../components/ui/Button';
import { Alert } from '../components/ui/Alert';
import { Card, CardContent } from '../components/ui/Card';
import { useAuthStore } from '../store/auth.store';
import { useApplicationsStore } from '../store/applications.store';
import { Loader2, Package, User, Filter, Download, DollarSign } from 'lucide-react';

interface AppData {
  name: string;
  type: string;
  price: number;
  description: string;
}

export const ApplicationsPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, developerId } = useAuthStore();
  const {
    applications,
    monetizedApplications,
    isLoading,
    error,
    fetchMyApplications,
    fetchMonetizedApplications,
    createApplication,
    deleteApplication,
    clearError,
  } = useApplicationsStore();

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [hasFetched, setHasFetched] = useState(false);
  const isDeveloper = user?.role === 'DEVELOPER';

  const loadApplications = useCallback(async () => {
    if (hasFetched || isLoading) return;

    try {
      if (isDeveloper) {
        await fetchMyApplications();
      } else {
        await fetchMonetizedApplications();
      }
      setHasFetched(true);
    } catch (err) {
      console.error('Failed to load applications:', err);
    }
  }, [isDeveloper, fetchMyApplications, fetchMonetizedApplications, hasFetched, isLoading]);

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    loadApplications();
  }, [user, navigate, loadApplications]);

  useEffect(() => {
    return () => {
      clearError();
    };
  }, [clearError]);

  const handleCreateApp = async (appData: AppData) => {
    try {
      await createApplication(appData);
      await fetchMyApplications();
    } catch (err) {
      console.error('Create app error:', err);
    }
  };

  const handleDeleteApp = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this application?')) {
      try {
        await deleteApplication(id);
        await fetchMyApplications();
      } catch (err) {
        console.error('Delete app error:', err);
      }
    }
  };

  const appsToShow = isDeveloper ? applications : monetizedApplications;

  if (!user) {
    navigate('/login');
    return null;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {isDeveloper ? 'My Applications' : 'Available Applications'}
          </h1>
          <p className="mt-1 text-gray-600">
            {isDeveloper ? 'Manage your applications' : 'Browse and purchase applications'}
          </p>
        </div>

        <div className="flex items-center gap-3">
          {isDeveloper ? (
            <Button onClick={() => setIsCreateModalOpen(true)} className="flex items-center gap-2">
              <Package className="h-4 w-4" />
              Create Application
            </Button>
          ) : (
            <Button
              variant="outline"
              onClick={() => navigate('/my-downloads')}
              className="flex items-center gap-2"
            >
              <Download className="h-4 w-4" />
              My Downloads
            </Button>
          )}
        </div>
      </div>

      {/* Статистика */}
      <Card className="bg-gradient-to-r from-primary-50 to-blue-50">
        <CardContent className="p-6">
          <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
            <div className="flex items-center gap-3">
              <div className="rounded-lg bg-primary-100 p-2">
                {isDeveloper ? (
                  <Package className="h-6 w-6 text-primary-600" />
                ) : (
                  <User className="h-6 w-6 text-primary-600" />
                )}
              </div>
              <div>
                <h3 className="font-medium text-gray-900">
                  {isDeveloper ? 'Developer Dashboard' : 'User Dashboard'}
                </h3>
                <div className="mt-1 flex items-center gap-4">
                  <span className="text-sm text-gray-600">
                    Name: <span className="font-medium">{user.username}</span>
                  </span>
                  <span className="text-sm text-gray-600">
                    Role: <span className="font-medium">{user.role}</span>
                  </span>
                  {developerId && (
                    <span className="text-sm text-gray-600">
                      Developer ID: <span className="font-medium">{developerId}</span>
                    </span>
                  )}
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2 rounded-lg border bg-white px-3 py-2">
              <Filter className="h-4 w-4 text-gray-500" />
              <span className="text-sm text-gray-700">
                {isDeveloper ? 'Your Apps' : 'Available Apps'}:{' '}
                <span className="font-semibold">{appsToShow.length}</span>
              </span>
              {!isDeveloper && appsToShow.length > 0 && (
                <>
                  <span className="text-gray-400">|</span>
                  <DollarSign className="h-4 w-4 text-green-500" />
                  <span className="text-sm text-gray-700">
                    Total Value:{' '}
                    <span className="font-semibold text-green-600">
                      ${appsToShow.reduce((sum, app) => sum + app.price, 0).toFixed(2)}
                    </span>
                  </span>
                </>
              )}
            </div>
          </div>
        </CardContent>
      </Card>

      {error && (
        <Alert variant="danger" title="Error">
          {error}
          {error.includes('404') && (
            <div className="mt-2 text-sm">
              <p>Possible API endpoint issues. Check:</p>
              <ul className="mt-1 list-disc pl-5">
                <li>Is backend running on port 727</li>
                <li>Is endpoint /api/applications accessible</li>
                <li>Do you have correct access rights with your token</li>
              </ul>
            </div>
          )}
        </Alert>
      )}

      {isLoading ? (
        <div className="flex flex-col items-center justify-center py-12">
          <Loader2 className="mb-4 h-12 w-12 animate-spin text-primary-600" />
          <p className="text-gray-600">
            {isDeveloper ? 'Loading your applications...' : 'Loading available applications...'}
          </p>
        </div>
      ) : appsToShow.length === 0 ? (
        <Card>
          <CardContent className="py-12">
            <div className="mx-auto max-w-md text-center">
              <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-gray-100">
                {isDeveloper ? (
                  <Package className="h-8 w-8 text-gray-400" />
                ) : (
                  <User className="h-8 w-8 text-gray-400" />
                )}
              </div>

              <h3 className="mb-2 text-lg font-medium text-gray-900">
                {isDeveloper ? 'You have no applications yet' : 'No applications available'}
              </h3>

              <p className="mb-6 text-gray-600">
                {isDeveloper
                  ? 'Create your first application!'
                  : 'There are no applications available yet. Check back later!'}
              </p>

              {isDeveloper ? (
                <Button onClick={() => setIsCreateModalOpen(true)}>
                  Create Your First Application
                </Button>
              ) : (
                <div className="space-y-3">
                  <p className="text-sm text-gray-500">Want to create applications?</p>
                  <Button variant="outline" onClick={() => navigate('/register')}>
                    Register as Developer
                  </Button>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="mb-4 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Package className="h-5 w-5 text-gray-500" />
              <span className="text-sm font-medium text-gray-700">
                {isDeveloper ? 'Your Applications' : 'Available Applications'}
              </span>
              <span className="rounded-full bg-gray-100 px-2 py-1 text-xs text-gray-600">
                {appsToShow.length}
              </span>
            </div>

            {!isDeveloper && appsToShow.length > 0 && (
              <div className="text-sm text-gray-600">Showing {appsToShow.length} applications</div>
            )}
          </div>

          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
            {appsToShow.map(app => (
              <AppCard
                key={app.id}
                application={app}
                onDelete={isDeveloper ? handleDeleteApp : undefined}
                currentDeveloperId={developerId || undefined}
              />
            ))}
          </div>
        </>
      )}

      {isDeveloper && (
        <CreateAppForm
          isOpen={isCreateModalOpen}
          onClose={() => setIsCreateModalOpen(false)}
          onSubmit={handleCreateApp}
          developerId={developerId}
        />
      )}
    </div>
  );
};
