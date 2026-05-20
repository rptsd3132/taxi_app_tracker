import { randomUUID } from 'crypto';
import { prisma } from '../../config/prisma';

export const getSubscriptionStatus = async (userId: string) => {
  const user = await prisma.user.findUnique({
    where: { id: userId },
    select: { subscriptionStatus: true, subscriptionEndDate: true },
  });
  if (!user) throw new Error('USER_NOT_FOUND');

  const now = new Date();
  const endDate = user.subscriptionEndDate;
  const isActive =
    user.subscriptionStatus === 'ACTIVE' && endDate !== null && endDate > now;

  const msPerDay = 1000 * 60 * 60 * 24;
  const daysRemaining =
    endDate && endDate > now ? Math.ceil((endDate.getTime() - now.getTime()) / msPerDay) : 0;

  return { status: user.subscriptionStatus, endDate, daysRemaining, isActive };
};

export const createPayment = async (
  userId: string,
  amount: number,
  paymentMethod: string,
) => {
  const transactionId = randomUUID();

  await prisma.payment.create({
    data: {
      userId,
      amount,
      currency: 'LKR',
      paymentMethod,
      transactionId,
      status: 'PENDING',
    },
  });

  return {
    transactionId,
    paymentUrl: `https://pay.rptsd.com/checkout/${transactionId}`,
  };
};

export const confirmPayment = async (userId: string, transactionId: string) => {
  const payment = await prisma.payment.findUnique({ where: { transactionId } });
  if (!payment) throw new Error('PAYMENT_NOT_FOUND');
  if (payment.userId !== userId) throw new Error('FORBIDDEN');
  if (payment.status !== 'PENDING') throw new Error('ALREADY_PROCESSED');

  const now = new Date();

  await prisma.payment.update({
    where: { id: payment.id },
    data: { status: 'SUCCESS', paidAt: now },
  });

  const user = await prisma.user.findUnique({
    where: { id: userId },
    select: { subscriptionEndDate: true },
  });

  const base =
    user?.subscriptionEndDate && user.subscriptionEndDate > now
      ? user.subscriptionEndDate
      : now;

  const newEndDate = new Date(base);
  newEndDate.setDate(newEndDate.getDate() + 30);

  const updated = await prisma.user.update({
    where: { id: userId },
    data: { subscriptionStatus: 'ACTIVE', subscriptionEndDate: newEndDate },
    select: { subscriptionStatus: true, subscriptionEndDate: true },
  });

  return updated;
};
