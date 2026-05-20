import jwt from 'jsonwebtoken';
import { JWTPayload } from '../modules/auth/auth.types';

const USER_SECRET = process.env.JWT_SECRET!;
const ADMIN_SECRET = process.env.JWT_ADMIN_SECRET!;

export const generateUserToken = (payload: JWTPayload): string =>
  jwt.sign(payload, USER_SECRET, { expiresIn: '7d' });

export const generateAdminToken = (payload: JWTPayload): string =>
  jwt.sign(payload, ADMIN_SECRET, { expiresIn: '1d' });

export const verifyUserToken = (token: string): JWTPayload =>
  jwt.verify(token, USER_SECRET) as JWTPayload;

export const verifyAdminToken = (token: string): JWTPayload =>
  jwt.verify(token, ADMIN_SECRET) as JWTPayload;
