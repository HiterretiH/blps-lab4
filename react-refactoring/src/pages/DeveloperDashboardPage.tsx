import React from 'react';
import { Card, CardHeader, CardContent, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { BarChart3, DollarSign, Download, Users } from 'lucide-react';

export const DeveloperDashboardPage: React.FC = () => {
  const stats = [
    {
      label: 'Total Applications',
      value: '3',
      icon: <BarChart3 className="h-5 w-5" />,
      change: '+1 this month',
    },
    {
      label: 'Total Revenue',
      value: '$1,245.80',
      icon: <DollarSign className="h-5 w-5" />,
      change: '+12.5%',
    },
    {
      label: 'Total Downloads',
      value: '1,234',
      icon: <Download className="h-5 w-5" />,
      change: '+234 this week',
    },
    { label: 'Active Users', value: '892', icon: <Users className="h-5 w-5" />, change: '+5.2%' },
  ];

  const recentApps = [
    { id: 1, name: 'Pixel Runner', type: 'GAME', status: 'ACCEPTED', revenue: '$845.20' },
    { id: 2, name: 'Meditation Pro', type: 'HEALTH', status: 'PENDING', revenue: '$0' },
    { id: 3, name: 'Finance Tracker', type: 'FINANCE', status: 'ACCEPTED', revenue: '$400.60' },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Developer Dashboard</h1>
          <p className="mt-1 text-gray-600">
            Welcome back! Here's what's happening with your apps.
          </p>
        </div>
        <Button size="lg">
          <BarChart3 className="mr-2 h-5 w-5" />
          New Application
        </Button>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat, index) => (
          <Card key={index} className="bg-gradient-to-br from-white to-gray-50">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600">{stat.label}</p>
                  <p className="mt-2 text-2xl font-bold text-gray-900">{stat.value}</p>
                  <p className="mt-1 text-sm text-green-600">{stat.change}</p>
                </div>
                <div className="rounded-lg bg-primary-100 p-3">
                  {React.cloneElement(stat.icon, { className: 'h-6 w-6 text-primary-600' })}
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Recent Applications */}
      <Card>
        <CardHeader>
          <CardTitle>Recent Applications</CardTitle>
          <Button variant="outline" size="sm">
            View All
          </Button>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b">
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">
                    Application
                  </th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Type</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Status</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Revenue</th>
                  <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">Actions</th>
                </tr>
              </thead>
              <tbody>
                {recentApps.map(app => (
                  <tr key={app.id} className="border-b hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <div>
                        <p className="font-medium text-gray-900">{app.name}</p>
                        <p className="text-sm text-gray-500">ID: #{app.id}</p>
                      </div>
                    </td>
                    <td className="px-4 py-3">
                      <span className="inline-flex items-center rounded-full bg-blue-100 px-2.5 py-0.5 text-xs font-medium text-blue-800">
                        {app.type}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <span
                        className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${
                          app.status === 'ACCEPTED'
                            ? 'bg-green-100 text-green-800'
                            : 'bg-yellow-100 text-yellow-800'
                        }`}
                      >
                        {app.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 font-medium text-gray-900">{app.revenue}</td>
                    <td className="px-4 py-3">
                      <div className="flex space-x-2">
                        <Button variant="outline" size="sm">
                          View
                        </Button>
                        <Button variant="secondary" size="sm">
                          Edit
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
            <Button variant="outline" className="h-24 flex-col">
              <DollarSign className="mb-2 h-8 w-8 text-gray-600" />
              <span>Request Payout</span>
            </Button>
            <Button variant="outline" className="h-24 flex-col">
              <BarChart3 className="mb-2 h-8 w-8 text-gray-600" />
              <span>View Analytics</span>
            </Button>
            <Button variant="outline" className="h-24 flex-col">
              <Users className="mb-2 h-8 w-8 text-gray-600" />
              <span>User Stats</span>
            </Button>
            <Button variant="outline" className="h-24 flex-col">
              <Download className="mb-2 h-8 w-8 text-gray-600" />
              <span>Download Reports</span>
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};
