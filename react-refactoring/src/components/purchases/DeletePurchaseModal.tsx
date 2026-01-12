import React from 'react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { AlertTriangle } from 'lucide-react';

interface DeletePurchaseModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  purchaseTitle?: string;
  isLoading?: boolean;
}

export const DeletePurchaseModal: React.FC<DeletePurchaseModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  purchaseTitle,
  isLoading = false,
}) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Delete In-App Purchase"
      size="sm"
    >
      <div className="space-y-4">
        <div className="flex items-center space-x-3 rounded-lg bg-red-50 p-3">
          <AlertTriangle className="h-5 w-5 text-red-600" />
          <p className="text-sm text-red-800">
            This action cannot be undone. All data associated with this purchase will be permanently deleted.
          </p>
        </div>

        {purchaseTitle && (
          <div className="rounded-lg bg-gray-50 p-3">
            <p className="font-medium">Purchase to delete:</p>
            <p className="text-gray-600">{purchaseTitle}</p>
          </div>
        )}

        <div className="flex justify-end space-x-3 pt-4">
          <Button variant="outline" onClick={onClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button
            variant="danger"
            onClick={onConfirm}
            isLoading={isLoading}
          >
            Delete Purchase
          </Button>
        </div>
      </div>
    </Modal>
  );
};