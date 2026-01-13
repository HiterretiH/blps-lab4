import React, { useState } from 'react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { Alert } from '@/components/ui/Alert';
import { CheckCircle, AlertTriangle, Info, DollarSign } from 'lucide-react';

interface MonetizeConfirmationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => Promise<void>;
  applicationName?: string;
  applicationId: number;
  developerId: number;
  isLoading?: boolean;
}

export const MonetizeConfirmationModal: React.FC<MonetizeConfirmationModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  applicationName,
  applicationId,
  developerId,
  isLoading = false,
}) => {
  const [error, setError] = useState<string | null>(null);

  const handleConfirm = async () => {
    setError(null);
    try {
      await onConfirm();
    } catch (err: any) {
      console.error('Monetization error:', err);
      setError(err.message || 'Failed to monetize application');
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Confirm Monetization"
      size="lg"
    >
      <div className="space-y-6">
        {error && (
          <Alert variant="danger" title="Error">
            {error}
          </Alert>
        )}

        {/* Header */}
        <div className="flex items-start space-x-4">
          <div className="rounded-full bg-blue-100 p-3">
            <DollarSign className="h-6 w-6 text-blue-600" />
          </div>
          <div>
            <h3 className="text-lg font-semibold text-gray-900">
              Ready to Monetize?
            </h3>
            <p className="mt-1 text-gray-600">
              You're about to enable monetization for "{applicationName || `App #${applicationId}`}"
            </p>
          </div>
        </div>

        {/* Warning Section */}
        <div className="rounded-lg border border-amber-200 bg-amber-50 p-4">
          <div className="flex items-start">
            <AlertTriangle className="h-5 w-5 text-amber-600 mr-2 mt-0.5" />
            <div>
              <h4 className="font-medium text-amber-900">Important Considerations</h4>
              <ul className="mt-2 space-y-2 text-sm text-amber-700">
                <li className="flex items-start">
                  <span className="mr-2">•</span>
                  <span>Ensure your app complies with platform monetization policies</span>
                </li>
                <li className="flex items-start">
                  <span className="mr-2">•</span>
                  <span>All in-app purchases and ads must follow user experience guidelines</span>
                </li>
                <li className="flex items-start">
                  <span className="mr-2">•</span>
                  <span>Tax documentation should be up to date for revenue collection</span>
                </li>
                <li className="flex items-start">
                  <span className="mr-2">•</span>
                  <span>Consider how monetization affects user retention</span>
                </li>
              </ul>
            </div>
          </div>
        </div>

        {/* Benefits Section */}
        <div className="rounded-lg border border-gray-200 p-4">
          <h4 className="font-medium text-gray-900 mb-3">Benefits of Monetization</h4>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div className="flex items-start space-x-3">
              <CheckCircle className="h-5 w-5 text-green-500 mt-0.5" />
              <div>
                <p className="font-medium text-gray-900">Revenue Generation</p>
                <p className="text-sm text-gray-600">Earn from downloads, purchases, and ads</p>
              </div>
            </div>
            <div className="flex items-start space-x-3">
              <CheckCircle className="h-5 w-5 text-green-500 mt-0.5" />
              <div>
                <p className="font-medium text-gray-900">Analytics Dashboard</p>
                <p className="text-sm text-gray-600">Track earnings and user engagement</p>
              </div>
            </div>
            <div className="flex items-start space-x-3">
              <CheckCircle className="h-5 w-5 text-green-500 mt-0.5" />
              <div>
                <p className="font-medium text-gray-900">Flexible Payouts</p>
                <p className="text-sm text-gray-600">Withdraw earnings when you want</p>
              </div>
            </div>
            <div className="flex items-start space-x-3">
              <CheckCircle className="h-5 w-5 text-green-500 mt-0.5" />
              <div>
                <p className="font-medium text-gray-900">User Growth</p>
                <p className="text-sm text-gray-600">Reinvest earnings to grow your user base</p>
              </div>
            </div>
          </div>
        </div>

        {/* Setup Details */}
        <div className="rounded-lg bg-gray-50 p-4">
          <div className="flex items-center space-x-3">
            <Info className="h-5 w-5 text-gray-500" />
            <div>
              <p className="text-sm font-medium text-gray-900">Setup Details</p>
              <div className="mt-1 grid grid-cols-2 gap-4 text-sm text-gray-600">
                <div>
                  <span className="font-medium">Developer ID:</span> {developerId}
                </div>
                <div>
                  <span className="font-medium">Application ID:</span> {applicationId}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Terms Checkbox */}
        <div className="rounded-lg border border-gray-200 p-4">
          <label className="flex items-start">
            <input
              type="checkbox"
              className="mt-1 h-4 w-4 text-primary-600 rounded border-gray-300 focus:ring-primary-500"
              required
            />
            <span className="ml-3 text-sm text-gray-700">
              I confirm that my application complies with all platform monetization policies,
              including but not limited to content guidelines, user experience requirements,
              and legal regulations. I understand that inappropriate monetization may result
              in suspension of monetization features or removal from the platform.
            </span>
          </label>
        </div>

        {/* Actions */}
        <div className="flex justify-end space-x-3 pt-4 border-t">
          <Button
            variant="outline"
            onClick={onClose}
            disabled={isLoading}
            className="min-w-[100px]"
          >
            Cancel
          </Button>
          <Button
            onClick={handleConfirm}
            isLoading={isLoading}
            disabled={isLoading}
            className="min-w-[120px] bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700"
          >
            <CheckCircle className="h-4 w-4 mr-2" />
            Enable Monetization
          </Button>
        </div>

        {/* Footer Note */}
        <div className="text-center">
          <p className="text-xs text-gray-500">
            Once enabled, you can manage monetization settings from the Monetization Dashboard
          </p>
        </div>
      </div>
    </Modal>
  );
};