import React from 'react';

export const DashboardPage: React.FC = () => {
  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-gray-900">Dashboard</h1>
      <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
        <div className="rounded-lg bg-white p-6 shadow">
          <h3 className="text-lg font-medium text-gray-900">Applications</h3>
          <p className="mt-2 text-3xl font-bold text-primary-600">0</p>
        </div>
        <div className="rounded-lg bg-white p-6 shadow">
          <h3 className="text-lg font-medium text-gray-900">Revenue</h3>
          <p className="mt-2 text-3xl font-bold text-green-600">$0.00</p>
        </div>
        <div className="rounded-lg bg-white p-6 shadow">
          <h3 className="text-lg font-medium text-gray-900">Downloads</h3>
          <p className="mt-2 text-3xl font-bold text-blue-600">0</p>
        </div>
      </div>
    </div>
  );
};
