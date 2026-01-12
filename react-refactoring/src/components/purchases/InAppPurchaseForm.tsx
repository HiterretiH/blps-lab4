import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  inAppPurchaseSchema,
  InAppPurchaseFormData,
} from '../schemas/inAppPurchase.schema';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Card, CardContent } from '@/components/ui/Card';
import { Alert } from '@/components/ui/Alert';

interface InAppPurchaseFormProps {
  onSubmit: (data: InAppPurchaseFormData) => Promise<void>;
  defaultValues?: Partial<InAppPurchaseFormData>;
  isLoading?: boolean;
  submitButtonText?: string;
  monetizedAppId?: number;
}

export const InAppPurchaseForm: React.FC<InAppPurchaseFormProps> = ({
  onSubmit,
  defaultValues,
  isLoading = false,
  submitButtonText = 'Create Purchase',
  monetizedAppId,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<InAppPurchaseFormData>({
    resolver: zodResolver(inAppPurchaseSchema),
    defaultValues: {
      title: '',
      description: '',
      price: 0.99,
      monetizedApplicationId: monetizedAppId,
      ...defaultValues,
    },
  });

  const handleFormSubmit = async (data: InAppPurchaseFormData) => {
    try {
      await onSubmit(data);
      reset();
    } catch (error) {
      // Ошибка обрабатывается в родительском компоненте
    }
  };

  return (
    <Card>
      <CardContent className="pt-6">
        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <Input
            label="Title *"
            {...register('title')}
            error={errors.title?.message}
            placeholder="e.g., Premium Pack, 100 Coins, Remove Ads"
            disabled={isLoading}
          />

          {/* Заменяем Textarea на Input с type="textarea" или создаем простой textarea */}
          <div className="space-y-1">
            <label className="block text-sm font-medium text-gray-700">
              Description
            </label>
            <textarea
              {...register('description')}
              className={`w-full rounded-md border px-3 py-2 text-sm ${
                errors.description
                  ? 'border-red-300 focus:border-red-500 focus:ring-red-500'
                  : 'border-gray-300 focus:border-blue-500 focus:ring-blue-500'
              } focus:outline-none focus:ring-1`}
              placeholder="Describe what this purchase includes..."
              rows={3}
              disabled={isLoading}
            />
            {errors.description && (
              <p className="text-sm text-red-600">{errors.description.message}</p>
            )}
          </div>

          <Input
            label="Price ($) *"
            type="number"
            step="0.01"
            min="0.01"
            max="999.99"
            {...register('price', { valueAsNumber: true })}
            error={errors.price?.message}
            placeholder="0.99"
            disabled={isLoading}
          />

          {monetizedAppId && (
            <div className="text-sm text-gray-500">
              This purchase will be linked to application #{monetizedAppId}
            </div>
          )}

          {errors.root && (
            <Alert variant="danger" title="Validation Error">
              {errors.root.message}
            </Alert>
          )}

          <div className="flex justify-end space-x-3 pt-4">
            <Button
              type="button"
              variant="outline"
              onClick={() => reset()}
              disabled={isLoading}
            >
              Clear
            </Button>
            <Button type="submit" isLoading={isLoading}>
              {submitButtonText}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};