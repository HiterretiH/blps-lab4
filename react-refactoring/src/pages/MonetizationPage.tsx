import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Alert } from '../components/ui/Alert';
import { Tabs, TabPanel } from '../components/ui/Tabs';
import { Badge } from '../components/ui/Badge';
import { Input } from '../components/ui/Input';
import { Modal } from '../components/ui/Modal';
import { InAppPurchaseForm } from '@/components/purchases/InAppPurchaseForm';
import { InAppPurchaseTable } from '@/components/purchases/InAppPurchaseTable';
import { DeletePurchaseModal } from '@/components/purchases/DeletePurchaseModal';
import { useInAppAdds } from '@/hooks/useInAppAdds';
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
import { useInAppPurchases } from '@/hooks/useInAppPurchases';
import { useToast } from '@/hooks/useToast';
import { InAppPurchase, InAppAdd } from '@/types';
import { InAppAddForm } from '@/components/adds/InAppAddForm';
import { InAppAddTable } from '@/components/adds/InAppAddTable';
import { DeleteAddModal } from '@/components/adds/DeleteAppModal';
import { inAppAddSchema } from '@/components/schemas/inAppAdd.schema';
import {
  DollarSign,
  CreditCard,
  TrendingUp,
  BarChart3,
  Download,
  Globe,
  PlusCircle,
  Link as LinkIcon,
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
  const { toast } = useToast();

  const [application, setApplication] = useState<any>(null);
  const [monetizationInfo, setMonetizationInfo] = useState<any>(null);
  const [payoutAmount, setPayoutAmount] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('overview');
  const [isProcessingPayout, setIsProcessingPayout] = useState(false);
  const [monetizedAppId, setMonetizedAppId] = useState<number | null>(null);
  
  const [purchaseToEdit, setPurchaseToEdit] = useState<InAppPurchase | null>(null);
  const [purchaseToDelete, setPurchaseToDelete] = useState<number | null>(null);
  const [isDeleteModalOpen, setDeleteModalOpen] = useState(false);
  const [isLinkingPurchases, setIsLinkingPurchases] = useState(false);
  
  const [isCreatingPurchase, setIsCreatingPurchase] = useState(false);
  const [isUpdatingPurchase, setIsUpdatingPurchase] = useState(false);
  const [isDeletingPurchase, setIsDeletingPurchase] = useState(false);

  const [adToEdit, setAdToEdit] = useState<InAppAdd | null>(null);
  const [adToDelete, setAdToDelete] = useState<number | null>(null);
  const [isDeleteAdModalOpen, setDeleteAdModalOpen] = useState(false);
  const [isCreatingAd, setIsCreatingAd] = useState(false);
  const [isUpdatingAd, setIsUpdatingAd] = useState(false);
  const [isDeletingAd, setIsDeletingAd] = useState(false);

  const hasLoadedData = useRef(false);
  const hasFetchedPurchases = useRef(false);

  const {
    purchases,
    isLoading: isLoadingPurchases,
    error: purchasesError,
    fetchPurchases,
    createPurchase: createPurchaseHook,
    updatePurchase: updatePurchaseHook,
    deletePurchase: deletePurchaseHook,
  } = useInAppPurchases();

  const {
    ads,
    isLoading: isLoadingAds,
    error: adsError,
    fetchAds,
    createAd,
    updateAd,
    deleteAd,
  } = useInAppAdds(monetizedAppId || undefined);

  const loadData = useCallback(async () => {
    if (!id) return;
    
    if (hasLoadedData.current) {
      console.log('Data already loaded, skipping');
      return;
    }
    
    setIsLoading(true);
    setError(null);

    try {
      const appId = parseInt(id);
      console.log(`Loading data for app ID: ${appId}`);

      const app = await applicationsService.getApplicationById(appId);
      setApplication(app);

      const monetization = await monetizationService.getMonetizationInfo(appId);
      if (monetization) {
        console.log(`Found monetization for app ${appId}:`, monetization);
        setMonetizationInfo(monetization);
        setMonetizedAppId(monetization.id);
      } else {
        console.log(`No monetization found for app ${appId}`);
      }
      
      hasLoadedData.current = true;
    } catch (err: any) {
      console.error('Failed to load data:', err);
      setError(err.message || 'Failed to load monetization data');
    } finally {
      setIsLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  useEffect(() => {
    if (monetizedAppId && !hasFetchedPurchases.current) {
      console.log(`monetizedAppId changed to ${monetizedAppId}, fetching data`);
      hasFetchedPurchases.current = true;
      
      fetchPurchases();
      fetchAds();
    }
  }, [monetizedAppId, fetchPurchases, fetchAds]);

  useEffect(() => {
    if (purchasesError) {
      setError(purchasesError);
    }
    if (adsError) {
      setError(adsError);
    }
  }, [purchasesError, adsError]);

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

      const paymentRequest = await paymentService.createPaymentRequest(
        monetizationInfo.application.id,
        amount
      );

      const isValid = await paymentService.validateCard(paymentRequest.id);

      if (isValid) {
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

  const handleLinkPurchases = async () => {
    if (!monetizationInfo) return;

    setIsLinkingPurchases(true);
    setError(null);

    try {
      await purchasesService.linkToMonetizedApp(monetizationInfo.id);
      setSuccess('Purchases linked successfully!');
      
      await fetchPurchases();
    } catch (err: any) {
      console.error('Link purchases error:', err);
      setError(err.message || 'Failed to link purchases');
    } finally {
      setIsLinkingPurchases(false);
    }
  };

  const handleCreatePurchase = async (data: any) => {
    setIsCreatingPurchase(true);
    setError(null);

    try {
      await createPurchaseHook({
        ...data,
        monetizedApplicationId: monetizationInfo?.id,
      });
      setSuccess('Purchase created successfully!');
    } catch (err: any) {
      console.error('Create purchase error:', err);
      setError(err.message || 'Failed to create purchase');
    } finally {
      setIsCreatingPurchase(false);
    }
  };

  const handleUpdatePurchase = async (id: number, data: any) => {
    setIsUpdatingPurchase(true);
    setError(null);

    try {
      await updatePurchaseHook(id, data);
      setSuccess('Purchase updated successfully!');
      setPurchaseToEdit(null);
    } catch (err: any) {
      console.error('Update purchase error:', err);
      setError(err.message || 'Failed to update purchase');
    } finally {
      setIsUpdatingPurchase(false);
    }
  };

  const handleDeletePurchase = async (id: number) => {
    setIsDeletingPurchase(true);
    setError(null);

    try {
      await deletePurchaseHook(id);
      setSuccess('Purchase deleted successfully!');
      setDeleteModalOpen(false);
      setPurchaseToDelete(null);
    } catch (err: any) {
      console.error('Delete purchase error:', err);
      setError(err.message || 'Failed to delete purchase');
    } finally {
      setIsDeletingPurchase(false);
    }
  };

  const handleCreateAd = async (data: any) => {
  if (!monetizedAppId) return;
  
  setIsCreatingAd(true);
  setError(null);

  try {
    await createAd({
      ...data,
      monetizedApplicationId: monetizedAppId,
    });
    setSuccess('Ad created successfully!');
  } catch (err: any) {
    console.error('Create ad error:', err);
    setError(err.message || 'Failed to create ad');
  } finally {
    setIsCreatingAd(false);
  }
};

const handleUpdateAd = async (id: number, data: any) => {
  setIsUpdatingAd(true);
  setError(null);

  try {
    await updateAd(id, {
      title: data.title,
      description: data.description,
      price: data.price,
      monetizedApplicationId: monetizedAppId,
    });
    setSuccess('Ad updated successfully!');
    setAdToEdit(null);
  } catch (err: any) {
    console.error('Update ad error:', err);
    setError(err.message || 'Failed to update ad');
  } finally {
    setIsUpdatingAd(false);
  }
};

const handleDeleteAd = async (id: number) => {
  setIsDeletingAd(true);
  setError(null);

  try {
    await deleteAd(id);
    setSuccess('Ad deleted successfully!');
    setDeleteAdModalOpen(false);
    setAdToDelete(null);
  } catch (err: any) {
    console.error('Delete ad error:', err);
    setError(err.message || 'Failed to delete ad');
  } finally {
    setIsDeletingAd(false);
  }
};

  const appPurchases = purchases.filter(
    purchase =>
      purchase.monetizedApplication && 
      purchase.monetizedApplication.id === monetizationInfo?.id
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
                  
                  loadData();
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
                    <div className="mb-6">
                      <h3 className="text-lg font-medium text-gray-900">In-App Purchases</h3>
                      <p className="mt-1 text-sm text-gray-600">
                        Create and manage in-app purchases for your application
                      </p>
                    </div>

                    {/* Форма создания покупки */}
                    <div className="mb-8">
                      <InAppPurchaseForm
                        onSubmit={handleCreatePurchase}
                        monetizedAppId={monetizationInfo?.id}
                        isLoading={isCreatingPurchase}
                        submitButtonText="Create Purchase"
                      />
                    </div>

                    {/* Таблица покупок */}
                    <div>
                      <div className="mb-4 flex items-center justify-between">
                        <h4 className="font-medium text-gray-900">
                          Linked Purchases ({appPurchases.length})
                        </h4>
                        <div className="flex space-x-2">
                          <Button
                            variant="outline"
                            onClick={handleLinkPurchases}
                            isLoading={isLinkingPurchases}
                          >
                            <LinkIcon className="h-4 w-4 mr-2" />
                            Link All Unlinked
                          </Button>
                        </div>
                      </div>

                      <InAppPurchaseTable
                        purchases={appPurchases}
                        monetizedAppId={monetizationInfo?.id}
                        onEdit={(purchase) => setPurchaseToEdit(purchase)}
                        onDelete={(id) => {
                          setPurchaseToDelete(id);
                          setDeleteModalOpen(true);
                        }}
                        isLoading={isLoadingPurchases}
                      />

                      {/* Модальное окно редактирования */}
                      {purchaseToEdit && (
                        <Modal
                          isOpen={!!purchaseToEdit}
                          onClose={() => setPurchaseToEdit(null)}
                          title="Edit Purchase"
                        >
                          <InAppPurchaseForm
                            defaultValues={{
                              title: purchaseToEdit.title,
                              description: purchaseToEdit.description || '',
                              price: purchaseToEdit.price,
                            }}
                            onSubmit={async (data) => {
                              await handleUpdatePurchase(purchaseToEdit.id, data);
                            }}
                            isLoading={isUpdatingPurchase}
                            submitButtonText="Save Changes"
                          />
                        </Modal>
                      )}

                      {/* Модальное окно удаления */}
                      <DeletePurchaseModal
                        isOpen={isDeleteModalOpen}
                        onClose={() => {
                          setDeleteModalOpen(false);
                          setPurchaseToDelete(null);
                        }}
                        onConfirm={async () => {
                          if (purchaseToDelete) {
                            await handleDeletePurchase(purchaseToDelete);
                          }
                        }}
                        purchaseTitle={
                          purchases.find(p => p.id === purchaseToDelete)?.title
                        }
                        isLoading={isDeletingPurchase}
                      />
                    </div>
                  </div>
                </TabPanel>

                {/* Ads Tab */}
                <TabPanel id="ads">
                <div className="p-6">
                  <div className="mb-6">
                    <h3 className="text-lg font-medium text-gray-900">Advertisements</h3>
                    <p className="mt-1 text-sm text-gray-600">
                      Create and manage in-app advertisements for revenue generation
                    </p>
                  </div>

                  {/* Форма создания рекламы */}
                  <div className="mb-8">
                    <InAppAddForm
                      onSubmit={handleCreateAd}
                      monetizedAppId={monetizationInfo?.id}
                      isLoading={isCreatingAd}
                      submitButtonText="Create Ad"
                    />
                  </div>

                  {/* Таблица рекламы */}
                  <div>
                    <div className="mb-4 flex items-center justify-between">
                      <h4 className="font-medium text-gray-900">
                        Active Ads ({ads.length})
                      </h4>
                    </div>

                    <InAppAddTable
                      ads={ads}
                      onEdit={(ad) => setAdToEdit(ad)}
                      onDelete={(id) => {
                        setAdToDelete(id);
                        setDeleteAdModalOpen(true);
                      }}
                      onViewStats={(id) => {
                        toast.info('Ad statistics view coming soon');
                      }}
                      isLoading={isLoadingAds}
                    />

                    {adToEdit && (
                    <Modal
                      isOpen={!!adToEdit}
                      onClose={() => setAdToEdit(null)}
                      title="Edit Advertisement"
                    >
                      <InAppAddForm
                        defaultValues={{
                          title: adToEdit.title,
                          description: adToEdit.description || '',
                          price: adToEdit.price,
                          monetizedApplicationId: monetizedAppId,
                        }}
                        onSubmit={async (data) => {
                          await handleUpdateAd(adToEdit.id, data);
                        }}
                        isLoading={isUpdatingAd}
                        submitButtonText="Save Changes"
                      />
                    </Modal>
                  )}

                    {/* Модальное окно удаления рекламы */}
                    <DeleteAddModal
                      isOpen={isDeleteAdModalOpen}
                      onClose={() => {
                        setDeleteAdModalOpen(false);
                        setAdToDelete(null);
                      }}
                      onConfirm={async () => {
                        if (adToDelete) {
                          await handleDeleteAd(adToDelete);
                        }
                      }}
                      adTitle={
                        ads.find(ad => ad.id === adToDelete)?.title
                      }
                      isLoading={isDeletingAd}
                    />
                  </div>

                  {/* Блок статистики рекламы */}
                  {ads.length > 0 && (
                    <div className="mt-8 rounded-lg bg-gradient-to-r from-blue-50 to-indigo-50 p-6">
                      <h4 className="mb-4 text-lg font-medium text-gray-900">Ad Revenue Insights</h4>
                      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                        <div className="rounded-lg bg-white p-4 shadow">
                          <p className="text-sm text-gray-600">Estimated Daily Revenue</p>
                          <p className="mt-1 text-2xl font-bold text-green-600">
                            ${(ads.reduce((sum, ad) => sum + ad.price * 100, 0)).toFixed(2)}
                          </p>
                          <p className="text-xs text-gray-500">Based on 100 views per ad per day</p>
                        </div>
                        
                        <div className="rounded-lg bg-white p-4 shadow">
                          <p className="text-sm text-gray-600">Monthly Potential</p>
                          <p className="mt-1 text-2xl font-bold text-blue-600">
                            ${(ads.reduce((sum, ad) => sum + ad.price * 100 * 30, 0)).toFixed(2)}
                          </p>
                          <p className="text-xs text-gray-500">30-day projection</p>
                        </div>
                        
                        <div className="rounded-lg bg-white p-4 shadow">
                          <p className="text-sm text-gray-600">Average Price per View</p>
                          <p className="mt-1 text-2xl font-bold text-purple-600">
                            ${(ads.reduce((sum, ad) => sum + ad.price, 0) / ads.length).toFixed(2)}
                          </p>
                          <p className="text-xs text-gray-500">Across all ads</p>
                        </div>
                      </div>
                      
                      <div className="mt-4 text-sm text-gray-600">
                        <p>
                          💡 <strong>Tip:</strong> Higher-priced ads (${'>'}1.00) typically perform better in premium apps. 
                          Consider A/B testing different ad placements and formats.
                        </p>
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