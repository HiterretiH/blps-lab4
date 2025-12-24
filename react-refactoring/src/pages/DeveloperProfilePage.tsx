import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Alert } from '../components/ui/Alert';
import { Input } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { Tabs, TabPanel } from '../components/ui/Tabs';
import { useAuthStore } from '../store/auth.store';
import { useApplicationsStore } from '../store/applications.store';
import { useMonetizationStore } from '../store/monetization.store';
import { developersService } from '../services/developers.service';
import { monetizationService } from '../services/monetization.service';
import { 
  User, Settings, DollarSign, Download, BarChart3, Package, 
  CreditCard, Users, Globe, Shield, Edit3 // Убрать TrendingUp
} from 'lucide-react';

interface Developer {
  id: number;
  name: string;
  description: string;
  // добавьте другие поля по необходимости
}

interface MonetizationData {
  id: number;
  applicationId: number;
  revenue?: number;
  downloadRevenue?: number;
  purchasesRevenue?: number;
  adsRevenue?: number;
  currentBalance?: number;
}

export const DeveloperProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const { user, developerId } = useAuthStore();
  const { applications } = useApplicationsStore();
  const { stats } = useMonetizationStore();
  
  const [developer, setDeveloper] = useState<Developer | null>(null);
  const [monetizationData, setMonetizationData] = useState<MonetizationData[]>([]);
  const [totalRevenue, setTotalRevenue] = useState(0);
  const [totalDownloads, setTotalDownloads] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [activeTab] = useState('overview');
  const [hasLoaded, setHasLoaded] = useState(false);

  const loadDeveloperData = useCallback(async () => {
    if (!developerId || hasLoaded) {
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      console.log('👨‍💻 Loading developer data...');
      
      const devData = await developersService.getDeveloper(developerId);
      setDeveloper(devData);

      const monetizationPromises = applications.map(app =>
        monetizationService.getMonetizationInfo(app.id).catch(() => null)
      );
      
      const monetizationResults = await Promise.all(monetizationPromises);
      const validMonetization = monetizationResults.filter(Boolean) as MonetizationData[];
      setMonetizationData(validMonetization);

      const revenue = validMonetization.reduce((sum, item) => sum + (item.revenue || 0), 0);
      const downloads = stats.reduce((sum, stat) => sum + stat.downloads, 0);

      setTotalRevenue(revenue);
      setTotalDownloads(downloads);

      setHasLoaded(true);
    } catch (err: unknown) { // Добавить тип для ошибки
      console.error('❌ Developer profile error:', err);
      setError('Failed to load developer data');
    } finally {
      setIsLoading(false);
    }
  }, [developerId, applications, stats, hasLoaded]);

  useEffect(() => {
    if (!user || user.role !== 'DEVELOPER') {
      navigate('/dashboard');
      return;
    }

    loadDeveloperData();
  }, [user, navigate, loadDeveloperData]);

  const monetizedAppsCount = monetizationData.length;
  const activeAppsCount = applications.filter(app => app.status === 1).length;
  const pendingAppsCount = applications.filter(app => app.status === 0).length;

  // Рассчитываем общую статистику по монетизации
  const totalDownloadRevenue = monetizationData.reduce((sum, item) => sum + (item.downloadRevenue || 0), 0);
  const totalPurchaseRevenue = monetizationData.reduce((sum, item) => sum + (item.purchasesRevenue || 0), 0);
  const totalAdRevenue = monetizationData.reduce((sum, item) => sum + (item.adsRevenue || 0), 0);

  const tabs = [
    { id: 'overview', label: 'Overview' },
    { id: 'apps', label: 'Applications' },
    { id: 'monetization', label: 'Monetization' },
    { id: 'analytics', label: 'Analytics' },
    { id: 'settings', label: 'Settings' },
  ];

  if (!user || user.role !== 'DEVELOPER') {
    navigate('/dashboard');
    return null;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div className="flex items-center space-x-4">
          <div className="rounded-full bg-gradient-to-r from-primary-500 to-primary-600 p-3">
            <User className="h-8 w-8 text-white" />
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Developer Profile</h1>
            <div className="mt-1 flex items-center space-x-3">
              <Badge variant="primary" className="flex items-center gap-1">
                <Shield className="h-3 w-3" />
                DEVELOPER
              </Badge>
              <span className="text-sm text-gray-600">ID: #{developerId}</span>
            </div>
          </div>
        </div>
        <Button variant="outline" className="flex items-center gap-2">
          <Edit3 className="h-4 w-4" />
          Edit Profile
        </Button>
      </div>

      {error && (
        <Alert variant="danger" title="Error">
          {error}
        </Alert>
      )}

      {isLoading ? (
        <div className="flex flex-col items-center justify-center py-12">
          <div className="h-8 w-8 animate-spin rounded-full border-b-2 border-primary-600"></div>
          <p className="mt-4 text-gray-600">Loading developer profile...</p>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
            <Card className="bg-gradient-to-br from-blue-50 to-white">
              <CardContent className="p-6">
                <div className="flex items-center">
                  <div className="mr-4 rounded-lg bg-blue-100 p-3">
                    <DollarSign className="h-6 w-6 text-blue-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-600">Total Revenue</p>
                    <p className="mt-1 text-2xl font-bold text-gray-900">
                      ${totalRevenue.toFixed(2)}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card className="bg-gradient-to-br from-green-50 to-white">
              <CardContent className="p-6">
                <div className="flex items-center">
                  <div className="mr-4 rounded-lg bg-green-100 p-3">
                    <Download className="h-6 w-6 text-green-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-600">Total Downloads</p>
                    <p className="mt-1 text-2xl font-bold text-gray-900">
                      {totalDownloads.toLocaleString()}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card className="bg-gradient-to-br from-purple-50 to-white">
              <CardContent className="p-6">
                <div className="flex items-center">
                  <div className="mr-4 rounded-lg bg-purple-100 p-3">
                    <Package className="h-6 w-6 text-purple-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-600">Active Apps</p>
                    <p className="mt-1 text-2xl font-bold text-gray-900">
                      {activeAppsCount}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card className="bg-gradient-to-br from-orange-50 to-white">
              <CardContent className="p-6">
                <div className="flex items-center">
                  <div className="mr-4 rounded-lg bg-orange-100 p-3">
                    <Users className="h-6 w-6 text-orange-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-600">Active Users</p>
                    <p className="mt-1 text-2xl font-bold text-gray-900">
                      {(totalDownloads * 0.1).toFixed(0)}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardContent className="p-0">
              <Tabs tabs={tabs} defaultTab={activeTab}>
                <TabPanel id="overview">
                  <div className="p-6">
                    <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                      <div>
                        <h3 className="mb-4 text-lg font-medium text-gray-900">Developer Information</h3>
                        <div className="space-y-4">
                          <div>
                            <label className="block text-sm font-medium text-gray-700">
                              Username
                            </label>
                            <p className="mt-1 text-gray-900">{user.username}</p>
                          </div>
                          <div>
                            <label className="block text-sm font-medium text-gray-700">
                              Email
                            </label>
                            <p className="mt-1 text-gray-900">{user.email || 'Not provided'}</p>
                          </div>
                          {developer && (
                            <>
                              <div>
                                <label className="block text-sm font-medium text-gray-700">
                                  Developer Name
                                </label>
                                <p className="mt-1 text-gray-900">{developer.name}</p>
                              </div>
                              <div>
                                <label className="block text-sm font-medium text-gray-700">
                                  Description
                                </label>
                                <p className="mt-1 text-gray-900">{developer.description}</p>
                              </div>
                            </>
                          )}
                        </div>
                      </div>

                      <div>
                        <h3 className="mb-4 text-lg font-medium text-gray-900">Quick Stats</h3>
                        <div className="space-y-3">
                          <div className="flex items-center justify-between rounded-lg border p-3">
                            <span className="text-sm text-gray-600">Applications Created</span>
                            <span className="font-semibold">{applications.length}</span>
                          </div>
                          <div className="flex items-center justify-between rounded-lg border p-3">
                            <span className="text-sm text-gray-600">Pending Reviews</span>
                            <span className="font-semibold">{pendingAppsCount}</span>
                          </div>
                          <div className="flex items-center justify-between rounded-lg border p-3">
                            <span className="text-sm text-gray-600">Monetized Apps</span>
                            <span className="font-semibold">{monetizedAppsCount}</span>
                          </div>
                          <div className="flex items-center justify-between rounded-lg border p-3">
                            <span className="text-sm text-gray-600">Average Rating</span>
                            <span className="font-semibold">
                              {stats.length > 0 
                                ? (stats.reduce((sum, s) => sum + s.rating, 0) / stats.length).toFixed(1)
                                : 'N/A'
                              }
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </TabPanel>

                <TabPanel id="apps">
                  <div className="p-6">
                    <div className="mb-6 flex items-center justify-between">
                      <h3 className="text-lg font-medium text-gray-900">Your Applications</h3>
                      <Button onClick={() => navigate('/developer/apps?create=true')}>
                        <Package className="mr-2 h-4 w-4" />
                        Create New App
                      </Button>
                    </div>

                    {applications.length === 0 ? (
                      <div className="py-12 text-center">
                        <Package className="mx-auto h-12 w-12 text-gray-400" />
                        <h3 className="mt-4 text-lg font-medium text-gray-900">No applications yet</h3>
                        <p className="mt-1 text-gray-600">
                          Create your first application to start monetizing!
                        </p>
                        <Button className="mt-6" onClick={() => navigate('/developer/apps?create=true')}>
                          Create Your First App
                        </Button>
                      </div>
                    ) : (
                      <div className="overflow-x-auto">
                        <table className="w-full">
                          <thead>
                            <tr className="border-b">
                              <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">
                                Name
                              </th>
                              <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">
                                Type
                              </th>
                              <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">
                                Price
                              </th>
                              <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">
                                Status
                              </th>
                              <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">
                                Revenue
                              </th>
                              <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">
                                Actions
                              </th>
                            </tr>
                          </thead>
                          <tbody>
                            {applications.map((app) => {
                              const appMonetization = monetizationData.find(m => m.applicationId === app.id);
                              return (
                                <tr key={app.id} className="border-b hover:bg-gray-50">
                                  <td className="px-4 py-3">
                                    <div>
                                      <p className="font-medium text-gray-900">{app.name}</p>
                                      <p className="text-sm text-gray-500">ID: #{app.id}</p>
                                    </div>
                                  </td>
                                  <td className="px-4 py-3">
                                    <Badge variant="info" className="text-xs">
                                      {app.type}
                                    </Badge>
                                  </td>
                                  <td className="px-4 py-3 font-medium text-gray-900">
                                    ${app.price.toFixed(2)}
                                  </td>
                                  <td className="px-4 py-3">
                                    <Badge
                                      variant={
                                        app.status === 1
                                          ? 'success'
                                          : app.status === 0
                                          ? 'warning'
                                          : 'danger'
                                      }
                                      className="text-xs"
                                    >
                                      {app.status === 1
                                        ? 'ACTIVE'
                                        : app.status === 0
                                        ? 'PENDING'
                                        : 'REJECTED'}
                                    </Badge>
                                  </td>
                                  <td className="px-4 py-3 font-medium text-gray-900">
                                    ${appMonetization?.revenue?.toFixed(2) || '0.00'}
                                  </td>
                                  <td className="px-4 py-3">
                                    <div className="flex space-x-2">
                                      <Button
                                        variant="outline"
                                        size="sm"
                                        onClick={() => navigate(`/applications/${app.id}`)}
                                      >
                                        View
                                      </Button>
                                      <Button
                                        variant="outline"
                                        size="sm"
                                        onClick={() => navigate(`/developer/monetization/${app.id}`)}
                                      >
                                        Monetize
                                      </Button>
                                    </div>
                                  </td>
                                </tr>
                              );
                            })}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </div>
                </TabPanel>

                <TabPanel id="monetization">
                  <div className="p-6">
                    <h3 className="mb-6 text-lg font-medium text-gray-900">Monetization Overview</h3>
                    
                    {monetizationData.length === 0 ? (
                      <div className="py-12 text-center">
                        <DollarSign className="mx-auto h-12 w-12 text-gray-400" />
                        <h3 className="mt-4 text-lg font-medium text-gray-900">No monetization data</h3>
                        <p className="mt-1 text-gray-600">
                          Monetize your applications to see revenue statistics
                        </p>
                        <Button 
                          className="mt-6" 
                          onClick={() => navigate('/developer/apps')}
                        >
                          Monetize Applications
                        </Button>
                      </div>
                    ) : (
                      <div className="space-y-6">
                        <div className="rounded-lg border p-6">
                          <h4 className="mb-4 font-medium text-gray-900">Revenue Breakdown</h4>
                          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                            <div className="rounded-lg bg-blue-50 p-4">
                              <div className="flex items-center">
                                <Download className="mr-3 h-5 w-5 text-blue-600" />
                                <div>
                                  <p className="text-sm text-gray-600">Download Revenue</p>
                                  <p className="text-xl font-bold text-gray-900">
                                    ${totalDownloadRevenue.toFixed(2)}
                                  </p>
                                </div>
                              </div>
                            </div>
                            <div className="rounded-lg bg-green-50 p-4">
                              <div className="flex items-center">
                                <CreditCard className="mr-3 h-5 w-5 text-green-600" />
                                <div>
                                  <p className="text-sm text-gray-600">In-App Purchases</p>
                                  <p className="text-xl font-bold text-gray-900">
                                    ${totalPurchaseRevenue.toFixed(2)}
                                  </p>
                                </div>
                              </div>
                            </div>
                            <div className="rounded-lg bg-purple-50 p-4">
                              <div className="flex items-center">
                                <Globe className="mr-3 h-5 w-5 text-purple-600" />
                                <div>
                                  <p className="text-sm text-gray-600">Ad Revenue</p>
                                  <p className="text-xl font-bold text-gray-900">
                                    ${totalAdRevenue.toFixed(2)}
                                  </p>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>

                        <div>
                          <h4 className="mb-4 font-medium text-gray-900">Monetized Applications</h4>
                          <div className="space-y-3">
                            {monetizationData.map((item) => {
                              const app = applications.find(a => a.id === item.applicationId);
                              if (!app) return null;
                              
                              return (
                                <div
                                  key={item.id}
                                  className="flex items-center justify-between rounded-lg border p-4"
                                >
                                  <div>
                                    <p className="font-medium text-gray-900">{app.name}</p>
                                    <div className="mt-1 flex items-center space-x-4">
                                      <span className="text-sm text-gray-600">
                                        Balance: ${item.currentBalance?.toFixed(2) || '0.00'}
                                      </span>
                                      <span className="text-sm text-gray-600">
                                        Total Revenue: ${item.revenue?.toFixed(2) || '0.00'}
                                      </span>
                                    </div>
                                  </div>
                                  <Button
                                    variant="outline"
                                    size="sm"
                                    onClick={() => navigate(`/monetization/${item.id}`)}
                                  >
                                    Manage
                                  </Button>
                                </div>
                              );
                            })}
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                </TabPanel>

                <TabPanel id="analytics">
                  <div className="p-6">
                    <h3 className="mb-6 text-lg font-medium text-gray-900">Analytics Dashboard</h3>
                    
                    {stats.length === 0 ? (
                      <div className="py-12 text-center">
                        <BarChart3 className="mx-auto h-12 w-12 text-gray-400" />
                        <h3 className="mt-4 text-lg font-medium text-gray-900">No analytics data</h3>
                        <p className="mt-1 text-gray-600">
                          Analytics will appear once your apps get downloads
                        </p>
                      </div>
                    ) : (
                      <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                        <Card>
                          <CardHeader>
                            <CardTitle>Downloads by Application</CardTitle>
                          </CardHeader>
                          <CardContent>
                            <div className="space-y-4">
                              {stats.map((stat) => {
                                const app = applications.find(a => a.id === stat.applicationId);
                                return (
                                  <div key={stat.id} className="flex items-center justify-between">
                                    <span className="text-sm text-gray-600">
                                      {app?.name || `App #${stat.applicationId}`}
                                    </span>
                                    <div className="flex items-center space-x-2">
                                      <span className="font-medium">{stat.downloads}</span>
                                      <div className="h-2 w-24 rounded-full bg-gray-200">
                                        <div
                                          className="h-full rounded-full bg-primary-600"
                                          style={{
                                            width: `${Math.min((stat.downloads / 1000) * 100, 100)}%`,
                                          }}
                                        />
                                      </div>
                                    </div>
                                  </div>
                                );
                              })}
                            </div>
                          </CardContent>
                        </Card>

                        <Card>
                          <CardHeader>
                            <CardTitle>Performance Metrics</CardTitle>
                          </CardHeader>
                          <CardContent>
                            <div className="space-y-4">
                              <div className="flex items-center justify-between">
                                <span className="text-sm text-gray-600">Average Downloads per App</span>
                                <span className="font-medium">
                                  {stats.length > 0
                                    ? Math.round(stats.reduce((sum, s) => sum + s.downloads, 0) / stats.length)
                                    : 0}
                                </span>
                              </div>
                              <div className="flex items-center justify-between">
                                <span className="text-sm text-gray-600">Conversion Rate</span>
                                <span className="font-medium">
                                  {monetizedAppsCount > 0
                                    ? `${((monetizedAppsCount / applications.length) * 100).toFixed(1)}%`
                                    : '0%'}
                                </span>
                              </div>
                              <div className="flex items-center justify-between">
                                <span className="text-sm text-gray-600">User Retention</span>
                                <span className="font-medium">
                                  {stats.length > 0 ? '85.2%' : 'N/A'}
                                </span>
                              </div>
                              <div className="flex items-center justify-between">
                                <span className="text-sm text-gray-600">Avg. Revenue per User</span>
                                <span className="font-medium">
                                  ${(totalRevenue / (totalDownloads || 1)).toFixed(2)}
                                </span>
                              </div>
                            </div>
                          </CardContent>
                        </Card>
                      </div>
                    )}
                  </div>
                </TabPanel>

                <TabPanel id="settings">
                  <div className="p-6">
                    <h3 className="mb-6 text-lg font-medium text-gray-900">Developer Settings</h3>
                    
                    <div className="space-y-6">
                      <div className="rounded-lg border p-6">
                        <h4 className="mb-4 font-medium text-gray-900">Profile Information</h4>
                        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                          <Input label="Developer Name" defaultValue={developer?.name || ''} />
                          <Input label="Contact Email" defaultValue={user.email || ''} />
                          <div className="md:col-span-2">
                            <label className="block text-sm font-medium text-gray-700">
                              Description
                            </label>
                            <textarea
                              className="mt-1 w-full rounded-lg border border-gray-300 p-3"
                              rows={4}
                              defaultValue={developer?.description || ''}
                              placeholder="Describe your development work..."
                            />
                          </div>
                        </div>
                        <div className="mt-4 flex justify-end">
                          <Button>Save Changes</Button>
                        </div>
                      </div>

                      <div className="rounded-lg border p-6">
                        <h4 className="mb-4 font-medium text-gray-900">Payment Settings</h4>
                        <div className="space-y-3">
                          <div className="flex items-center justify-between">
                            <span className="text-sm text-gray-600">Payout Method</span>
                            <span className="font-medium">Bank Transfer</span>
                          </div>
                          <div className="flex items-center justify-between">
                            <span className="text-sm text-gray-600">Payout Threshold</span>
                            <span className="font-medium">$50.00</span>
                          </div>
                          <div className="flex items-center justify-between">
                            <span className="text-sm text-gray-600">Next Payout Date</span>
                            <span className="font-medium">15th of each month</span>
                          </div>
                        </div>
                        <div className="mt-4">
                          <Button variant="outline">Update Payment Settings</Button>
                        </div>
                      </div>

                      <div className="rounded-lg border p-6">
                        <h4 className="mb-4 font-medium text-gray-900">Account Security</h4>
                        <div className="space-y-4">
                          <Button variant="outline" className="w-full justify-start">
                            <Settings className="mr-2 h-4 w-4" />
                            Change Password
                          </Button>
                          <Button variant="outline" className="w-full justify-start">
                            <Shield className="mr-2 h-4 w-4" />
                            Two-Factor Authentication
                          </Button>
                          <Button variant="outline" className="w-full justify-start">
                            <Globe className="mr-2 h-4 w-4" />
                            Connected Accounts
                          </Button>
                        </div>
                      </div>
                    </div>
                  </div>
                </TabPanel>
              </Tabs>
            </CardContent>
          </Card>
        </>
      )}
    </div>
  );
};