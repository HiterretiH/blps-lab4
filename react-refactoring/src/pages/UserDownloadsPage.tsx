import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Alert } from '../components/ui/Alert';
import { Badge } from '../components/ui/Badge';
import { Input } from '../components/ui/Input';
import { useAuthStore } from '../store/auth.store';
import { useDownloadsStore } from '../store/downloads.store';
import { purchasesService } from '../services/purchases.service';
import { adsService } from '../services/ads.service';
import { userActionsService } from '../services/userAction.service';
import { monetizationService } from '../services/monetization.service';
import { applicationsService } from '../services/applications.service';
import {
  Download,
  ShoppingBag,
  Eye,
  Package,
  CreditCard,
  ArrowLeft,
  DollarSign,
  BarChart3,
  Calendar,
  Trash2,
  ExternalLink,
  List,
  Tag,
  ArrowRight,
  Sparkles,
  Gift,
  Megaphone,
} from 'lucide-react';

export const UserDownloadsPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [selectedAppId, setSelectedAppId] = useState<number | null>(null);
  const [allPurchases, setAllPurchases] = useState<any[]>([]);
  const [allAds, setAllAds] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [appDetailsMap, setAppDetailsMap] = useState<Record<number, any>>({});

  const [showPaymentForm, setShowPaymentForm] = useState<number | null>(null);
  const [cardDetails, setCardDetails] = useState({
    cardNumber: '',
    cardHolderName: '',
    expiryDate: '',
    cvv: '',
  });
  const [cardErrors, setCardErrors] = useState<string[]>([]);
  const [processingPurchase, setProcessingPurchase] = useState(false);

  const cardRegex = {
    cardNumber: /^\d{16}$/, // ровно 16 цифр
    cardHolderName: /^[a-zA-Z\s]{3,}$/, // только буквы и пробелы, минимум 3 символа
    expiryDate: /^\d{2}\.\d{2}\.\d{4}$/, // формат dd.mm.yyyy
    cvv: /^\d{3,4}$/, // 3 или 4 цифры
  };

  const {
    downloadedApps,
    removeDownload,
    getDownloadStats,
    isLoading: downloadsLoading,
  } = useDownloadsStore();

  const downloadStats = getDownloadStats();

  useEffect(() => {
    if (downloadedApps.length > 0) {
      loadAllContent();
    }
  }, [downloadedApps]);

  useEffect(() => {
    const loadAppDetails = async () => {
      const details: Record<number, any> = {};
      for (const app of downloadedApps) {
        try {
          const appDetail = await applicationsService.getApplicationById(app.applicationId);
          details[app.applicationId] = appDetail;
        } catch (err) {
          console.error(`Error loading app details for ${app.applicationId}:`, err);
        }
      }
      setAppDetailsMap(details);
    };

    if (downloadedApps.length > 0) {
      loadAppDetails();
    }
  }, [downloadedApps]);

  useEffect(() => {
    if (downloadedApps.length > 0 && !selectedAppId) {
      setSelectedAppId(downloadedApps[0].applicationId);
    }
  }, [downloadedApps, selectedAppId]);

  const loadAllContent = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const purchasesData = await purchasesService.getAllPurchases();
      setAllPurchases(purchasesData);

      const adsData = await adsService.getAllAds();
      setAllAds(adsData);
    } catch (error) {
      console.error('Error loading content:', error);
      setError('Failed to load content');
    } finally {
      setIsLoading(false);
    }
  };

  const validateCardDetails = () => {
    const errors = [];

    if (!cardDetails.cardNumber.match(cardRegex.cardNumber)) {
      errors.push('Card number must be exactly 16 digits');
    }

    if (!cardDetails.cardHolderName.match(cardRegex.cardHolderName)) {
      errors.push('Card holder name must contain only letters and spaces, minimum 3 characters');
    }

    if (!cardDetails.expiryDate.match(cardRegex.expiryDate)) {
      errors.push('Expiry date must be in format DD.MM.YYYY (e.g., 01.12.2025)');
    } else {
      const [day, month, year] = cardDetails.expiryDate.split('.').map(Number);
      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth() + 1;

      if (year < currentYear || (year === currentYear && month < currentMonth)) {
        errors.push('Card has expired');
      }
    }

    if (!cardDetails.cvv.match(cardRegex.cvv)) {
      errors.push('CVV must be 3 or 4 digits');
    }

    setCardErrors(errors);
    return errors.length === 0;
  };

  useEffect(() => {
    if (
      showPaymentForm &&
      (cardDetails.cardNumber ||
        cardDetails.cardHolderName ||
        cardDetails.expiryDate ||
        cardDetails.cvv)
    ) {
      validateCardDetails();
    }
  }, [cardDetails, showPaymentForm]);

  const getPurchasesForApp = (appId: number) => {
    if (!allPurchases.length) return [];

    const downloadedApp = downloadedApps.find(app => app.applicationId === appId);
    if (!downloadedApp) return [];

    return allPurchases.filter(purchase => {
      const purchaseAppId = purchase.monetizedApplication?.application?.id;
      return purchaseAppId === appId;
    });
  };

  const getAdsForApp = (appId: number) => {
    if (!allAds.length) return [];

    return allAds.filter(ad => {
      const adAppId = ad.monetizedApplication?.application?.id;
      return adAppId === appId;
    });
  };

  const handleViewAd = async (adId: number, appName: string) => {
    try {
      setIsLoading(true);
      const result = await userActionsService.viewAdvertisement(adId);
      setSuccess(`Ad viewed successfully for ${appName}! ${result}`);

      await loadAllContent();
    } catch (err: any) {
      setError(err.message || 'Failed to view ad');
    } finally {
      setIsLoading(false);
    }
  };

  const handlePurchase = async (purchaseId: number, itemName: string) => {
    if (!validateCardDetails()) {
      setError('Please fix card validation errors');
      return;
    }

    setProcessingPurchase(true);
    setError(null);

    try {
      const result = await userActionsService.purchaseInAppItem(purchaseId, cardDetails);
      setSuccess(`Purchase successful for ${itemName}! ${result}`);
      setShowPaymentForm(null);
      setCardDetails({ cardNumber: '', cardHolderName: '', expiryDate: '', cvv: '' });
      setCardErrors([]);

      await loadAllContent();
    } catch (err: any) {
      setError(err.message || 'Failed to complete purchase');
    } finally {
      setProcessingPurchase(false);
    }
  };

  const handleRemoveDownload = (applicationId: number) => {
    if (window.confirm('Are you sure you want to remove this download?')) {
      removeDownload(applicationId);
      if (selectedAppId === applicationId) {
        setSelectedAppId(downloadedApps[0]?.applicationId || null);
      }
      setSuccess('Application removed from downloads');
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getSelectedApp = () => {
    return downloadedApps.find(app => app.applicationId === selectedAppId);
  };

  const formatCardNumber = (value: string) => {
    const v = value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
    const matches = v.match(/\d{4,16}/g);
    const match = (matches && matches[0]) || '';
    const parts = [];

    for (let i = 0, len = match.length; i < len; i += 4) {
      parts.push(match.substring(i, i + 4));
    }

    if (parts.length) {
      return parts.join(' ');
    } else {
      return value;
    }
  };

  const formatExpiryDate = (value: string) => {
    const v = value.replace(/\D/g, '');

    if (v.length >= 2) {
      return `${v.substring(0, 2)}.${v.substring(2, 4)}.${v.substring(4, 8)}`;
    }

    return v;
  };

  const selectedAppPurchases = selectedAppId ? getPurchasesForApp(selectedAppId) : [];
  const selectedAppAds = selectedAppId ? getAdsForApp(selectedAppId) : [];

  if (!user) {
    navigate('/login');
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
        {/* Заголовок и навигация */}
        <div className="mb-8">
          <Button
            variant="outline"
            onClick={() => navigate('/applications')}
            className="mb-4 flex items-center gap-2"
          >
            <ArrowLeft className="h-4 w-4" />
            Back to Applications
          </Button>

          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">My Downloads</h1>
              <p className="mt-1 text-gray-600">
                Manage your downloaded applications and their in-app content
              </p>
            </div>
            <div className="flex items-center gap-3">
              <Button onClick={() => navigate('/applications')} className="flex items-center gap-2">
                <Download className="h-4 w-4" />
                Browse More Apps
              </Button>
            </div>
          </div>
        </div>

        {error && (
          <Alert variant="danger" title="Error" className="mb-6">
            {error}
          </Alert>
        )}

        {success && (
          <Alert variant="success" title="Success" className="mb-6">
            {success}
          </Alert>
        )}

        {/* Статистика загрузок */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          <Card>
            <CardContent className="p-6">
              <div className="flex items-center">
                <div className="mr-4 rounded-lg bg-blue-100 p-3">
                  <Download className="h-6 w-6 text-blue-600" />
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-600">Total Downloads</p>
                  <p className="mt-1 text-2xl font-bold text-gray-900">
                    {downloadStats.totalDownloads}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center">
                <div className="mr-4 rounded-lg bg-green-100 p-3">
                  <DollarSign className="h-6 w-6 text-green-600" />
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-600">Total Spent</p>
                  <p className="mt-1 text-2xl font-bold text-gray-900">
                    ${downloadStats.totalSpent.toFixed(2)}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center">
                <div className="mr-4 rounded-lg bg-purple-100 p-3">
                  <Calendar className="h-6 w-6 text-purple-600" />
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-600">Last 30 Days</p>
                  <p className="mt-1 text-2xl font-bold text-gray-900">
                    {downloadStats.last30Days}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-6">
              <div className="flex items-center">
                <div className="mr-4 rounded-lg bg-orange-100 p-3">
                  <BarChart3 className="h-6 w-6 text-orange-600" />
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-600">Favorite Type</p>
                  <p className="mt-1 text-2xl font-bold text-gray-900">
                    {downloadStats.favoriteType}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* Левая колонка: Список приложений */}
          <div className="lg:col-span-1">
            <Card className="h-full sticky top-6">
              <CardHeader className="pb-3">
                <CardTitle className="flex items-center gap-2">
                  <Package className="h-5 w-5" />
                  Your Apps ({downloadedApps.length})
                </CardTitle>
              </CardHeader>
              <CardContent className="p-0">
                {downloadsLoading ? (
                  <div className="py-12 text-center">
                    <div className="h-8 w-8 animate-spin rounded-full border-b-2 border-primary-600 mx-auto"></div>
                    <p className="mt-2 text-sm text-gray-600">Loading your downloads...</p>
                  </div>
                ) : downloadedApps.length === 0 ? (
                  <div className="py-12 px-4 text-center">
                    <Download className="mx-auto h-12 w-12 text-gray-400" />
                    <h3 className="mt-4 text-lg font-medium text-gray-900">No downloads yet</h3>
                    <p className="mt-1 text-gray-600">Download applications to view them here</p>
                    <Button className="mt-6" onClick={() => navigate('/applications')}>
                      Browse Applications
                    </Button>
                  </div>
                ) : (
                  <div className="divide-y divide-gray-100 max-h-[calc(100vh-250px)] overflow-y-auto">
                    {downloadedApps.map(app => {
                      const isSelected = selectedAppId === app.applicationId;
                      const appPurchases = getPurchasesForApp(app.applicationId);
                      const appAds = getAdsForApp(app.applicationId);

                      return (
                        <div
                          key={app.id}
                          className={`p-4 cursor-pointer transition-all hover:bg-gray-50 ${
                            isSelected ? 'bg-primary-50 border-l-4 border-primary-500' : ''
                          }`}
                          onClick={() => setSelectedAppId(app.applicationId)}
                        >
                          <div className="flex items-start space-x-3">
                            <div className="rounded-lg bg-primary-100 p-2 flex-shrink-0">
                              <Package className="h-5 w-5 text-primary-600" />
                            </div>
                            <div className="flex-1 min-w-0">
                              <div className="flex items-start justify-between">
                                <h3 className="font-medium text-gray-900 truncate">{app.name}</h3>
                                <span className="font-bold text-gray-900 text-sm">
                                  ${app.price.toFixed(2)}
                                </span>
                              </div>
                              <p className="text-sm text-gray-500 mt-1 truncate">
                                {app.description}
                              </p>
                              <div className="mt-2 flex items-center gap-2">
                                <Badge variant="outline" size="sm">
                                  {app.type}
                                </Badge>
                                {appPurchases.length > 0 && (
                                  <Badge
                                    variant="success"
                                    size="sm"
                                    className="flex items-center gap-1"
                                  >
                                    <Gift className="h-3 w-3" />
                                    {appPurchases.length}
                                  </Badge>
                                )}
                                {appAds.length > 0 && (
                                  <Badge
                                    variant="info"
                                    size="sm"
                                    className="flex items-center gap-1"
                                  >
                                    <Megaphone className="h-3 w-3" />
                                    {appAds.length}
                                  </Badge>
                                )}
                              </div>
                              {isSelected && (
                                <div className="mt-2 flex items-center text-xs text-primary-600">
                                  <ArrowRight className="h-3 w-3 mr-1" />
                                  Viewing content
                                </div>
                              )}
                            </div>
                            <Button
                              variant="ghost"
                              size="sm"
                              className="text-gray-400 hover:text-red-600 flex-shrink-0"
                              onClick={e => {
                                e.stopPropagation();
                                handleRemoveDownload(app.applicationId);
                              }}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Правая колонка: Контент приложения */}
          <div className="lg:col-span-3 space-y-6">
            {selectedAppId && getSelectedApp() ? (
              <>
                {/* Заголовок и информация о приложении */}
                <Card>
                  <CardContent className="p-6">
                    <div className="flex items-start justify-between">
                      <div className="flex items-start space-x-4">
                        <div className="rounded-xl bg-gradient-to-r from-primary-500 to-primary-600 p-3">
                          <Package className="h-8 w-8 text-white" />
                        </div>
                        <div className="flex-1">
                          <div className="flex items-start justify-between">
                            <div>
                              <h2 className="text-2xl font-bold text-gray-900">
                                {getSelectedApp()?.name}
                              </h2>
                              <div className="mt-2 flex flex-wrap items-center gap-2">
                                <Badge variant="info">{getSelectedApp()?.type}</Badge>
                                <Badge variant="outline" className="font-semibold">
                                  ${getSelectedApp()?.price.toFixed(2)}
                                </Badge>
                                <span className="text-sm text-gray-500">
                                  Downloaded: {formatDate(getSelectedApp()?.downloadedAt)}
                                </span>
                              </div>
                            </div>
                            <Button
                              variant="outline"
                              onClick={() => navigate(`/applications/${selectedAppId}`)}
                              className="flex items-center gap-2"
                            >
                              <ExternalLink className="h-4 w-4" />
                              App Details
                            </Button>
                          </div>
                          <p className="mt-3 text-gray-600">
                            {appDetailsMap[selectedAppId]?.description ||
                              getSelectedApp()?.description}
                          </p>

                          {/* Quick Stats */}
                          <div className="mt-4 flex items-center gap-4">
                            <div className="flex items-center gap-2">
                              <Gift className="h-4 w-4 text-green-600" />
                              <span className="text-sm font-medium text-gray-700">
                                {selectedAppPurchases.length} in-app purchases
                              </span>
                            </div>
                            <div className="flex items-center gap-2">
                              <Megaphone className="h-4 w-4 text-blue-600" />
                              <span className="text-sm font-medium text-gray-700">
                                {selectedAppAds.length} available ads
                              </span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>

                {/* Все покупки и рекламы сразу */}
                <div className="space-y-6">
                  {/* Покупки */}
                  {selectedAppPurchases.length > 0 && (
                    <Card>
                      <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                          <ShoppingBag className="h-5 w-5" />
                          In-App Purchases ({selectedAppPurchases.length})
                        </CardTitle>
                      </CardHeader>
                      <CardContent>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                          {selectedAppPurchases.map(purchase => (
                            <Card
                              key={purchase.id}
                              className="border hover:border-primary-300 transition-colors"
                            >
                              <CardContent className="p-5">
                                <div className="flex items-start justify-between mb-3">
                                  <div>
                                    <h4 className="font-semibold text-gray-900 text-lg">
                                      {purchase.title}
                                    </h4>
                                    <p className="text-sm text-gray-600 mt-1">
                                      {purchase.description}
                                    </p>
                                  </div>
                                  <Badge variant="success" className="text-lg font-bold px-3 py-1">
                                    ${purchase.price?.toFixed(2) || '0.00'}
                                  </Badge>
                                </div>

                                <div className="mt-4">
                                  {showPaymentForm === purchase.id ? (
                                    <div className="space-y-3 border-t pt-3">
                                      <p className="text-sm font-medium text-gray-700">
                                        Payment for:{' '}
                                        <span className="text-primary-600">{purchase.title}</span>
                                      </p>

                                      {/* Ошибки валидации карты */}
                                      {cardErrors.length > 0 && (
                                        <div className="rounded-lg bg-red-50 border border-red-200 p-2">
                                          <p className="text-sm font-medium text-red-700 mb-1">
                                            Card validation errors:
                                          </p>
                                          <ul className="text-xs text-red-600 list-disc pl-4">
                                            {cardErrors.map((error, index) => (
                                              <li key={index}>{error}</li>
                                            ))}
                                          </ul>
                                        </div>
                                      )}

                                      <Input
                                        placeholder="Card Number (16 digits)"
                                        value={formatCardNumber(cardDetails.cardNumber)}
                                        onChange={e =>
                                          setCardDetails({
                                            ...cardDetails,
                                            cardNumber: e.target.value.replace(/\s/g, ''),
                                          })
                                        }
                                        className="text-sm"
                                        maxLength={19}
                                      />
                                      <Input
                                        placeholder="Card Holder Name (e.g., John Doe)"
                                        value={cardDetails.cardHolderName}
                                        onChange={e =>
                                          setCardDetails({
                                            ...cardDetails,
                                            cardHolderName: e.target.value,
                                          })
                                        }
                                        className="text-sm"
                                      />
                                      <div className="grid grid-cols-2 gap-2">
                                        <Input
                                          placeholder="DD.MM.YYYY (e.g., 01.12.2025)"
                                          value={cardDetails.expiryDate}
                                          onChange={e =>
                                            setCardDetails({
                                              ...cardDetails,
                                              expiryDate: formatExpiryDate(e.target.value),
                                            })
                                          }
                                          className="text-sm"
                                          maxLength={10}
                                        />
                                        <Input
                                          placeholder="CVV (3-4 digits)"
                                          type="password"
                                          value={cardDetails.cvv}
                                          onChange={e =>
                                            setCardDetails({
                                              ...cardDetails,
                                              cvv: e.target.value.replace(/\D/g, ''),
                                            })
                                          }
                                          className="text-sm"
                                          maxLength={4}
                                        />
                                      </div>

                                      <div className="text-xs text-gray-500 mt-2">
                                        <p className="font-medium mb-1">Format requirements:</p>
                                        <ul className="list-disc pl-4 space-y-1">
                                          <li>Card number: 16 digits (no spaces)</li>
                                          <li>Card holder: Letters only, minimum 3 characters</li>
                                          <li>Expiry date: DD.MM.YYYY format</li>
                                          <li>CVV: 3 or 4 digits</li>
                                        </ul>
                                      </div>

                                      <div className="flex space-x-2 pt-2">
                                        <Button
                                          variant="outline"
                                          size="sm"
                                          className="flex-1"
                                          onClick={() => {
                                            setShowPaymentForm(null);
                                            setCardDetails({
                                              cardNumber: '',
                                              cardHolderName: '',
                                              expiryDate: '',
                                              cvv: '',
                                            });
                                            setCardErrors([]);
                                          }}
                                          disabled={processingPurchase}
                                        >
                                          Cancel
                                        </Button>
                                        <Button
                                          size="sm"
                                          className="flex-1"
                                          onClick={() =>
                                            handlePurchase(purchase.id, purchase.title)
                                          }
                                          isLoading={processingPurchase}
                                          disabled={
                                            processingPurchase ||
                                            cardErrors.length > 0 ||
                                            !cardDetails.cardNumber ||
                                            !cardDetails.cardHolderName ||
                                            !cardDetails.expiryDate ||
                                            !cardDetails.cvv
                                          }
                                        >
                                          <CreditCard className="h-4 w-4 mr-1" />
                                          Buy
                                        </Button>
                                      </div>
                                    </div>
                                  ) : (
                                    <Button
                                      className="w-full"
                                      onClick={() => setShowPaymentForm(purchase.id)}
                                      variant="primary"
                                    >
                                      <CreditCard className="mr-2 h-4 w-4" />
                                      Buy for ${purchase.price?.toFixed(2)}
                                    </Button>
                                  )}
                                </div>
                              </CardContent>
                            </Card>
                          ))}
                        </div>
                      </CardContent>
                    </Card>
                  )}

                  {/* Рекламы */}
                  {selectedAppAds.length > 0 && (
                    <Card>
                      <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                          <Eye className="h-5 w-5" />
                          Available Ads ({selectedAppAds.length})
                        </CardTitle>
                      </CardHeader>
                      <CardContent>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                          {selectedAppAds.map(ad => (
                            <Card
                              key={ad.id}
                              className="border hover:border-blue-300 transition-colors"
                            >
                              <CardContent className="p-5">
                                <div className="flex items-start justify-between mb-3">
                                  <div>
                                    <h4 className="font-semibold text-gray-900 text-lg">
                                      {ad.title}
                                    </h4>
                                    <p className="text-sm text-gray-600 mt-1">{ad.description}</p>
                                  </div>
                                  <Badge variant="info" className="text-lg font-bold px-3 py-1">
                                    ${ad.price?.toFixed(2)} per view
                                  </Badge>
                                </div>

                                <div className="mt-4">
                                  <Button
                                    className="w-full"
                                    variant="outline"
                                    onClick={() => handleViewAd(ad.id, getSelectedApp()?.name)}
                                    disabled={isLoading}
                                  >
                                    <Eye className="mr-2 h-4 w-4" />
                                    View Ad (Earn ${ad.price?.toFixed(2)})
                                  </Button>
                                  <p className="text-xs text-gray-500 mt-2 text-center">
                                    Support {getSelectedApp()?.name} developer
                                  </p>
                                </div>
                              </CardContent>
                            </Card>
                          ))}
                        </div>
                      </CardContent>
                    </Card>
                  )}

                  {/* Если нет ни покупок, ни рекламы */}
                  {selectedAppPurchases.length === 0 && selectedAppAds.length === 0 && (
                    <Card>
                      <CardContent className="py-12 text-center">
                        <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-gray-100">
                          <Package className="h-8 w-8 text-gray-400" />
                        </div>
                        <h3 className="text-lg font-medium text-gray-900">
                          No additional content available
                        </h3>
                        <p className="mt-1 text-gray-600 max-w-md mx-auto">
                          This application doesn't have any in-app purchases or ads yet. Check back
                          later or contact the developer for updates.
                        </p>
                      </CardContent>
                    </Card>
                  )}
                </div>

                {/* Общая информация о монетизации */}
                {(selectedAppPurchases.length > 0 || selectedAppAds.length > 0) && (
                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center gap-2">
                        <DollarSign className="h-5 w-5" />
                        {getSelectedApp()?.name} Monetization Summary
                      </CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div className="text-center p-4 bg-blue-50 rounded-lg">
                          <p className="text-sm font-medium text-blue-600">In-App Purchases</p>
                          <p className="mt-2 text-2xl font-bold text-blue-700">
                            $
                            {selectedAppPurchases
                              .reduce((sum, p) => sum + (p.price || 0), 0)
                              .toFixed(2)}
                          </p>
                          <p className="text-xs text-blue-600 mt-1">
                            {selectedAppPurchases.length} available items
                          </p>
                        </div>
                        <div className="text-center p-4 bg-green-50 rounded-lg">
                          <p className="text-sm font-medium text-green-600">Ads Revenue</p>
                          <p className="mt-2 text-2xl font-bold text-green-700">
                            $
                            {selectedAppAds
                              .reduce((sum, ad) => sum + (ad.price || 0), 0)
                              .toFixed(2)}
                          </p>
                          <p className="text-xs text-green-600 mt-1">
                            {selectedAppAds.length} available ads
                          </p>
                        </div>
                        <div className="text-center p-4 bg-purple-50 rounded-lg">
                          <p className="text-sm font-medium text-purple-600">App Value</p>
                          <p className="mt-2 text-2xl font-bold text-purple-700">
                            ${getSelectedApp()?.price.toFixed(2)}
                          </p>
                          <p className="text-xs text-purple-600 mt-1">Already purchased</p>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                )}
              </>
            ) : (
              <Card>
                <CardContent className="py-16 text-center">
                  <Package className="mx-auto h-16 w-16 text-gray-400" />
                  <h3 className="mt-6 text-xl font-medium text-gray-900">
                    {downloadedApps.length > 0 ? 'Select an Application' : 'No Downloads'}
                  </h3>
                  <p className="mt-2 text-gray-600 max-w-md mx-auto">
                    {downloadedApps.length > 0
                      ? 'Choose an application from the sidebar to view its in-app purchases and ads'
                      : 'Download applications first to see their content here'}
                  </p>
                  {downloadedApps.length === 0 && (
                    <Button className="mt-6" onClick={() => navigate('/applications')}>
                      <Download className="mr-2 h-4 w-4" />
                      Browse Applications
                    </Button>
                  )}
                </CardContent>
              </Card>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
