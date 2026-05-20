import { Request, Response } from 'express';
import * as svc from './comment.service';
import { createCommentSchema } from './comment.validator';
import { sendSuccess, sendError } from '../../utils/response.util';

export const createComment = async (req: Request, res: Response): Promise<void> => {
  const result = createCommentSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.issues[0].message, 'VALIDATION_ERROR', 422);
    return;
  }
  try {
    const comment = await svc.createComment(req.user!.id, result.data.message);
    sendSuccess(res, comment, 'Comment submitted', 201);
  } catch {
    sendError(res, 'Failed to submit comment', 'SERVER_ERROR', 500);
  }
};

export const getMyComments = async (req: Request, res: Response): Promise<void> => {
  try {
    const comments = await svc.getMyComments(req.user!.id);
    sendSuccess(res, comments, 'Comments fetched');
  } catch {
    sendError(res, 'Failed to fetch comments', 'SERVER_ERROR', 500);
  }
};
