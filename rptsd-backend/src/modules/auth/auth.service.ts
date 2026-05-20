import { prisma } from '../../config/prisma';
import { hashPassword, comparePassword } from '../../utils/bcrypt.util';
import { generateUserToken, generateAdminToken } from '../../utils/jwt.util';
import { RegisterRequest } from './auth.types';

export const registerUser = async (data: RegisterRequest) => {
  const existing = await prisma.user.findUnique({ where: { email: data.email } });
  if (existing) throw new Error('EMAIL_TAKEN');

  const passwordHash = await hashPassword(data.password);
  const subscriptionEndDate = new Date();
  subscriptionEndDate.setDate(subscriptionEndDate.getDate() + 7);

  return prisma.user.create({
    data: {
      name: data.name,
      email: data.email,
      phone: data.phone,
      passwordHash,
      subscriptionStatus: 'TRIAL',
      subscriptionEndDate,
      deviceId: data.deviceId ?? null,
    },
  });
};

export const loginUser = async (email: string, password: string, deviceId?: string) => {
  const user = await prisma.user.findUnique({ where: { email } });
  if (!user) throw new Error('INVALID_CREDENTIALS');

  const valid = await comparePassword(password, user.passwordHash);
  if (!valid) throw new Error('INVALID_CREDENTIALS');

  if (user.isSuspended) throw new Error('ACCOUNT_SUSPENDED');

  if (deviceId && user.deviceId && user.deviceId !== deviceId) {
    throw new Error('DEVICE_CONFLICT');
  }

  const updated = await prisma.user.update({
    where: { id: user.id },
    data: { deviceId: deviceId ?? user.deviceId },
  });

  const token = generateUserToken({ id: user.id, email: user.email });
  return { user: updated, token };
};

export const logoutUser = async (userId: string) => {
  return prisma.user.update({
    where: { id: userId },
    data: { deviceId: null },
  });
};

export const loginAdmin = async (email: string, password: string) => {
  const admin = await prisma.admin.findUnique({ where: { email } });
  if (!admin) throw new Error('INVALID_CREDENTIALS');

  const valid = await comparePassword(password, admin.passwordHash);
  if (!valid) throw new Error('INVALID_CREDENTIALS');

  const token = generateAdminToken({ id: admin.id, email: admin.email, role: admin.role });
  return { admin, token };
};

export const getUserById = async (id: string) => {
  return prisma.user.findUnique({ where: { id } });
};

export const getAdminById = async (id: string) => {
  return prisma.admin.findUnique({ where: { id } });
};
