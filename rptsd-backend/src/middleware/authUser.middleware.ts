import { Request, Response, NextFunction } from 'express';
import { verifyUserToken } from '../utils/jwt.util';
import { sendError } from '../utils/response.util';

export const authUser = (req: Request, res: Response, next: NextFunction): void => {
  const authHeader = req.headers.authorization;
  if (!authHeader?.startsWith('Bearer ')) {
    sendError(res, 'Authorization token required', 'UNAUTHORIZED', 401);
    return;
  }

  const token = authHeader.slice(7);
  try {
    const payload = verifyUserToken(token);
    req.user = { id: payload.id, email: payload.email };
    next();
  } catch {
    sendError(res, 'Invalid or expired token', 'UNAUTHORIZED', 401);
  }
};
