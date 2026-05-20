import { prisma } from '../../config/prisma';
import { UserListQuery, PaymentListQuery, CommentListQuery } from './admin.types';

// ─── Dashboard ────────────────────────────────────────────────────────────────

export const getDashboard = async () => {
  const now = new Date();
  const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
  const startOfWeek = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);

  const [
    totalUsers,
    activeSubscriptions,
    expiredSubscriptions,
    revenueResult,
    newUsersThisWeek,
    recentPayments,
    recentComments,
  ] = await Promise.all([
    prisma.user.count(),
    prisma.user.count({ where: { subscriptionStatus: 'ACTIVE' } }),
    prisma.user.count({ where: { subscriptionStatus: 'EXPIRED' } }),
    prisma.payment.aggregate({
      where: { status: 'SUCCESS', paidAt: { gte: startOfMonth } },
      _sum: { amount: true },
    }),
    prisma.user.count({ where: { createdAt: { gte: startOfWeek } } }),
    prisma.payment.findMany({
      take: 10,
      orderBy: { createdAt: 'desc' },
      include: { user: { select: { id: true, name: true, email: true } } },
    }),
    prisma.comment.findMany({
      take: 5,
      where: { status: 'NEW' },
      orderBy: { createdAt: 'desc' },
      include: { user: { select: { id: true, name: true, email: true } } },
    }),
  ]);

  return {
    totalUsers,
    activeSubscriptions,
    expiredSubscriptions,
    revenueThisMonth: Number(revenueResult._sum.amount ?? 0),
    newUsersThisWeek,
    recentPayments,
    recentComments,
  };
};

// ─── Users ────────────────────────────────────────────────────────────────────

export const listUsers = async ({ page, limit, status, search }: UserListQuery) => {
  const skip = (page - 1) * limit;

  const where = {
    ...(status && { subscriptionStatus: status as any }),
    ...(search && {
      OR: [
        { name: { contains: search, mode: 'insensitive' as const } },
        { email: { contains: search, mode: 'insensitive' as const } },
      ],
    }),
  };

  const [users, total] = await Promise.all([
    prisma.user.findMany({
      skip,
      take: limit,
      where,
      orderBy: { createdAt: 'desc' },
      select: {
        id: true,
        name: true,
        email: true,
        phone: true,
        subscriptionStatus: true,
        subscriptionEndDate: true,
        isSuspended: true,
        createdAt: true,
      },
    }),
    prisma.user.count({ where }),
  ]);

  const userIds = users.map((u) => u.id);
  const statsAgg = await prisma.tripStat.groupBy({
    by: ['userId'],
    where: { userId: { in: userIds } },
    _sum: { ridesAccepted: true, totalEarnings: true },
  });

  const statsMap = Object.fromEntries(
    statsAgg.map((s) => [s.userId, s._sum]),
  );

  const items = users.map((u) => ({
    ...u,
    totalRides: statsMap[u.id]?.ridesAccepted ?? 0,
    totalEarnings: Number(statsMap[u.id]?.totalEarnings ?? 0),
  }));

  return { items, total, page, totalPages: Math.ceil(total / limit) };
};

export const getUserDetail = async (id: string) => {
  const thirtyDaysAgo = new Date();
  thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

  const user = await prisma.user.findUnique({
    where: { id },
    include: {
      tripStats: {
        where: { date: { gte: thirtyDaysAgo } },
        orderBy: { date: 'desc' },
      },
      payments: { orderBy: { createdAt: 'desc' } },
      comments: {
        orderBy: { createdAt: 'desc' },
        include: {
          repliedBy: { select: { id: true, name: true, role: true } },
        },
      },
    },
  });
  if (!user) throw new Error('USER_NOT_FOUND');

  const { passwordHash: _, ...safeUser } = user;
  return safeUser;
};

export const suspendUser = async (id: string, isSuspended: boolean) => {
  const user = await prisma.user.findUnique({ where: { id } });
  if (!user) throw new Error('USER_NOT_FOUND');

  return prisma.user.update({
    where: { id },
    data: { isSuspended },
    select: { id: true, isSuspended: true, name: true, email: true },
  });
};

export const extendSubscription = async (id: string, days: number) => {
  const user = await prisma.user.findUnique({
    where: { id },
    select: { subscriptionEndDate: true },
  });
  if (!user) throw new Error('USER_NOT_FOUND');

  const now = new Date();
  const base =
    user.subscriptionEndDate && user.subscriptionEndDate > now
      ? user.subscriptionEndDate
      : now;

  const newEndDate = new Date(base);
  newEndDate.setDate(newEndDate.getDate() + days);

  return prisma.user.update({
    where: { id },
    data: { subscriptionStatus: 'ACTIVE', subscriptionEndDate: newEndDate },
    select: {
      id: true,
      subscriptionStatus: true,
      subscriptionEndDate: true,
      name: true,
      email: true,
    },
  });
};

// ─── Payments ─────────────────────────────────────────────────────────────────

export const listPayments = async ({ page, limit, status, search }: PaymentListQuery) => {
  const skip = (page - 1) * limit;

  const where = {
    ...(status && { status: status as any }),
    ...(search && {
      user: { email: { contains: search, mode: 'insensitive' as const } },
    }),
  };

  const [payments, total] = await Promise.all([
    prisma.payment.findMany({
      skip,
      take: limit,
      where,
      orderBy: { createdAt: 'desc' },
      include: {
        user: { select: { id: true, name: true, email: true, phone: true } },
      },
    }),
    prisma.payment.count({ where }),
  ]);

  return { items: payments, total, page, totalPages: Math.ceil(total / limit) };
};

// ─── Comments ─────────────────────────────────────────────────────────────────

export const listComments = async ({ page, limit = 20, status }: CommentListQuery) => {
  const skip = (page - 1) * limit;
  const where = { ...(status && { status: status as any }) };

  const [comments, total] = await Promise.all([
    prisma.comment.findMany({
      skip,
      take: limit,
      where,
      orderBy: { createdAt: 'desc' },
      include: {
        user: { select: { id: true, name: true, email: true } },
        repliedBy: { select: { id: true, name: true, role: true } },
      },
    }),
    prisma.comment.count({ where }),
  ]);

  return { items: comments, total, page, totalPages: Math.ceil(total / limit) };
};

export const replyToComment = async (
  commentId: string,
  reply: string,
  adminId: string,
) => {
  const comment = await prisma.comment.findUnique({ where: { id: commentId } });
  if (!comment) throw new Error('COMMENT_NOT_FOUND');

  return prisma.comment.update({
    where: { id: commentId },
    data: {
      adminReply: reply,
      repliedAt: new Date(),
      repliedById: adminId,
      status: 'REPLIED',
    },
    include: {
      user: { select: { id: true, name: true, email: true } },
    },
  });
};
