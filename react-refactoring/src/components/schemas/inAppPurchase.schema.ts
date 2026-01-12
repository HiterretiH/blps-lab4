import { z } from 'zod';

export const inAppPurchaseSchema = z.object({
  title: z
    .string()
    .min(1, 'Title is required')
    .max(100, 'Title must be less than 100 characters'),
  description: z
    .string()
    .max(500, 'Description must be less than 500 characters')
    .optional(),
  price: z
    .number({ invalid_type_error: 'Price must be a number' })
    .positive('Price must be greater than 0')
    .max(999.99, 'Price must be less than $1000'),
  monetizedApplicationId: z
    .number()
    .int()
    .positive('Invalid application ID')
    .optional()
    .nullable(),
});

export type InAppPurchaseFormData = z.infer<typeof inAppPurchaseSchema>;