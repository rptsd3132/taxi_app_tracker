import { Request, Response } from 'express';
import * as authService from './auth.service';
import { registerSchema, loginSchema, adminLoginSchema } from './auth.validator';
import { sendSuccess, sendError } from '../../utils/response.util';

export const register = async (req: Request, res: Response): Promise<void> => {
  const result = registerSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.issues[0].message, 'VALIDATION_ERROR', 422);
    return;
  }

  try {
    const user = await authService.registerUser(result.data);
    const { passwordHash: _, ...safeUser } = user;
    sendSuccess(res, safeUser, 'Registration successful', 201);
  } catch (err) {
    if (err instanceof Error && err.message === 'EMAIL_TAKEN') {
      sendError(res, 'Email already registered', 'EMAIL_TAKEN', 409);
      return;
    }
    sendError(res, 'Registration failed', 'SERVER_ERROR', 500);
  }
};

export const login = async (req: Request, res: Response): Promise<void> => {
  const result = loginSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.issues[0].message, 'VALIDATION_ERROR', 422);
    return;
  }

  const { email, password, deviceId } = result.data;

  try {
    const { user, token } = await authService.loginUser(email, password, deviceId);
    sendSuccess(res, {
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        phone: user.phone,
        subscriptionStatus: user.subscriptionStatus,
        subscriptionEndDate: user.subscriptionEndDate,
      },
      token,
    }, 'Login successful');
  } catch (err) {
    if (err instanceof Error) {
      if (err.message === 'INVALID_CREDENTIALS') {
        sendError(res, 'Invalid email or password', 'INVALID_CREDENTIALS', 401);
        return;
      }
      if (err.message === 'ACCOUNT_SUSPENDED') {
        sendError(res, 'Account is suspended', 'ACCOUNT_SUSPENDED', 403);
        return;
      }
      if (err.message === 'DEVICE_CONFLICT') {
        sendError(res, 'Already logged in on another device', 'DEVICE_CONFLICT', 403);
        return;
      }
    }
    sendError(res, 'Login failed', 'SERVER_ERROR', 500);
  }
};

export const logout = async (req: Request, res: Response): Promise<void> => {
  try {
    await authService.logoutUser(req.user!.id);
    sendSuccess(res, null, 'Logged out successfully');
  } catch {
    sendError(res, 'Logout failed', 'SERVER_ERROR', 500);
  }
};

export const me = async (req: Request, res: Response): Promise<void> => {
  try {
    const user = await authService.getUserById(req.user!.id);
    if (!user) {
      sendError(res, 'User not found', 'NOT_FOUND', 404);
      return;
    }
    const { passwordHash: _, ...safeUser } = user;
    sendSuccess(res, safeUser, 'User fetched');
  } catch {
    sendError(res, 'Failed to fetch user', 'SERVER_ERROR', 500);
  }
};

export const adminLogin = async (req: Request, res: Response): Promise<void> => {
  const result = adminLoginSchema.safeParse(req.body);
  if (!result.success) {
    sendError(res, result.error.issues[0].message, 'VALIDATION_ERROR', 422);
    return;
  }

  const { email, password } = result.data;

  try {
    const { admin, token } = await authService.loginAdmin(email, password);
    const { passwordHash: _, ...safeAdmin } = admin;
    sendSuccess(res, { admin: safeAdmin, token }, 'Admin login successful');
  } catch (err) {
    if (err instanceof Error && err.message === 'INVALID_CREDENTIALS') {
      sendError(res, 'Invalid email or password', 'INVALID_CREDENTIALS', 401);
      return;
    }
    sendError(res, 'Login failed', 'SERVER_ERROR', 500);
  }
};
