import React, { useState, useEffect } from 'react'; // Добавить useEffect
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Alert } from '../components/ui/Alert';
import { Tabs, TabPanel } from '../components/ui/Tabs';
import { Badge } from '../components/ui/Badge';
import { Input } from '../components/ui/Input';
import { useAuthStore } from '../store/auth.store';
import { useMonetizationStore } from '../store/monetization.store';
import { monetizationService } from '../services/monetization.service';
import { paymentService } from '../services/payment.service';
import { applicationsService } from '../services/applications.service';
import {
  DollarSign,
  CreditCard,
  TrendingUp,
  BarChart3,
  Download,
  Globe,
  PlusCircle,
  Link,
} from 'lucide-react';

// Добавить интерфейсы для типов
interface Application {
  id: number;
  name: string;
  type: string;
  price: number;
  status: number;
  developerId?: number;
}

interface MonetizationInfo {
  id: number;
  currentBalance?: number;
  revenue?: number;
  downloadRevenue?: number;
  purchasesRevenue?: number;
  adsRevenue?: number;
}

interface Purchase {
  id: number;
  title: string;
  description: string;
  price: number;
  monetizedApplicationId?: number;
}

interface Ad {
  id: number;
  title: string;
  description: string;
  price: number;
  monetizedApplicationId?: number;
}

interface TabConfig {
  id: string;
  label: string;
}

