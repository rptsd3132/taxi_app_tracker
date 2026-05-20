import { prisma } from '../../config/prisma';

export const createComment = async (userId: string, message: string) => {
  return prisma.comment.create({
    data: { userId, message, status: 'NEW' },
  });
};

export const getMyComments = async (userId: string) => {
  return prisma.comment.findMany({
    where: { userId },
    orderBy: { createdAt: 'desc' },
    include: {
      repliedBy: {
        select: { id: true, name: true, role: true },
      },
    },
  });
};
