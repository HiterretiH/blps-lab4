import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Alert } from '../components/ui/Alert';
import { Tabs, TabPanel } from '../components/ui/Tabs';
import { Badge } from '../components/ui/Badge';
import { Input } from '../components/ui/Input';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '../components/ui/Table';
import { useAuthStore } from '../store/auth.store';
import { monetizationService } from '../services/monetization.service';
import { paymentService } from '../services/payment.service';
import { applicationsService } from '../services/applications.service';
import { adsService } from '../services/ads.service';
import { purchasesService } from '../services/purchases.service';
import {
  DollarSign,
  CreditCard,
  TrendingUp,
  BarChart3,
  Download,
  Globe,
  PlusCircle,
  Link,
  ArrowLeft,
  CheckCircle,
  XCircle,
  Package,
  ShoppingBag,
} from 'lucide-react';

export const MonetizationPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();

  const [application, setApplication] = useState<any>(null);
  const [monetizationInfo, setMonetizationInfo] = useState<any>(null);
  const [ads, setAds] = useState<any[]>([]);
  const [purchases, setPurchases] = useState<any[]>([]);
  const [payoutAmount, setPayoutAmount] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('overview');
  const [showCreateAd, setShowCreateAd] = useState(false);
  const [newAd, setNewAd] = useState({ title: '', description: '', price: '' });
  const [isProcessingPayout, setIsProcessingPayout] = useState(false);
  const [monetizedAppId, setMonetizedAppId] = useState<number | null>(null);

  useEffect(() => {
    const loadData = async () => {
      if (!id) return;

      if (!monetizedAppId && monetizationInfo) {
        setMonetizedAppId(monetizationInfo.id);
      }

      setIsLoading(true);

      try {
        const appId = parseInt(id);

        const app = await applicationsService.getApplicationById(appId);
        setApplication(app);


        const monetization = await monetizationService.getMonetizationInfo(appId);
        if (monetization) {
          setMonetizationInfo(monetization);
          setMonetizedAppId(monetization.id);

          try {
            const adsData = await adsService.getAdsByMonetizedApp(monetization.id);
            setAds(adsData);
          } catch (adsError) {
            console.error('Error loading ads:', adsError);
            setAds([]);
          }

          const allPurchases = await purchasesService.getAllPurchases();
          setPurchases(allPurchases);
        }
      } catch (err: any) {
        console.error('Failed to load data:', err);
        setError(err.message || 'Failed to load monetization data');
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, [id]);

  const handleRequestPayout = async () => {
    if (!monetizationInfo || !payoutAmount) {
      setError('Please enter payout amount');
      return;
    }

    setIsProcessingPayout(true);
    setError(null);
    setSuccess(null);

    try {
      const amount = parseFloat(payoutAmount);
      if (amount <= 0 || amount > (monetizationInfo.currentBalance || 0)) {
        setError(
          `Invalid payout amount. Available balance: $${monetizationInfo.currentBalance?.toFixed(2)}`
        );
        setIsProcessingPayout(false);
        return;
      }

      console.log('Step 1: Creating payment request...');
      const paymentRequest = await paymentService.createPaymentRequest(
        monetizationInfo.application.id,
        amount
      );
      console.log('Payment request created:', paymentRequest);

      console.log('Step 2: Validating card...');
      console.log(
        `Using payment request ID: ${paymentRequest.id} (not applicationId: ${paymentRequest.applicationId})`
      );

      const isValid = await paymentService.validateCard(paymentRequest.id);
      console.log('Card validation result:', isValid);

      if (isValid) {
        console.log('Step 3: Making payout...');
        const result = await monetizationService.makePayout(paymentRequest);

        setSuccess(`✅ Payout successful! ${result}`);
        setPayoutAmount('');

        if (application) {
          const updated = await monetizationService.getMonetizationInfo(application.id);
          setMonetizationInfo(updated);
        }
      } else {
        setError('Card validation failed. Please check your payment information.');
      }
    } catch (err: any) {
      console.error('Payout error:', err);
      setError(err.message || 'Payout request failed');
    } finally {
      setIsProcessingPayout(false);
    }
  };

  const handleCreateAd = async () => {
    if (!monetizedAppId || !newAd.title || !newAd.price) {
      setError('Please fill all required fields');
      return;
    }

    try {
      await adsService.createAd({
        monetizedApplicationId: monetizedAppId,
        title: newAd.title,
        description: newAd.description,
        price: parseFloat(newAd.price),
      });

      const adsData = await adsService.getAdsByMonetizedApp(monetizedAppId);
      setAds(adsData);

      setNewAd({ title: '', description: '', price: '' });
      setShowCreateAd(false);
      setSuccess('Ad created successfully!');
    } catch (err: any) {
      console.error('Create ad error:', err);
      setError(err.message || 'Failed to create ad');
    }
  };

  const handleLinkPurchases = async () => {
    if (!monetizationInfo) return;

    try {
      const linked = await purchasesService.linkToMonetizedApp(monetizationInfo.id);
      setPurchases(linked);
      setSuccess('Purchases linked successfully!');

      const allPurchases = await purchasesService.getAllPurchases();
      setPurchases(allPurchases);
    } catch (err: any) {
      console.error('Link purchases error:', err);
      setError(err.message || 'Failed to link purchases');
    }
  };

  const handleCreatePurchase = async () => {
    try {
      const newPurchases = await purchasesService.createPurchases({
        titles: ['New Item'],
        descriptions: ['New item description'],
        prices: [0.99],
      });

      if (monetizationInfo && newPurchases.length > 0) {
        await purchasesService.linkToMonetizedApp(monetizationInfo.id);
      }

      const updatedPurchases = await purchasesService.getAllPurchases();
      setPurchases(updatedPurchases);
      setSuccess('Purchase created and linked successfully!');
    } catch (err: any) {
      console.error('Create purchase error:', err);
      setError(err.message || 'Failed to create purchase');
    }
  };

  const appPurchases = purchases.filter(
    purchase =>
      purchase.monetizedApplication && purchase.monetizedApplication.id === monetizationInfo?.id
  );

  const tabs = [
    { id: 'overview', label: 'Overview' },
    { id: 'purchases', label: 'In-App Purchases' },
    { id: 'ads', label: 'Ads' },
    { id: 'payout', label: 'Payout' },
  ];

  if (!user || (user.role !== 'DEVELOPER' && user.role !== 'PRIVACY_POLICY')) {
    navigate('/dashboard');
    return null;
  }

  return (
    <div className="space-y-6">
      {/* Заголовок */}
      <div className="flex items-start justify-between">
        <div>
          <Button
            variant="outline"
            onClick={() => navigate(-1)}
            className="mb-4 flex items-center gap-2"
          >
            <ArrowLeft className="h-4 w-4" />
            Back
          </Button>
          <h1 className="text-3xl font-bold text-gray-900">Monetization</h1>
          {application && (
            <div className="mt-1 flex items-center gap-3">
              <span className="text-gray-600">Application:</span>
              <Badge variant="info">{application.name}</Badge>
              <span className="text-sm text-gray-500">ID: #{application.id}</span>
              {monetizationInfo && (
                <Badge variant="success" className="flex items-center gap-1">
                  <DollarSign className="h-3 w-3" />
                  Monetized
                </Badge>
              )}
            </div>
          )}
        </div>
      </div>

      {error && (
        <Alert variant="danger" title="Error">
          {error}
        </Alert>
      )}

      {success && (
        <Alert variant="success" title="Success">
          {success}
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
                  if (!application || !user.userId) {
                    setError('Missing application or user ID');
                    return;
                  }

                  const response = await fetch(
                    `http://localhost:727/api/developers/by-user/${user.userId}`,
                    {
                      headers: {
                        Authorization: `Bearer ${localStorage.getItem('auth_token')}`,
                        'Content-Type': 'application/json',
                      },
                    }
                  );

                  if (!response.ok) {
                    throw new Error('Failed to fetch developer');
                  }

                  const developer = await response.json();

                  const newMonetized = await monetizationService.createMonetizedApplication({
                    developerId: developer.id,
                    applicationId: application.id,
                    currentBalance: 0,
                    revenue: 0,
                    downloadRevenue: 0,
                    adsRevenue: 0,
                    purchasesRevenue: 0,
                  });
                  setMonetizationInfo(newMonetized);
                  setSuccess('Monetization setup successful!');
                } catch (err: any) {
                  console.error('Failed to create monetization:', err);
                  setError(err.message || 'Failed to create monetization');
                }
              }}
            >
              Set Up Monetization
            </Button>
          </CardContent>
        </Card>
      ) : (
        <>
          {/* Карточки статистики */}
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
            <Card>
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

            <Card>
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

            <Card>
              <CardContent className="p-6">
                <div className="flex items-center">
                  <div className="mr-4 rounded-lg bg-purple-100 p-3">
                    <ShoppingBag className="h-6 w-6 text-purple-600" />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-600">Purchase Revenue</p>
                    <p className="mt-1 text-2xl font-bold text-gray-900">
                      ${monetizationInfo.purchasesRevenue?.toFixed(2) || '0.00'}
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
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

          {/* Вкладки */}
          <Card>
            <CardContent className="p-0">
              <Tabs tabs={tabs} defaultTab={activeTab}>
                {/* Overview Tab */}
                <TabPanel id="overview">
                  <div className="p-6">
                    <h3 className="mb-4 text-lg font-medium text-gray-900">Revenue Breakdown</h3>
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
                            <ShoppingBag className="mr-3 h-5 w-5 text-green-600" />
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

                    <div className="mt-6 grid grid-cols-1 gap-4 md:grid-cols-2">
                      <div className="rounded-lg bg-gray-50 p-4">
                        <h4 className="font-medium text-gray-900">Quick Stats</h4>
                        <div className="mt-3 space-y-2">
                          <div className="flex items-center justify-between">
                            <span className="text-sm text-gray-600">Total Purchases</span>
                            <span className="font-medium">{appPurchases.length}</span>
                          </div>
                          <div className="flex items-center justify-between">
                            <span className="text-sm text-gray-600">Active Ads</span>
                            <span className="font-medium">{ads.length}</span>
                          </div>
                          <div className="flex items-center justify-between">
                            <span className="text-sm text-gray-600">Payout Eligible</span>
                            <Badge
                              variant={monetizationInfo.currentBalance >= 1 ? 'success' : 'warning'}
                            >
                              {monetizationInfo.currentBalance >= 1 ? 'Yes' : 'No'}
                            </Badge>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </TabPanel>

                {/* Purchases Tab */}
                <TabPanel id="purchases">
                  <div className="p-6">
                    <div className="mb-6 flex items-center justify-between">
                      <div>
                        <h3 className="text-lg font-medium text-gray-900">In-App Purchases</h3>
                        <p className="mt-1 text-sm text-gray-600">
                          {appPurchases.length} purchases linked to this application
                        </p>
                      </div>
                      <div className="flex space-x-2">
                        <Button
                          variant="outline"
                          onClick={handleLinkPurchases}
                          className="flex items-center gap-2"
                        >
                          <Link className="h-4 w-4" />
                          Link Purchases
                        </Button>
                        <Button onClick={handleCreatePurchase} className="flex items-center gap-2">
                          <PlusCircle className="h-4 w-4" />
                          Create New
                        </Button>
                      </div>
                    </div>

                    {appPurchases.length === 0 ? (
                      <div className="py-12 text-center">
                        <ShoppingBag className="mx-auto h-12 w-12 text-gray-400" />
                        <h3 className="mt-4 text-lg font-medium text-gray-900">
                          No purchases linked
                        </h3>
                        <p className="mt-1 text-gray-600">
                          Link existing purchases or create new ones for this application
                        </p>
                        <div className="mt-6 flex justify-center space-x-3">
                          <Button onClick={handleLinkPurchases}>Link Existing Purchases</Button>
                          <Button variant="outline" onClick={handleCreatePurchase}>
                            Create New
                          </Button>
                        </div>
                      </div>
                    ) : (
                      <div className="overflow-x-auto">
                        <Table>
                          <TableHeader>
                            <TableRow>
                              <TableHead>ID</TableHead>
                              <TableHead>Title</TableHead>
                              <TableHead>Description</TableHead>
                              <TableHead>Price</TableHead>
                              <TableHead>Status</TableHead>
                            </TableRow>
                          </TableHeader>
                          <TableBody>
                            {appPurchases.map(purchase => (
                              <TableRow key={purchase.id}>
                                <TableCell className="font-medium">#{purchase.id}</TableCell>
                                <TableCell className="font-medium">{purchase.title}</TableCell>
                                <TableCell>
                                  <p className="text-sm text-gray-600">{purchase.description}</p>
                                </TableCell>
                                <TableCell className="font-medium">
                                  ${purchase.price?.toFixed(2) || '0.00'}
                                </TableCell>
                                <TableCell>
                                  <Badge variant="success">
                                    <CheckCircle className="h-3 w-3 mr-1" />
                                    Active
                                  </Badge>
                                </TableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>

                        <div className="mt-4 text-sm text-gray-500">
                          Total purchases value: $
                          {appPurchases.reduce((sum, p) => sum + (p.price || 0), 0).toFixed(2)}
                        </div>
                      </div>
                    )}

                    {/* Все доступные покупки */}
                    <div className="mt-8">
                      <h4 className="mb-4 font-medium text-gray-900">
                        All Available Purchases ({purchases.length})
                      </h4>
                      {purchases.length > 0 && (
                        <div className="rounded-lg border">
                          <div className="max-h-60 overflow-y-auto">
                            <Table>
                              <TableHeader>
                                <TableRow>
                                  <TableHead>ID</TableHead>
                                  <TableHead>Title</TableHead>
                                  <TableHead>Price</TableHead>
                                  <TableHead>Linked To</TableHead>
                                </TableRow>
                              </TableHeader>
                              <TableBody>
                                {purchases.slice(0, 10).map(purchase => (
                                  <TableRow key={purchase.id}>
                                    <TableCell className="font-medium">#{purchase.id}</TableCell>
                                    <TableCell>{purchase.title}</TableCell>
                                    <TableCell className="font-medium">
                                      ${purchase.price?.toFixed(2) || '0.00'}
                                    </TableCell>
                                    <TableCell>
                                      {purchase.monetizedApplication &&
                                      purchase.monetizedApplication.id === monetizationInfo.id ? (
                                        <Badge variant="success">This App</Badge>
                                      ) : purchase.monetizedApplication ? (
                                        <Badge variant="warning">Another App</Badge>
                                      ) : (
                                        <Badge variant="secondary">Not Linked</Badge>
                                      )}
                                    </TableCell>
                                  </TableRow>
                                ))}
                              </TableBody>
                            </Table>
                          </div>
                          {purchases.length > 10 && (
                            <div className="border-t p-3 text-center text-sm text-gray-500">
                              Showing 10 of {purchases.length} purchases
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                </TabPanel>

                {/* Ads Tab */}
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

                    {ads.length === 0 ? (
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
                      <div className="overflow-x-auto">
                        <Table>
                          <TableHeader>
                            <TableRow>
                              <TableHead>ID</TableHead>
                              <TableHead>Title</TableHead>
                              <TableHead>Description</TableHead>
                              <TableHead>Price per View</TableHead>
                              <TableHead>Status</TableHead>
                              <TableHead>Actions</TableHead>
                            </TableRow>
                          </TableHeader>
                          <TableBody>
                            {ads.map(ad => (
                              <TableRow key={ad.id}>
                                <TableCell className="font-medium">#{ad.id}</TableCell>
                                <TableCell className="font-medium">{ad.title}</TableCell>
                                <TableCell>
                                  <p className="text-sm text-gray-600">{ad.description}</p>
                                </TableCell>
                                <TableCell className="font-medium">
                                  ${ad.price?.toFixed(2) || '0.00'}
                                </TableCell>
                                <TableCell>
                                  <Badge variant="success">
                                    <CheckCircle className="h-3 w-3 mr-1" />
                                    Active
                                  </Badge>
                                </TableCell>
                                <TableCell>
                                  <Button
                                    variant="outline"
                                    size="sm"
                                    className="text-red-600"
                                    onClick={async () => {
                                      if (
                                        window.confirm('Are you sure you want to delete this ad?')
                                      ) {
                                        try {
                                          await adsService.deleteAd(ad.id);

                                          if (monetizedAppId) {
                                            const updatedAds =
                                              await adsService.getAdsByMonetizedApp(monetizedAppId);
                                            setAds(updatedAds);
                                          }
                                          setSuccess('Ad deleted successfully!');
                                        } catch (err) {
                                          console.error('Failed to delete ad:', err);
                                          setError('Failed to delete ad');
                                        }
                                      }
                                    }}
                                  >
                                    <XCircle className="h-4 w-4 mr-1" />
                                    Delete
                                  </Button>
                                </TableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>

                        <div className="mt-4 text-sm text-gray-500">
                          Total ads: {ads.length} | Total daily potential: $
                          {(ads.reduce((sum, ad) => sum + (ad.price || 0), 0) * 100).toFixed(2)}{' '}
                          (estimated)
                        </div>
                      </div>
                    )}
                  </div>
                </TabPanel>

                {/* Payout Tab */}
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
                                <p className="mt-1 text-sm text-gray-500">
                                  This is the amount you can withdraw
                                </p>
                              </div>

                              <div>
                                <label className="block text-sm font-medium text-gray-700">
                                  Payout Amount
                                </label>
                                <Input
                                  type="number"
                                  step="0.01"
                                  min="1"
                                  max={monetizationInfo.currentBalance}
                                  value={payoutAmount}
                                  onChange={e => setPayoutAmount(e.target.value)}
                                  placeholder="Enter amount"
                                  disabled={isProcessingPayout}
                                />
                                <div className="mt-2 flex gap-2">
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    onClick={() =>
                                      setPayoutAmount(
                                        (monetizationInfo.currentBalance * 0.25).toFixed(2)
                                      )
                                    }
                                    disabled={isProcessingPayout}
                                  >
                                    25%
                                  </Button>
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    onClick={() =>
                                      setPayoutAmount(
                                        (monetizationInfo.currentBalance * 0.5).toFixed(2)
                                      )
                                    }
                                    disabled={isProcessingPayout}
                                  >
                                    50%
                                  </Button>
                                  <Button
                                    size="sm"
                                    variant="outline"
                                    onClick={() =>
                                      setPayoutAmount(
                                        monetizationInfo.currentBalance?.toFixed(2) || '0'
                                      )
                                    }
                                    disabled={isProcessingPayout}
                                  >
                                    100%
                                  </Button>
                                </div>
                                <p className="mt-1 text-sm text-gray-500">Minimum payout: $1.00</p>
                              </div>

                              <Button
                                className="w-full"
                                onClick={handleRequestPayout}
                                disabled={
                                  isProcessingPayout ||
                                  !payoutAmount ||
                                  parseFloat(payoutAmount) < 1 ||
                                  parseFloat(payoutAmount) > (monetizationInfo.currentBalance || 0)
                                }
                                isLoading={isProcessingPayout}
                              >
                                {isProcessingPayout ? 'Processing...' : 'Request Payout'}
                              </Button>
                            </div>
                          </CardContent>
                        </Card>
                      </div>

                      <div>
                        <h3 className="mb-4 text-lg font-medium text-gray-900">
                          Payout Information
                        </h3>
                        <Card>
                          <CardContent className="p-6">
                            <div className="space-y-4">
                              <div className="rounded-lg bg-blue-50 p-4">
                                <p className="text-sm text-blue-700">
                                  <strong>Note:</strong> Payouts are processed within 3-5 business
                                  days. Please ensure your payment information is up to date.
                                </p>
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
                                  <span className="text-sm text-gray-600">Minimum Payout</span>
                                  <span className="font-medium">$1.00</span>
                                </div>
                                <div className="flex items-center justify-between">
                                  <span className="text-sm text-gray-600">Transaction Fee</span>
                                  <span className="font-medium">2.9%</span>
                                </div>
                                <div className="flex items-center justify-between">
                                  <span className="text-sm text-gray-600">Next Payout Date</span>
                                  <span className="font-medium">15th of next month</span>
                                </div>
                              </div>

                              <div className="pt-4 border-t">
                                <h4 className="font-medium text-gray-900 mb-2">Payout History</h4>
                                <div className="text-center py-8">
                                  <CreditCard className="mx-auto h-12 w-12 text-gray-400" />
                                  <p className="mt-2 text-gray-600">No payout history available</p>
                                  <p className="text-sm text-gray-500">
                                    Your first payout will appear here
                                  </p>
                                </div>
                              </div>

                              <Button variant="outline" className="w-full">
                                Update Payment Information
                              </Button>
                            </div>
                          </CardContent>
                        </Card>
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