export const MonetizationPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user, developerId } = useAuthStore();
  const {
    ads,
    fetchAds,
    createAd,
    linkPurchasesToApp,
  } = useMonetizationStore();

  const [application, setApplication] = useState<Application | null>(null);
  const [monetizationInfo, setMonetizationInfo] = useState<MonetizationInfo | null>(null);
  const [appPurchases, setAppPurchases] = useState<Purchase[]>([]);
  const [appAds, setAppAds] = useState<Ad[]>([]);
  const [payoutAmount, setPayoutAmount] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('overview');
  const [showCreateAd, setShowCreateAd] = useState(false);
  const [newAd, setNewAd] = useState({ title: '', description: '', price: '' });

  // Использовать useEffect
  useEffect(() => {
    const loadData = async () => {
      if (!id) return;
      
      setIsLoading(true);
      try {
        // Загрузить данные приложения и монетизации
        if (id) {
          const appId = parseInt(id);
          const app = await applicationsService.getApplicationById(appId);
          setApplication(app);
          
          const monetization = await monetizationService.getMonetizationInfo(appId);
          setMonetizationInfo(monetization);
        }
      } catch (err: unknown) {
        console.error('Failed to load data:', err);
        setError('Failed to load monetization data');
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, [id]);

  const handleRequestPayout = async () => {
    if (!monetizationInfo || !payoutAmount) return;

    try {
      const amount = parseFloat(payoutAmount);
      if (amount <= 0 || amount > (monetizationInfo.currentBalance || 0)) {
        setError('Invalid payout amount');
        return;
      }

      const paymentRequest = await monetizationService.sendForm(monetizationInfo.id, amount);
      const isValid = await paymentService.validateCard(paymentRequest.applicationId);

      if (isValid) {
        const result = await monetizationService.makePayout(paymentRequest);
        alert(`Payout successful: ${result}`);
        setPayoutAmount('');

        if (application) {
          const updated = await monetizationService.getMonetizationInfo(application.id);
          setMonetizationInfo(updated);
        }
      } else {
        setError('Card validation failed');
      }
    } catch (err: unknown) {
      console.error('Payout error:', err);
      setError('Payout request failed');
    }
  };

  const handleCreateAd = async () => {
    if (!monetizationInfo || !newAd.title || !newAd.price) {
      setError('Please fill all required fields');
      return;
    }

    try {
      await createAd({
        monetizedApplicationId: monetizationInfo.id,
        title: newAd.title,
        description: newAd.description,
        price: parseFloat(newAd.price),
      });

      await fetchAds();
      const filteredAds = ads.filter(a => a.monetizedApplicationId === monetizationInfo.id);
      setAppAds(filteredAds);

      setNewAd({ title: '', description: '', price: '' });
      setShowCreateAd(false);
    } catch (err: unknown) {
      console.error('Create ad error:', err);
      setError('Failed to create ad');
    }
  };

  const handleLinkPurchases = async () => {
    if (!monetizationInfo) return;

    try {
      const linked = await linkPurchasesToApp(monetizationInfo.id);
      setAppPurchases(linked);
    } catch (err: unknown) {
      console.error('Link purchases error:', err);
      setError('Failed to link purchases');
    }
  };

  const tabs: TabConfig[] = [
    { id: 'overview', label: 'Overview' },
    { id: 'purchases', label: 'In-App Purchases' },
    { id: 'ads', label: 'Ads' },
    { id: 'payout', label: 'Payout' },
    { id: 'analytics', label: 'Analytics' },
  ];

  if (!user || (user.role !== 'DEVELOPER' && user.role !== 'PRIVACY_POLICY')) {
    navigate('/dashboard');
    return null;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Monetization</h1>
          {application && (
            <div className="mt-1 flex items-center gap-3">
              <span className="text-gray-600">Application:</span>
              <Badge variant="info">{application.name}</Badge>
              <span className="text-sm text-gray-500">ID: #{application.id}</span>
            </div>
          )}
        </div>
        <Button variant="outline" onClick={() => navigate(`/applications/${id}`)}>
          Back to Application
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
          <p className="mt-4 text-gray-600">Loading monetization data...</p>
        </div>
      ) : !monetizationInfo ? (
        <Card>
          <CardContent className="py-12 text-center">
            <DollarSign className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-4 text-lg font-medium text-gray-900">Application Not Monetized</h3>
            <p className="mt-1 text-gray-600">
              This application is not set up for monetization yet.
            </p>
            <Button
              className="mt-6"
              onClick={async () => {
                try {
                  if (!application || !developerId) {
                    setError('Missing application or developer ID');
                    return;
                  }
                  
                  const newMonetized = await monetizationService.createMonetizedApplication({
                    developerId: developerId,
                    applicationId: application.id,
                    currentBalance: 0,
                    revenue: 0,
                    downloadRevenue: 0,
                    adsRevenue: 0,
                    purchasesRevenue: 0,
                  });
                  setMonetizationInfo(newMonetized);
                } catch (err: unknown) {
                  console.error('Failed to create monetization:', err);
                  setError('Failed to create monetization');
                }
              }}
            >
              Set Up Monetization
            </Button>
          </CardContent>
        </Card>
      ) : (
        <>
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
            <Card className="bg-gradient-to-br from-green-50 to-white">
              <CardContent className="p-6">
                <div className="flex items-center">
                  <div className="mr-4 rounded-lg bg-green-100 p-3">
                    <DollarSign className="h-6 w-6 text-green-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-600">Current Balance</p>
                    <p className="mt-1 text-2xl font-bold text-gray-900">
                      ${monetizationInfo.currentBalance?.toFixed(2) || '0.00'}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card className="bg-gradient-to-br from-blue-50 to-white">
              <CardContent className="p-6">
                <div className="flex items-center">
                  <div className="mr-4 rounded-lg bg-blue-100 p-3">
                    <TrendingUp className="h-6 w-6 text-blue-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-600">Total Revenue</p>
                    <p className="mt-1 text-2xl font-bold text-gray-900">
                      ${monetizationInfo.revenue?.toFixed(2) || '0.00'}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card className="bg-gradient-to-br from-purple-50 to-white">
              <CardContent className="p-6">
                <div className="flex items-center">
                  <div className="mr-4 rounded-lg bg-purple-100 p-3">
                    <CreditCard className="h-6 w-6 text-purple-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-600">In-App Purchases</p>
                    <p className="mt-1 text-2xl font-bold text-gray-900">
                      ${monetizationInfo.purchasesRevenue?.toFixed(2) || '0.00'}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card className="bg-gradient-to-br from-orange-50 to-white">
              <CardContent className="p-6">
                <div className="flex items-center">
                  <div className="mr-4 rounded-lg bg-orange-100 p-3">
                    <Globe className="h-6 w-6 text-orange-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-600">Ad Revenue</p>
                    <p className="mt-1 text-2xl font-bold text-gray-900">
                      ${monetizationInfo.adsRevenue?.toFixed(2) || '0.00'}
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
                    <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
                      <div className="lg:col-span-2">
                        <h3 className="mb-4 text-lg font-medium text-gray-900">
                          Revenue Breakdown
                        </h3>
                        <div className="space-y-4">
                          <div className="rounded-lg border p-4">
                            <div className="flex items-center justify-between">
                              <div className="flex items-center">
                                <Download className="mr-3 h-5 w-5 text-blue-600" />
                                <div>
                                  <p className="font-medium text-gray-900">Download Revenue</p>
                                  <p className="text-sm text-gray-600">Income from app downloads</p>
                                </div>
                              </div>
                              <p className="text-xl font-bold text-gray-900">
                                ${monetizationInfo.downloadRevenue?.toFixed(2) || '0.00'}
                              </p>
                            </div>
                          </div>

                          <div className="rounded-lg border p-4">
                            <div className="flex items-center justify-between">
                              <div className="flex items-center">
                                <CreditCard className="mr-3 h-5 w-5 text-green-600" />
                                <div>
                                  <p className="font-medium text-gray-900">In-App Purchases</p>
                                  <p className="text-sm text-gray-600">
                                    Revenue from purchases within the app
                                  </p>
                                </div>
                              </div>
                              <p className="text-xl font-bold text-gray-900">
                                ${monetizationInfo.purchasesRevenue?.toFixed(2) || '0.00'}
                              </p>
                            </div>
                          </div>

                          <div className="rounded-lg border p-4">
                            <div className="flex items-center justify-between">
                              <div className="flex items-center">
                                <Globe className="mr-3 h-5 w-5 text-purple-600" />
                                <div>
                                  <p className="font-medium text-gray-900">Ad Revenue</p>
                                  <p className="text-sm text-gray-600">
                                    Income from displayed advertisements
                                  </p>
                                </div>
                              </div>
                              <p className="text-xl font-bold text-gray-900">
                                ${monetizationInfo.adsRevenue?.toFixed(2) || '0.00'}
                              </p>
                            </div>
                          </div>
                        </div>
                      </div>

                      <div>
                        <h3 className="mb-4 text-lg font-medium text-gray-900">Quick Actions</h3>
                        <div className="space-y-3">
                          <Button
                            className="w-full justify-start"
                            onClick={() => setActiveTab('payout')}
                          >
                            <DollarSign className="mr-2 h-4 w-4" />
                            Request Payout
                          </Button>
                          <Button
                            variant="outline"
                            className="w-full justify-start"
                            onClick={() => setActiveTab('purchases')}
                          >
                            <CreditCard className="mr-2 h-4 w-4" />
                            Manage Purchases
                          </Button>
                          <Button
                            variant="outline"
                            className="w-full justify-start"
                            onClick={() => setActiveTab('ads')}
                          >
                            <Globe className="mr-2 h-4 w-4" />
                            Manage Ads
                          </Button>
                          <Button
                            variant="outline"
                            className="w-full justify-start"
                            onClick={() => setActiveTab('analytics')}
                          >
                            <BarChart3 className="mr-2 h-4 w-4" />
                            View Analytics
                          </Button>
                        </div>

                        <div className="mt-6 rounded-lg border p-4">
                          <h4 className="font-medium text-gray-900">Monetization Status</h4>
                          <div className="mt-3 space-y-2">
                            <div className="flex items-center justify-between">
                              <span className="text-sm text-gray-600">Setup Complete</span>
                              <Badge variant="success">Yes</Badge>
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-sm text-gray-600">Revenue Streams</span>
                              <span className="font-medium">3 Active</span>
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-sm text-gray-600">Payout Eligible</span>
                              <Badge
                                variant={
                                  (monetizationInfo.currentBalance || 0) >= 50 ? 'success' : 'warning'
                                }
                              >
                                {(monetizationInfo.currentBalance || 0) >= 50 ? 'Yes' : 'Soon'}
                              </Badge>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </TabPanel>

                <TabPanel id="purchases">
                  <div className="p-6">
                    <div className="mb-6 flex items-center justify-between">
                      <h3 className="text-lg font-medium text-gray-900">In-App Purchases</h3>
                      <div className="flex space-x-2">
                        <Button
                          variant="outline"
                          onClick={handleLinkPurchases}
                          className="flex items-center gap-2"
                        >
                          <Link className="h-4 w-4" />
                          Link Existing Purchases
                        </Button>
                        <Button
                          onClick={() => navigate('/developer/purchases/create')}
                          className="flex items-center gap-2"
                        >
                          <PlusCircle className="h-4 w-4" />
                          Create New
                        </Button>
                      </div>
                    </div>

                    {appPurchases.length === 0 ? (
                      <div className="py-12 text-center">
                        <CreditCard className="mx-auto h-12 w-12 text-gray-400" />
                        <h3 className="mt-4 text-lg font-medium text-gray-900">
                          No purchases configured
                        </h3>
                        <p className="mt-1 text-gray-600">
                          Add in-app purchases to generate additional revenue
                        </p>
                        <div className="mt-6 flex justify-center space-x-3">
                          <Button onClick={handleLinkPurchases}>Link Existing Purchases</Button>
                          <Button
                            variant="outline"
                            onClick={() => navigate('/developer/purchases/create')}
                          >
                            Create New
                          </Button>
                        </div>
                      </div>
                    ) : (
                      <div className="overflow-x-auto">
                        <table className="w-full">
                          <thead>
                            <tr className="border-b">
                              <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">
                                Title
                              </th>
                              <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">
                                Description
                              </th>
                              <th className="px-4 py-3 text-left text-sm font-medium text-gray-600">
                                Price
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
                            {appPurchases.map(purchase => (
                              <tr key={purchase.id} className="border-b hover:bg-gray-50">
                                <td className="px-4 py-3">
                                  <p className="font-medium text-gray-900">{purchase.title}</p>
                                </td>
                                <td className="px-4 py-3">
                                  <p className="text-sm text-gray-600">{purchase.description}</p>
                                </td>
                                <td className="px-4 py-3 font-medium text-gray-900">
                                  ${purchase.price.toFixed(2)}
                                </td>
                                <td className="px-4 py-3">
                                  <p className="font-medium text-green-600">
                                    ${((purchase.price || 0) * 0.7).toFixed(2)}
                                  </p>
                                  <p className="text-xs text-gray-500">Estimated revenue</p>
                                </td>
                                <td className="px-4 py-3">
                                  <div className="flex space-x-2">
                                    <Button variant="outline" size="sm">
                                      Edit
                                    </Button>
                                    <Button variant="outline" size="sm" className="text-red-600">
                                      Remove
                                    </Button>
                                  </div>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </div>
                </TabPanel>

                <TabPanel id="ads">
                  <div className="p-6">
                    <div className="mb-6 flex items-center justify-between">
                      <h3 className="text-lg font-medium text-gray-900">Advertisements</h3>
                      <Button
                        onClick={() => setShowCreateAd(true)}
                        className="flex items-center gap-2"
                      >
                        <PlusCircle className="h-4 w-4" />
                        Create Ad
                      </Button>
                    </div>

                    {showCreateAd && (
                      <Card className="mb-6">
                        <CardContent className="p-6">
                          <h4 className="mb-4 font-medium text-gray-900">Create New Ad</h4>
                          <div className="space-y-4">
                            <Input
                              label="Ad Title"
                              value={newAd.title}
                              onChange={e => setNewAd({ ...newAd, title: e.target.value })}
                              placeholder="Enter ad title"
                            />
                            <Input
                              label="Description"
                              value={newAd.description}
                              onChange={e => setNewAd({ ...newAd, description: e.target.value })}
                              placeholder="Enter ad description"
                            />
                            <Input
                              label="Price per View"
                              type="number"
                              step="0.01"
                              value={newAd.price}
                              onChange={e => setNewAd({ ...newAd, price: e.target.value })}
                              placeholder="0.50"
                            />
                            <div className="flex justify-end space-x-3">
                              <Button variant="outline" onClick={() => setShowCreateAd(false)}>
                                Cancel
                              </Button>
                              <Button onClick={handleCreateAd}>Create Ad</Button>
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    )}

                    {appAds.length === 0 ? (
                      <div className="py-12 text-center">
                        <Globe className="mx-auto h-12 w-12 text-gray-400" />
                        <h3 className="mt-4 text-lg font-medium text-gray-900">
                          No ads configured
                        </h3>
                        <p className="mt-1 text-gray-600">
                          Create ads to generate revenue from views
                        </p>
                        <Button className="mt-6" onClick={() => setShowCreateAd(true)}>
                          Create Your First Ad
                        </Button>
                      </div>
                    ) : (
                      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
                        {appAds.map(ad => (
                          <Card key={ad.id}>
                            <CardContent className="p-6">
                              <div className="flex items-start justify-between">
                                <div>
                                  <h4 className="font-medium text-gray-900">{ad.title}</h4>
                                  <p className="mt-1 text-sm text-gray-600">{ad.description}</p>
                                  <div className="mt-3">
                                    <p className="text-2xl font-bold text-primary-600">
                                      ${ad.price.toFixed(2)}
                                    </p>
                                    <p className="text-sm text-gray-500">per view</p>
                                  </div>
                                </div>
                                <Badge variant="info">Active</Badge>
                              </div>
                              <div className="mt-4 flex space-x-2">
                                <Button variant="outline" size="sm" className="flex-1">
                                  Edit
                                </Button>
                                <Button variant="outline" size="sm" className="flex-1 text-red-600">
                                  Remove
                                </Button>
                              </div>
                            </CardContent>
                          </Card>
                        ))}
                      </div>
                    )}
                  </div>
                </TabPanel>

                <TabPanel id="payout">
                  <div className="p-6">
                    <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
                      <div>
                        <h3 className="mb-4 text-lg font-medium text-gray-900">Request Payout</h3>
                        <Card>
                          <CardContent className="p-6">
                            <div className="space-y-4">
                              <div>
                                <label className="block text-sm font-medium text-gray-700">
                                  Available Balance
                                </label>
                                <p className="mt-1 text-3xl font-bold text-green-600">
                                  ${monetizationInfo.currentBalance?.toFixed(2) || '0.00'}
                                </p>
                              </div>

                              <div>
                                <label className="block text-sm font-medium text-gray-700">
                                  Payout Amount
                                </label>
                                <Input
                                  type="number"
                                  step="0.01"
                                  min="0"
                                  max={monetizationInfo.currentBalance}
                                  value={payoutAmount}
                                  onChange={e => setPayoutAmount(e.target.value)}
                                  placeholder="Enter amount"
                                />
                                <p className="mt-1 text-sm text-gray-500">Minimum payout: $50.00</p>
                              </div>

                              <div className="rounded-lg bg-yellow-50 p-4">
                                <p className="text-sm text-yellow-700">
                                  <strong>Note:</strong> Payouts are processed on the 15th of each
                                  month. Please ensure your payment information is up to date.
                                </p>
                              </div>

                              <Button
                                className="w-full"
                                onClick={handleRequestPayout}
                                disabled={!payoutAmount || parseFloat(payoutAmount) < 50}
                              >
                                Request Payout
                              </Button>
                            </div>
                          </CardContent>
                        </Card>
                      </div>

                      <div>
                        <h3 className="mb-4 text-lg font-medium text-gray-900">Payout History</h3>
                        <Card>
                          <CardContent className="p-6">
                            {(monetizationInfo.currentBalance || 0) === 0 ? (
                              <div className="py-8 text-center">
                                <DollarSign className="mx-auto h-12 w-12 text-gray-400" />
                                <p className="mt-2 text-gray-600">No payout history available</p>
                              </div>
                            ) : (
                              <div className="space-y-4">
                                <div className="rounded-lg border p-4">
                                  <div className="flex items-center justify-between">
                                    <div>
                                      <p className="font-medium text-gray-900">
                                        Estimated Next Payout
                                      </p>
                                      <p className="text-sm text-gray-600">15th of next month</p>
                                    </div>
                                    <p className="text-xl font-bold text-green-600">
                                      ${monetizationInfo.currentBalance?.toFixed(2) || '0.00'}
                                    </p>
                                  </div>
                                </div>

                                <div className="space-y-3">
                                  <div className="flex items-center justify-between">
                                    <span className="text-sm text-gray-600">Payout Method</span>
                                    <span className="font-medium">Bank Transfer</span>
                                  </div>
                                  <div className="flex items-center justify-between">
                                    <span className="text-sm text-gray-600">Processing Time</span>
                                    <span className="font-medium">3-5 business days</span>
                                  </div>
                                  <div className="flex items-center justify-between">
                                    <span className="text-sm text-gray-600">Transaction Fee</span>
                                    <span className="font-medium">2.9% + $0.30</span>
                                  </div>
                                </div>

                                <Button variant="outline" className="w-full">
                                  Update Payment Information
                                </Button>
                              </div>
                            )}
                          </CardContent>
                        </Card>
                      </div>
                    </div>
                  </div>
                </TabPanel>

                <TabPanel id="analytics">
                  <div className="p-6">
                    <h3 className="mb-6 text-lg font-medium text-gray-900">Revenue Analytics</h3>

                    <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                      <Card>
                        <CardHeader>
                          <CardTitle>Revenue Sources</CardTitle>
                        </CardHeader>
                        <CardContent>
                          <div className="space-y-4">
                            <div className="flex items-center justify-between">
                              <div className="flex items-center">
                                <div className="mr-3 h-3 w-3 rounded-full bg-blue-500"></div>
                                <span className="text-sm text-gray-600">Downloads</span>
                              </div>
                              <span className="font-medium">
                                ${monetizationInfo.downloadRevenue?.toFixed(2) || '0.00'}
                              </span>
                            </div>
                            <div className="flex items-center justify-between">
                              <div className="flex items-center">
                                <div className="mr-3 h-3 w-3 rounded-full bg-green-500"></div>
                                <span className="text-sm text-gray-600">In-App Purchases</span>
                              </div>
                              <span className="font-medium">
                                ${monetizationInfo.purchasesRevenue?.toFixed(2) || '0.00'}
                              </span>
                            </div>
                            <div className="flex items-center justify-between">
                              <div className="flex items-center">
                                <div className="mr-3 h-3 w-3 rounded-full bg-purple-500"></div>
                                <span className="text-sm text-gray-600">Ad Revenue</span>
                              </div>
                              <span className="font-medium">
                                ${monetizationInfo.adsRevenue?.toFixed(2) || '0.00'}
                              </span>
                            </div>
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
                              <span className="text-sm text-gray-600">Daily Revenue</span>
                              <span className="font-medium">
                                ${((monetizationInfo.revenue || 0) / 30).toFixed(2)}
                              </span>
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-sm text-gray-600">Conversion Rate</span>
                              <span className="font-medium">3.2%</span>
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-sm text-gray-600">User Retention</span>
                              <span className="font-medium">78.5%</span>
                            </div>
                            <div className="flex items-center justify-between">
                              <span className="text-sm text-gray-600">Avg. Revenue per User</span>
                              <span className="font-medium">
                                ${((monetizationInfo.revenue || 0) / 100).toFixed(2)}
                              </span>
                            </div>
                          </div>
                        </CardContent>
                      </Card>
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