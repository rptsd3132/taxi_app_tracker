import { prisma } from '../../config/prisma';
import { SyncStatsRequest } from './stats.types';

export const syncStats = async (userId: string, data: SyncStatsRequest) => {
  const date = new Date(data.date + 'T00:00:00.000Z');

  return prisma.tripStat.upsert({
    where: { userId_date: { userId, date } },
    update: {
      ridesAccepted: data.ridesAccepted,
      ridesSkipped: data.ridesSkipped,
      totalEarnings: data.totalEarnings,
      syncedAt: new Date(),
    },
    create: {
      userId,
      date,
      ridesAccepted: data.ridesAccepted,
      ridesSkipped: data.ridesSkipped,
      totalEarnings: data.totalEarnings,
    },
  });
};

export const getMyStats = async (userId: string, days: number) => {
  const since = new Date();
  since.setDate(since.getDate() - days);
  since.setHours(0, 0, 0, 0);

  return prisma.tripStat.findMany({
    where: { userId, date: { gte: since } },
    orderBy: { date: 'desc' },
  });
};
