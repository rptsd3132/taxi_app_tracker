import { Request, Response } from 'express';
import * as svc from './stats.service';
import { syncStatsSchema } from './stats.validator';
import { sendSuccess, sendError } from '../../utils/response.util';

export const syncStats = async (req: Request, res: Response): Promise<void> => {
  const result = syncStatsSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.issues[0].message, 'VALIDATION_ERROR', 422);
    return;
  }
  try {
    const stat = await svc.syncStats(req.user!.id, result.data);
    sendSuccess(res, stat, 'Stats synced');
  } catch {
    sendError(res, 'Failed to sync stats', 'SERVER_ERROR', 500);
  }
};

export const getMyStats = async (req: Request, res: Response): Promise<void> => {
  const daysParam = parseInt(req.query.days as string) || 30;
  const days = Math.min(Math.max(daysParam, 1), 365);
  try {
    const stats = await svc.getMyStats(req.user!.id, days);
    sendSuccess(res, stats, `Stats for last ${days} days`);
  } catch {
    sendError(res, 'Failed to fetch stats', 'SERVER_ERROR', 500);
  }
};
