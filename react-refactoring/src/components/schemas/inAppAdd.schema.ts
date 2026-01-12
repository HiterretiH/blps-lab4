import { z } from 'zod';

export const inAppAddSchema = z.object({
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
    .max(10, 'Price must be less than $10 per view'),
  monetizedApplicationId: z
    .number()
    .int()
    .positive('Invalid application ID')
    .optional()
    .nullable(),
});

export type InAppAddFormData = z.infer<typeof inAppAddSchema>;