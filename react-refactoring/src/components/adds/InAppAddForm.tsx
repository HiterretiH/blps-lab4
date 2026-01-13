import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  inAppAddSchema,
  InAppAddFormData,
} from '../schemas/inAppAdd.schema';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Card, CardContent } from '@/components/ui/Card';
import { Alert } from '@/components/ui/Alert';

interface InAppAddFormProps {
  onSubmit: (data: InAppAddFormData) => Promise<void>;
  defaultValues?: Partial<InAppAddFormData>;
  isLoading?: boolean;
  submitButtonText?: string;
  monetizedAppId?: number;
}

export const InAppAddForm: React.FC<InAppAddFormProps> = ({
  onSubmit,
  defaultValues,
  isLoading = false,
  submitButtonText = 'Create Ad',
  monetizedAppId,
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<InAppAddFormData>({
    resolver: zodResolver(inAppAddSchema),
    defaultValues: {
      title: '',
      description: '',
      price: 0.50,
      monetizedApplicationId: monetizedAppId,
      ...defaultValues,
    },
  });

  const handleFormSubmit = async (data: InAppAddFormData) => {
    try {
      await onSubmit(data);
      reset();
    } catch (error) {
    }
  };

  return (
    <Card>
      <CardContent className="pt-6">
        <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
          <Input
            label="Ad Title *"
            {...register('title')}
            error={errors.title?.message}
            placeholder="e.g., Banner Ad, Interstitial, Rewarded Video"
            disabled={isLoading}
          />

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
              placeholder="Describe the ad content and placement..."
              rows={3}
              disabled={isLoading}
            />
            {errors.description && (
              <p className="text-sm text-red-600">{errors.description.message}</p>
            )}
          </div>

          <Input
            label="Price per View ($) *"
            type="number"
            step="0.01"
            min="0.01"
            max="10"
            {...register('price', { valueAsNumber: true })}
            error={errors.price?.message}
            placeholder="0.50"
            disabled={isLoading}
            helpText="Amount earned each time a user views this ad"
          />

          {monetizedAppId && (
            <div className="text-sm text-gray-500">
              This ad will be linked to application #{monetizedAppId}
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