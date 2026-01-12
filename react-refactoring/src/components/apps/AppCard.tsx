import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/Card';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';
import { Alert } from '../ui/Alert';
import { Application } from '../../types';
import { useDownloadsStore } from '../../store/downloads.store';
import { useAuthStore } from '../../store/auth.store';
import { userActionsService } from '../../services/userAction.service';
import { Package, DollarSign, Trash2, Download, Check, CreditCard } from 'lucide-react';

interface AppCardProps {
  application: Application;
  onDelete?: (id: number) => void;
  currentDeveloperId?: number;
}

export const AppCard: React.FC<AppCardProps> = ({ application, onDelete, currentDeveloperId }) => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [showPaymentForm, setShowPaymentForm] = useState(false);
  const [cardDetails, setCardDetails] = useState({
    cardNumber: '',
    cardHolderName: '',
    expiryDate: '',
    cvv: '',
  });
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const { isDownloaded, addDownload, isLoading: downloadsLoading } = useDownloadsStore();

  const downloaded = isDownloaded(application.id);
  //const isDeveloper = user?.role === 'DEVELOPER';
  const isOwner = currentDeveloperId === application.developerId;

  const shouldShowDownloadButton = user?.role === 'USER' && !downloaded;

  const cardRegex = {
    cardNumber: /^\d{16}$/, // ровно 16 цифр
    cardHolderName: /^[a-zA-Z\s]{3,}$/, // только буквы и пробелы, минимум 3 символа
    expiryDate: /^\d{2}\.\d{2}\.\d{4}$/, // формат dd.mm.yyyy или mm.dd.yyyy
    cvv: /^\d{3,4}$/, // 3 или 4 цифры
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
      errors.push('Expiry date must be in format DD.MM.YYYY or MM.DD.YYYY (e.g., 01.12.2025)');
    } else {
      const [day, month, year] = cardDetails.expiryDate.split('.').map(Number);
      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth() + 1;
      
      // Проверяем месяц и год (игнорируем день)
      if (year < currentYear || (year === currentYear && month < currentMonth)) {
        errors.push('Card has expired');
      }
    }

    if (!cardDetails.cvv.match(cardRegex.cvv)) {
      errors.push('CVV must be 3 or 4 digits');
    }

    return errors;
  };

  const handleDownload = async () => {
    if (!shouldShowDownloadButton) return;

    if (application.price > 0) {
      setShowPaymentForm(true);
      return;
    }

    await processDownload();
  };

  const processDownload = async () => {
    setIsProcessing(true);
    setError(null);
    setSuccessMessage(null);

    try {
      if (application.price > 0) {
        const validationErrors = validateCardDetails();
        if (validationErrors.length > 0) {
          setError(validationErrors.join(', '));
          setIsProcessing(false);
          return;
        }

        await userActionsService.downloadApplication(application.id, {
          cardNumber: cardDetails.cardNumber,
          cardHolderName: cardDetails.cardHolderName,
          expiryDate: cardDetails.expiryDate,
          cvv: cardDetails.cvv,
        });
      }

      await addDownload({
        id: application.id,
        name: application.name,
        type: application.type,
        price: application.price,
        description: application.description,
        developerId: application.developerId,
      });

      setShowPaymentForm(false);
      setCardDetails({ cardNumber: '', cardHolderName: '', expiryDate: '', cvv: '' });
      setSuccessMessage(
        `Successfully ${application.price > 0 ? 'purchased' : 'downloaded'} "${application.name}"!`
      );

      setTimeout(() => {
        setSuccessMessage(null);
      }, 3000);
    } catch (err: any) {
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else if (err.message) {
        setError(err.message);
      } else {
        setError(
          'Failed to download application. Please check your payment details and try again.'
        );
      }
    } finally {
      setIsProcessing(false);
    }
  };

  const handlePurchase = async () => {
    if (
      !cardDetails.cardNumber ||
      !cardDetails.cardHolderName ||
      !cardDetails.expiryDate ||
      !cardDetails.cvv
    ) {
      setError('Please fill all payment details');
      return;
    }

    await processDownload();
  };

  const getStatusBadge = (status: number) => {
    switch (status) {
      case 1:
        return <Badge variant="success">ACCEPTED</Badge>;
      case 0:
        return <Badge variant="warning">PENDING</Badge>;
      case 2:
        return <Badge variant="danger">REJECTED</Badge>;
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

  const getPriceBadge = (price: number) => {
    if (price === 0) {
      return (
        <Badge variant="success" className="flex items-center gap-1">
          <span>FREE</span>
        </Badge>
      );
    } else if (price < 5) {
      return (
        <Badge variant="info" className="flex items-center gap-1">
          <DollarSign className="h-3 w-3" />
          <span>${price.toFixed(2)}</span>
        </Badge>
      );
    } else if (price < 15) {
      return (
        <Badge variant="warning" className="flex items-center gap-1">
          <DollarSign className="h-3 w-3" />
          <span>${price.toFixed(2)}</span>
        </Badge>
      );
    } else {
      return (
        <Badge variant="danger" className="flex items-center gap-1">
          <DollarSign className="h-3 w-3" />
          <span>${price.toFixed(2)}</span>
        </Badge>
      );
    }
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

  return (
    <Card className="h-full flex flex-col hover:shadow-lg transition-shadow">
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between">
          <div>
            <CardTitle className="text-lg font-bold text-gray-900">{application.name}</CardTitle>
            <div className="mt-1 flex items-center space-x-2">
              {getTypeBadge(application.type)}
              {getStatusBadge(application.status)}
              {isOwner && (
                <Badge variant="primary" className="flex items-center gap-1">
                  <Package className="h-3 w-3" />
                  <span>YOUR APP</span>
                </Badge>
              )}
            </div>
          </div>
          {isOwner && onDelete && (
            <Button
              variant="outline"
              size="sm"
              className="text-red-600 hover:text-red-700 hover:bg-red-50"
              onClick={() => onDelete(application.id)}
            >
              <Trash2 className="h-4 w-4" />
            </Button>
          )}
        </div>
      </CardHeader>

      <CardContent className="flex-grow">
        {successMessage && (
          <Alert variant="success" className="mb-3 py-2">
            {successMessage}
          </Alert>
        )}

        {error && (
          <Alert variant="danger" className="mb-3 py-2">
            {error}
          </Alert>
        )}

        <p className="text-gray-600 text-sm mb-4 line-clamp-3">{application.description}</p>

        <div className="mt-4 pt-4 border-t">
          <div className="flex items-center justify-between">
            <div className="flex items-center text-gray-700">
              {getPriceBadge(application.price)}
            </div>

            <div className="flex items-center space-x-2">
              {downloaded ? (
                <Badge variant="success" className="flex items-center gap-1">
                  <Check className="h-3 w-3" />
                  Downloaded
                </Badge>
              ) : shouldShowDownloadButton ? (
                <Button
                  size="sm"
                  onClick={handleDownload}
                  isLoading={downloadsLoading || isProcessing}
                  disabled={downloadsLoading || isProcessing}
                  className="flex items-center gap-1"
                  variant={application.price === 0 ? 'primary' : 'secondary'}
                >
                  <Download className="h-4 w-4" />
                  {application.price === 0
                    ? 'Download Free'
                    : `Buy $${application.price.toFixed(2)}`}
                </Button>
              ) : null}
            </div>
          </div>
        </div>

        {/* Форма оплаты для платных приложений */}
        {showPaymentForm && (
          <div className="mt-4 p-3 border rounded-lg bg-gray-50">
            <p className="text-sm font-medium text-gray-700 mb-2">
              Payment Details for {application.name}
            </p>
            <div className="space-y-2">
              <input
                type="text"
                placeholder="Card Number (e.g., 1234567890123456)"
                className="w-full px-3 py-2 text-sm border rounded"
                value={formatCardNumber(cardDetails.cardNumber)}
                onChange={e =>
                  setCardDetails({ ...cardDetails, cardNumber: e.target.value.replace(/\s/g, '') })
                }
                maxLength={19}
              />
              <input
                type="text"
                placeholder="Card Holder Name (e.g., John Doe)"
                className="w-full px-3 py-2 text-sm border rounded"
                value={cardDetails.cardHolderName}
                onChange={e => setCardDetails({ ...cardDetails, cardHolderName: e.target.value })}
              />
              <div className="grid grid-cols-2 gap-2">
                <input
                  type="text"
                  placeholder="DD.MM.YYYY (e.g., 01.12.2025)"
                  className="w-full px-3 py-2 text-sm border rounded"
                  value={cardDetails.expiryDate}
                  onChange={e =>
                    setCardDetails({ ...cardDetails, expiryDate: formatExpiryDate(e.target.value) })
                  }
                  maxLength={10}
                />
                <input
                  type="text"
                  placeholder="CVV (3-4 digits)"
                  className="w-full px-3 py-2 text-sm border rounded"
                  value={cardDetails.cvv}
                  onChange={e =>
                    setCardDetails({ ...cardDetails, cvv: e.target.value.replace(/\D/g, '') })
                  }
                  maxLength={4}
                />
              </div>
            </div>
            <div className="mt-3 text-xs text-gray-500 mb-3">
              <p>Format requirements:</p>
              <ul className="list-disc pl-5 mt-1 space-y-1">
                <li>Card number: 16 digits (no spaces)</li>
                <li>Card holder: Letters only, minimum 3 characters</li>
                <li>Expiry date: DD.MM.YYYY format</li>
                <li>CVV: 3 or 4 digits</li>
              </ul>
            </div>
            <div className="mt-3 flex space-x-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  setShowPaymentForm(false);
                  setCardDetails({ cardNumber: '', cardHolderName: '', expiryDate: '', cvv: '' });
                  setError(null);
                }}
                disabled={isProcessing}
              >
                Cancel
              </Button>
              <Button
                size="sm"
                onClick={handlePurchase}
                isLoading={isProcessing}
                disabled={isProcessing}
                className="flex items-center gap-1"
              >
                <CreditCard className="h-4 w-4" />
                Pay ${application.price.toFixed(2)}
              </Button>
            </div>
          </div>
        )}

        {/* Кнопка для перехода в скачанные */}
        {downloaded && (
          <Button
            variant="outline"
            size="sm"
            className="w-full mt-2"
            onClick={() => navigate('/my-downloads')}
          >
            <Check className="mr-2 h-4 w-4" />
            Open in Downloads
          </Button>
        )}
      </CardContent>
    </Card>
  );
};
