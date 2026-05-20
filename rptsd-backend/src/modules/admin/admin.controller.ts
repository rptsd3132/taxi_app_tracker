import { Request, Response } from 'express';
import { z } from 'zod';
import * as svc from './admin.service';
import { sendSuccess, sendError } from '../../utils/response.util';

const suspendSchema = z.object({ isSuspended: z.boolean() });
const extendSchema = z.object({ days: z.number().int().positive() });
const replySchema = z.object({ reply: z.string().min(1, 'Reply is required') });

// ─── Dashboard ────────────────────────────────────────────────────────────────

export const dashboard = async (_req: Request, res: Response): Promise<void> => {
  try {
    const data = await svc.getDashboard();
    sendSuccess(res, data, 'Dashboard data fetched');
  } catch {
    sendError(res, 'Failed to load dashboard', 'SERVER_ERROR', 500);
  }
};

// ─── Users ────────────────────────────────────────────────────────────────────

export const listUsers = async (req: Request, res: Response): Promise<void> => {
  const page = Math.max(parseInt(req.query.page as string) || 1, 1);
  const limit = Math.min(parseInt(req.query.limit as string) || 20, 100);
  const status = req.query.status as string | undefined;
  const search = req.query.search as string | undefined;
  try {
    const data = await svc.listUsers({ page, limit, status, search });
    sendSuccess(res, data, 'Users fetched');
  } catch {
    sendError(res, 'Failed to fetch users', 'SERVER_ERROR', 500);
  }
};

export const getUserDetail = async (req: Request, res: Response): Promise<void> => {
  try {
    const data = await svc.getUserDetail(req.params.id as string);
    sendSuccess(res, data, 'User detail fetched');
  } catch (err) {
    if (err instanceof Error && err.message === 'USER_NOT_FOUND') {
      sendError(res, 'User not found', 'NOT_FOUND', 404);
      return;
    }
    sendError(res, 'Failed to fetch user', 'SERVER_ERROR', 500);
  }
};

export const suspendUser = async (req: Request, res: Response): Promise<void> => {
  const result = suspendSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.issues[0].message, 'VALIDATION_ERROR', 422);
    return;
  }
  try {
    const data = await svc.suspendUser(req.params.id as string, result.data.isSuspended);
    const msg = result.data.isSuspended ? 'User suspended' : 'User unsuspended';
    sendSuccess(res, data, msg);
  } catch (err) {
    if (err instanceof Error && err.message === 'USER_NOT_FOUND') {
      sendError(res, 'User not found', 'NOT_FOUND', 404);
      return;
    }
    sendError(res, 'Failed to update user', 'SERVER_ERROR', 500);
  }
};

export const extendSubscription = async (req: Request, res: Response): Promise<void> => {
  const result = extendSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.issues[0].message, 'VALIDATION_ERROR', 422);
    return;
  }
  try {
    const data = await svc.extendSubscription(req.params.id as string, result.data.days);
    sendSuccess(res, data, `Subscription extended by ${result.data.days} days`);
  } catch (err) {
    if (err instanceof Error && err.message === 'USER_NOT_FOUND') {
      sendError(res, 'User not found', 'NOT_FOUND', 404);
      return;
    }
    sendError(res, 'Failed to extend subscription', 'SERVER_ERROR', 500);
  }
};

// ─── Payments ─────────────────────────────────────────────────────────────────

export const listPayments = async (req: Request, res: Response): Promise<void> => {
  const page = Math.max(parseInt(req.query.page as string) || 1, 1);
  const limit = Math.min(parseInt(req.query.limit as string) || 20, 100);
  const status = req.query.status as string | undefined;
  const search = req.query.search as string | undefined;
  try {
    const data = await svc.listPayments({ page, limit, status, search });
    sendSuccess(res, data, 'Payments fetched');
  } catch {
    sendError(res, 'Failed to fetch payments', 'SERVER_ERROR', 500);
  }
};

// ─── Comments ─────────────────────────────────────────────────────────────────

export const listComments = async (req: Request, res: Response): Promise<void> => {
  const page = Math.max(parseInt(req.query.page as string) || 1, 1);
  const limit = Math.min(parseInt(req.query.limit as string) || 20, 100);
  const status = req.query.status as string | undefined;
  try {
    const data = await svc.listComments({ page, limit, status });
    sendSuccess(res, data, 'Comments fetched');
  } catch {
    sendError(res, 'Failed to fetch comments', 'SERVER_ERROR', 500);
  }
};

export const replyToComment = async (req: Request, res: Response): Promise<void> => {
  const result = replySchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.issues[0].message, 'VALIDATION_ERROR', 422);
    return;
  }
  try {
    const data = await svc.replyToComment(
      req.params.id as string,
      result.data.reply,
      req.admin!.id,
    );
    sendSuccess(res, data, 'Reply posted');
  } catch (err) {
    if (err instanceof Error && err.message === 'COMMENT_NOT_FOUND') {
      sendError(res, 'Comment not found', 'NOT_FOUND', 404);
      return;
    }
    sendError(res, 'Failed to post reply', 'SERVER_ERROR', 500);
  }
};
