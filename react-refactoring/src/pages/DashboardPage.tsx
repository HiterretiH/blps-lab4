import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Alert } from '../components/ui/Alert';
import { useAuthStore } from '../store/auth.store';
import { useApplicationsStore } from '../store/applications.store';
import { useMonetizationStore } from '../store/monetization.store';
import { 
  BarChart3, DollarSign, Download, Users, Package, TrendingUp, 
  Activity, Globe
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const navigate = useNavigate();
  const { user, developerId } = useAuthStore();
  const { applications } = useApplicationsStore();
  const { stats } = useMonetizationStore();
  
  const [dashboardStats, setDashboardStats] = useState({
    totalRevenue: 0,
    totalDownloads: 0,
    totalUsers: 1000,
    activeApps: 0,
    pendingApps: 0,
    monetizedApps: 0,
  });

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    console.log('📊 Dashboard: using cached data');
  }, [user, navigate]);

  // Calculate stats when data changes - исправить с использованием requestAnimationFrame
  useEffect(() => {
    const calculateAndSetStats = () => {
      if (applications.length === 0 && stats.length === 0) return;

      const myApps = applications.filter(app => 
        developerId ? app.developerId === developerId : true
      );

      const totalRevenue = myApps.reduce((sum, app) => sum + app.price, 0);
      const totalDownloads = stats.reduce((sum, stat) => sum + stat.downloads, 0);
      const activeApps = myApps.filter(app => app.status === 1).length;
      const pendingApps = myApps.filter(app => app.status === 0).length;

      // Использовать requestAnimationFrame для асинхронного обновления
      requestAnimationFrame(() => {
        setDashboardStats(prev => ({
          ...prev,
          totalRevenue,
          totalDownloads,
          activeApps,
          pendingApps,
          monetizedApps: myApps.length,
          totalUsers: 1000 + Math.floor(Math.random() * 500),
        }));
      });
    };

    calculateAndSetStats();
  }, [applications, stats, developerId]);

  if (!user) {
    return null;
  }

  const statsCards = [
    {
      title: 'Total Revenue',
      value: `$${dashboardStats.totalRevenue.toFixed(2)}`,
      icon: DollarSign,
      color: 'text-green-600',
      bgColor: 'bg-green-50',
      change: '+12.5%',
      changeColor: 'text-green-600',
    },
    {
      title: 'Total Downloads',
      value: dashboardStats.totalDownloads.toLocaleString(),
      icon: Download,
      color: 'text-blue-600',
      bgColor: 'bg-blue-50',
      change: '+234',
      changeColor: 'text-blue-600',
    },
    {
      title: 'Active Users',
      value: dashboardStats.totalUsers.toLocaleString(),
      icon: Users,
      color: 'text-purple-600',
      bgColor: 'bg-purple-50',
      change: '+5.2%',
      changeColor: 'text-purple-600',
    },
    {
      title: 'Active Applications',
      value: dashboardStats.activeApps.toString(),
      icon: Package,
      color: 'text-orange-600',
      bgColor: 'bg-orange-50',
      change: `+${dashboardStats.pendingApps} pending`,
      changeColor: 'text-orange-600',
    },
  ];

  const recentActivity = [
    { user: user.username, action: 'logged in', time: 'Just now', type: 'login' },
    { user: 'System', action: 'updated statistics', time: '2 hours ago', type: 'system' },
    { user: 'Other User', action: 'downloaded an app', time: '4 hours ago', type: 'download' },
    { user: 'Developer Team', action: 'released new version', time: '1 day ago', type: 'update' },
  ];

  return (
    <div className="space-y-6">
      {/* Р—Р°РіРѕР»РѕРІРѕРє */}
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
          <p className="mt-1 text-gray-600">
            Welcome back, {user.username}! Here's what's happening with your account.
          </p>
        </div>
        <div className="flex items-center space-x-3">
          <Button variant="outline" onClick={() => navigate('/applications')}>
            View All Apps
          </Button>
          {user.role === 'DEVELOPER' && (
            <Button onClick={() => navigate('/developer/apps')}>Developer Portal</Button>
          )}
        </div>
      </div>

      {error && (
        <Alert variant="danger" title="Error">
          {error}
        </Alert>
      )}

      {isLoading ? (
        <div className="flex flex-col items-center justify-center py-12">
          <div className="h-8 w-8 animate-spin rounded-full border-b-2 border-primary-600"></div>
          <p className="mt-4 text-gray-600">Loading dashboard data...</p>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
            {statsCards.map((stat, index) => (
              <Card key={index} className="overflow-hidden">
                <CardContent className="p-6">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-gray-600">{stat.title}</p>
                      <p className="mt-2 text-2xl font-bold text-gray-900">{stat.value}</p>
                      <p className={`mt-1 text-sm ${stat.changeColor}`}>{stat.change}</p>
                    </div>
                    <div className={`rounded-lg ${stat.bgColor} p-3`}>
                      <stat.icon className={`h-6 w-6 ${stat.color}`} />
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
            <Card className="lg:col-span-2">
              <CardHeader>
                <CardTitle>Recent Applications</CardTitle>
                <Button variant="outline" size="sm" onClick={() => navigate('/applications')}>
                  View All
                </Button>
              </CardHeader>
              <CardContent>
                {applications.length === 0 ? (
                  <div className="py-8 text-center">
                    <Package className="mx-auto h-12 w-12 text-gray-400" />
                    <p className="mt-2 text-gray-600">No applications yet</p>
                    {user.role === 'DEVELOPER' && (
                      <Button
                        className="mt-4"
                        onClick={() => navigate('/developer/apps?create=true')}
                      >
                        Create Your First App
                      </Button>
                    )}
                  </div>
                ) : (
                  <div className="space-y-4">
                    {applications.slice(0, 5).map(app => (
                      <div
                        key={app.id}
                        className="flex items-center justify-between rounded-lg border p-4 hover:bg-gray-50"
                      >
                        <div>
                          <p className="font-medium text-gray-900">{app.name}</p>
                          <div className="mt-1 flex items-center space-x-2">
                            <span className="text-sm text-gray-600">
                              {app.type} • ${app.price}
                            </span>
                            <span
                              className={`rounded-full px-2 py-1 text-xs font-medium ${
                                app.status === 1
                                  ? 'bg-green-100 text-green-800'
                                  : app.status === 0
                                    ? 'bg-yellow-100 text-yellow-800'
                                    : 'bg-red-100 text-red-800'
                              }`}
                            >
                              {app.status === 1
                                ? 'Active'
                                : app.status === 0
                                  ? 'Pending'
                                  : 'Rejected'}
                            </span>
                          </div>
                        </div>
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => navigate(`/applications/${app.id}`)}
                        >
                          View
                        </Button>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Recent Activity</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {recentActivity.map((activity, index) => (
                    <div key={index} className="flex items-start space-x-3">
                      <div
                        className={`rounded-full p-2 ${
                          activity.type === 'login'
                            ? 'bg-blue-100'
                            : activity.type === 'download'
                              ? 'bg-green-100'
                              : activity.type === 'update'
                                ? 'bg-purple-100'
                                : 'bg-gray-100'
                        }`}
                      >
                        {activity.type === 'login' && <Users className="h-4 w-4 text-blue-600" />}
                        {activity.type === 'download' && (
                          <Download className="h-4 w-4 text-green-600" />
                        )}
                        {activity.type === 'update' && (
                          <Activity className="h-4 w-4 text-purple-600" />
                        )}
                        {activity.type === 'system' && <Globe className="h-4 w-4 text-gray-600" />}
                      </div>
                      <div className="flex-1">
                        <p className="text-sm font-medium text-gray-900">{activity.user}</p>
                        <p className="text-sm text-gray-600">{activity.action}</p>
                        <p className="text-xs text-gray-500">{activity.time}</p>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
                <Button
                  variant="outline"
                  className="h-24 flex-col"
                  onClick={() => navigate('/applications')}
                >
                  <Package className="mb-2 h-8 w-8 text-gray-600" />
                  <span>Browse Apps</span>
                </Button>

                {user.role === 'DEVELOPER' && (
                  <>
                    <Button
                      variant="outline"
                      className="h-24 flex-col"
                      onClick={() => navigate('/developer/apps?create=true')}
                    >
                      <BarChart3 className="mb-2 h-8 w-8 text-gray-600" />
                      <span>Create App</span>
                    </Button>
                    <Button
                      variant="outline"
                      className="h-24 flex-col"
                      onClick={() => navigate('/developer/analytics')}
                    >
                      <TrendingUp className="mb-2 h-8 w-8 text-gray-600" />
                      <span>Analytics</span>
                    </Button>
                  </>
                )}

                <Button
                  variant="outline"
                  className="h-24 flex-col"
                  onClick={() => navigate('/profile')}
                >
                  <Users className="mb-2 h-8 w-8 text-gray-600" />
                  <span>Profile</span>
                </Button>
              </div>
            </CardContent>
          </Card>
        </>
      )}
    </div>
  );
};
