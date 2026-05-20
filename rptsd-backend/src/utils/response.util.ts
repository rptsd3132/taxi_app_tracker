import { Response } from 'express';

export const sendSuccess = (
  res: Response,
  data: unknown,
  message: string,
  statusCode = 200,
): void => {
  res.status(statusCode).json({ success: true, data, message });
};

export const sendError = (
  res: Response,
  error: string,
  code: string,
  statusCode = 400,
): void => {
  res.status(statusCode).json({ success: false, error, code });
};
