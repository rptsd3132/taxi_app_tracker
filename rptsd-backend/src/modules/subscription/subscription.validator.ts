import { z } from 'zod';

export const createPaymentSchema = z.object({
  amount: z.number().positive('Amount must be positive'),
  paymentMethod: z.string().min(1, 'Payment method is required'),
});

export const confirmPaymentSchema = z.object({
  transactionId: z.string().min(1, 'Transaction ID is required'),
});
