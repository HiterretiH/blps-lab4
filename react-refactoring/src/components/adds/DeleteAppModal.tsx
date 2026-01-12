import React from 'react';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { AlertTriangle } from 'lucide-react';

interface DeleteAddModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  adTitle?: string;
  isLoading?: boolean;
}

export const DeleteAddModal: React.FC<DeleteAddModalProps> = ({
  isOpen,
  onClose,
  onConfirm,
  adTitle,
  isLoading = false,
}) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Delete Advertisement"
      size="sm"
    >
      <div className="space-y-4">
        <div className="flex items-center space-x-3 rounded-lg bg-red-50 p-3">
          <AlertTriangle className="h-5 w-5 text-red-600" />
          <p className="text-sm text-red-800">
            This action cannot be undone. All statistics and revenue data for this ad will be permanently deleted.
          </p>
        </div>

        {adTitle && (
          <div className="rounded-lg bg-gray-50 p-3">
            <p className="font-medium">Ad to delete:</p>
            <p className="text-gray-600">{adTitle}</p>
            <p className="text-sm text-gray-500 mt-1">
              All associated revenue data will be lost
            </p>
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
            Delete Ad
          </Button>
        </div>
      </div>
    </Modal>
  );
};