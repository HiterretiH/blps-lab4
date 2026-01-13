import React, { useEffect, useState, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Alert } from '../components/ui/Alert';
import { Badge } from '../components/ui/Badge';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '../components/ui/Table';
import { useAuthStore } from '../store/auth.store';
import { useApplicationsStore } from '../store/applications.store';
import { useMonetizationStore } from '../store/monetization.store';
import { monetizationService } from '../services/monetization.service';
import { statsService } from '../services/stats.service';
import { MonetizeConfirmationModal } from '../components/monetization/MonetizeConfirmationModal';
import {
  User,
  DollarSign,
  Download,
  Package,
  CreditCard,
  Users,
  Globe,
  Shield,
  BarChart3,
  TrendingUp,
  Calendar,
  CheckCircle,
  Clock,
  XCircle,
  Zap,
} from 'lucide-react';

export const DeveloperProfilePage: React.FC = () => {
  const navigate = useNavigate();
  const { user, developerId } = useAuthStore();
  const { applications, fetchMyApplications } = useApplicationsStore();
  const { stats, fetchStats } = useMonetizationStore();
  
  const [totalRevenue, setTotalRevenue] = useState(0);
  const [totalDownloads, setTotalDownloads] = useState(0);
  const [monetizedAppsData, setMonetizedAppsData] = useState<any[]>([]);
  const [developerStats, setDeveloperStats] = useState<any[]>([]);
  const [appDetails, setAppDetails] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const [showMonetizeModal, setShowMonetizeModal] = useState(false);
  const [selectedApp, setSelectedApp] = useState<any>(null);
  const [isMonetizing, setIsMonetizing] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const isMounted = useRef(true);
  const isLoadingRef = useRef(false);

  useEffect(() => {
    isMounted.current = true;
    return () => {
      isMounted.current = false;
    };
  }, []);

  const loadDeveloperData = useCallback(async () => {
    if (!developerId || isLoadingRef.current) return;
    isLoadingRef.current = true;
    setIsLoading(true);
    setError(null);
    
    try {
      console.log('🔄 Loading developer statistics for ID:', developerId);
      const [apps, appStats, monetizedApps, devStats] = await Promise.all([
        fetchMyApplications(),
        fetchStats(),
        monetizationService.getAllMonetizedAppsByDeveloper(developerId),
        statsService.getStatsByDeveloper(developerId),
      ]);

      if (!isMounted.current) return;

      setMonetizedAppsData(monetizedApps);
      setDeveloperStats(devStats);

      const appDetailsData = apps.map(app => {
        const appStats = devStats.find((stat: any) => stat.application.id === app.id);
        const monetization = monetizedApps.find((m: any) => m.application.id === app.id);
        return {
          ...app,
          downloads: appStats?.downloads || 0,
          rating: appStats?.rating || 0,
          revenue: monetization?.revenue || 0,
          downloadRevenue: monetization?.downloadRevenue || 0,
          adsRevenue: monetization?.adsRevenue || 0,
          purchasesRevenue: monetization?.purchasesRevenue || 0,
          currentBalance: monetization?.currentBalance || 0,
          isMonetized: !!monetization,
        };
      });

      setAppDetails(appDetailsData);

      let totalRev = 0;
      let totalDls = 0;
      monetizedApps.forEach((app: any) => {
        totalRev += app.revenue || 0;
      });
      devStats.forEach((stat: any) => {
        totalDls += stat.downloads || 0;
      });

      setTotalRevenue(totalRev);
      setTotalDownloads(totalDls);
    } catch (err) {
      console.error('❌ Error loading developer data:', err);
      if (isMounted.current) {
        setError('Failed to load developer data');
      }
    } finally {
      if (isMounted.current) {
        setIsLoading(false);
      }
      isLoadingRef.current = false;
    }
  }, [developerId, fetchMyApplications, fetchStats]);

  useEffect(() => {
    if (!user || user.role !== 'DEVELOPER') {
      navigate('/dashboard');
      return;
    }
    if (developerId) {
      loadDeveloperData();
    }
  }, [user, navigate, developerId, loadDeveloperData]);

  const handleMonetizeApp = async () => {
    if (!selectedApp || !developerId) return;

    setIsMonetizing(true);
    setError(null);
    setSuccessMessage(null);

    try {
      const monetizedApp = await monetizationService.monetizeApplication(
        selectedApp.id,
        developerId
      );

      setSuccessMessage(`✅ "${selectedApp.name}" has been successfully monetized!`);

      setTimeout(() => {
        setShowMonetizeModal(false);
        setSelectedApp(null);
        loadDeveloperData();
      }, 2000);

    } catch (err: any) {
      console.error('Monetization error:', err);
      setError(err.message || 'Failed to monetize application');
    } finally {
      setIsMonetizing(false);
    }
  };

  const handleMonetizeClick = (app: any) => {
    setSelectedApp(app);
    setShowMonetizeModal(true);
    setError(null);
    setSuccessMessage(null);
  };

  const { monetizedAppsCount, activeAppsCount, pendingAppsCount, rejectedAppsCount } =
    React.useMemo(() => {
      return {
        monetizedAppsCount: appDetails.filter(app => app.isMonetized).length,
        activeAppsCount: appDetails.filter(app => app.status === 1).length,
        pendingAppsCount: appDetails.filter(app => app.status === 0).length,
        rejectedAppsCount: appDetails.filter(app => app.status === 2).length,
      };
    }, [appDetails]);

  const { totalDownloadRevenue, totalPurchaseRevenue, totalAdRevenue, avgRating } =
    React.useMemo(() => {
      return {
        totalDownloadRevenue: appDetails.reduce((sum, app) => sum + (app.downloadRevenue || 0), 0),
        totalPurchaseRevenue: appDetails.reduce((sum, app) => sum + (app.purchasesRevenue || 0), 0),
        totalAdRevenue: appDetails.reduce((sum, app) => sum + (app.adsRevenue || 0), 0),
        avgRating:
          appDetails.length > 0
            ? (
                appDetails.reduce((sum, app) => sum + (app.rating || 0), 0) / appDetails.length
              ).toFixed(1)
            : '0.0',
      };
    }, [appDetails]);

  if (!user || user.role !== 'DEVELOPER') {
    navigate('/dashboard');
    return null;
  }

  const getStatusBadge = (status: number) => {
    switch (status) {
      case 1:
        return (
          <Badge variant="success" className="flex items-center gap-1">
            <CheckCircle className="h-3 w-3" />
            ACTIVE
          </Badge>
        );
      case 0:
        return (
          <Badge variant="warning" className="flex items-center gap-1">
            <Clock className="h-3 w-3" />
            PENDING
          </Badge>
        );
      case 2:
        return (
          <Badge variant="danger" className="flex items-center gap-1">
            <XCircle className="h-3 w-3" />
            REJECTED
          </Badge>
        );
      default:
        return <Badge variant="secondary">UNKNOWN</Badge>;
    }
  };

  const getTypeBadge = (type: string) => {
    const colors: Record<string, string> = {
      GAME: 'bg-purple-100 text-purple-800',
      UTILITY: 'bg-blue-100 text-blue-800',
      EDUCATION: 'bg-green-100 text-green-800',
      HEALTH: 'bg-red-100 text-red-800',
      FINANCE: 'bg-yellow-100 text-yellow-800',
      SOCIAL: 'bg-pink-100 text-pink-800',
      PRODUCTIVITY: 'bg-indigo-100 text-indigo-800',
      ENTERTAINMENT: 'bg-orange-100 text-orange-800',
    };
    const colorClass = colors[type] || 'bg-gray-100 text-gray-800';
    return (
      <span className={`px-2 py-1 rounded-full text-xs font-medium ${colorClass}`}>{type}</span>
    );
  };

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
              <Badge variant="outline" className="flex items-center gap-1">
                <Users className="h-3 w-3" />
                {applications.length} Apps
              </Badge>
            </div>
          </div>
        </div>
        <Button
          variant="outline"
          onClick={() => navigate('/developer/apps?create=true')}
          className="flex items-center gap-2"
        >
          <Package className="h-4 w-4" />
          New Application
        </Button>
      </div>

      {/* Сообщение об успехе */}
      {successMessage && (
        <Alert variant="success" title="Success">
          {successMessage}
        </Alert>
      )}

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
          {/* Статистические карточки */}
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-4">
            <StatCard
              title="Total Revenue"
              value={`$${totalRevenue.toFixed(2)}`}
              icon={<DollarSign className="h-6 w-6" />}
              color="blue"
              description="All-time revenue"
            />
            <StatCard
              title="Total Downloads"
              value={totalDownloads.toLocaleString()}
              icon={<Download className="h-6 w-6" />}
              color="green"
              description="Lifetime downloads"
            />
            <Card>
              <CardContent className="p-6">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-600">Monetized Apps</p>
                    <p className="mt-2 text-2xl font-bold text-gray-900">{monetizedAppsCount}</p>
                    <p className="mt-1 text-sm text-gray-500">
                      {applications.length > 0
                        ? `${Math.round((monetizedAppsCount / applications.length) * 100)}% of total`
                        : '0%'}
                    </p>
                  </div>
                  <div className="rounded-lg bg-blue-100 p-3">
                    <TrendingUp className="h-6 w-6 text-blue-600" />
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Разбивка доходов */}
          <Card>
            <CardHeader>
              <CardTitle>Revenue Breakdown</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                <div className="rounded-lg border p-4">
                  <div className="flex items-center">
                    <div className="mr-4 rounded-lg bg-blue-100 p-3">
                      <Download className="h-6 w-6 text-blue-600" />
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-600">Download Revenue</p>
                      <p className="mt-1 text-2xl font-bold text-gray-900">
                        ${totalDownloadRevenue.toFixed(2)}
                      </p>
                      <p className="mt-1 text-sm text-gray-500">
                        {totalRevenue > 0
                          ? `${Math.round((totalDownloadRevenue / totalRevenue) * 100)}% of total`
                          : '0%'}
                      </p>
                    </div>
                  </div>
                </div>
                <div className="rounded-lg border p-4">
                  <div className="flex items-center">
                    <div className="mr-4 rounded-lg bg-green-100 p-3">
                      <CreditCard className="h-6 w-6 text-green-600" />
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-600">In-App Purchases</p>
                      <p className="mt-1 text-2xl font-bold text-gray-900">
                        ${totalPurchaseRevenue.toFixed(2)}
                      </p>
                      <p className="mt-1 text-sm text-gray-500">
                        {totalRevenue > 0
                          ? `${Math.round((totalPurchaseRevenue / totalRevenue) * 100)}% of total`
                          : '0%'}
                      </p>
                    </div>
                  </div>
                </div>
                <div className="rounded-lg border p-4">
                  <div className="flex items-center">
                    <div className="mr-4 rounded-lg bg-purple-100 p-3">
                      <Globe className="h-6 w-6 text-purple-600" />
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-600">Ad Revenue</p>
                      <p className="mt-1 text-2xl font-bold text-gray-900">
                        ${totalAdRevenue.toFixed(2)}
                      </p>
                      <p className="mt-1 text-sm text-gray-500">
                        {totalRevenue > 0
                          ? `${Math.round((totalAdRevenue / totalRevenue) * 100)}% of total`
                          : '0%'}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Таблица приложений */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle>All Applications ({appDetails.length})</CardTitle>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => navigate('/developer/apps?create=true')}
                >
                  <Package className="mr-2 h-4 w-4" />
                  Create New
                </Button>
                <Button variant="outline" size="sm" onClick={() => loadDeveloperData()}>
                  <Calendar className="mr-2 h-4 w-4" />
                  Refresh
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              {appDetails.length === 0 ? (
                <div className="py-12 text-center">
                  <Package className="mx-auto h-12 w-12 text-gray-400" />
                  <h3 className="mt-4 text-lg font-medium text-gray-900">No applications yet</h3>
                  <p className="mt-1 text-gray-600">Create your first application to get started</p>
                  <Button className="mt-4" onClick={() => navigate('/developer/apps?create=true')}>
                    Create Your First App
                  </Button>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Application</TableHead>
                        <TableHead>Type</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead className="text-right">Downloads</TableHead>
                        <TableHead className="text-right">Rating</TableHead>
                        <TableHead className="text-right">Price</TableHead>
                        <TableHead className="text-right">Revenue</TableHead>
                        <TableHead className="text-right">Balance</TableHead>
                        <TableHead className="text-right">Actions</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {appDetails.map(app => (
                        <TableRow key={app.id}>
                          <TableCell className="font-medium">
                            <div>
                              <p className="font-semibold">{app.name}</p>
                              <p className="text-sm text-gray-500 truncate max-w-xs">
                                {app.description}
                              </p>
                            </div>
                          </TableCell>
                          <TableCell>{getTypeBadge(app.type)}</TableCell>
                          <TableCell>{getStatusBadge(app.status)}</TableCell>
                          <TableCell className="text-right">
                            <div className="flex items-center justify-end gap-1">
                              <Download className="h-4 w-4 text-gray-500" />
                              {app.downloads.toLocaleString()}
                            </div>
                          </TableCell>
                          <TableCell className="text-right">
                            <div className="flex items-center justify-end gap-1">
                              <BarChart3 className="h-4 w-4 text-yellow-500" />
                              {app.rating || 0}/5
                            </div>
                          </TableCell>
                          <TableCell className="text-right font-semibold">
                            ${app.price?.toFixed(2) || '0.00'}
                          </TableCell>
                          <TableCell className="text-right font-semibold text-green-600">
                            ${app.revenue?.toFixed(2) || '0.00'}
                          </TableCell>
                          <TableCell className="text-right font-semibold text-blue-600">
                            ${app.currentBalance?.toFixed(2) || '0.00'}
                          </TableCell>
                          <TableCell className="text-right">
                            <div className="flex gap-2 justify-end">
                              <Button
                                variant="outline"
                                size="sm"
                                onClick={() => navigate(`/applications/${app.id}`)}
                              >
                                View
                              </Button>
                              {app.isMonetized ? (
                                <Button
                                  variant="secondary"
                                  size="sm"
                                  onClick={() => navigate(`/monetization/${app.id}`)}
                                  className="flex items-center gap-1"
                                >
                                  <DollarSign className="h-4 w-4" />
                                  Monetize
                                </Button>
                              ) : (
                                <Button
                                  variant="primary"
                                  size="sm"
                                  onClick={() => handleMonetizeClick(app)}
                                  className="flex items-center gap-1 bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700"
                                >
                                  <Zap className="h-4 w-4" />
                                  Monetize
                                </Button>
                              )}
                            </div>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                  
                  {/* Итоговая статистика */}
                  <div className="mt-6 grid grid-cols-1 md:grid-cols-3 gap-4 border-t pt-6">
                    <div className="text-center">
                      <p className="text-sm text-gray-600">Total Applications Value</p>
                      <p className="text-2xl font-bold text-gray-900">
                        ${appDetails.reduce((sum, app) => sum + (app.price || 0), 0).toFixed(2)}
                      </p>
                    </div>
                    <div className="text-center">
                      <p className="text-sm text-gray-600">Total Downloads</p>
                      <p className="text-2xl font-bold text-gray-900">
                        {appDetails
                          .reduce((sum, app) => sum + (app.downloads || 0), 0)
                          .toLocaleString()}
                      </p>
                    </div>
                    <div className="text-center">
                      <p className="text-sm text-gray-600">Average Rating</p>
                      <p className="text-2xl font-bold text-gray-900">{avgRating}/5</p>
                    </div>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </>
      )}

      {/* Модальное окно подтверждения монетизации */}
      {selectedApp && developerId && (
        <MonetizeConfirmationModal
          isOpen={showMonetizeModal}
          onClose={() => {
            setShowMonetizeModal(false);
            setSelectedApp(null);
          }}
          onConfirm={handleMonetizeApp}
          applicationName={selectedApp.name}
          applicationId={selectedApp.id}
          developerId={developerId}
          isLoading={isMonetizing}
        />
      )}
    </div>
  );
};

const StatCard: React.FC<{
  title: string;
  value: string;
  icon: React.ReactNode;
  color: 'blue' | 'green' | 'purple' | 'orange';
  description?: string;
  trend?: string;
}> = ({ title, value, icon, color, description, trend }) => {
  const colors = {
    blue: { bg: 'bg-blue-100', text: 'text-blue-600', trend: 'text-blue-700' },
    green: { bg: 'bg-green-100', text: 'text-green-600', trend: 'text-green-700' },
    purple: { bg: 'bg-purple-100', text: 'text-purple-600', trend: 'text-purple-700' },
    orange: { bg: 'bg-orange-100', text: 'text-orange-600', trend: 'text-orange-700' },
  };
  return (
    <Card>
      <CardContent className="p-6">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-gray-600">{title}</p>
            <p className="mt-2 text-2xl font-bold text-gray-900">{value}</p>
            {description && <p className="mt-1 text-sm text-gray-500">{description}</p>}
            {trend && (
              <p className={`mt-1 text-sm font-medium ${colors[color].trend}`}>
                <TrendingUp className="inline h-3 w-3 mr-1" />
                {trend}
              </p>
            )}
          </div>
          <div className={`rounded-lg ${colors[color].bg} p-3`}>
            {React.cloneElement(icon as any, { className: `h-6 w-6 ${colors[color].text}` })}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};