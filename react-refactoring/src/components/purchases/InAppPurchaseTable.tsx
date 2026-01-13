import React, { useState } from 'react';
import { InAppPurchase } from '@/types';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/Table';
import { Button } from '@/components/ui/Button';
import { Badge } from '@/components/ui/Badge';
import { Pencil, Trash2, Link as LinkIcon, Unlink } from 'lucide-react';
import { purchasesService } from '@/services/purchases.service';
import { useToast } from '@/hooks/useToast';

interface InAppPurchaseTableProps {
  purchases: InAppPurchase[];
  onEdit?: (purchase: InAppPurchase) => void;
  onDelete?: (id: number) => void;
  monetizedAppId?: number;
  showActions?: boolean;
  isLoading?: boolean;
}

export const InAppPurchaseTable: React.FC<InAppPurchaseTableProps> = ({
  purchases,
  onEdit,
  onDelete,
  monetizedAppId,
  showActions = true,
  isLoading = false,
}) => {
  const [linkingId, setLinkingId] = useState<number | null>(null);
  const [unlinkingId, setUnlinkingId] = useState<number | null>(null);
  const { toast } = useToast();

  const handleLink = async (purchaseId: number) => {
    if (!monetizedAppId) return;
    
    setLinkingId(purchaseId);
    try {
      await purchasesService.linkToMonetizedApp(monetizedAppId);
      toast.success('Purchase linked successfully');
    } catch (error: any) {
      toast.error(error.message || 'Failed to link purchase');
    } finally {
      setLinkingId(null);
    }
  };

  const handleUnlink = async (purchaseId: number) => {
    setUnlinkingId(purchaseId);
    try {
      const response = await fetch(
        `http://localhost:727/api/in-app-purchases/${purchaseId}`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${localStorage.getItem('auth_token')}`,
          },
          body: JSON.stringify({ monetizedApplicationId: null }),
        }
      );
      
      if (!response.ok) throw new Error('Failed to unlink purchase');
      
      toast.success('Purchase unlinked successfully');
    } catch (error: any) {
      toast.error(error.message || 'Failed to unlink purchase');
    } finally {
      setUnlinkingId(null);
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center py-8">
        <div className="h-8 w-8 animate-spin rounded-full border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (purchases.length === 0) {
    return (
      <div className="rounded-lg border border-dashed p-8 text-center">
        <p className="text-gray-500">No purchases found</p>
        <p className="text-sm text-gray-400 mt-1">
          Create your first in-app purchase to get started
        </p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-lg border">
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>ID</TableHead>
            <TableHead>Title</TableHead>
            <TableHead>Description</TableHead>
            <TableHead>Price</TableHead>
            <TableHead>Linked To</TableHead>
            {showActions && <TableHead className="text-right">Actions</TableHead>}
          </TableRow>
        </TableHeader>
        <TableBody>
          {purchases.map(purchase => (
            <TableRow key={purchase.id}>
              <TableCell className="font-medium">#{purchase.id}</TableCell>
              <TableCell className="font-medium">{purchase.title}</TableCell>
              <TableCell>
                <p className="max-w-md truncate text-sm text-gray-600">
                  {purchase.description || 'No description'}
                </p>
              </TableCell>
              <TableCell className="font-medium">
                ${purchase.price.toFixed(2)}
              </TableCell>
              <TableCell>
                {purchase.monetizedApplication ? (
                  <div className="flex items-center space-x-2">
                    <Badge variant="success">Linked</Badge>
                    <span className="text-xs text-gray-500">
                      App #{purchase.monetizedApplication.id}
                    </span>
                  </div>
                ) : (
                  <Badge variant="secondary">Not Linked</Badge>
                )}
              </TableCell>
              {showActions && (
                <TableCell>
                  <div className="flex justify-end space-x-2">
                    {onEdit && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => onEdit(purchase)}
                      >
                        <Pencil className="h-4 w-4" />
                      </Button>
                    )}
                    
                    
                    
                    {onDelete && (
                      <Button
                        variant="outline"
                        size="sm"
                        className="text-red-600 hover:text-red-700"
                        onClick={() => onDelete(purchase.id)}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    )}
                  </div>
                </TableCell>
              )}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};