import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Alert } from '../components/ui/Alert';
import { Input } from '../components/ui/Input';
import { Badge } from '../components/ui/Badge';
import { applicationsService } from '../services/applications.service';
import { userActionsService } from '../services/userAction.service';
import { Download, CreditCard, Package, ArrowLeft } from 'lucide-react';
export const DownloadAppPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [application, setApplication] = useState<any>(null);
  const [cardDetails, setCardDetails] = useState({
    cardNumber: '',
    cardHolderName: '',
    expiryDate: '',
    cvv: '',
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  useEffect(() => {
    if (id) {
      loadApplication(parseInt(id));
    }
  }, [id]);
  const loadApplication = async (appId: number) => {
    try {
      const app = await applicationsService.getApplicationById(appId);
      setApplication(app);
    } catch (err) {
      console.error('Error loading application:', err);
      setError('Application not found');
    }
  };
  const handleDownload = async () => {
    if (!application || !id) return;
    setIsLoading(true);
    setError('');
    try {
      await userActionsService.downloadApplication(parseInt(id), cardDetails);
      setSuccess(true);
      setTimeout(() => {
        navigate('/my-downloads');
      }, 2000);
    } catch (err) {
      setError('Download failed. Please check your payment details.');
    } finally {
      setIsLoading(false);
    }
  };
  if (!application && !error) {
    return (
      <div className="flex justify-center py-12">
        <div className="h-8 w-8 animate-spin rounded-full border-b-2 border-primary-600"></div>
      </div>
    );
  }
  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <Button variant="outline" onClick={() => navigate(-1)} className="flex items-center gap-2">
        <ArrowLeft className="h-4 w-4" />
        Back
      </Button>
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {}
        <Card>
          <CardHeader>
            <CardTitle>Application Details</CardTitle>
          </CardHeader>
          <CardContent>
            {application && (
              <div className="space-y-4">
                <div className="flex items-center gap-3">
                  <div className="rounded-lg bg-primary-100 p-3">
                    <Package className="h-6 w-6 text-primary-600" />
                  </div>
                  <div>
                    <h2 className="text-xl font-bold text-gray-900">{application.name}</h2>
                    <Badge variant="info" className="mt-1">
                      {application.type}
                    </Badge>
                  </div>
                </div>
                <div>
                  <h3 className="font-medium text-gray-900">Description</h3>
                  <p className="mt-1 text-gray-600">{application.description}</p>
                </div>
                <div className="border-t pt-4">
                  <div className="flex justify-between items-center">
                    <span className="text-gray-600">Price</span>
                    <span className="text-3xl font-bold text-primary-600">
                      ${application.price.toFixed(2)}
                    </span>
                  </div>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
        {}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <CreditCard className="h-5 w-5" />
              Payment Details
            </CardTitle>
          </CardHeader>
          <CardContent>
            {success ? (
              <Alert variant="success">Download successful! Redirecting to your downloads...</Alert>
            ) : (
              <div className="space-y-4">
                {error && <Alert variant="danger">{error}</Alert>}
                <Input
                  label="Card Number"
                  placeholder="1234 5678 9012 3456"
                  value={cardDetails.cardNumber}
                  onChange={e => setCardDetails({ ...cardDetails, cardNumber: e.target.value })}
                />
                <Input
                  label="Card Holder Name"
                  placeholder="John Doe"
                  value={cardDetails.cardHolderName}
                  onChange={e => setCardDetails({ ...cardDetails, cardHolderName: e.target.value })}
                />
                <div className="grid grid-cols-2 gap-4">
                  <Input
                    label="Expiry Date"
                    placeholder="MM/YY"
                    value={cardDetails.expiryDate}
                    onChange={e => setCardDetails({ ...cardDetails, expiryDate: e.target.value })}
                  />
                  <Input
                    label="CVV"
                    placeholder="123"
                    type="password"
                    value={cardDetails.cvv}
                    onChange={e => setCardDetails({ ...cardDetails, cvv: e.target.value })}
                  />
                </div>
                <Button
                  className="w-full"
                  size="lg"
                  onClick={handleDownload}
                  isLoading={isLoading}
                  disabled={!cardDetails.cardNumber || !cardDetails.cardHolderName}
                >
                  <Download className="mr-2 h-5 w-5" />
                  Download & Pay ${application?.price.toFixed(2)}
                </Button>
                <p className="text-sm text-gray-500 text-center">
                  By downloading, you agree to our terms of service
                </p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
