import { Request, Response } from 'express';
import * as svc from './subscription.service';
import { createPaymentSchema, confirmPaymentSchema } from './subscription.validator';
import { sendSuccess, sendError } from '../../utils/response.util';

export const getStatus = async (req: Request, res: Response): Promise<void> => {
  try {
    const data = await svc.getSubscriptionStatus(req.user!.id);
    sendSuccess(res, data, 'Subscription status fetched');
  } catch {
    sendError(res, 'Failed to fetch subscription status', 'SERVER_ERROR', 500);
  }
};

export const createPayment = async (req: Request, res: Response): Promise<void> => {
  const result = createPaymentSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.issues[0].message, 'VALIDATION_ERROR', 422);
    return;
  }
  try {
    const data = await svc.createPayment(
      req.user!.id,
      result.data.amount,
      result.data.paymentMethod,
    );
    sendSuccess(res, data, 'Payment created', 201);
  } catch {
    sendError(res, 'Failed to create payment', 'SERVER_ERROR', 500);
  }
};

export const confirmPayment = async (req: Request, res: Response): Promise<void> => {
  const result = confirmPaymentSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.issues[0].message, 'VALIDATION_ERROR', 422);
    return;
  }
  try {
    const data = await svc.confirmPayment(req.user!.id, result.data.transactionId);
    sendSuccess(res, data, 'Payment confirmed. Subscription extended for 30 days.');
  } catch (err) {
    if (err instanceof Error) {
      if (err.message === 'PAYMENT_NOT_FOUND') {
        sendError(res, 'Transaction not found', 'PAYMENT_NOT_FOUND', 404);
        return;
      }
      if (err.message === 'FORBIDDEN') {
        sendError(res, 'This payment does not belong to you', 'FORBIDDEN', 403);
        return;
      }
      if (err.message === 'ALREADY_PROCESSED') {
        sendError(res, 'Payment already processed', 'ALREADY_PROCESSED', 409);
        return;
      }
    }
    sendError(res, 'Failed to confirm payment', 'SERVER_ERROR', 500);
  }
};
