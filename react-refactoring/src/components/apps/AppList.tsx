import React from 'react';
import { Application } from '../../types';
import { AppCard } from './AppCard';
import { Alert } from '../ui/Alert';
import { Loader2 } from 'lucide-react';

interface AppListProps {
  applications: Application[];
  isLoading?: boolean;
  error?: string;
  onAppSelect?: (app: Application) => void;
  onAppDelete?: (id: number) => void;
  showActions?: boolean;
}

export const AppList: React.FC<AppListProps> = ({
  applications,
  isLoading = false,
  error,
  onAppSelect,
  onAppDelete,
  showActions = true,
}) => {
  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center py-12">
        <Loader2 className="mb-4 h-12 w-12 animate-spin text-primary-600" />
        <p className="text-gray-600">Loading applications...</p>
      </div>
    );
  }

  if (error) {
    return (
      <Alert variant="danger" title="Error">
        {error}
      </Alert>
    );
  }

  if (applications.length === 0) {
    return (
      <div className="rounded-xl border border-gray-200 bg-gradient-to-b from-gray-50 to-white py-12 text-center">
        <div className="mx-auto max-w-md">
          <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-gray-100">
            <div className="text-2xl text-gray-400">📱</div>
          </div>
          <h3 className="mb-2 text-lg font-medium text-gray-900">No applications found</h3>
          <p className="text-gray-600">
            {showActions
              ? 'Create your first application to get started!'
              : 'Check back later for new applications.'}
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
      {applications.map(app => (
        <AppCard
          key={app.id}
          application={app}
          onSelect={onAppSelect}
          onDelete={onAppDelete}
          showActions={showActions}
        />
      ))}
    </div>
  );
};
