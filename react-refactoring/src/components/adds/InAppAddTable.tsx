import React from 'react';
import { InAppAdd } from '@/types';
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
import { Pencil, Trash2, Eye } from 'lucide-react';

interface InAppAddTableProps {
  ads: InAppAdd[];
  onEdit?: (ad: InAppAdd) => void;
  onDelete?: (id: number) => void;
  onViewStats?: (id: number) => void;
  isLoading?: boolean;
}

export const InAppAddTable: React.FC<InAppAddTableProps> = ({
  ads,
  onEdit,
  onDelete,
  onViewStats,
  isLoading = false,
}) => {
  if (isLoading) {
    return (
      <div className="flex justify-center py-8">
        <div className="h-8 w-8 animate-spin rounded-full border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (ads.length === 0) {
    return (
      <div className="rounded-lg border border-dashed p-8 text-center">
        <p className="text-gray-500">No ads found</p>
        <p className="text-sm text-gray-400 mt-1">
          Create your first ad to start generating revenue
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
            <TableHead>Price per View</TableHead>
            <TableHead>Daily Potential</TableHead>
            <TableHead>Status</TableHead>
            <TableHead className="text-right">Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {ads.map(ad => (
            <TableRow key={ad.id}>
              <TableCell className="font-medium">#{ad.id}</TableCell>
              <TableCell className="font-medium">{ad.title}</TableCell>
              <TableCell>
                <p className="max-w-md truncate text-sm text-gray-600">
                  {ad.description || 'No description'}
                </p>
              </TableCell>
              <TableCell className="font-medium">
                ${ad.price.toFixed(2)}
              </TableCell>
              <TableCell className="font-medium">
                ${(ad.price * 100).toFixed(2)}
                <span className="text-xs text-gray-500 block">(est. 100 views/day)</span>
              </TableCell>
              <TableCell>
                <Badge variant="success">
                  <Eye className="h-3 w-3 mr-1" />
                  Active
                </Badge>
              </TableCell>
              <TableCell>
                <div className="flex justify-end space-x-2">
                  
                  {onEdit && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => onEdit(ad)}
                    >
                      <Pencil className="h-4 w-4" />
                    </Button>
                  )}
                  
                  {onDelete && (
                    <Button
                      variant="outline"
                      size="sm"
                      className="text-red-600 hover:text-red-700"
                      onClick={() => onDelete(ad.id)}
                    >
                      <Trash2 className="h-4 w-4" />
                    </Button>
                  )}
                </div>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
      
      <div className="border-t p-4">
        <div className="flex items-center justify-between text-sm text-gray-600">
          <div>
            Total ads: {ads.length}
          </div>
          <div>
            Total daily revenue potential: $
            {(ads.reduce((sum, ad) => sum + ad.price * 100, 0)).toFixed(2)}
          </div>
        </div>
      </div>
    </div>
  );
};